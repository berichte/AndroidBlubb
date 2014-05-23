package com.blubb.alubb.basics;

/**
 * Created by Benjamin Richter on 22.05.2014.
 */
public interface BlubbMessage {
    BlubbUser getAuthor();

    void getType();
    void getMessage();
    void getCreationTime();
    BlubbThread getThread();
    BlubbMessage getQuotedMessage();
}
