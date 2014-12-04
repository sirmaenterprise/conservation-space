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
 * The Class AstText.
 */
public final class AstText extends AstNode {
	
	/** The value. */
	private final String value;

	/**
	 * Instantiates a new ast text.
	 *
	 * @param value the value
	 */
	public AstText(String value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isLiteralText()
	 */
	public boolean isLiteralText() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isLeftValue()
	 */
	public boolean isLeftValue() {
		return false;
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
		return null;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isReadOnly(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	public boolean isReadOnly(Bindings bindings, ELContext context) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#setValue(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Object)
	 */
	public void setValue(Bindings bindings, ELContext context, Object value) {
		throw new ELException(LocalMessages.get("error.value.set.rvalue", getStructuralId(bindings)));
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getValueReference(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	public ValueReference getValueReference(Bindings bindings, ELContext context) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#eval(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	@Override 
	public Object eval(Bindings bindings, ELContext context) {
		return value;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getMethodInfo(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Class, java.lang.Class[])
	 */
	public MethodInfo getMethodInfo(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#invoke(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Class, java.lang.Class[], java.lang.Object[])
	 */
	public Object invoke(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes, Object[] paramValues) {
		return returnType == null ? value : bindings.convert(value, returnType);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\"" + value + "\"";
	}	

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#appendStructure(java.lang.StringBuilder, org.activiti.engine.impl.juel.Bindings)
	 */
	@Override 
	public void appendStructure(StringBuilder b, Bindings bindings) {
		int end = value.length() - 1;
		for (int i = 0; i < end; i++) {
			char c = value.charAt(i);
			if ((c == '#' || c == '$') && value.charAt(i + 1) == '{') {
				b.append('\\');
			}
			b.append(c);
		}
		if (end >= 0) {
			b.append(value.charAt(end));
		}
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
