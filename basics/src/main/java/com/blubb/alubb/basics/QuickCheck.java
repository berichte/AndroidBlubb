package com.blubb.alubb.basics;

import java.util.List;

/**
 * Created by Benjamin Richter on 03.06.2014.
 * Simple class just to return the QuickCheck result.
 */
public class QuickCheck {

    /**
     * Name for Logging purposes
     */
    private static final String NAME = "QuickCheck";

    /**
     * List of thread from the quick check.
     */
    public List<BlubbThread> threads;

    /**
     * List of messages from the quick check.
     */
    public List<BlubbMessage> messages;

    /**
     * Constructor for the QuickCheck.
     *
     * @param threads  List of threads for the quick check.
     * @param messages List of messages for the quick check.
     */
    public QuickCheck(List<BlubbThread> threads, List<BlubbMessage> messages) {
        this.threads = threads;
        this.messages = messages;
    }

    /**
     * Check whether this QuickCheck has any results in either the thread list or the message list.
     *
     * @return True if the thread list or the message list is not empty.
     */
    public boolean hasResult() {
        return !(threads.isEmpty() && messages.isEmpty());
    }
}
