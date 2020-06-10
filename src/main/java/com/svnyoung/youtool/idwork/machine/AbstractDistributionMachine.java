package com.svnyoung.youtool.idwork.machine;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

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
            Integer atomIncrement = this.atomIncrement();

            this.storeCode(localMachine,machine);
        }
        return machine;
    }


    protected String getLocalMachineIdentity(){
        return String.valueOf(fetchMacAddress());
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


    private static long fetchMacAddress() {
        try {
            Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
            if (ifs != null) {
                while (ifs.hasMoreElements()) {
                    NetworkInterface iface = ifs.nextElement();
                    byte[] hardware = iface.getHardwareAddress();
                    if (hardware != null && hardware.length == 6
                            && hardware[1] != (byte) 0xff) {
                        // 下面代码是把mac地址拼装成String
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < hardware.length; i++) {
                            // mac[i] & 0xFF 是为了把byte转化为正整数
                            String s = Integer.toHexString(hardware[i] & 0xFF);
                            sb.append(s.length() == 1 ? 0 + s : s);
                        }
                        return Long.parseLong(sb.toString(),16);
                    }
                }
            }
        } catch (SocketException ex) {

            throw new RuntimeException(ex);
        }
        throw new RuntimeException("can't get mac address");
    }


}
