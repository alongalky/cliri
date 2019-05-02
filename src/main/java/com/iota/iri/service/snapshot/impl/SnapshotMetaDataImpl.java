package com.iota.iri.service.snapshot.impl;

import com.iota.iri.model.Hash;
import com.iota.iri.service.snapshot.SnapshotMetaData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implements the basic contract of the {@link SnapshotMetaData} interface.
 */
public class SnapshotMetaDataImpl implements SnapshotMetaData {
    /**
     * Internal property for the value returned by {@link SnapshotMetaData#getInitialHash()}.
     */
    private Hash initialHash;

    /**
     * Internal property for the value returned by {@link SnapshotMetaData#getInitialIndex()}.
     */
    private int initialIndex;

    /**
     * Internal property for the value returned by {@link SnapshotMetaData#getInitialTimestamp()}.
     */
    private long initialTimestamp;

    /**
     * Internal property for the value returned by {@link SnapshotMetaData#getHash()}.
     */
    private Hash hash;

    /**
     * Internal property for the value returned by {@link SnapshotMetaData#getIndex()}.
     */
    private int index;

    /**
     * Internal property for the value returned by {@link SnapshotMetaData#getTimestamp()}.
     */
    private long timestamp;

    /**
     * Internal property for the value returned by {@link SnapshotMetaData#getSolidEntryPoints()}.
     */
    private Map<Hash, Integer> solidEntryPoints;

    /**
     * Internal property for the value returned by {@link SnapshotMetaData#getSeenMilestones()}.
     */
    private Map<Hash, Integer> seenMilestones;

    /**
     * Creates a meta data object with the given information.
     *
     * It simply stores the passed in parameters in the internal properties.
     *
     * @param hash hash of the transaction that the snapshot belongs to
     * @param index milestone index that the snapshot belongs to
     * @param timestamp timestamp of the transaction that the snapshot belongs to
     * @param solidEntryPoints map with the transaction hashes of the solid entry points associated to their milestone
     *                         index
     * @param seenMilestones map of milestone transaction hashes associated to their milestone index
     */
    public SnapshotMetaDataImpl(Hash hash, Long timestamp) {

        this.initialHash = hash;
        this.initialIndex = index;
        this.initialTimestamp = timestamp;

        setHash(hash);
        setIndex(index);
        setTimestamp(timestamp);
    }

    /**
     * Creates a deep clone of the passed in {@link SnapshotMetaData}.
     *
     * @param snapshotMetaData object that shall be cloned
     */
    public SnapshotMetaDataImpl(SnapshotMetaData snapshotMetaData) {
        this(snapshotMetaData.getInitialHash(), snapshotMetaData.getInitialTimestamp());

        this.setIndex(snapshotMetaData.getIndex());
        this.setHash(snapshotMetaData.getHash());
        this.setTimestamp(snapshotMetaData.getTimestamp());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Hash getInitialHash() {
        return initialHash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInitialHash(Hash initialHash) {
        this.initialHash = initialHash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getInitialTimestamp() {
        return initialTimestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInitialTimestamp(long initialTimestamp) {
        this.initialTimestamp = initialTimestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Hash getHash() {
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndex() {
        return this.index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(SnapshotMetaData newMetaData) {
        initialHash = newMetaData.getInitialHash();
        initialTimestamp = newMetaData.getInitialTimestamp();

        setHash(newMetaData.getHash());
        setTimestamp(newMetaData.getTimestamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), initialHash, initialIndex, initialTimestamp, hash, index, timestamp,
                solidEntryPoints, seenMilestones);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }

        return Objects.equals(initialHash, ((SnapshotMetaDataImpl) obj).initialHash) &&
               Objects.equals(initialIndex, ((SnapshotMetaDataImpl) obj).initialIndex) &&
               Objects.equals(initialTimestamp, ((SnapshotMetaDataImpl) obj).initialTimestamp) &&
               Objects.equals(hash, ((SnapshotMetaDataImpl) obj).hash) &&
               Objects.equals(index, ((SnapshotMetaDataImpl) obj).index) &&
               Objects.equals(timestamp, ((SnapshotMetaDataImpl) obj).timestamp) &&
               Objects.equals(solidEntryPoints, ((SnapshotMetaDataImpl) obj).solidEntryPoints) &&
               Objects.equals(seenMilestones, ((SnapshotMetaDataImpl) obj).seenMilestones);

    }
}
