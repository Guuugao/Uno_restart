package com.uno_restart.util;

import org.sqids.Sqids;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RoomIDUtil {
    private static final Sqids sqids  = Sqids.builder()
            .minLength(8)
            .build();

    private static final Lock lock = new ReentrantLock();

    public static String getNextId() {
        lock.lock();
        try {
            return sqids.encode(List.of(System.currentTimeMillis()));
        } finally {
            lock.unlock();
        }
    }
}
