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
import java.util.Arrays;

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELException;
import org.activiti.engine.impl.javax.el.MethodInfo;
import org.activiti.engine.impl.javax.el.MethodNotFoundException;
import org.activiti.engine.impl.javax.el.PropertyNotFoundException;
import org.activiti.engine.impl.javax.el.ValueExpression;
import org.activiti.engine.impl.javax.el.ValueReference;


// TODO: Auto-generated Javadoc
/**
 * The Class AstIdentifier.
 */
public class AstIdentifier extends AstNode implements IdentifierNode {
	
	/** The name. */
	private final String name;
	
	/** The index. */
	private final int index;

	/**
	 * Instantiates a new ast identifier.
	 *
	 * @param name the name
	 * @param index the index
	 */
	public AstIdentifier(String name, int index) {
		this.name = name;
		this.index = index;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getType(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	public Class<?> getType(Bindings bindings, ELContext context) {
		ValueExpression expression = bindings.getVariable(index);
		if (expression != null) {
			return expression.getType(context);
		}
		context.setPropertyResolved(false);
		Class<?> result = context.getELResolver().getType(context, null, name);
		if (!context.isPropertyResolved()) {
			throw new PropertyNotFoundException(LocalMessages.get("error.identifier.property.notfound", name));
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isLeftValue()
	 */
	public boolean isLeftValue() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isMethodInvocation()
	 */
	public boolean isMethodInvocation() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isLiteralText()
	 */
	public boolean isLiteralText() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getValueReference(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	public ValueReference getValueReference(Bindings bindings, ELContext context) {
		ValueExpression expression = bindings.getVariable(index);
		if (expression != null) {
			return expression.getValueReference(context);
		}
		return new ValueReference(null, name);
	}
	
	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#eval(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	@Override 
	public Object eval(Bindings bindings, ELContext context) {
		ValueExpression expression = bindings.getVariable(index);
		if (expression != null) {
			return expression.getValue(context);
		}
		context.setPropertyResolved(false);
		Object result = context.getELResolver().getValue(context, null, name);
		if (!context.isPropertyResolved()) {
			throw new PropertyNotFoundException(LocalMessages.get("error.identifier.property.notfound", name));
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#setValue(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Object)
	 */
	public void setValue(Bindings bindings, ELContext context, Object value) {
		ValueExpression expression = bindings.getVariable(index);
		if (expression != null) {
			expression.setValue(context, value);
			return;
		}
		context.setPropertyResolved(false);
		context.getELResolver().setValue(context, null, name, value);
		if (!context.isPropertyResolved()) {
			throw new PropertyNotFoundException(LocalMessages.get("error.identifier.property.notfound", name));
		}
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isReadOnly(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	public boolean isReadOnly(Bindings bindings, ELContext context) {
		ValueExpression expression = bindings.getVariable(index);
		if (expression != null) {
			return expression.isReadOnly(context);
		}
		context.setPropertyResolved(false);
		boolean result = context.getELResolver().isReadOnly(context, null, name);
		if (!context.isPropertyResolved()) {
			throw new PropertyNotFoundException(LocalMessages.get("error.identifier.property.notfound", name));
		}
		return result;
	}

	/**
	 * Gets the method.
	 *
	 * @param bindings the bindings
	 * @param context the context
	 * @param returnType the return type
	 * @param paramTypes the param types
	 * @return the method
	 */
	protected Method getMethod(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes) {
		Object value = eval(bindings, context);
		if (value == null) {
			throw new MethodNotFoundException(LocalMessages.get("error.identifier.method.notfound", name));
		}
		if (value instanceof Method) {
			Method method = (Method)value;
			if (returnType != null && !returnType.isAssignableFrom(method.getReturnType())) {
				throw new MethodNotFoundException(LocalMessages.get("error.identifier.method.notfound", name));
			}
			if (!Arrays.equals(method.getParameterTypes(), paramTypes)) {
				throw new MethodNotFoundException(LocalMessages.get("error.identifier.method.notfound", name));
			}
			return method;
		}
		throw new MethodNotFoundException(LocalMessages.get("error.identifier.method.notamethod", name, value.getClass()));
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getMethodInfo(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Class, java.lang.Class[])
	 */
	public MethodInfo getMethodInfo(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes) {
		Method method = getMethod(bindings, context, returnType, paramTypes);
		return new MethodInfo(method.getName(), method.getReturnType(), paramTypes);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#invoke(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Class, java.lang.Class[], java.lang.Object[])
	 */
	public Object invoke(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes, Object[] params) {
		Method method = getMethod(bindings, context, returnType, paramTypes);
		try {
			return method.invoke(null, params);
		} catch (IllegalAccessException e) {
			throw new ELException(LocalMessages.get("error.identifier.method.access", name));
		} catch (IllegalArgumentException e) {
			throw new ELException(LocalMessages.get("error.identifier.method.invocation", name, e));
		} catch (InvocationTargetException e) {
			throw new ELException(LocalMessages.get("error.identifier.method.invocation", name, e.getCause()));
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#appendStructure(java.lang.StringBuilder, org.activiti.engine.impl.juel.Bindings)
	 */
	@Override 
	public void appendStructure(StringBuilder b, Bindings bindings) {
		b.append(bindings != null && bindings.isVariableBound(index) ? "<var>" : name);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.IdentifierNode#getIndex()
	 */
	public int getIndex() {
		return index;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.IdentifierNode#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.Node#getCardinality()
	 */
	public int getCardinality() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.Node#getChild(int)
	 */
	public AstNode getChild(int i) {
		return null;
	}
}
