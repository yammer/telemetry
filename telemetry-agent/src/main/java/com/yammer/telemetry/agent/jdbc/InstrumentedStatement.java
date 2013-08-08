package com.yammer.telemetry.agent.jdbc;

import com.yammer.telemetry.tracing.AnnotationNames;
import com.yammer.telemetry.tracing.Span;

import java.sql.*;

public class InstrumentedStatement implements Statement {
    private final InstrumentedConnection connection;
    private final Statement underlying;

    public InstrumentedStatement(InstrumentedConnection connection, Statement underlying) {
        this.connection = connection;
        this.underlying = underlying;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        try (Span span = Span.startSpan("SQL Query: " + sql)) {
            span.addAnnotation(AnnotationNames.SERVICE_NAME, connection.getUrl());
            span.addAnnotation(AnnotationNames.CLIENT_SENT);
            ResultSet set = underlying.executeQuery(sql);
            span.addAnnotation(AnnotationNames.CLIENT_RECEIVED);
            return set;
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        try (Span span = Span.startSpan("SQL Update: " + sql)) {
            span.addAnnotation(AnnotationNames.SERVICE_NAME, connection.getUrl());
            span.addAnnotation(AnnotationNames.CLIENT_SENT);
            int rows = underlying.executeUpdate(sql);
            span.addAnnotation(AnnotationNames.CLIENT_RECEIVED);
            return rows;
        }
    }

    @Override
    public void close() throws SQLException {
        underlying.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return underlying.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        underlying.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return underlying.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        underlying.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        underlying.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return underlying.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        underlying.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        underlying.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return underlying.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        underlying.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        underlying.setCursorName(name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        try (Span span = Span.startSpan("SQL: " + sql)) {
            span.addAnnotation(AnnotationNames.SERVICE_NAME, connection.getUrl());
            span.addAnnotation(AnnotationNames.CLIENT_SENT);
            boolean result = underlying.execute(sql);
            span.addAnnotation(AnnotationNames.CLIENT_RECEIVED);
            return result;
        }
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return underlying.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return underlying.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return underlying.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        underlying.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return underlying.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        underlying.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return underlying.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return underlying.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return underlying.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        underlying.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        underlying.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        try (Span span = Span.startSpan("SQL Batch")) {
            span.addAnnotation(AnnotationNames.SERVICE_NAME, connection.getUrl());
            span.addAnnotation(AnnotationNames.CLIENT_SENT);
            int[] updateCounts = underlying.executeBatch();
            span.addAnnotation(AnnotationNames.CLIENT_RECEIVED);
            return updateCounts;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return underlying.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return underlying.getGeneratedKeys();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        try (Span span = Span.startSpan("SQL Update: " + sql)) {
            span.addAnnotation(AnnotationNames.SERVICE_NAME, connection.getUrl());
            span.addAnnotation(AnnotationNames.CLIENT_SENT);
            int count = underlying.executeUpdate(sql, autoGeneratedKeys);
            span.addAnnotation(AnnotationNames.CLIENT_RECEIVED);
            return count;
        }
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        try (Span span = Span.startSpan("SQL Update: " + sql)) {
            span.addAnnotation(AnnotationNames.SERVICE_NAME, connection.getUrl());
            span.addAnnotation(AnnotationNames.CLIENT_SENT);
            int count = underlying.executeUpdate(sql, columnIndexes);
            span.addAnnotation(AnnotationNames.CLIENT_RECEIVED);
            return count;
        }
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        try (Span span = Span.startSpan("SQL Update: " + sql)) {
            span.addAnnotation(AnnotationNames.SERVICE_NAME, connection.getUrl());
            span.addAnnotation(AnnotationNames.CLIENT_SENT);
            int count = underlying.executeUpdate(sql, columnNames);
            span.addAnnotation(AnnotationNames.CLIENT_RECEIVED);
            return count;
        }
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        try (Span span = Span.startSpan("SQL: " + sql)) {
            span.addAnnotation(AnnotationNames.SERVICE_NAME, connection.getUrl());
            span.addAnnotation(AnnotationNames.CLIENT_SENT);
            boolean result = underlying.execute(sql, autoGeneratedKeys);
            span.addAnnotation(AnnotationNames.CLIENT_RECEIVED);
            return result;
        }
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        try (Span span = Span.startSpan("SQL: " + sql)) {
            span.addAnnotation(AnnotationNames.SERVICE_NAME, connection.getUrl());
            span.addAnnotation(AnnotationNames.CLIENT_SENT);
            boolean result = underlying.execute(sql, columnIndexes);
            span.addAnnotation(AnnotationNames.CLIENT_RECEIVED);
            return result;
        }
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        try (Span span = Span.startSpan("SQL: " + sql)) {
            span.addAnnotation(AnnotationNames.SERVICE_NAME, connection.getUrl());
            span.addAnnotation(AnnotationNames.CLIENT_SENT);
            boolean result = underlying.execute(sql, columnNames);
            span.addAnnotation(AnnotationNames.CLIENT_RECEIVED);
            return result;
        }
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return underlying.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return underlying.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        underlying.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return underlying.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        underlying.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return underlying.isCloseOnCompletion();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return underlying.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return underlying.isWrapperFor(iface);
    }
}
