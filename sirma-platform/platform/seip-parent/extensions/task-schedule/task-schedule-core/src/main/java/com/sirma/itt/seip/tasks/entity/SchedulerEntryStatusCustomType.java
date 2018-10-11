package com.sirma.itt.seip.tasks.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.engine.spi.SessionImplementor;

import com.sirma.itt.seip.db.customtype.UserTypeAdapter;
import com.sirma.itt.seip.tasks.SchedulerEntryStatus;

/**
 * Custom type for {@link SchedulerEntryStatus} enum.
 *
 * @author BBonev
 */
public class SchedulerEntryStatusCustomType extends UserTypeAdapter {

	/** SQLtype. */
	private static final int[] SQL_TYPES = { Types.VARCHAR };

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
		return SchedulerEntryStatus.valueOf(value);
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
			if (value instanceof SchedulerEntryStatus) {
				SchedulerEntryStatus type = (SchedulerEntryStatus) value;
				preparedStatement.setString(index, type.toString());
			} else {
				throw new IllegalArgumentException(
						"Invalid field type. Must be " + SchedulerEntryStatus.class + " but was " + value.getClass());
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
		return SchedulerEntryStatus.class;
	}

	/**
	 * Returns the SQL types.
	 *
	 * @return The SQL types
	 */
	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

}
