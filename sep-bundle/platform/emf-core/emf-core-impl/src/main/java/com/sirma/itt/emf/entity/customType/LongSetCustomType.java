/**
 *
 */
package com.sirma.itt.emf.entity.customType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

/**
 * Defines list custom type. A list is represented by a string field in the
 * database
 *
 * @author Kiril Penev
 * @author BBonev
 */
public class LongSetCustomType implements UserType {

	/** SQLtype. */
	private static final int[] SQL_TYPES = { Types.INTEGER };

	/**
	 * Copy the current object.
	 *
	 * @param value The value that needs to be copy
	 * @return A copy
	 * @throws HibernateException the hibernate exception
	 */
	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	/**
	 * Defines mutable. The current type is mutable.
	 *
	 * @return always <code>true</code>.
	 */
	@Override
	public boolean isMutable() {
		return true;
	}

	/**
	 * Assembles the object.
	 *
	 * @param cached Cached instance
	 * @param owner Owner
	 * @return The object
	 * @throws HibernateException the hibernate exception
	 */
	@Override
	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	/**
	 * Disassembles the object.
	 *
	 * @param value The object
	 * @return Serializable
	 * @throws HibernateException the hibernate exception
	 */
	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}

	/**
	 * Replace the current object.
	 *
	 * @param original The original object
	 * @param target The target
	 * @param owner The owner
	 * @return The object
	 * @throws HibernateException the hibernate exception
	 */
	@Override
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	/**
	 * Equals two objects.
	 *
	 * @param x First Object
	 * @param y Second object
	 * @return True if the objects are equals or false if they are not
	 * @throws HibernateException the hibernate exception
	 */
	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y) {
			return true;
		}
		if ((null == x) || (null == y)) {
			return false;
		}
		return x.equals(y);
	}

	/**
	 * Returns hashcode of the current objects.
	 *
	 * @param arg0 Object to hash
	 * @return The hashcode
	 * @throws HibernateException the hibernate exception
	 */
	@Override
	public int hashCode(Object arg0) throws HibernateException {
		return arg0.hashCode();
	}

	/**
	 * Gets the object from the database.
	 *
	 * @param resultSet Result set
	 * @param names Names of the fields
	 * @param sessionImplementor the session implementor
	 * @param owner The object owner
	 * @return The builded object
	 * @throws HibernateException the hibernate exception
	 * @throws SQLException the sQL exception
	 */
	@Override
	public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor sessionImplementor, Object owner)
			throws HibernateException, SQLException {
		String value = resultSet.getString(names[0]);
		Set<Object> list = new LinkedHashSet<Object>();
		if (value != null) {
			String[] values = value.split("\\|");
			list = new LinkedHashSet<Object>(values.length);
			for (String string : values) {
				list.add(Long.parseLong(string));
			}
		}
		return list;
	}

	/**
	 * Sets the custom type object.
	 *
	 * @param preparedStatement The prepared statement
	 * @param value The value
	 * @param index The index of the field
	 * @param sessionImplementor the session implementor
	 * @throws HibernateException the hibernate exception
	 * @throws SQLException the sQL exception
	 */
	@Override
	public void nullSafeSet(PreparedStatement preparedStatement, Object value,
			int index, SessionImplementor sessionImplementor) throws HibernateException, SQLException {
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
				throw new IllegalArgumentException(
						"Invalid field type. Must be java.util.List but was "
								+ value.getClass());
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
		return SQL_TYPES;
	}

}
