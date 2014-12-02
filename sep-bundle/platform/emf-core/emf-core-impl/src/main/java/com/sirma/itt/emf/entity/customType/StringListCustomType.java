/**
 * 
 */
package com.sirma.itt.emf.entity.customType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
public class StringListCustomType implements UserType {

	/** SQLtype. */
	private static final int[] SQL_TYPES = { Types.VARCHAR };

	/**
	 * Copy the current object.
	 *
	 * @param value The value that needs to be copy
	 * @return A copy
	 * @throws HibernateException the hibernate exception
	 */
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	/**
	 * Defines mutable. The current type is mutable.
	 * 
	 * @return always <code>true</code>.
	 */
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
		List<String> list = Collections.emptyList();
		if (value != null) {
			String[] values = value.split("\\|");
			list = Arrays.asList(values);
		}
		return new ArrayList<String>(list);
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
			preparedStatement.setNull(index, Types.VARCHAR);
		} else {
			StringBuilder stringValue = new StringBuilder();

			if (value instanceof List) {
				List<?> list = (List<?>) value;
				for (Object str : list) {
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
	public Class<?> returnedClass() {
		return ArrayList.class;
	}

	/**
	 * Returns the SQL types.
	 *
	 * @return The SQL types
	 */
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

}
