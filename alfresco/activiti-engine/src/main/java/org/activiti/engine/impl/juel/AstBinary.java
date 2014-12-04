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
 * The Class AstBinary.
 */
public class AstBinary extends AstRightValue {
	
	/**
	 * The Interface Operator.
	 */
	public interface Operator {
		
		/**
		 * Eval.
		 *
		 * @param bindings the bindings
		 * @param context the context
		 * @param left the left
		 * @param right the right
		 * @return the object
		 */
		public Object eval(Bindings bindings, ELContext context, AstNode left, AstNode right);		
	}
	
	/**
	 * The Class SimpleOperator.
	 */
	public static abstract class SimpleOperator implements Operator {
		
		/* (non-Javadoc)
		 * @see org.activiti.engine.impl.juel.AstBinary.Operator#eval(org.activiti.engine.impl.juel.Bindings, org.activiti.engine.impl.javax.el.ELContext, org.activiti.engine.impl.juel.AstNode, org.activiti.engine.impl.juel.AstNode)
		 */
		public Object eval(Bindings bindings, ELContext context, AstNode left, AstNode right) {
			return apply(bindings, left.eval(bindings, context), right.eval(bindings, context));
		}

		/**
		 * Apply.
		 *
		 * @param converter the converter
		 * @param o1 the o1
		 * @param o2 the o2
		 * @return the object
		 */
		protected abstract Object apply(TypeConverter converter, Object o1, Object o2);
	}
	
	/** The Constant ADD. */
	public static final Operator ADD = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return NumberOperations.add(converter, o1, o2); }
		@Override public String toString() { return "+"; }
	};
	
	/** The Constant AND. */
	public static final Operator AND = new Operator() {
		public Object eval(Bindings bindings, ELContext context, AstNode left, AstNode right) {
			Boolean l = bindings.convert(left.eval(bindings, context), Boolean.class);
			return Boolean.TRUE.equals(l) ? bindings.convert(right.eval(bindings, context), Boolean.class) : Boolean.FALSE;
		}
		@Override public String toString() { return "&&"; }
	};
	
	/** The Constant DIV. */
	public static final Operator DIV = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return NumberOperations.div(converter, o1, o2); }
		@Override public String toString() { return "/"; }
	};
	
	/** The Constant EQ. */
	public static final Operator EQ = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return BooleanOperations.eq(converter, o1, o2); }
		@Override public String toString() { return "=="; }
	};
	
	/** The Constant GE. */
	public static final Operator GE = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return BooleanOperations.ge(converter, o1, o2); }
		@Override public String toString() { return ">="; }
	};
	
	/** The Constant GT. */
	public static final Operator GT = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return BooleanOperations.gt(converter, o1, o2); }
		@Override public String toString() { return ">"; }
	};
	
	/** The Constant LE. */
	public static final Operator LE = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return BooleanOperations.le(converter, o1, o2); }
		@Override public String toString() { return "<="; }
	};
	
	/** The Constant LT. */
	public static final Operator LT = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return BooleanOperations.lt(converter, o1, o2); }
		@Override public String toString() { return "<"; }
	};
	
	/** The Constant MOD. */
	public static final Operator MOD = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return NumberOperations.mod(converter, o1, o2); }
		@Override public String toString() { return "%"; }
	};
	
	/** The Constant MUL. */
	public static final Operator MUL = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return NumberOperations.mul(converter, o1, o2); }
		@Override public String toString() { return "*"; }
	};
	
	/** The Constant NE. */
	public static final Operator NE = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return BooleanOperations.ne(converter, o1, o2); }
		@Override public String toString() { return "!="; }
	};
	
	/** The Constant OR. */
	public static final Operator OR = new Operator() {
		public Object eval(Bindings bindings, ELContext context, AstNode left, AstNode right) {
			Boolean l = bindings.convert(left.eval(bindings, context), Boolean.class);
			return Boolean.TRUE.equals(l) ? Boolean.TRUE : bindings.convert(right.eval(bindings, context), Boolean.class);
		}
		@Override public String toString() { return "||"; }
	};
	
	/** The Constant SUB. */
	public static final Operator SUB = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return NumberOperations.sub(converter, o1, o2); }
		@Override public String toString() { return "-"; }
	};

	/** The operator. */
	private final Operator operator;
	
	/** The right. */
	private final AstNode left, right;

	/**
	 * Instantiates a new ast binary.
	 *
	 * @param left the left
	 * @param right the right
	 * @param operator the operator
	 */
	public AstBinary(AstNode left, AstNode right, Operator operator) {
		this.left = left;
		this.right = right;
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
	public Object eval(Bindings bindings, ELContext context) {
		return operator.eval(bindings, context, left, right);
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
		left.appendStructure(b, bindings);
		b.append(' ');
		b.append(operator);
		b.append(' ');
		right.appendStructure(b, bindings);
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
	public AstNode getChild(int i) {
		return i == 0 ? left : i == 1 ? right : null;
	}
}
