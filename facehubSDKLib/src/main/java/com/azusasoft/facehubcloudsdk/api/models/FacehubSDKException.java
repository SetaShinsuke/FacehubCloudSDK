package com.azusasoft.facehubcloudsdk.api.models;

/**
 * Created by SETA on 2016/6/17.
 */
public class FacehubSDKException extends Exception {
    private ErrorType errorType = ErrorType.none;

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public enum ErrorType {
        none,loginError_needRetry
    }

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
