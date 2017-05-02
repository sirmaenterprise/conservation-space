/**
 *
 */
package com.sirma.itt.seip.db.customtype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

/**
 * Defines list custom type. A list is represented by a string field in the database
 *
 * @author Kiril Penev
 * @author BBonev
 */
public class StringListCustomType extends UserTypeAdapter {

	private static final int[] TYPE = new int[] { Types.VARCHAR };

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
		String value = resultSet.getString(names[0]);
		List<String> list = Collections.emptyList();
		if (value != null) {
			String[] values = value.split("\\|");
			list = Arrays.asList(values);
		}
		return new ArrayList<>(list);
	}

	/**
	 * Sets the custom type object.
	 *
	 * @param statement
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
	public void nullSafeSet(PreparedStatement statement, Object value, int index,
			SessionImplementor sessionImplementor) throws SQLException {
		if (null == value) {
			statement.setNull(index, Types.VARCHAR);
		} else {
			StringBuilder builder = new StringBuilder();

			if (value instanceof Collection<?>) {
				Collection<?> list = (Collection<?>) value;
				for (Object str : list) {
					builder.append(str).append("|");
				}
				if (builder.length() > 1) {
					builder.deleteCharAt(builder.length() - 1);
				}
				String builded = builder.toString();
				if ("".equals(builded)) {
					statement.setString(index, null);
				} else {
					statement.setString(index, builded);
				}
			} else {
				throw new HibernateException("Invalid field type. Must be java.util.List but was " + value.getClass());
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
		return ArrayList.class;
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
