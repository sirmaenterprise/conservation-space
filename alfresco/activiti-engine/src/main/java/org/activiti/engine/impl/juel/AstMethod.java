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
import org.activiti.engine.impl.javax.el.MethodNotFoundException;
import org.activiti.engine.impl.javax.el.PropertyNotFoundException;
import org.activiti.engine.impl.javax.el.ValueReference;


// TODO: Auto-generated Javadoc
/**
 * The Class AstMethod.
 */
public class AstMethod extends AstNode {
	
	/** The property. */
	private final AstProperty property;
	
	/** The params. */
	private final AstParameters params;
	
	/**
	 * Instantiates a new ast method.
	 *
	 * @param property the property
	 * @param params the params
	 */
	public AstMethod(AstProperty property, AstParameters params) {
		this.property = property;
		this.params = params;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#isLiteralText()
	 */
	public boolean isLiteralText() {
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
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getMethodInfo(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Class, java.lang.Class[])
	 */
	public MethodInfo getMethodInfo(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes) {
		return null;
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
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getValueReference(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	public final ValueReference getValueReference(Bindings bindings, ELContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#appendStructure(java.lang.StringBuilder, org.activiti.engine.impl.juel.Bindings)
	 */
	@Override
	public void appendStructure(StringBuilder builder, Bindings bindings) {
		property.appendStructure(builder, bindings);
		params.appendStructure(builder, bindings);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#eval(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	@Override
	public Object eval(Bindings bindings, ELContext context) {
		return invoke(bindings, context, null, null, null);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#invoke(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, java.lang.Class, java.lang.Class[], java.lang.Object[])
	 */
	public Object invoke(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes, Object[] paramValues) {
		Object base = property.getPrefix().eval(bindings, context);
		if (base == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.base.null", property.getPrefix()));
		}
		Object method = property.getProperty(bindings, context);
		if (method == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.method.notfound", "null", base));
		}
		String name = bindings.convert(method, String.class);
		paramValues = params.eval(bindings, context);

		context.setPropertyResolved(false);
		Object result = context.getELResolver().invoke(context, base, name, paramTypes, paramValues);
		if (!context.isPropertyResolved()) {
			throw new MethodNotFoundException(LocalMessages.get("error.property.method.notfound", name, base.getClass()));
		}
//		if (returnType != null && !returnType.isInstance(result)) { // should we check returnType for method invocations?
//			throw new MethodNotFoundException(LocalMessages.get("error.property.method.notfound", name, base.getClass()));
//		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.Node#getCardinality()
	 */
	public int getCardinality() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.Node#getChild(int)
	 */
	public Node getChild(int i) {
		return i == 0 ? property : i == 1 ? params : null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "<method>";
	}
}
