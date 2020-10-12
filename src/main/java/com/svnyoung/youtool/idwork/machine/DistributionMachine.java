package com.svnyoung.youtool.idwork.machine;

import java.util.List;

/**
 *
 * 获取机器本机的信息，目前通过存放机器的mac地址方式来获取
 * 此处需要注意的是，同一台机器会产生相同的机器ID，所以相同的一个应用不要在同一个地方产生
 * @author: sunyang
 * @date: 2019/7/17 14:18
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public interface DistributionMachine {


    /**
     * 获取机器编码
     * @date 2019/7/17 14:19
     * @param
     * @return
     * @throws Exception 获取code
     */
    MachineInfo getMachine() throws Exception;



    /**
     * 获取所有的机器信息
     * @date 2020/6/10 19:40
     * @param 
     * @return 
     * @throws 
     */
    List<MachineInfo> attainAll();




}
