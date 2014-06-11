package com.yammer.telemetry.agent.handlers;

import com.google.common.base.Optional;
import com.yammer.telemetry.tracing.Span;

import java.math.BigInteger;
import java.util.concurrent.*;

@SuppressWarnings("UnusedDeclaration")
public class InstrumentedScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    private ThreadLocal<Span> local = new ThreadLocal<>();

    public InstrumentedScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public InstrumentedScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public InstrumentedScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public InstrumentedScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
        return decoratedTask(runnable, super.decorateTask(runnable, task));
    }

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
        return decoratedTask(callable, super.decorateTask(callable, task));
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);

        if (r instanceof InstrumentedRunnableScheduledFuture) {
            InstrumentedRunnableScheduledFuture instrumentedRunnable = (InstrumentedRunnableScheduledFuture) r;

            BigInteger traceId = instrumentedRunnable.getTraceId();
            BigInteger spanId = instrumentedRunnable.getSpanId();

            if (traceId != null && spanId != null) {
                Span span = Span.attachSpan(traceId, spanId, instrumentedRunnable.getName());
                local.set(span);
                span.addAnnotation("Before", instrumentedRunnable.getName());
            }
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        if (r instanceof InstrumentedRunnableScheduledFuture) {
            Span span = local.get();
            if (span != null) {
                span.addAnnotation("After", ((InstrumentedRunnableScheduledFuture) r).getName());
                span.end();
            }
        }
    }

    private <T, V> RunnableScheduledFuture<V> decoratedTask(T task, RunnableScheduledFuture<V> vRunnableScheduledFuture) {
        BigInteger traceId = null;
        BigInteger spanId = null;
        Optional<Span> currentSpan = Span.currentSpan();
        if (currentSpan.isPresent()) {
            traceId = currentSpan.get().getTraceId();
            spanId = currentSpan.get().getId();
            currentSpan.get().addAnnotation("Scheduled Task", task.getClass().getName());
        }
        return new InstrumentedRunnableScheduledFuture<>(vRunnableScheduledFuture, task.getClass().getName(), traceId, spanId);
    }
}
