package com.azusasoft.facehubcloudsdk.api;

public interface ResultHandlerInterface {
    void onResponse(Object response);

    void onError(Exception e);
}
