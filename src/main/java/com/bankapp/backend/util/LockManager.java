package com.bankapp.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public final class LockManager {
    // Map accountNumber -> ReentrantLock
    private static final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private LockManager() {}

    // Acquire locks for two accounts in a fixed global order to avoid deadlock.
    public static void acquireLocks(String acctA, String acctB) {
        if (acctA == null && acctB == null) return;
        if (acctA == null) {
            getLock(acctB).lock();
            return;
        }
        if (acctB == null) {
            getLock(acctA).lock();
            return;
        }

        // Enforce consistent ordering by comparing account strings
        if (acctA.compareTo(acctB) < 0) {
            getLock(acctA).lock();
            getLock(acctB).lock();
        } else if (acctA.compareTo(acctB) > 0) {
            getLock(acctB).lock();
            getLock(acctA).lock();
        } else {
            // same account
            getLock(acctA).lock();
        }
    }

    // Release both locks (null-safe)
    public static void releaseLocks(String acctA, String acctB) {
        if (acctA == null && acctB == null) return;
        if (acctA == null) {
            unlockIfHeld(acctB);
            return;
        }
        if (acctB == null) {
            unlockIfHeld(acctA);
            return;
        }

        if (acctA.equals(acctB)) {
            unlockIfHeld(acctA);
            return;
        }

        // Release in reverse order of acquisition (not strictly required for ReentrantLock but clearer)
        if (acctA.compareTo(acctB) < 0) {
            unlockIfHeld(acctB);
            unlockIfHeld(acctA);
        } else {
            unlockIfHeld(acctA);
            unlockIfHeld(acctB);
        }
    }

    private static ReentrantLock getLock(String account) {
        return locks.computeIfAbsent(account, k -> new ReentrantLock());
    }

    private static void unlockIfHeld(String account) {
        ReentrantLock lock = locks.get(account);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
