/*
 * Based on JUEL 2.2.1 code, 2006-2009 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.activiti.engine.impl.juel;

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELException;


// TODO: Auto-generated Javadoc
/**
 * Object wrapper expression.
 *
 * @author Christoph Beck
 */
public final class ObjectValueExpression extends org.activiti.engine.impl.javax.el.ValueExpression {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The converter. */
	private final TypeConverter converter;
	
	/** The object. */
	private final Object object;
	
	/** The type. */
	private final Class<?> type;

	/**
	 * Wrap an object into a value expression.
	 * @param converter type converter
	 * @param object the object to wrap
	 * @param type the expected type this object will be coerced in {@link #getValue(ELContext)}.
	 */
	public ObjectValueExpression(TypeConverter converter, Object object, Class<?> type) {
		super();

		this.converter = converter;
		this.object = object;
		this.type = type;
		
		if (type == null) {
			throw new NullPointerException(LocalMessages.get("error.value.notype"));
		}
	}

	/**
	 * Two object value expressions are equal if and only if their wrapped objects are equal.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == getClass()) {
			ObjectValueExpression other = (ObjectValueExpression)obj;
			if (type != other.type) {
				return false;
			}
			return object == other.object || object != null && object.equals(other.object);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.javax.el.Expression#hashCode()
	 */
	@Override
	public int hashCode() {
		return object == null ? 0 : object.hashCode();
	}

	/**
	 * Answer the wrapped object, coerced to the expected type.
	 *
	 * @param context the context
	 * @return the value
	 */
	@Override
	public Object getValue(ELContext context) {
		return converter.convert(object, type);
	}

	/**
	 * Answer <code>null</code>.
	 *
	 * @return the expression string
	 */
	@Override
	public String getExpressionString() {
		return null;
	}

	/**
	 * Answer <code>false</code>.
	 *
	 * @return true, if is literal text
	 */
	@Override
	public boolean isLiteralText() {
		return false;
	}

	/**
	 * Answer <code>null</code>.
	 *
	 * @param context the context
	 * @return the type
	 */
	@Override
	public Class<?> getType(ELContext context) {
		return null;
	}

	/**
	 * Answer <code>true</code>.
	 *
	 * @param context the context
	 * @return true, if is read only
	 */
	@Override
	public boolean isReadOnly(ELContext context) {
		return true;
	}

	/**
	 * Throw an exception.
	 *
	 * @param context the context
	 * @param value the value
	 */
	@Override
	public void setValue(ELContext context, Object value) {
		throw new ELException(LocalMessages.get("error.value.set.rvalue", "<object value expression>"));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ValueExpression(" + object + ")";
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.javax.el.ValueExpression#getExpectedType()
	 */
	@Override
	public Class<?> getExpectedType() {
		return type;
	}
}
