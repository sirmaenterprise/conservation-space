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
import org.activiti.engine.impl.javax.el.MethodInfo;
import org.activiti.engine.impl.javax.el.ValueReference;


// TODO: Auto-generated Javadoc
/**
 * The Class AstRightValue.
 *
 * @author Christoph Beck
 */
public abstract class AstRightValue extends AstNode {
	
	/**
	 * Answer <code>false</code>.
	 *
	 * @return true, if is literal text
	 */
	public final boolean isLiteralText() {
		return false;
	}

	/**
	 * according to the spec, the result is undefined for rvalues, so answer <code>null</code>.
	 *
	 * @param bindings the bindings
	 * @param context the context
	 * @return the type
	 */
	public final Class<?> getType(Bindings bindings, ELContext context) {
		return null;
	}

	/**
	 * non-lvalues are always readonly, so answer <code>true</code>.
	 *
	 * @param bindings the bindings
	 * @param context the context
	 * @return true, if is read only
	 */
	public final boolean isReadOnly(Bindings bindings, ELContext context) {
		return true;
	}

	/**
	 * non-lvalues are always readonly, so throw an exception.
	 *
	 * @param bindings the bindings
	 * @param context the context
	 * @param value the value
	 */
	public final void setValue(Bindings bindings, ELContext context, Object value) {
		throw new ELException(LocalMessages.get("error.value.set.rvalue", getStructuralId(bindings)));
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getMethodInfo(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Class, java.lang.Class[])
	 */
	public final MethodInfo getMethodInfo(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#invoke(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Class, java.lang.Class[], java.lang.Object[])
	 */
	public final Object invoke(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes, Object[] paramValues) {
		throw new ELException(LocalMessages.get("error.method.invalid", getStructuralId(bindings)));
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isLeftValue()
	 */
	public final boolean isLeftValue() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isMethodInvocation()
	 */
	public boolean isMethodInvocation() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getValueReference(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	public final ValueReference getValueReference(Bindings bindings, ELContext context) {
		return null;
	}
}
