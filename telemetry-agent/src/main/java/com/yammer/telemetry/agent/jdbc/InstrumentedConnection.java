package com.yammer.telemetry.agent.jdbc;

import com.yammer.telemetry.tracing.Span;
import com.yammer.telemetry.tracing.SpanHelper;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class InstrumentedConnection implements Connection {
    private final String url;
    private final Connection underyling;

    public InstrumentedConnection(String url, Connection underyling) {
        this.url = url;
        this.underyling = underyling;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return new InstrumentedStatement(this, underyling.createStatement(), null);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new InstrumentedPreparedStatement(this, underyling.prepareStatement(sql), sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return new InstrumentedCallableStatement(this, underyling.prepareCall(sql), sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return underyling.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        underyling.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return underyling.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        try (Span ignored = SpanHelper.startSpan("COMMIT")) {
            underyling.commit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        try (Span ignored = SpanHelper.startSpan("ROLLBACK")) {
            underyling.rollback();
        }
    }

    @Override
    public void close() throws SQLException {
        underyling.close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return underyling.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return underyling.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        underyling.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return underyling.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        underyling.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return underyling.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        underyling.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return underyling.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return underyling.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        underyling.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return new InstrumentedStatement(this, underyling.createStatement(resultSetType, resultSetConcurrency), null);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return new InstrumentedPreparedStatement(this, underyling.prepareStatement(sql, resultSetType, resultSetConcurrency), sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return new InstrumentedCallableStatement(this, underyling.prepareCall(sql, resultSetType, resultSetConcurrency), sql);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return underyling.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        underyling.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        underyling.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return underyling.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return underyling.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return underyling.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        try (Span ignored = SpanHelper.startSpan("ROLLBACK")) {
            underyling.rollback(savepoint);
        }
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        underyling.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return new InstrumentedStatement(this, underyling.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability), null);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return new InstrumentedPreparedStatement(this, underyling.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return new InstrumentedCallableStatement(this, underyling.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return new InstrumentedPreparedStatement(this, underyling.prepareStatement(sql, autoGeneratedKeys), sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return new InstrumentedPreparedStatement(this, underyling.prepareStatement(sql, columnIndexes), sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return new InstrumentedPreparedStatement(this, underyling.prepareStatement(sql, columnNames), sql);
    }

    @Override
    public Clob createClob() throws SQLException {
        return underyling.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return underyling.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return underyling.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return underyling.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return underyling.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        underyling.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        underyling.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return underyling.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return underyling.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return underyling.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return underyling.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        underyling.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return underyling.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        underyling.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        underyling.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return underyling.getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return underyling.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return underyling.isWrapperFor(iface);
    }

    public String getUrl() {
        return url;
    }
}
