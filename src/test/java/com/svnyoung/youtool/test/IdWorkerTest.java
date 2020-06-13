package com.svnyoung.youtool.test;

import com.svnyoung.youtool.idwork.sequence.Sequence;
import com.svnyoung.youtool.idwork.sequence.SnowFlakeSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * name：
 * author：zengcj
 * date： 2018/6/23 13:36
 */

@RunWith(JUnit4.class)
public class IdWorkerTest {


    private final Logger logger= LoggerFactory.getLogger(IdWorkerTest.class);

    @Test
    public void snowFlakeTest(){
        Sequence sequence = new SnowFlakeSequence(1,1,1);
        System.out.println(sequence.nextValue());

        Object [] os = sequence.valueBy(sequence.nextValue());
        System.out.println(os);
        os = sequence.valueBy(sequence.nextValue());
        System.out.println(os);
    }

}
