package com.ziogram.telegram.client;

public final class Client {
    protected static native long createNativeClient();

    protected static native void nativeClientSend(long nativeClientId, long eventId, TdApi.Function function);

    protected static native int nativeClientReceive(long nativeClientId, long[] eventIds, TdApi.Object[] events, double timeout);

    protected static native TdApi.Object nativeClientExecute(TdApi.Function function);

    protected static native void destroyNativeClient(long nativeClientId);
}
