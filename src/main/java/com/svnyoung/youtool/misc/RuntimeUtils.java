package com.svnyoung.youtool.misc;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * @author: sunyang
 * @date: 2020/10/6 10:08
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public class RuntimeUtils {

    public static int getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName();

        try {
            return Integer.parseInt(name.substring(0, name.indexOf(64)));
        } catch (Exception var3) {
            return -1;
        }
    }

}
