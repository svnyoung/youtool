package com.svnyoung.youtool.idwork.zookeeper;

/**
 * 通过Zookeeper方式配置获取机器码
 *
 * @author: sunyang
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public class ZookeeperMachineProperties {


    /**
     * session超时时间
     * **/
    private int sessionTimeoutMs = 5000;

    /**连接超时时间**/
    private int connectionTimeoutMs = 5000;

    /**睡眠时间**/
    private int baseSleepTimeMs = 1000;

    /**
     * 最大重试次数
     * **/
    private int maxRetries = 3;

    /**
     * zookeeper 地址
     * **/
    private String quorum;

    /**
     * 机器存放的文件夹
     * **/
    private String machineFolder = "/idworker/machines/";


    /**
     * 机器的自增序列
     * **/
    private String sequencePath = "/idworker/sequence";

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public void setBaseSleepTimeMs(int baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getQuorum() {
        return quorum;
    }

    public void setQuorum(String quorum) {
        this.quorum = quorum;
    }

    public String getMachineFolder() {
        return machineFolder;
    }

    public void setMachineFolder(String machineFolder) {
        this.machineFolder = machineFolder;
    }

    public String getSequencePath() {
        return sequencePath;
    }

    public void setSequencePath(String sequencePath) {
        this.sequencePath = sequencePath;
    }
}