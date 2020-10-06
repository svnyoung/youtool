package com.svnyoung.youtool.thread.merge;

/**
 * @author: sunyang
 * @date: 2020/5/11 17:43
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public interface MergeService<M,N> {

    /**
     *  执行merge请求
     * @param holdSize 占用大小
     * @param request 用户请求
     * @return 返回merge结果
     * @throws Exception 异常
     * **/
    N merge(M request, int holdSize)throws Exception;

    /**
     * 执行单个merge请求
     * @param request 单个请求
     * @return 返回merge结果
     * @throws Exception merge异常
     * ***/
    N merge(M request)throws Exception;


    /**
     * 设置merge之后处理器
     *
     * @param mnMergeDispatcher  merge Handel
     *
     * **/
    void setMergeDispatcher(MergeDispatcher<M, N> mnMergeDispatcher);
}
