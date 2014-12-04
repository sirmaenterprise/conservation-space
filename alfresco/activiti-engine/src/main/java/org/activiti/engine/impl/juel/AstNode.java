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


// TODO: Auto-generated Javadoc
/**
 * The Class AstNode.
 */
public abstract class AstNode implements ExpressionNode {
	
	/**
	 * evaluate and return the (optionally coerced) result.
	 *
	 * @param bindings the bindings
	 * @param context the context
	 * @param type the type
	 * @return the value
	 */
	public final Object getValue(Bindings bindings, ELContext context, Class<?> type) {
		Object value = eval(bindings, context);
		if (type != null) {
			value = bindings.convert(value, type);
		}
		return value;
	}

	/**
	 * Append structure.
	 *
	 * @param builder the builder
	 * @param bindings the bindings
	 */
	public abstract void appendStructure(StringBuilder builder, Bindings bindings);

	/**
	 * Eval.
	 *
	 * @param bindings the bindings
	 * @param context the context
	 * @return the object
	 */
	public abstract Object eval(Bindings bindings, ELContext context);
  
	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.ExpressionNode#getStructuralId(org.activiti.engine.impl.juel.Bindings)
	 */
	public final String getStructuralId(Bindings bindings) {
		StringBuilder builder = new StringBuilder();
		appendStructure(builder, bindings);
		return builder.toString();
	}
}
