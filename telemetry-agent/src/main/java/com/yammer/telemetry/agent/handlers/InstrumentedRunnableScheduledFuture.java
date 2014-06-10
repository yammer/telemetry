package com.yammer.telemetry.agent.handlers;

import java.math.BigInteger;
import java.util.concurrent.*;

public class InstrumentedRunnableScheduledFuture<V> implements RunnableScheduledFuture<V> {
    private final RunnableScheduledFuture<V> delegate;
    private final String name;
    private final BigInteger traceId;
    private final BigInteger spanId;

    public InstrumentedRunnableScheduledFuture(RunnableScheduledFuture<V> delegate, String name, BigInteger traceId, BigInteger spanId) {
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
    public boolean isPeriodic() {
        return delegate.isPeriodic();
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
    public V get() throws InterruptedException, ExecutionException {
        return delegate.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.get(timeout, unit);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return delegate.getDelay(unit);
    }

    @Override
    public int compareTo(Delayed o) {
        return delegate.compareTo(o);
    }
}
