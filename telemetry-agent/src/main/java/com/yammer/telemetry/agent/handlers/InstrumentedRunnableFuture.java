package com.yammer.telemetry.agent.handlers;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class InstrumentedRunnableFuture<T> implements RunnableFuture<T> {
    private final RunnableFuture<T> delegate;
    private final String name;
    private final BigInteger traceId;
    private final BigInteger spanId;

    public InstrumentedRunnableFuture(RunnableFuture<T> delegate, String name, BigInteger traceId, BigInteger spanId) {
        this.delegate = delegate;
        this.name = name;
        this.traceId = traceId;
        this.spanId = spanId;
    }

    public String getName() {
        return name;
    }

    public BigInteger getTraceId() {
        return traceId;
    }

    public BigInteger getSpanId() {
        return spanId;
    }

    @Override
    public void run() {
        delegate.run();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return delegate.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return delegate.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.get(timeout, unit);
    }
}
