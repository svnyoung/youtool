package com.svnyoung.youtool.misc;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author: sunyang
 * @date: 2020/6/11 19:37
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public class NetUtils {




    public static long getLocalMacAddress() {
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
