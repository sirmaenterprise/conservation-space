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

import java.util.List;

import org.activiti.engine.impl.javax.el.ELContext;


// TODO: Auto-generated Javadoc
/**
 * The Class AstParameters.
 */
public class AstParameters extends AstRightValue {
	
	/** The nodes. */
	private final List<AstNode> nodes;
	
	/**
	 * Instantiates a new ast parameters.
	 *
	 * @param nodes the nodes
	 */
	public AstParameters(List<AstNode> nodes) {
		this.nodes = nodes;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#eval(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	@Override
	public Object[] eval(Bindings bindings, ELContext context) {
		Object[] result = new Object[nodes.size()];
		for (int i = 0; i < nodes.size(); i++) {
			result[i] = nodes.get(i).eval(bindings, context);
		}		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(...)";
	}	

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#appendStructure(java.lang.StringBuilder, org.activiti.engine.impl.juel.Bindings)
	 */
	@Override 
	public void appendStructure(StringBuilder builder, Bindings bindings) {
		builder.append("(");
		for (int i = 0; i < nodes.size(); i++) {
			if (i > 0) {
				builder.append(", ");
			}
			nodes.get(i).appendStructure(builder, bindings);
		}
		builder.append(")");
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.Node#getCardinality()
	 */
	public int getCardinality() {
		return nodes.size();
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.Node#getChild(int)
	 */
	public AstNode getChild(int i) {
		return nodes.get(i);
	}
}
