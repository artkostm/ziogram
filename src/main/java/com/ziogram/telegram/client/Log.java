package com.ziogram.telegram.client;

public final class Log {
    public static native void setVerbosityLevel(int var0);

    /** @deprecated */
    @Deprecated
    public static native boolean setFilePath(String var0);

    /** @deprecated */
    @Deprecated
    public static native void setMaxFileSize(long var0);

    private static void onFatalError(String var0) {
        class ThrowError implements Runnable {
            private final String errorMessage;

            ThrowError(String var1) {
                this.errorMessage = var1;
            }

            public void run() {
                throw new RuntimeException("TDLib fatal error: " + this.errorMessage);
            }
        }

        (new Thread(new ThrowError(var0), "TDLib fatal error thread")).start();

        while(true) {
            while(true) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException var2) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}