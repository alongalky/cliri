package com.iota.iri.service.tipselection.impl;

import com.iota.iri.conf.TipSelConfig;
import com.iota.iri.model.Hash;
import com.iota.iri.model.HashId;
import com.iota.iri.service.ledger.LedgerService;
import com.iota.iri.service.snapshot.SnapshotProvider;
import com.iota.iri.service.tipselection.*;
import com.iota.iri.storage.Tangle;
import com.iota.iri.utils.collections.interfaces.UnIterableMap;

import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of <tt>TipSelector</tt> that selects 2 tips,
 * based on cumulative weights and transition function alpha.
 *
 */
public class TipSelectorImpl implements TipSelector {

    public static final String REFERENCE_TRANSACTION_TOO_OLD = "reference transaction is too old";
    public static final String TIPS_NOT_CONSISTENT = "inconsistent tips pair selected";
    public static final int NUMBER_OF_TIPS_IN_GET_CONFIDENCES = 20;

    private final EntryPointSelector entryPointSelector;
    private final RatingCalculator ratingCalculator;
    private final Walker walker;

    private final LedgerService ledgerService;
    private final Tangle tangle;
    private final ReferenceChecker referenceChecker;
    private final TipSelConfig config;

    /**
     * Constructor for Tip Selector.
     *
     * @param tangle Tangle object which acts as a database interface.
     * @param snapshotProvider allows access to snapshots of the ledger state
     * @param ledgerService used by walk validator to check ledger consistency.
     * @param entryPointSelector instance of the entry point selector to get tip selection starting points.
     * @param ratingCalculator instance of rating calculator, to calculate weighted walks.
     * @param walkerAlpha instance of walker (alpha), to perform weighted random walks as per the IOTA white paper.
     * @param referenceChecker instance of reference checker, for getConfidences
     * @param config configurations to set internal parameters.
     */
    public TipSelectorImpl(Tangle tangle,
                           SnapshotProvider snapshotProvider,
                           LedgerService ledgerService,
                           EntryPointSelector entryPointSelector,
                           RatingCalculator ratingCalculator,
                           Walker walkerAlpha,
                           ReferenceChecker referenceChecker,
                           TipSelConfig config) {

        this.entryPointSelector = entryPointSelector;
        this.ratingCalculator = ratingCalculator;

        this.walker = walkerAlpha;

        //used by walkValidator
        this.ledgerService = ledgerService;
        this.tangle = tangle;
        this.config = config;
        this.referenceChecker = referenceChecker;
    }

    /**
     * {@inheritDoc}
     *
     * Implementation of getTransactionsToApprove
     *
     * General process:
     * <ol>
     * <li><b>Preparation:</b> select <CODE>entryPoint</CODE> and calculate rating for all referencing transactions
     * <li><b>1st Random Walk:</b> starting from <CODE>entryPoint</CODE>.
     * <li><b>2nd Random Walk:</b> if <CODE>reference</CODE> exists and is in the rating calulationg, start from <CODE>reference</CODE>,
     *     otherwise start again from <CODE>entryPoint</CODE>.
     * <li><b>Validate:</b> check that both tips are not contradicting.
     * </ol>
     * @param reference  An optional transaction hash to be referenced by tips.
     * @return  Transactions to approve
     * @throws Exception If DB fails to retrieve transactions
     */
    @Override
    public List<Hash> getTransactionsToApprove(Optional<Hash> reference) throws Exception {
        try {
            snapshotProvider.getLatestSnapshot().lockRead();

            //preparation
            Hash entryPoint = entryPointSelector.getEntryPoint();
            UnIterableMap<HashId, Integer> rating = ratingCalculator.calculate(entryPoint);

            //random walk
            List<Hash> tips = new LinkedList<>();
            WalkValidator walkValidator = new WalkValidatorImpl(tangle, snapshotProvider, ledgerService, config);
            Hash tip = walker.walk(entryPoint, rating, walkValidator);
            tips.add(tip);

            if (reference.isPresent()) {
                checkReference(reference.get(), rating);
                entryPoint = reference.get();
            }

            //passing the same walkValidator means that the walks will be consistent with each other
            tip = walker.walk(entryPoint, rating, walkValidator);
            tips.add(tip);

            //validate
            if (!ledgerService.tipsConsistent(tips)) {
                throw new IllegalStateException(TIPS_NOT_CONSISTENT);
            }

            return tips;
        } finally {
            snapshotProvider.getLatestSnapshot().unlockRead();
        }
    }

    private void checkReference(HashId reference, UnIterableMap<HashId, Integer> rating)
            throws InvalidAlgorithmParameterException {
        if (!rating.containsKey(reference)) {
            throw new InvalidAlgorithmParameterException(REFERENCE_TRANSACTION_TOO_OLD);
        }
    }

    @Override
    public List<Double> getConfidences(List<Hash> transactions) throws Exception {
        Hash entryPoint = entryPointSelector.getEntryPoint();
        UnIterableMap<HashId, Integer> rating = ratingCalculator.calculate(entryPoint);

        List<Hash> tips = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TIPS_IN_GET_CONFIDENCES; i++) {
            WalkValidator walkValidator = new WalkValidatorImpl(tangle, ledgerService);
            Hash tip = walker.walk(entryPoint, rating, walkValidator);
            tips.add(tip);
        }

        List<Double> res = new ArrayList<>();
        for (Hash transaction : transactions) {
            int counter = 0;

            for (Hash tip : tips) {
                if (referenceChecker.doesReference(tip, transaction)) {
                    counter++;
                }
            }

            res.add(((double) counter) / NUMBER_OF_TIPS_IN_GET_CONFIDENCES);
        }

        return res;
    }
}
