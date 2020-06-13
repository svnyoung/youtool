package com.svnyoung.youtool.idwork.zookeeper;

import com.svnyoung.youtool.idwork.machine.AbstractDistributionMachine;
import com.svnyoung.youtool.idwork.machine.Machine;
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
    protected Integer getPidSeq(String pid) {
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
    protected Machine fetchMachine(String identity) throws Exception {
        Object isExists = client.checkExists().forPath(zookeeperMachineProperties.getMachineFolder() + identity);
        if (isExists == null) {
            return null;
        } else {
            String machineStr = new String(client.getData().forPath(zookeeperMachineProperties.getMachineFolder() + identity));
            return this.buildMachine(machineStr);
        }
    }


    private Machine buildMachine(String line) {
        String[] machineArr = line.split(SYMBOL);
        Machine machine = new Machine();
        machine.setCode(Integer.parseInt(machineArr[0]));
        machine.setHostname(machineArr[1]);
        machine.setIp(machineArr[2]);
        machine.setMacAddress(machineArr[3]);
        return machine;
    }

    private String rebuildMachine(Machine machine) {
        StringBuffer line = new StringBuffer();
        line.append(machine.getCode()).append(SYMBOL)
                .append(machine.getHostname()).append(SYMBOL)
                .append(machine.getIp()).append(SYMBOL)
                .append(machine.getMacAddress()).append(SYMBOL);
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
    protected void storeCode(String identity, Machine machine) throws Exception {
        String path = zookeeperMachineProperties.getMachineFolder() + identity;
        Object isExists = client.checkExists().forPath(path);
        if (isExists == null) {
            client.create().creatingParentsIfNeeded().forPath(path, this.rebuildMachine(machine).getBytes());
        } else {
            client.setData().forPath(path, this.rebuildMachine(machine).getBytes());
        }
    }

    @Override
    public List<Machine> attainAll() {
        return null;
    }
}
