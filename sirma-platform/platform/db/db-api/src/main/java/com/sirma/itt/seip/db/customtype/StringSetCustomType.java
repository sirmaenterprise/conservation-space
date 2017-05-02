package com.sirma.itt.seip.db.customtype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Defines list custom type. A list is represented by a string field in the database
 *
 * @author Kiril Penev
 * @author BBonev
 */
public class StringSetCustomType extends UserTypeAdapter {
	public static final String TYPE_NAME = "com.sirma.itt.seip.db.customtype.StringSetCustomType";
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
		if (StringUtils.isNotNullOrEmpty(value)) {
			String[] values = value.split("\\|");
			Set<Object> set = CollectionUtils.createHashSet(values.length);
			for (int i = 0; i < values.length; i++) {
				set.add(values[i]);
			}
			return set;
		}
		return CollectionUtils.createHashSet(0);
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
			preparedStatement.setNull(index, Types.VARCHAR);
		} else {
			StringBuilder builder = new StringBuilder();

			if (value instanceof Set) {
				Set<?> set = (Set<?>) value;
				for (Object str : set) {
					builder.append(str).append("|");
				}
				if (builder.length() > 1) {
					builder.deleteCharAt(builder.length() - 1);
				}
				String builded = builder.toString();
				if ("".equals(builded)) {
					preparedStatement.setString(index, null);
				} else {
					preparedStatement.setString(index, builded);
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
		return new int[] { Types.VARCHAR };
	}

}