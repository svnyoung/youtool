package com.svnyoung.youtool.idwork.sequence;

import com.svnyoung.youtool.idwork.machine.DistributionMachine;

/**
 * @author: sunyang
 * @date: 2019/7/18 13:36
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public class ElasticSequence extends SnowFlakeSequence {

    public ElasticSequence(int nodeId, DistributionMachine distributionMachine) throws Exception {
        super(nodeId, distributionMachine.getMachine().getCode(),distributionMachine.getMachine().getPidSeq());
    }

}
