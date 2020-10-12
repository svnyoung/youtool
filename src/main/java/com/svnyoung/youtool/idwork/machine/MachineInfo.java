package com.svnyoung.youtool.idwork.machine;

/**
 * @author: sunyang
 * @date: 2020/6/10 19:30
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public class MachineInfo {

    /**机器码**/
    private Integer code;

    /**IP地址**/
    private String ip;

    /**mac地址**/
    private String macAddress;

    /**主机名**/
    private String hostname;

    /**进程号**/
    private Integer pid;

    private Integer pidSeq;


    public Integer getPidSeq() {
        return pidSeq;
    }

    public void setPidSeq(Integer pidSeq) {
        this.pidSeq = pidSeq;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
