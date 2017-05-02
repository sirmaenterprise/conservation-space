package com.sirma.itt.seip.db.customtype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

/**
 * Custom type for boolean.
 *
 * @author BBonev
 */
public class BooleanCustomType extends UserTypeAdapter {

	private static final int[] TYPE = new int[] { Types.SMALLINT };

	public static final String TYPE_NAME = "com.sirma.itt.seip.db.customtype.BooleanCustomType";
	/**
	 * Gets the object from the database.
	 *
	 * @param resultSet
	 *            Result set
	 * @param names
	 *            Names of the fields
	 * @param sessionImplementor
	 *            the session implementor
	 * @param owner
	 *            The object owner
	 * @return The builded object
	 * @throws SQLException
	 *             the sQL exception
	 */
	@Override
	public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor sessionImplementor, Object owner)
			throws SQLException {
		short value = resultSet.getShort(names[0]);
		if (value == 0) {
			return null;
		}
		return Boolean.valueOf(value == 1);
	}

	/**
	 * Sets the custom type object.
	 *
	 * @param preparedStatement
	 *            The prepared statement
	 * @param value
	 *            The value
	 * @param index
	 *            The index of the field
	 * @param sessionImplementor
	 *            the session implementor
	 * @throws SQLException
	 *             the sQL exception
	 */
	@Override
	public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index,
			SessionImplementor sessionImplementor) throws SQLException {
		if (null == value) {
			preparedStatement.setNull(index, Types.SMALLINT);
		} else {
			if (value instanceof Boolean) {
				Boolean type = (Boolean) value;
				short s = 0;
				if (Boolean.TRUE.equals(type)) {
					s = 1;
				} else {
					s = 2;
				}
				preparedStatement.setShort(index, s);
			} else {
				throw new HibernateException(
						"Invalid field type. Must be " + Boolean.class + " but was " + value.getClass());
			}
		}
	}

	/**
	 * Returns the class that we will work with.
	 *
	 * @return The class that we will work with
	 */
	@Override
	public Class<?> returnedClass() {
		return Boolean.class;
	}

	/**
	 * Returns the SQL types.
	 *
	 * @return The SQL types
	 */
	@Override
	public int[] sqlTypes() {
		return TYPE;
	}

}
