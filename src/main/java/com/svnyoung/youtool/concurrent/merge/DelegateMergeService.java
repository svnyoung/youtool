package com.svnyoung.youtool.concurrent.merge;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: sunyang
 * @date: 2020/5/11 18:04
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public class DelegateMergeService<M, N> implements MergeService<M, N> {

    private Logger logger = LoggerFactory.getLogger(DelegateMergeService.class);


    private BlockingQueue<Command<M>> queue;

    private MergeDispatcher<M, N> mergeDispatcher;

    private long lastMergeTime = System.currentTimeMillis();

    private AtomicInteger currVolume = new AtomicInteger();

    private int maxVolume = 100;

    private int maxWait = 1;

    private int rejectMergeSize = 50;

    private ExecutorService executorService;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public DelegateMergeService() {
        queue = new LinkedBlockingQueue<>();
        executorService = Executors.newCachedThreadPool();
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (System.currentTimeMillis() - lastMergeTime > maxWait) {
                this.process();
            }
        }, 50, 50, TimeUnit.MICROSECONDS);

    }

    public void setMaxVolume(int maxVolume) {
        this.maxVolume = maxVolume;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public void setRejectMergeSize(int rejectMergeSize) {
        this.rejectMergeSize = rejectMergeSize;
    }

    @Override
    public N merge(M request, int holdSize) throws Exception {
        BindThreadCommand<M, N> mergeRequest = new BindThreadCommand<>();
        mergeRequest.setParameter(request);
        mergeRequest.setHoldSize(holdSize);
        mergeRequest.setCompletableFuture(new CompletableFuture<>());
        //如果单次提交占位数据很大，则直接在当前线程处理
        if(holdSize >= rejectMergeSize){
            Map<Command<M>, N> mergeOptionNMap = mergeDispatcher.handler(Collections.singletonList(mergeRequest));
            return mergeOptionNMap.get(mergeRequest);
        }
        if (currVolume.addAndGet(holdSize) <= maxVolume) {
            queue.add(mergeRequest);
        } else {
            this.process();
            synchronized (currVolume){
                currVolume.addAndGet(holdSize);
                queue.add(mergeRequest);
            }
        }
        return mergeRequest.getCompletableFuture().get();
    }


    private void process() {
        List<Command<M>> commands = new ArrayList<>();
        int size;
        synchronized (currVolume) {
            size = queue.drainTo(commands);
            currVolume.set(0);
        }
        if (size > 0) {
            executorService.submit(() -> {
                final Map<Command<M>, N> mergeOptionNMap = new HashMap<>();
                Throwable throwable = null;
                try {
                    mergeOptionNMap.putAll(mergeDispatcher.handler(commands));
                }catch (Throwable e){
                    throwable = e;
                    logger.error("执行合并处理异常",e);
                }
                for (Command<M> mCommand : commands){
                    if(throwable == null){
                        N result = mergeOptionNMap.get(mCommand);
                        ((BindThreadCommand) mCommand).getCompletableFuture().complete(result);
                    }else {
                        ((BindThreadCommand) mCommand).getCompletableFuture().completeExceptionally(throwable);
                    }

                }
            });
            this.lastMergeTime = System.currentTimeMillis();
        }
    }

    @Override
    public N merge(M request) throws Exception {
        return this.merge(request, 1);
    }

    @Override
    public void setMergeDispatcher(MergeDispatcher<M, N> mergeDispatcher) {
        this.mergeDispatcher = mergeDispatcher;
    }

}
