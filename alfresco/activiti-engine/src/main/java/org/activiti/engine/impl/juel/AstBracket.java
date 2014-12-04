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
 * The Class AstBracket.
 */
public class AstBracket extends AstProperty {
	
	/** The property. */
	protected final AstNode property;
	
	/**
	 * Instantiates a new ast bracket.
	 *
	 * @param base the base
	 * @param property the property
	 * @param lvalue the lvalue
	 * @param strict the strict
	 */
	public AstBracket(AstNode base, AstNode property, boolean lvalue, boolean strict) {
		super(base, lvalue, strict);
		this.property = property;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstProperty#getProperty(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	@Override
	protected Object getProperty(Bindings bindings, ELContext context) throws ELException {
		return property.eval(bindings, context);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[...]";
	}	

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#appendStructure(java.lang.StringBuilder, org.activiti.engine.impl.juel.Bindings)
	 */
	@Override 
	public void appendStructure(StringBuilder b, Bindings bindings) {
		getChild(0).appendStructure(b, bindings);
		b.append("[");
		getChild(1).appendStructure(b, bindings);
		b.append("]");
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.Node#getCardinality()
	 */
	public int getCardinality() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstProperty#getChild(int)
	 */
	@Override
	public AstNode getChild(int i) {
		return i == 1 ? property : super.getChild(i);
	}
}
