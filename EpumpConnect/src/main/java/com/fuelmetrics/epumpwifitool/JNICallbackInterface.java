package com.fuelmetrics.epumpwifitool;

public interface JNICallbackInterface {
    void tx_data(String data, int len);
}
