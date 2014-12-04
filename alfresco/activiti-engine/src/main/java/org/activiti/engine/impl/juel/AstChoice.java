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
 * The Class AstChoice.
 */
public class AstChoice extends AstRightValue {
	
	/** The no. */
	private final AstNode question, yes, no;
	
	/**
	 * Instantiates a new ast choice.
	 *
	 * @param question the question
	 * @param yes the yes
	 * @param no the no
	 */
	public AstChoice(AstNode question, AstNode yes, AstNode no) {
		this.question = question;
		this.yes = yes;
		this.no = no;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#eval(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	@Override 
	public Object eval(Bindings bindings, ELContext context) throws ELException {
		Boolean value = bindings.convert(question.eval(bindings, context), Boolean.class);
		return value.booleanValue() ? yes.eval(bindings, context) : no.eval(bindings, context);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "?";
	}	

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#appendStructure(java.lang.StringBuilder, org.activiti.engine.impl.juel.Bindings)
	 */
	@Override 
	public void appendStructure(StringBuilder b, Bindings bindings) {
		question.appendStructure(b, bindings);
		b.append(" ? ");
		yes.appendStructure(b, bindings);
		b.append(" : ");
		no.appendStructure(b, bindings);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.Node#getCardinality()
	 */
	public int getCardinality() {
		return 3;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.Node#getChild(int)
	 */
	public AstNode getChild(int i) {
		return i == 0 ? question : i == 1 ? yes : i == 2 ? no : null;
	}
}
