/**
 *
 */
package com.sirma.itt.seip.db.customtype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

import com.sirma.itt.seip.db.customtype.UserTypeAdapter;

/**
 * Defines list custom type. A list is represented by a string field in the database
 *
 * @author Kiril Penev
 * @author BBonev
 */
public class LongSetCustomType extends UserTypeAdapter {

	private static final int[] TYPE = new int[] { Types.INTEGER };

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
		Set<Object> list = new LinkedHashSet<>();
		if (value != null) {
			String[] values = value.split("\\|");
			list = new LinkedHashSet<>(values.length);
			for (String string : values) {
				list.add(Long.valueOf(string));
			}
		}
		return list;
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
			preparedStatement.setNull(index, Types.INTEGER);
		} else {
			StringBuilder stringValue = new StringBuilder();

			if (value instanceof Set) {
				Set<?> set = (Set<?>) value;
				for (Object str : set) {
					stringValue.append(str).append("|");
				}
				if (stringValue.length() > 1) {
					stringValue.deleteCharAt(stringValue.length() - 1);
				}
				String finalString = stringValue.toString();
				if ("".equals(finalString)) {
					preparedStatement.setString(index, null);
				} else {
					preparedStatement.setString(index, finalString);
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
		return LinkedHashSet.class;
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
