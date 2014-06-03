package com.blubb.alubb.beapcom;

import com.blubb.alubb.basics.BlubbMessage;
import com.blubb.alubb.basics.BlubbThread;

import java.util.List;

/**
 * Created by Benjamin Richter on 03.06.2014.
 * Simple class just to return the QuickCheck result.
 */
public class QuickCheck {
    private static final String NAME = "QuickCheck";
    public List<BlubbThread> threads;
    public List<BlubbMessage> messages;

    public QuickCheck(List<BlubbThread> threads, List<BlubbMessage> messages) {
        this.threads = threads;
        this.messages = messages;
    }

    public boolean hasResult() {
        if(threads.isEmpty() && messages.isEmpty()) return false;
        return true;
    }
}
