/**
 * Copyright (c) 2010 05.08.2010 , Sirma ITT. /* /**
 */
package com.sirma.itt.commons.utils.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class wraps the {@link PreparedStatement} and also provide naming of
 * parameters. Note that it is not possible to use mixed syntax - with named
 * parameters and with ? marks.
 * 
 * @author Hristo Iliev
 */
public class NamedPreparedStatement implements PreparedStatement {

	/** The statement this object is wrapping. */
	private final PreparedStatement statement;

	/**
	 * Maps parameter names to arrays of ints which are the parameter indices.
	 */
	private final Map<String, List<Integer>> indexMap;

	/**
	 * Creates a NamedParameterStatement. Wraps a call to c.
	 * {@link Connection#prepareStatement(java.lang.String) prepareStatement}.
	 * 
	 * @param connection
	 *            the database connection
	 * @param query
	 *            the parameterized query
	 * @throws SQLException
	 *             if the statement could not be created
	 */
	public NamedPreparedStatement(final Connection connection,
			final String query) throws SQLException {
		indexMap = new HashMap<String, List<Integer>>();
		String parsedQuery = parse(query, indexMap);
		statement = connection.prepareStatement(parsedQuery);
	}

	/**
	 * Parses a query with named parameters. The parameter-index mappings are
	 * put into the map, and the parsed query is returned.
	 * 
	 * @param query
	 *            query to parse
	 * @param paramMap
	 *            map to hold parameter-index mappings
	 * @return the parsed query
	 */
	private static final String parse(final String query,
			final Map<String, List<Integer>> paramMap) {
		int length = query.length();
		StringBuilder parsedQuery = new StringBuilder(length);
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		int index = 1;

		for (int i = 0; i < length; i++) {
			char c = query.charAt(i);
			if (inSingleQuote) {
				if (c == '\'') {
					inSingleQuote = false;
				}
			} else if (inDoubleQuote) {
				if (c == '"') {
					inDoubleQuote = false;
				}
			} else {
				if (c == '\'') {
					inSingleQuote = true;
				} else if (c == '"') {
					inDoubleQuote = true;
				} else if ((c == ':') && (i + 1 < length)
						&& Character.isJavaIdentifierStart(query.charAt(i + 1))) {
					int j = i + 2;
					while ((j < length)
							&& Character.isJavaIdentifierPart(query.charAt(j))) {
						j++;
					}
					String name = query.substring(i + 1, j);
					c = '?'; // replace the parameter with a question mark
					i += name.length(); // skip past the end if the parameter

					List<Integer> indexList = paramMap.get(name);
					if (indexList == null) {
						indexList = new LinkedList<Integer>();
						paramMap.put(name, indexList);
					}
					indexList.add(new Integer(index));

					index++;
				}
			}
			parsedQuery.append(c);
		}

		return parsedQuery.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResultSet executeQuery(final String sql) throws SQLException {
		return statement.executeQuery(sql);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		return statement.unwrap(iface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResultSet executeQuery() throws SQLException {
		return statement.executeQuery();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int executeUpdate(final String sql) throws SQLException {
		return statement.executeUpdate(sql);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		return statement.isWrapperFor(iface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int executeUpdate() throws SQLException {
		return statement.executeUpdate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws SQLException {
		statement.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNull(final int parameterIndex, final int sqlType)
			throws SQLException {
		statement.setNull(parameterIndex, sqlType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxFieldSize() throws SQLException {
		return statement.getMaxFieldSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBoolean(final int parameterIndex, final boolean x)
			throws SQLException {
		statement.setBoolean(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMaxFieldSize(final int max) throws SQLException {
		statement.setMaxFieldSize(max);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setByte(final int parameterIndex, final byte x)
			throws SQLException {
		statement.setByte(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setShort(final int parameterIndex, final short x)
			throws SQLException {
		statement.setShort(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxRows() throws SQLException {
		return statement.getMaxRows();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInt(final int parameterIndex, final int x)
			throws SQLException {
		statement.setInt(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMaxRows(final int max) throws SQLException {
		statement.setMaxRows(max);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLong(final int parameterIndex, final long x)
			throws SQLException {
		statement.setLong(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEscapeProcessing(final boolean enable) throws SQLException {
		statement.setEscapeProcessing(enable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFloat(final int parameterIndex, final float x)
			throws SQLException {
		statement.setFloat(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getQueryTimeout() throws SQLException {
		return statement.getQueryTimeout();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDouble(final int parameterIndex, final double x)
			throws SQLException {
		statement.setDouble(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setQueryTimeout(final int seconds) throws SQLException {
		statement.setQueryTimeout(seconds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBigDecimal(final int parameterIndex, final BigDecimal x)
			throws SQLException {
		statement.setBigDecimal(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cancel() throws SQLException {
		statement.cancel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setString(final int parameterIndex, final String x)
			throws SQLException {
		statement.setString(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		return statement.getWarnings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBytes(final int parameterIndex, final byte[] x)
			throws SQLException {
		statement.setBytes(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearWarnings() throws SQLException {
		statement.clearWarnings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDate(final int parameterIndex, final Date x)
			throws SQLException {
		statement.setDate(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCursorName(final String name) throws SQLException {
		statement.setCursorName(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTime(final int parameterIndex, final Time x)
			throws SQLException {
		statement.setTime(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimestamp(final int parameterIndex, final Timestamp x)
			throws SQLException {
		statement.setTimestamp(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean execute(final String sql) throws SQLException {
		return statement.execute(sql);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAsciiStream(final int parameterIndex, final InputStream x,
			final int length) throws SQLException {
		statement.setAsciiStream(parameterIndex, x, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResultSet getResultSet() throws SQLException {
		return statement.getResultSet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public void setUnicodeStream(final int parameterIndex, final InputStream x,
			final int length) throws SQLException {
		statement.setUnicodeStream(parameterIndex, x, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getUpdateCount() throws SQLException {
		return statement.getUpdateCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getMoreResults() throws SQLException {
		return statement.getMoreResults();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBinaryStream(final int parameterIndex, final InputStream x,
			final int length) throws SQLException {
		statement.setBinaryStream(parameterIndex, x, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFetchDirection(final int direction) throws SQLException {
		statement.setFetchDirection(direction);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearParameters() throws SQLException {
		statement.clearParameters();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFetchDirection() throws SQLException {
		return statement.getFetchDirection();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setObject(final int parameterIndex, final Object x,
			final int targetSqlType) throws SQLException {
		statement.setObject(parameterIndex, x, targetSqlType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFetchSize(final int rows) throws SQLException {
		statement.setFetchSize(rows);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFetchSize() throws SQLException {
		return statement.getFetchSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setObject(final int parameterIndex, final Object x)
			throws SQLException {
		statement.setObject(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getResultSetConcurrency() throws SQLException {
		return statement.getResultSetConcurrency();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getResultSetType() throws SQLException {
		return statement.getResultSetType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addBatch(final String sql) throws SQLException {
		statement.addBatch(sql);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearBatch() throws SQLException {
		statement.clearBatch();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean execute() throws SQLException {
		return statement.execute();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] executeBatch() throws SQLException {
		return statement.executeBatch();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addBatch() throws SQLException {
		statement.addBatch();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCharacterStream(final int parameterIndex,
			final Reader reader, final int length) throws SQLException {
		statement.setCharacterStream(parameterIndex, reader, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRef(final int parameterIndex, final Ref x)
			throws SQLException {
		statement.setRef(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return statement.getConnection();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBlob(final int parameterIndex, final Blob x)
			throws SQLException {
		statement.setBlob(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setClob(final int parameterIndex, final Clob x)
			throws SQLException {
		statement.setClob(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getMoreResults(final int current) throws SQLException {
		return statement.getMoreResults(current);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setArray(final int parameterIndex, final Array x)
			throws SQLException {
		statement.setArray(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return statement.getMetaData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		return statement.getGeneratedKeys();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDate(final int parameterIndex, final Date x,
			final Calendar cal) throws SQLException {
		statement.setDate(parameterIndex, x, cal);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int executeUpdate(final String sql, final int autoGeneratedKeys)
			throws SQLException {
		return statement.executeUpdate(sql, autoGeneratedKeys);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTime(final int parameterIndex, final Time x,
			final Calendar cal) throws SQLException {
		statement.setTime(parameterIndex, x, cal);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int executeUpdate(final String sql, final int[] columnIndexes)
			throws SQLException {
		return statement.executeUpdate(sql, columnIndexes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimestamp(final int parameterIndex, final Timestamp x,
			final Calendar cal) throws SQLException {
		statement.setTimestamp(parameterIndex, x, cal);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNull(final int parameterIndex, final int sqlType,
			final String typeName) throws SQLException {
		statement.setNull(parameterIndex, sqlType, typeName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int executeUpdate(final String sql, final String[] columnNames)
			throws SQLException {
		return statement.executeUpdate(sql, columnNames);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean execute(final String sql, final int autoGeneratedKeys)
			throws SQLException {
		return statement.execute(sql, autoGeneratedKeys);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setURL(final int parameterIndex, final URL x)
			throws SQLException {
		statement.setURL(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		return statement.getParameterMetaData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRowId(final int parameterIndex, final RowId x)
			throws SQLException {
		statement.setRowId(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean execute(final String sql, final int[] columnIndexes)
			throws SQLException {
		return statement.execute(sql, columnIndexes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNString(final int parameterIndex, final String value)
			throws SQLException {
		statement.setNString(parameterIndex, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNCharacterStream(final int parameterIndex,
			final Reader value, final long length) throws SQLException {
		statement.setNCharacterStream(parameterIndex, value, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean execute(final String sql, final String[] columnNames)
			throws SQLException {
		return statement.execute(sql, columnNames);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNClob(final int parameterIndex, final NClob value)
			throws SQLException {
		statement.setNClob(parameterIndex, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setClob(final int parameterIndex, final Reader reader,
			final long length) throws SQLException {
		statement.setClob(parameterIndex, reader, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getResultSetHoldability() throws SQLException {
		return statement.getResultSetHoldability();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBlob(final int parameterIndex,
			final InputStream inputStream, final long length)
			throws SQLException {
		statement.setBlob(parameterIndex, inputStream, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isClosed() throws SQLException {
		return statement.isClosed();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPoolable(final boolean poolable) throws SQLException {
		statement.setPoolable(poolable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNClob(final int parameterIndex, final Reader reader,
			final long length) throws SQLException {
		statement.setNClob(parameterIndex, reader, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPoolable() throws SQLException {
		return statement.isPoolable();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSQLXML(final int parameterIndex, final SQLXML xmlObject)
			throws SQLException {
		statement.setSQLXML(parameterIndex, xmlObject);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setObject(final int parameterIndex, final Object x,
			final int targetSqlType, final int scaleOrLength)
			throws SQLException {
		statement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAsciiStream(final int parameterIndex, final InputStream x,
			final long length) throws SQLException {
		statement.setAsciiStream(parameterIndex, x, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBinaryStream(final int parameterIndex, final InputStream x,
			final long length) throws SQLException {
		statement.setBinaryStream(parameterIndex, x, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCharacterStream(final int parameterIndex,
			final Reader reader, final long length) throws SQLException {
		statement.setCharacterStream(parameterIndex, reader, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAsciiStream(final int parameterIndex, final InputStream x)
			throws SQLException {
		statement.setAsciiStream(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBinaryStream(final int parameterIndex, final InputStream x)
			throws SQLException {
		statement.setBinaryStream(parameterIndex, x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCharacterStream(final int parameterIndex, final Reader reader)
			throws SQLException {
		statement.setCharacterStream(parameterIndex, reader);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNCharacterStream(final int parameterIndex, final Reader value)
			throws SQLException {
		statement.setNCharacterStream(parameterIndex, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setClob(final int parameterIndex, final Reader reader)
			throws SQLException {
		statement.setClob(parameterIndex, reader);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBlob(final int parameterIndex, final InputStream inputStream)
			throws SQLException {
		statement.setBlob(parameterIndex, inputStream);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNClob(final int parameterIndex, final Reader reader)
			throws SQLException {
		statement.setNClob(parameterIndex, reader);
	}

	/**
	 * @param parameterName
	 * @param sqlType
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setNull(int, int)
	 */
	public void setNull(final String parameterName, final int sqlType)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setNull(index.intValue(), sqlType);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setBoolean(int, boolean)
	 */
	public void setBoolean(final String parameterName, final boolean x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setBoolean(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setByte(int, byte)
	 */
	public void setByte(final String parameterName, final byte x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setByte(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setShort(int, short)
	 */
	public void setShort(final String parameterName, final short x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setShort(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setInt(int, int)
	 */
	public void setInt(final String parameterName, final int x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setInt(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setLong(int, long)
	 */
	public void setLong(final String parameterName, final long x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setLong(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setFloat(int, float)
	 */
	public void setFloat(final String parameterName, final float x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setFloat(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setDouble(int, double)
	 */
	public void setDouble(final String parameterName, final double x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setDouble(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
	 */
	public void setBigDecimal(final String parameterName, final BigDecimal x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setBigDecimal(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setString(int, java.lang.String)
	 */
	public void setString(final String parameterName, final String x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setString(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setBytes(int, byte[])
	 */
	public void setBytes(final String parameterName, final byte[] x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setBytes(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
	 */
	public void setDate(final String parameterName, final Date x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setDate(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
	 */
	public void setTime(final String parameterName, final Time x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setTime(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
	 */
	public void setTimestamp(final String parameterName, final Timestamp x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setTimestamp(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @param length
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream,
	 *      int)
	 */
	public void setAsciiStream(final String parameterName, final InputStream x,
			final int length) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setAsciiStream(index.intValue(), x, length);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @param length
	 * @throws SQLException
	 * @deprecated
	 * @see java.sql.PreparedStatement#setUnicodeStream(int,
	 *      java.io.InputStream, int)
	 */
	@Deprecated
	public void setUnicodeStream(final String parameterName,
			final InputStream x, final int length) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setUnicodeStream(index.intValue(), x, length);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @param length
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream,
	 *      int)
	 */
	public void setBinaryStream(final String parameterName,
			final InputStream x, final int length) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setBinaryStream(index.intValue(), x, length);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @param targetSqlType
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
	 */
	public void setObject(final String parameterName, final Object x,
			final int targetSqlType) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setObject(index.intValue(), x, targetSqlType);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
	 */
	public void setObject(final String parameterName, final Object x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setObject(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param reader
	 * @param length
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader,
	 *      int)
	 */
	public void setCharacterStream(final String parameterName,
			final Reader reader, final int length) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setCharacterStream(index.intValue(), reader, length);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
	 */
	public void setRef(final String parameterName, final Ref x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setRef(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
	 */
	public void setBlob(final String parameterName, final Blob x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setBlob(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
	 */
	public void setClob(final String parameterName, final Clob x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setClob(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
	 */
	public void setArray(final String parameterName, final Array x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setArray(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @param cal
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date,
	 *      java.util.Calendar)
	 */
	public void setDate(final String parameterName, final Date x,
			final Calendar cal) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setDate(index.intValue(), x, cal);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @param cal
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time,
	 *      java.util.Calendar)
	 */
	public void setTime(final String parameterName, final Time x,
			final Calendar cal) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setTime(index.intValue(), x, cal);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @param cal
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp,
	 *      java.util.Calendar)
	 */
	public void setTimestamp(final String parameterName, final Timestamp x,
			final Calendar cal) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setTimestamp(index.intValue(), x, cal);
		}
	}

	/**
	 * @param parameterName
	 * @param sqlType
	 * @param typeName
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
	 */
	public void setNull(final String parameterName, final int sqlType,
			final String typeName) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setNull(index.intValue(), sqlType, typeName);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
	 */
	public void setURL(final String parameterName, final URL x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setURL(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
	 */
	public void setRowId(final String parameterName, final RowId x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setRowId(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param value
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
	 */
	public void setNString(final String parameterName, final String value)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setNString(index.intValue(), value);
		}
	}

	/**
	 * @param parameterName
	 * @param value
	 * @param length
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader,
	 *      long)
	 */
	public void setNCharacterStream(final String parameterName,
			final Reader value, final long length) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setNCharacterStream(index.intValue(), value, length);
		}
	}

	/**
	 * @param parameterName
	 * @param value
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
	 */
	public void setNClob(final String parameterName, final NClob value)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setNClob(index.intValue(), value);
		}
	}

	/**
	 * @param parameterName
	 * @param reader
	 * @param length
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
	 */
	public void setClob(final String parameterName, final Reader reader,
			final long length) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setClob(index.intValue(), reader, length);
		}
	}

	/**
	 * @param parameterName
	 * @param inputStream
	 * @param length
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
	 */
	public void setBlob(final String parameterName,
			final InputStream inputStream, final long length)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setBlob(index.intValue(), inputStream, length);
		}
	}

	/**
	 * @param parameterName
	 * @param reader
	 * @param length
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
	 */
	public void setNClob(final String parameterName, final Reader reader,
			final long length) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setNClob(index.intValue(), reader, length);
		}
	}

	/**
	 * @param parameterName
	 * @param xmlObject
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
	 */
	public void setSQLXML(final String parameterName, final SQLXML xmlObject)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setSQLXML(index.intValue(), xmlObject);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @param targetSqlType
	 * @param scaleOrLength
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int,
	 *      int)
	 */
	public void setObject(final String parameterName, final Object x,
			final int targetSqlType, final int scaleOrLength)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setObject(index.intValue(), x, targetSqlType,
					scaleOrLength);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @param length
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream,
	 *      long)
	 */
	public void setAsciiStream(final String parameterName, final InputStream x,
			final long length) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setAsciiStream(index.intValue(), x, length);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @param length
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream,
	 *      long)
	 */
	public void setBinaryStream(final String parameterName,
			final InputStream x, final long length) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setBinaryStream(index.intValue(), x, length);
		}
	}

	/**
	 * @param parameterName
	 * @param reader
	 * @param length
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader,
	 *      long)
	 */
	public void setCharacterStream(final String parameterName,
			final Reader reader, final long length) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setCharacterStream(index.intValue(), reader, length);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)
	 */
	public void setAsciiStream(final String parameterName, final InputStream x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setAsciiStream(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param x
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)
	 */
	public void setBinaryStream(final String parameterName, final InputStream x)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setBinaryStream(index.intValue(), x);
		}
	}

	/**
	 * @param parameterName
	 * @param reader
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)
	 */
	public void setCharacterStream(final String parameterName,
			final Reader reader) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setCharacterStream(index.intValue(), reader);
		}
	}

	/**
	 * @param parameterName
	 * @param value
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)
	 */
	public void setNCharacterStream(final String parameterName,
			final Reader value) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setNCharacterStream(index.intValue(), value);
		}
	}

	/**
	 * @param parameterName
	 * @param reader
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
	 */
	public void setClob(final String parameterName, final Reader reader)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setClob(index.intValue(), reader);
		}
	}

	/**
	 * @param parameterName
	 * @param inputStream
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
	 */
	public void setBlob(final String parameterName,
			final InputStream inputStream) throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setBlob(index.intValue(), inputStream);
		}
	}

	/**
	 * @param parameterName
	 * @param reader
	 * @throws SQLException
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
	 */
	public void setNClob(final String parameterName, final Reader reader)
			throws SQLException {
		List<Integer> indexes = indexMap.get(parameterName);
		for (Integer index : indexes) {
			statement.setNClob(index.intValue(), reader);
		}
	}

	@Override
	public void closeOnCompletion() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
