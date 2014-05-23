package com.blubb.alubb.beapcom;

/**
 * Created by Benjamin Richter on 22.05.2014.
 */
public interface BlubbReplyReceiver {
    public void receiveResponse(String response);
    public void receiveResponse(BlubbResponse response);
    public void receiveException(Exception e);
}
