package com.svnyoung.youtool.idwork.machine;

/**
 * @author: sunyang
 * @date: 2020/6/10 19:30
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public class Machine {

    private Integer code;

    private String ip;

    private String macAddress;

    private String hostname;


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
