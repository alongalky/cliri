package com.iota.iri.service.tipselection.impl;

import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.service.tipselection.ReferenceChecker;
import com.iota.iri.storage.Tangle;
import com.iota.iri.storage.rocksDB.RocksDBPersistenceProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.ArrayList;
import java.util.List;

import static com.iota.iri.controllers.TransactionViewModelTest.*;

public class ReferenceCheckerImplTest {
    private static final TemporaryFolder dbFolder = new TemporaryFolder();
    private static final TemporaryFolder logFolder = new TemporaryFolder();
    private static Tangle tangle;

    @AfterClass
    public static void tearDown() throws Exception {
        tangle.shutdown();
        dbFolder.delete();
    }

    @BeforeClass
    public static void setUp() throws Exception {
        tangle = new Tangle();
        dbFolder.create();
        logFolder.create();
        tangle.addPersistenceProvider(new RocksDBPersistenceProvider(dbFolder.getRoot().getAbsolutePath(), logFolder
                .getRoot().getAbsolutePath(), 1000));
        tangle.init();
    }

    @Test
    public void testReferenceCheckerReturnsTrueForSameTransaction() throws Exception {
        TransactionViewModel transaction;
        transaction = new TransactionViewModel(getRandomTransactionTrits(), getRandomTransactionHash());

        transaction.store(tangle);

        ReferenceChecker referenceChecker = new ReferenceCheckerImpl(tangle);

        Assert.assertTrue(referenceChecker.doesReference(transaction.getHash(), transaction.getHash()));
    }

    @Test
    public void testReferenceCheckerReturnsFalseForTwoUnrelatedTxs() throws Exception {
        List<TransactionViewModel> transactions = new ArrayList<>();

        transactions.add(new TransactionViewModel(getRandomTransactionTrits(), getRandomTransactionHash()));
        transactions.add(new TransactionViewModel(getRandomTransactionTrits(), getRandomTransactionHash()));

        for (TransactionViewModel transaction : transactions) {
            transaction.store(tangle);
        }

        ReferenceChecker referenceChecker = new ReferenceCheckerImpl(tangle);

        Assert.assertFalse(referenceChecker.doesReference(transactions.get(0).getHash(), transactions.get(1).getHash()));
        Assert.assertFalse(referenceChecker.doesReference(transactions.get(1).getHash(), transactions.get(0).getHash()));
    }

    @Test
    public void testReferenceCheckerReturnsTrueForDirectApprovers() throws Exception {
        List<TransactionViewModel> transactions = new ArrayList<>();

        transactions.add(new TransactionViewModel(getRandomTransactionTrits(), getRandomTransactionHash()));
        transactions.add(new TransactionViewModel(getRandomTransactionTrits(), getRandomTransactionHash()));
        transactions.add(new TransactionViewModel(getRandomTransactionWithTrunkAndBranch(
                transactions.get(0).getHash(),
                transactions.get(1).getHash()),
                getRandomTransactionHash()));

        for (TransactionViewModel transaction : transactions) {
            transaction.store(tangle);
        }

        ReferenceChecker referenceChecker = new ReferenceCheckerImpl(tangle);

        Assert.assertTrue(referenceChecker.doesReference(transactions.get(2).getHash(), transactions.get(1).getHash()));
        Assert.assertTrue(referenceChecker.doesReference(transactions.get(2).getHash(), transactions.get(0).getHash()));
    }

    @Test
    public void testReferenceCheckerReturnsFalseForDirectApproversWhenReversingDirection() throws Exception {
        List<TransactionViewModel> transactions = new ArrayList<>();

        transactions.add(new TransactionViewModel(getRandomTransactionTrits(), getRandomTransactionHash()));
        transactions.add(new TransactionViewModel(getRandomTransactionTrits(), getRandomTransactionHash()));
        transactions.add(new TransactionViewModel(getRandomTransactionWithTrunkAndBranch(
                transactions.get(0).getHash(),
                transactions.get(1).getHash()),
                getRandomTransactionHash()));

        for (TransactionViewModel transaction : transactions) {
            transaction.store(tangle);
        }

        ReferenceChecker referenceChecker = new ReferenceCheckerImpl(tangle);

        Assert.assertFalse(referenceChecker.doesReference(transactions.get(0).getHash(), transactions.get(2).getHash()));
        Assert.assertFalse(referenceChecker.doesReference(transactions.get(1).getHash(), transactions.get(2).getHash()));
    }
}