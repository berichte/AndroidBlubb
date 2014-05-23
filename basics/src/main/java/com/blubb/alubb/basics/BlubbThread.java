package com.blubb.alubb.basics;

import com.blubb.alubb.blubexceptions.InvalidParameterException;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * Class representing a blubb thread where user
 * can post massages.
 */
public class BlubbThread {
    /** Name of the Thread.*/
    private String threadName;
    /** Description for this Thread. */
    private String threadDescription;
    /** The creator of the Thread. */
    private BlubbUser threadCreator;
    /** Type of this thread, e.g. Chatthread, pollThread, taskThread.*/
    private ThreadType threadType;

    /**
     *
     * @param threadName Name of the Thread, must not be null or empty
     * @param threadDescription Description for the thread, can be null or empty.
     * @param threadCreator BlubbUser who created this thread must not be null.
     * @param threadType Type of this thread, e.g. ChatThread must not be null.
     * @throws com.blubb.alubb.blubexceptions.InvalidParameterException
     */
    public BlubbThread(String threadName, String threadDescription,
                       BlubbUser threadCreator, ThreadType threadType)
            throws InvalidParameterException {
        BlubbParameterChecker.checkString(threadName);
        BlubbParameterChecker.checkForNull(threadCreator);
        BlubbParameterChecker.checkForNull(threadType);
        this.threadName = threadName;
        this.threadDescription = threadDescription;
        this.threadCreator = threadCreator;
        this.threadType = threadType;
    }
}
