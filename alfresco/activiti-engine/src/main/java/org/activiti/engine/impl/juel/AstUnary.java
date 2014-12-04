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
 * The Class AstUnary.
 */
public class AstUnary extends AstRightValue {
	
	/**
	 * The Interface Operator.
	 */
	public interface Operator {
		
		/**
		 * Eval.
		 *
		 * @param bindings the bindings
		 * @param context the context
		 * @param node the node
		 * @return the object
		 */
		public Object eval(Bindings bindings, ELContext context, AstNode node);		
	}
	
	/**
	 * The Class SimpleOperator.
	 */
	public static abstract class SimpleOperator implements Operator {
		
		/* (non-Javadoc)
		 * @see org.activiti.engine.impl.juel.AstUnary.Operator#eval(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, org.activiti.engine.impl.juel.AstNode)
		 */
		public Object eval(Bindings bindings, ELContext context, AstNode node) {
			return apply(bindings, node.eval(bindings, context));
		}

		/**
		 * Apply.
		 *
		 * @param converter the converter
		 * @param o the o
		 * @return the object
		 */
		protected abstract Object apply(TypeConverter converter, Object o);
	}
	
	/** The Constant EMPTY. */
	public static final Operator EMPTY = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o) { return BooleanOperations.empty(converter, o); }
		@Override public String toString() { return "empty"; }
	};
	
	/** The Constant NEG. */
	public static final Operator NEG = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o) { return NumberOperations.neg(converter, o); }
		@Override public String toString() { return "-"; }
	};
	
	/** The Constant NOT. */
	public static final Operator NOT = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o) { return !converter.convert(o, Boolean.class); }
		@Override public String toString() { return "!"; }
	};

	/** The operator. */
	private final Operator operator;
	
	/** The child. */
	private final AstNode child;

	/**
	 * Instantiates a new ast unary.
	 *
	 * @param child the child
	 * @param operator the operator
	 */
	public AstUnary(AstNode child, AstUnary.Operator operator) {
		this.child = child;
		this.operator = operator;
	}

	/**
	 * Gets the operator.
	 *
	 * @return the operator
	 */
	public Operator getOperator() {
		return operator;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#eval(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext)
	 */
	@Override
	public Object eval(Bindings bindings, ELContext context) throws ELException {
		return operator.eval(bindings, context, child);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "'" + operator.toString() + "'";
	}	

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.juel.AstNode#appendStructure(java.lang.StringBuilder, org.activiti.engine.impl.juel.Bindings)
	 */
	@Override
	public void appendStructure(StringBuilder b, Bindings bindings) {
		b.append(operator);
		b.append(' ');
		child.appendStructure(b, bindings);
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
