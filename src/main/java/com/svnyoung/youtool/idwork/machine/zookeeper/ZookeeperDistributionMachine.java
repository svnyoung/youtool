package com.svnyoung.youtool.idwork.machine.zookeeper;

import com.svnyoung.youtool.idwork.machine.AbstractDistributionMachine;
import com.svnyoung.youtool.idwork.machine.MachineInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;

import java.util.List;

/**
 * @author: sunyang
 * @date: 2019/7/17 14:37
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public class ZookeeperDistributionMachine extends AbstractDistributionMachine {


    private final static String SYMBOL = new String(new byte[]{0x1, 0x2, 0x3});


    private CuratorFramework client;

    private ZookeeperMachineProperties zookeeperMachineProperties;


    @Override
    protected Integer getPidSeq(String identity, String pid)  throws Exception{
        client
                .create().creatingParentContainersIfNeeded().forPath(String.format(zookeeperMachineProperties.getMachinePidFolder(),identity,pid));

        return null;
    }

    public ZookeeperDistributionMachine(ZookeeperMachineProperties zookeeperMachineProperties) throws Exception {
        this.zookeeperMachineProperties = zookeeperMachineProperties;
        this.init();
    }

    public CuratorFramework getClient() {
        return client;
    }


    public void init() throws Exception {
        this.client = CuratorFrameworkFactory.newClient(
                zookeeperMachineProperties.getQuorum(),
                zookeeperMachineProperties.getSessionTimeoutMs(),
                zookeeperMachineProperties.getConnectionTimeoutMs(),
                new ExponentialBackoffRetry(
                        zookeeperMachineProperties.getBaseSleepTimeMs(),
                        zookeeperMachineProperties.getMaxRetries()));
        this.client.start();
    }

    @Override
    protected MachineInfo fetchMachine(String identity) throws Exception {
        Object isExists = client.checkExists().forPath(String.format(zookeeperMachineProperties.getMachineFolder() , identity));
        if (isExists == null) {
            return null;
        } else {
            String machineStr = new String(client.getData().forPath(String.format(zookeeperMachineProperties.getMachineFolder() , identity)));
            return this.buildMachine(machineStr);
        }
    }


    private MachineInfo buildMachine(String line) {
        String[] machineArr = line.split(SYMBOL);
        MachineInfo machineInfo = new MachineInfo();
        machineInfo.setCode(Integer.parseInt(machineArr[0]));
        machineInfo.setHostname(machineArr[1]);
        machineInfo.setIp(machineArr[2]);
        machineInfo.setMacAddress(machineArr[3]);
        return machineInfo;
    }

    private String rebuildMachine(MachineInfo machineInfo) {
        StringBuffer line = new StringBuffer();
        line.append(machineInfo.getCode()).append(SYMBOL)
                .append(machineInfo.getHostname()).append(SYMBOL)
                .append(machineInfo.getIp()).append(SYMBOL)
                .append(machineInfo.getMacAddress()).append(SYMBOL);
        return line.toString();
    }

    @Override
    protected Integer atomIncrement() throws Exception {
        DistributedAtomicInteger atomicInteger = new DistributedAtomicInteger(client,
                zookeeperMachineProperties.getSequencePath(),
                new RetryNTimes(zookeeperMachineProperties.getMaxRetries(),
                        zookeeperMachineProperties.getBaseSleepTimeMs()));
        return atomicInteger.increment().postValue();
    }

    @Override
    protected void coverIdentity(String identity, MachineInfo machineInfo) throws Exception {
        String path = String.format(zookeeperMachineProperties.getMachineFolder() , identity);
        Object isExists = client.checkExists().forPath(path);
        if (isExists == null) {
            client.create().creatingParentsIfNeeded().forPath(path, this.rebuildMachine(machineInfo).getBytes());
        } else {
            client.setData().forPath(path, this.rebuildMachine(machineInfo).getBytes());
        }
    }

    @Override
    public List<MachineInfo> attainAll() {
        return null;
    }
}
