package com.svnyoung.youtool.thread.merge;

/**
 * @author: sunyang
 * @date: 2020/5/11 17:47
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public interface Command<M> {

    /**
     * 获取用户参数
     * @return 返回用户参数
     * **/
    M getParameter();

    /**
     * 获取占用资源数
     * @return 返回用户资源数
     * **/
    int getHoldSize();

    /**
     * 设置占用资源数
     * @param holdSize 占用资源数
     * */
    void setHoldSize(int holdSize);


    /**
     * 设置参数
     * @param parameter 设置参数
     * **/
    void setParameter(M parameter);

}
