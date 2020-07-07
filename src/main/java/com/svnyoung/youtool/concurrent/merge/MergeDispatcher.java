package com.svnyoung.youtool.concurrent.merge;

import java.util.List;
import java.util.Map;

/**
 * @author: sunyang
 * @date: 2020/5/11 17:59
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public interface MergeDispatcher<M,N> {

    /***
     * 分化请求使用
     * @param mergeOptions  合并的项目
     * @throws Exception 处理结果出错
     * @return 返回映射关系
     * **/
    Map<Command<M>,N> handler(List<Command<M>> mergeOptions) throws Exception;

}
