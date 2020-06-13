package com.svnyoung.youtool.idwork.machine;

import com.svnyoung.youtool.misc.NetUtils;

/**
 * @author: sunyang
 * @date: 2019/7/17 14:20
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public abstract class AbstractDistributionMachine implements DistributionMachine {

    @Override
    public synchronized Machine getMachine() throws Exception{
        String localMachine = getLocalMachineIdentity();
        Machine machine = fetchMachine(localMachine);
        if(machine == null){
            machine = new Machine();
            Integer atomIncrement = this.atomIncrement();
            machine.setCode(atomIncrement);
            this.storeCode(localMachine,machine);
        }
        return machine;
    }


    /**
     * 通过进程号获取进程序列
     * @date 2020/6/11 19:34
     * @param pid 进程号
     * @return 
     * @throws 
     */
    protected abstract Integer getPidSeq(String pid);


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
    protected abstract Machine fetchMachine(String identity) throws Exception;

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
     * @param machine 机器
     * @return
     * @throws Exception
     */
    protected abstract void storeCode(String identity,Machine machine)throws Exception;





}
