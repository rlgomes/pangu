package org.pangu.junit.util;

import org.apache.log4j.Logger;
import org.pangu.junit.JSONTest;

public class BaseTest {

    protected final static Logger _logger = Logger.getLogger(JSONTest.class);
    
    protected final static double MAX_DIFF = 0.10f; // 10% error tolerance

    public void logTestStarted(String name) {
        String cname = getClass().getSimpleName();
        _logger.info("\n" + cname + " - " + name);
    }
}
