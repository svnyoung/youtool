package com.svnyoung.youtool.idwork.machine;

import com.svnyoung.youtool.misc.NetUtils;
import com.svnyoung.youtool.misc.RuntimeUtils;

import java.net.InetAddress;

/**
 * @author: sunyang
 * @date: 2019/7/17 14:20
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public abstract class AbstractDistributionMachine implements DistributionMachine {

    @Override
    public synchronized MachineInfo getMachine() throws Exception{
        String localMachine = this.getLocalMachineIdentity();
        MachineInfo machineInfo = this.fetchMachine(localMachine);
        if(machineInfo == null){
            InetAddress inetAddress = NetUtils.getLocalInetAddress();
            machineInfo = new MachineInfo();
            Integer atomIncrement = this.atomIncrement();
            machineInfo.setMacAddress(localMachine);
            machineInfo.setIp(inetAddress.getHostAddress());
            machineInfo.setHostname(inetAddress.getHostName());
            machineInfo.setCode(atomIncrement);
            machineInfo.setPid(RuntimeUtils.getPid());
            this.coverIdentity(localMachine, machineInfo);
        }else {

        }
        return machineInfo;
    }


    /**
     * 通过进程号获取进程序列
     * @date 2020/6/11 19:34
     * @param pid 进程号
     * @param identity 主机身份
     * @return 
     * @throws Exception
     */
    protected abstract Integer getPidSeq(String identity,String pid) throws Exception;


    protected String getLocalMachineIdentity(){
        return String.valueOf(NetUtils.getLocalMacAddress());
    }


    /**
     *
     * 查询机器码
     * @date 2019/7/17 15:40
     * @param identity 身份
     * @return
     * @throws Exception
     */
    protected abstract MachineInfo fetchMachine(String identity) throws Exception;

    /**
     *  原子增长
     * @date 2019/7/17 15:41
     * @param
     * @return
     * @throws Exception
     */
    protected abstract Integer atomIncrement() throws Exception;


    /**
     *
     * 存放机器码
     * @date 2019/7/17 15:40
     * @param identity 机器的身份
     * @param machineInfo 机器
     * @return
     * @throws Exception
     */
    protected abstract void coverIdentity(String identity, MachineInfo machineInfo)throws Exception;




}
