package com.svnyoung.youtool.idwork.sequence;

/**
 * @author: sunyang
 * @date: 2019/7/17 15:33
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public interface Sequence {

    /**
     *
     * 获取下一个id，此处一定是唯一
     * @date 2020/4/16 9:28
     * @param
     * @return
     * @throws
     */
    String nextValue();


    /**
     * id的构成
     * @param id
     * @return 返回值
     * **/
    Object[] valueBy(String id);

}
