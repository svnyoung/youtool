package com.svnyoung.youtool.thread.merge;

import java.util.concurrent.CompletableFuture;

/**
 * @author: sunyang
 * @date: 2020/5/12 13:55
 * @version: 1.0
 * @since: 1.0
 * @see:
 */
public final class BindThreadCommand<M,N> implements Command<M>, ThreadBind<N> {

    private Thread thread = Thread.currentThread();

    private M parameter;

    private int holdSize;

    private CompletableFuture<N> completableFuture;

    @Override
    public void setHoldSize(int holdSize) {
        this.holdSize = holdSize;
    }

    @Override
    public void setParameter(M parameter) {
        this.parameter = parameter;
    }

    @Override
    public Thread getThread() {
        return thread;
    }

    @Override
    public M getParameter() {
        return parameter;
    }

    @Override
    public int getHoldSize() {
        return holdSize;
    }

    @Override
    public CompletableFuture<N> getCompletableFuture() {
        return completableFuture;
    }

    @Override
    public void setCompletableFuture(CompletableFuture<N> completableFuture) {
        this.completableFuture = completableFuture;
    }

}
