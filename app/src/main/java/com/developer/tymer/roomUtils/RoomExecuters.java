package com.developer.tymer.roomUtils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RoomExecuters {

    private static final Object LOCK = new Object();
    private static RoomExecuters sIntance;

    public Executor getDiskIo() {
        return DiskIo;
    }

    public Executor getNetworkIo() {
        return NetworkIo;
    }

    public Executor getMainThread() {
        return MainThread;
    }

    private final Executor DiskIo, NetworkIo, MainThread;

    private RoomExecuters(Executor diskIo, Executor networkIo, Executor mainThread) {
        DiskIo = diskIo;
        NetworkIo = networkIo;
        MainThread = mainThread;
    }

    public static RoomExecuters getsIntance() {
        if (sIntance == null) {
            synchronized (LOCK) {
                sIntance = new RoomExecuters(Executors.newSingleThreadExecutor(),
                        //Ideal no of threads is equal to no of available processors
                        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()),
                        new MainThreadExecuter());
            }
        }
        return sIntance;
    }

    private static class MainThreadExecuter implements Executor {

        @Override
        public void execute(Runnable runnable) {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }
}
