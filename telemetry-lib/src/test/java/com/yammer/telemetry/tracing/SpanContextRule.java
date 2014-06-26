package com.yammer.telemetry.tracing;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * This is used in tests that create spans to ensure we don't leak any spans beyond the test.
 * Essentially it captures a before and after state and fails the test if we've added or removed
 * spans which existed before or after the test.
 *
 * Significantly simplifies identifying where leaks come from.
 */
public class SpanContextRule implements TestRule {
    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable caught = null;
                final ImmutableList<Span> beforeState = SpanHelper.captureSpans();
                try {
                    base.evaluate();
                } catch (Throwable t) {
                    caught = t;
                } finally {
                    ImmutableList<Span> afterState = SpanHelper.captureSpans();
                    verify(beforeState, afterState);
                }

                if (caught != null) {
                    throw caught;
                }
            }
        };
    }

    private static void verify(final ImmutableList<Span> beforeState, final ImmutableList<Span> afterState) throws Throwable {
        if (!beforeState.equals(afterState)) {
            ImmutableList<Span> added = ImmutableList.copyOf(Iterables.filter(afterState, new Predicate<Span>() {
                @Override
                public boolean apply(Span input) {
                    return !beforeState.contains(input);
                }
            }));
            ImmutableList<Span> removed = ImmutableList.copyOf(Iterables.filter(beforeState, new Predicate<Span>() {
                @Override
                public boolean apply(Span input) {
                    return !afterState.contains(input);
                }
            }));
            if (!added.isEmpty() || !removed.isEmpty()) {
                throw new Exception("SpanContext was not cleaned up after test. (before = " + beforeState.size() + " after = " + afterState.size() + ")\n" +
                        "Added: " + added + "\n" +
                        "Removed: " + removed);
            }
        }
    }

}
