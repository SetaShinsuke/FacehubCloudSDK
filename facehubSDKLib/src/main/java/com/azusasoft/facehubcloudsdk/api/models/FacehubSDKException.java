package com.azusasoft.facehubcloudsdk.api.models;

/**
 * Created by SETA on 2016/6/17.
 */
public class FacehubSDKException extends Exception {

    public FacehubSDKException() {
        super();
    }

    public FacehubSDKException(String detailMessage) {
        super(detailMessage);
    }

    public FacehubSDKException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public FacehubSDKException(Throwable throwable) {
        super(throwable);
    }
}
