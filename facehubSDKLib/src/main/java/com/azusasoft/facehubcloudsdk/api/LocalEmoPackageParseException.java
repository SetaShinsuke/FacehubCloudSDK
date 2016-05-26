package com.azusasoft.facehubcloudsdk.api;

/**
 * Created by SETA on 2016/5/26.
 * 本地预存表情解析JSON出错时抛出的异常
 */
public class LocalEmoPackageParseException extends Exception {
    public LocalEmoPackageParseException() {
        super();
    }

    public LocalEmoPackageParseException(String detailMessage) {
        super(detailMessage);
    }

    public LocalEmoPackageParseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public LocalEmoPackageParseException(Throwable throwable) {
        super(throwable);
    }
}
