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
import org.activiti.engine.impl.javax.el.MethodInfo;
import org.activiti.engine.impl.javax.el.ValueReference;


// TODO: Auto-generated Javadoc
/**
 * The Class AstEval.
 */
public final class AstEval extends AstNode {
	
	/** The child. */
	private final AstNode child;
	
	/** The deferred. */
	private final boolean deferred;

	/**
	 * Instantiates a new ast eval.
	 *
	 * @param child the child
	 * @param deferred the deferred
	 */
	public AstEval(AstNode child, boolean deferred) {
		this.child = child;
		this.deferred = deferred;
	}

	/**
	 * Checks if is deferred.
	 *
	 * @return true, if is deferred
	 */
	public boolean isDeferred() {
		return deferred;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isLeftValue()
	 */
	public boolean isLeftValue() {
		return getChild(0).isLeftValue();
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isMethodInvocation()
	 */
	public boolean isMethodInvocation() {
		return getChild(0).isMethodInvocation();
	}
	
	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getValueReference(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	public ValueReference getValueReference(Bindings bindings, ELContext context) {
		return child.getValueReference(bindings, context);
	}
	
	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#eval(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	@Override
	public Object eval(Bindings bindings, ELContext context) {
		return child.eval(bindings, context);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return (deferred ? "#" : "$") +"{...}";
	}	

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#appendStructure(java.lang.StringBuilder, org.activiti.engine.impl.juel.Bindings)
	 */
	@Override
	public void appendStructure(StringBuilder b, Bindings bindings) {
		b.append(deferred ? "#{" : "${");
		child.appendStructure(b, bindings);
		b.append("}");
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getMethodInfo(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Class, java.lang.Class[])
	 */
	public MethodInfo getMethodInfo(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes) {
		return child.getMethodInfo(bindings, context, returnType, paramTypes);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#invoke(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Class, java.lang.Class[], java.lang.Object[])
	 */
	public Object invoke(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes, Object[] paramValues) {
		return child.invoke(bindings, context, returnType, paramTypes, paramValues);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getType(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	public Class<?> getType(Bindings bindings, ELContext context) {
		return child.getType(bindings, context);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isLiteralText()
	 */
	public boolean isLiteralText() {
		return child.isLiteralText();
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isReadOnly(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	public boolean isReadOnly(Bindings bindings, ELContext context) {
		return child.isReadOnly(bindings, context);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#setValue(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Object)
	 */
	public void setValue(Bindings bindings, ELContext context, Object value) {
		child.setValue(bindings, context, value);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.Node#getCardinality()
	 */
	public int getCardinality() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.Node#getChild(int)
	 */
	public AstNode getChild(int i) {
		return i == 0 ? child : null;
	}
}
