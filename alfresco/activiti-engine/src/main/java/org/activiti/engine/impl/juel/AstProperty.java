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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELException;
import org.activiti.engine.impl.javax.el.MethodInfo;
import org.activiti.engine.impl.javax.el.MethodNotFoundException;
import org.activiti.engine.impl.javax.el.PropertyNotFoundException;
import org.activiti.engine.impl.javax.el.ValueReference;


// TODO: Auto-generated Javadoc
/**
 * The Class AstProperty.
 */
public abstract class AstProperty extends AstNode {
	
	/** The prefix. */
	protected final AstNode prefix;
	
	/** The lvalue. */
	protected final boolean lvalue;
	
	/** The strict. */
	protected final boolean strict; // allow null as property value?
	
	/**
	 * Instantiates a new ast property.
	 *
	 * @param prefix the prefix
	 * @param lvalue the lvalue
	 * @param strict the strict
	 */
	public AstProperty(AstNode prefix, boolean lvalue, boolean strict) {
		this.prefix = prefix;
		this.lvalue = lvalue;
		this.strict = strict;
	}

	/**
	 * Gets the property.
	 *
	 * @param bindings the bindings
	 * @param context the context
	 * @return the property
	 * @throws ELException the eL exception
	 */
	protected abstract Object getProperty(Bindings bindings, ELContext context) throws ELException;

	/**
	 * Gets the prefix.
	 *
	 * @return the prefix
	 */
	protected AstNode getPrefix() {
		return prefix;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getValueReference(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	public ValueReference getValueReference(Bindings bindings, ELContext context) {
		return new ValueReference(prefix.eval(bindings, context), getProperty(bindings, context));
	}
	
	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#eval(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	@Override
	public Object eval(Bindings bindings, ELContext context) {
		Object base = prefix.eval(bindings, context);
		if (base == null) {
			return null;
		}
		Object property = getProperty(bindings, context);
		if (property == null && strict) {
			return null;
		}
		context.setPropertyResolved(false);
		Object result = context.getELResolver().getValue(context, base, property);
		if (!context.isPropertyResolved()) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", property, base));
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isLiteralText()
	 */
	public final boolean isLiteralText() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isLeftValue()
	 */
	public final boolean isLeftValue() {
		return lvalue;
	}
	
	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isMethodInvocation()
	 */
	public boolean isMethodInvocation() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getType(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	public Class<?> getType(Bindings bindings, ELContext context) {
		if (!lvalue) {
			return null;
		}
		Object base = prefix.eval(bindings, context);
		if (base == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.base.null", prefix));
		}
		Object property = getProperty(bindings, context);
		if (property == null && strict) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", "null", base));
		}
		context.setPropertyResolved(false);
		Class<?> result = context.getELResolver().getType(context, base, property);
		if (!context.isPropertyResolved()) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", property, base));
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isReadOnly(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	public boolean isReadOnly(Bindings bindings, ELContext context) throws ELException {
		if (!lvalue) {
			return true;
		}
		Object base = prefix.eval(bindings, context);
		if (base == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.base.null", prefix));
		}
		Object property = getProperty(bindings, context);
		if (property == null && strict) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", "null", base));
		}
		context.setPropertyResolved(false);
		boolean result = context.getELResolver().isReadOnly(context, base, property);
		if (!context.isPropertyResolved()) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", property, base));
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#setValue(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Object)
	 */
	public void setValue(Bindings bindings, ELContext context, Object value) throws ELException {
		if (!lvalue) {
			throw new ELException(LocalMessages.get("error.value.set.rvalue", getStructuralId(bindings)));
		}
		Object base = prefix.eval(bindings, context);
		if (base == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.base.null", prefix));
		}
		Object property = getProperty(bindings, context);
		if (property == null && strict) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", "null", base));
		}
		context.setPropertyResolved(false);
		context.getELResolver().setValue(context, base, property, value);
		if (!context.isPropertyResolved()) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", property, base));
		}
	}
	
	/**
	 * Find method.
	 *
	 * @param name the name
	 * @param clazz the clazz
	 * @param returnType the return type
	 * @param paramTypes the param types
	 * @return the method
	 */
	protected Method findMethod(String name, Class<?> clazz, Class<?> returnType, Class<?>[] paramTypes) {
		Method method = null;
		try {
			method = clazz.getMethod(name, paramTypes);
		} catch (NoSuchMethodException e) {
			throw new MethodNotFoundException(LocalMessages.get("error.property.method.notfound", name, clazz));
		}
		if (returnType != null && !returnType.isAssignableFrom(method.getReturnType())) {
			throw new MethodNotFoundException(LocalMessages.get("error.property.method.notfound", name, clazz));
		}
		return method;
	}
	
	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getMethodInfo(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Class, java.lang.Class[])
	 */
	public MethodInfo getMethodInfo(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes) {
		Object base = prefix.eval(bindings, context);
		if (base == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.base.null", prefix));
		}
		Object property = getProperty(bindings, context);
		if (property == null && strict) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.method.notfound", "null", base));
		}
		String name = bindings.convert(property, String.class);
		Method method = findMethod(name, base.getClass(), returnType, paramTypes);
		return new MethodInfo(method.getName(), method.getReturnType(), paramTypes);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#invoke(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Class, java.lang.Class[], java.lang.Object[])
	 */
	public Object invoke(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes, Object[] paramValues) {
		Object base = prefix.eval(bindings, context);
		if (base == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.base.null", prefix));
		}
		Object property = getProperty(bindings, context);
		if (property == null && strict) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.method.notfound", "null", base));
		}
		String name = bindings.convert(property, String.class);
		Method method = findMethod(name, base.getClass(), returnType, paramTypes);
		try {
			return method.invoke(base, paramValues);
		} catch (IllegalAccessException e) {
			throw new ELException(LocalMessages.get("error.property.method.access", name, base.getClass()));
		} catch (IllegalArgumentException e) {
			throw new ELException(LocalMessages.get("error.property.method.invocation", name, base.getClass()), e);
		} catch (InvocationTargetException e) {
			throw new ELException(LocalMessages.get("error.property.method.invocation", name, base.getClass()), e.getCause());
		}
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.Node#getChild(int)
	 */
	public AstNode getChild(int i) {
		return i == 0 ? prefix : null;
	}
}
