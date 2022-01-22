package org.monkey.mmq.core.notify;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @ClassNameDefaultPublisherTest
 * @Description
 * @Author Solley
 * @Date2022/1/22 10:30
 * @Version V1.0
 **/
public class DefaultPublisherTest {

    @Test
    public void notifySubscriber() {
        final Runnable job = () -> {
            System.out.println("sss");
            Double ss = null;
            Double tt = 100 / ss;
        };
        try {
            job.run();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }
}