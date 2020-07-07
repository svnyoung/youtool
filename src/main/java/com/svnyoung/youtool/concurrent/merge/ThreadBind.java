package com.svnyoung.youtool.concurrent.merge;

import java.util.concurrent.CompletableFuture;

/**
 * @author: sunyang
 * @date: 2020/5/12 14:08
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public interface ThreadBind<T> {


    /**
     *
     * 获取绑定的当前线程
     * @return 线程
     * **/
    Thread getThread();


    /**
     *
     * 获取处理线程类
     * @return 处理线程类
     * **/
    CompletableFuture<T> getCompletableFuture();


    /**
     * 设置
     * @param completableFuture 设置线程处理
     * **/
    void setCompletableFuture(CompletableFuture<T> completableFuture);

}
