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

import static org.activiti.engine.impl.juel.Builder.Feature.*;
import static org.activiti.engine.impl.juel.Scanner.Symbol.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.juel.Builder.Feature;
import org.activiti.engine.impl.juel.Scanner.ScanException;
import org.activiti.engine.impl.juel.Scanner.Symbol;
import org.activiti.engine.impl.juel.Scanner.Token;


// TODO: Auto-generated Javadoc
/**
 * Handcrafted top-down parser.
 *
 * @author Christoph Beck
 */
public class Parser {
	
	/**
	 * Parse exception type.
	 */
	@SuppressWarnings("serial")
	public static class ParseException extends Exception {
		
		/** The position. */
		final int position;
		
		/** The encountered. */
		final String encountered;
		
		/** The expected. */
		final String expected;
		
		/**
		 * Instantiates a new parses the exception.
		 *
		 * @param position the position
		 * @param encountered the encountered
		 * @param expected the expected
		 */
		public ParseException(int position, String encountered, String expected) {
			super(LocalMessages.get("error.parse", position, encountered, expected));
			this.position = position;
			this.encountered = encountered;
			this.expected = expected;
		}
	}

	/**
	 * Token type (used to store lookahead).
	 */
	private static final class LookaheadToken {
		
		/** The token. */
		final Token token;
		
		/** The position. */
		final int position;

		/**
		 * Instantiates a new lookahead token.
		 *
		 * @param token the token
		 * @param position the position
		 */
		LookaheadToken(Token token, int position) {
			this.token = token;
			this.position = position;
		}
	}

	/**
	 * The Enum ExtensionPoint.
	 */
	public enum ExtensionPoint {
		
		/** The or. */
		OR,
		
		/** The and. */
		AND,
		
		/** The eq. */
		EQ,
		
		/** The cmp. */
		CMP,
		
		/** The add. */
		ADD,
		
		/** The mul. */
		MUL,
		
		/** The unary. */
		UNARY,
		
		/** The literal. */
		LITERAL
	}

	/**
	 * Provide limited support for syntax extensions.
	 */
	public static abstract class ExtensionHandler {
		
		/** The point. */
		private final ExtensionPoint point;
		
		/**
		 * Instantiates a new extension handler.
		 *
		 * @param point the point
		 */
		public ExtensionHandler(ExtensionPoint point) {
			this.point = point;
		}

		/**
		 * Gets the extension point.
		 *
		 * @return the extension point specifying where this syntax extension is active
		 */
		public ExtensionPoint getExtensionPoint() {
			return point;
		}
		
		/**
		 * Called by the parser if it handles a extended token associated with this handler
		 * at the appropriate extension point.
		 *
		 * @param children the children
		 * @return abstract syntax tree node
		 */
		public abstract AstNode createAstNode(AstNode... children);
	}

	/** The Constant EXPR_FIRST. */
	private static final String EXPR_FIRST =
		IDENTIFIER + "|" + 
		STRING + "|" + FLOAT + "|" + INTEGER + "|" + TRUE + "|" + FALSE + "|" + NULL + "|" +
		MINUS + "|" + NOT + "|" + EMPTY + "|" +
		LPAREN;
	
	/** The context. */
	protected final Builder context;
	
	/** The scanner. */
	protected final Scanner scanner;

	/** The identifiers. */
	private List<IdentifierNode> identifiers = Collections.emptyList();
	
	/** The functions. */
	private List<FunctionNode> functions = Collections.emptyList();
	
	/** The lookahead. */
	private List<LookaheadToken> lookahead = Collections.emptyList();

	/** The token. */
	private Token token; // current token
	
	/** The position. */
	private int position;// current token's position
	
	/** The extensions. */
	protected Map<Scanner.ExtensionToken, ExtensionHandler> extensions = Collections.emptyMap();

	/**
	 * Instantiates a new parser.
	 *
	 * @param context the context
	 * @param input the input
	 */
	public Parser(Builder context, String input) {
		this.context = context;
		this.scanner = createScanner(input);
	}

	/**
	 * Creates the scanner.
	 *
	 * @param expression the expression
	 * @return the scanner
	 */
	protected Scanner createScanner(String expression) {
		return new Scanner(expression);
	}

	/**
	 * Put extension handler.
	 *
	 * @param token the token
	 * @param extension the extension
	 */
	public void putExtensionHandler(Scanner.ExtensionToken token, ExtensionHandler extension) {
		if (extensions.isEmpty()) {
			extensions = new HashMap<Scanner.ExtensionToken, ExtensionHandler>(16);
		}
		extensions.put(token, extension);
	}
	
	/**
	 * Gets the extension handler.
	 *
	 * @param token the token
	 * @return the extension handler
	 */
	protected ExtensionHandler getExtensionHandler(Token token) {
		return extensions.get(token);
	}
	
	/**
	 * Parse an integer literal.
	 *
	 * @param string string to parse
	 * @return <code>Long.valueOf(string)</code>
	 * @throws ParseException the parse exception
	 */
	protected Number parseInteger(String string) throws ParseException {
		try {
			return Long.valueOf(string);
		} catch (NumberFormatException e) {
			fail(INTEGER);
			return null;
		}
	}
	
	/**
	 * Parse a floating point literal.
	 *
	 * @param string string to parse
	 * @return <code>Double.valueOf(string)</code>
	 * @throws ParseException the parse exception
	 */
	protected Number parseFloat(String string) throws ParseException {
		try {
			return Double.valueOf(string);
		} catch (NumberFormatException e) {
			fail(FLOAT);
			return null;
		}
	}

	/**
	 * Creates the ast binary.
	 *
	 * @param left the left
	 * @param right the right
	 * @param operator the operator
	 * @return the ast binary
	 */
	protected AstBinary createAstBinary(AstNode left, AstNode right, AstBinary.Operator operator) {
		return new AstBinary(left, right, operator);
	}
	
	/**
	 * Creates the ast bracket.
	 *
	 * @param base the base
	 * @param property the property
	 * @param lvalue the lvalue
	 * @param strict the strict
	 * @return the ast bracket
	 */
	protected AstBracket createAstBracket(AstNode base, AstNode property, boolean lvalue, boolean strict) {
		return new AstBracket(base, property, lvalue, strict);
	}
	
	/**
	 * Creates the ast choice.
	 *
	 * @param question the question
	 * @param yes the yes
	 * @param no the no
	 * @return the ast choice
	 */
	protected AstChoice createAstChoice(AstNode question, AstNode yes, AstNode no) {
		return new AstChoice(question, yes, no);
	}
	
	/**
	 * Creates the ast composite.
	 *
	 * @param nodes the nodes
	 * @return the ast composite
	 */
	protected AstComposite createAstComposite(List<AstNode> nodes) {
		return new AstComposite(nodes);
	}
	
	/**
	 * Creates the ast dot.
	 *
	 * @param base the base
	 * @param property the property
	 * @param lvalue the lvalue
	 * @return the ast dot
	 */
	protected AstDot createAstDot(AstNode base, String property, boolean lvalue) {
		return new AstDot(base, property, lvalue);
	}
	
	/**
	 * Creates the ast function.
	 *
	 * @param name the name
	 * @param index the index
	 * @param params the params
	 * @return the ast function
	 */
	protected AstFunction createAstFunction(String name, int index, AstParameters params) {
		return new AstFunction(name, index, params, context.isEnabled(Feature.VARARGS));
	}

	/**
	 * Creates the ast identifier.
	 *
	 * @param name the name
	 * @param index the index
	 * @return the ast identifier
	 */
	protected AstIdentifier createAstIdentifier(String name, int index) {
		return new AstIdentifier(name, index);
	}
	
	/**
	 * Creates the ast method.
	 *
	 * @param property the property
	 * @param params the params
	 * @return the ast method
	 */
	protected AstMethod createAstMethod(AstProperty property, AstParameters params) {
		return new AstMethod(property, params);
	}
	
	/**
	 * Creates the ast unary.
	 *
	 * @param child the child
	 * @param operator the operator
	 * @return the ast unary
	 */
	protected AstUnary createAstUnary(AstNode child, AstUnary.Operator operator) {
		return new AstUnary(child, operator);
	}

	/**
	 * Gets the functions.
	 *
	 * @return the functions
	 */
	protected final List<FunctionNode> getFunctions() {
		return functions;
	}
	
	/**
	 * Gets the identifiers.
	 *
	 * @return the identifiers
	 */
	protected final List<IdentifierNode> getIdentifiers() {
		return identifiers;
	}

	/**
	 * Gets the token.
	 *
	 * @return the token
	 */
	protected final Token getToken() {
		return token;
	}

	/**
	 * throw exception.
	 *
	 * @param expected the expected
	 * @throws ParseException the parse exception
	 */
	protected void fail(String expected) throws ParseException {
		throw new ParseException(position, "'" + token.getImage() + "'", expected);
	}

	/**
	 * throw exception.
	 *
	 * @param expected the expected
	 * @throws ParseException the parse exception
	 */
	protected void fail(Symbol expected) throws ParseException {
		fail(expected.toString());
	}

	/**
	 * get lookahead symbol.
	 *
	 * @param index the index
	 * @return the token
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected final Token lookahead(int index) throws ScanException, ParseException {
		if (lookahead.isEmpty()) {
			lookahead = new LinkedList<LookaheadToken>();
		}
		while (index >= lookahead.size()) {
			lookahead.add(new LookaheadToken(scanner.next(), scanner.getPosition()));
		}
		return lookahead.get(index).token;
	}

	/**
	 * consume current token (get next token).
	 *
	 * @return the consumed token (which was the current token when calling this method)
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected final Token consumeToken() throws ScanException, ParseException {
		Token result = token;
		if (lookahead.isEmpty()) {
			token = scanner.next();
			position = scanner.getPosition();
		} else {
			LookaheadToken next = lookahead.remove(0);
			token = next.token;
			position = next.position;
		}
		return result;
	}

	/**
	 * consume current token (get next token); throw exception if the current token doesn't
	 * match the expected symbol.
	 *
	 * @param expected the expected
	 * @return the token
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected final Token consumeToken(Symbol expected) throws ScanException, ParseException {
		if (token.getSymbol() != expected) {
			fail(expected);
		}
		return consumeToken();
	}
	
	/**
	 * tree := text? ((dynamic text?)+ | (deferred text?)+)?.
	 *
	 * @return the tree
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	public Tree tree() throws ScanException, ParseException {
		consumeToken();
		AstNode t = text();
		if (token.getSymbol() == EOF) {
			if (t == null) {
				t = new AstText("");
			}
			return new Tree(t, functions, identifiers, false);
		}
		AstEval e = eval();
		if (token.getSymbol() == EOF && t == null) {
			return new Tree(e, functions, identifiers, e.isDeferred());
		}
		ArrayList<AstNode> list = new ArrayList<AstNode>();
		if (t != null) {
			list.add(t);
		}
		list.add(e);
		t = text();
		if (t != null) {
			list.add(t);
		}
		while (token.getSymbol() != EOF) {
			if (e.isDeferred()) {
				list.add(eval(true, true));
			} else {
				list.add(eval(true, false));
			}
			t = text();
			if (t != null) {
				list.add(t);
			}
		}
		return new Tree(createAstComposite(list), functions, identifiers, e.isDeferred());
	}

	/**
	 * text := &lt;TEXT&gt;.
	 *
	 * @return the ast node
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstNode text() throws ScanException, ParseException {
		AstNode v = null;
		if (token.getSymbol() == TEXT) {
			v = new AstText(token.getImage());
			consumeToken();
		}
		return v;
	}

	/**
	 * eval := dynamic | deferred.
	 *
	 * @return the ast eval
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstEval eval() throws ScanException, ParseException {
		AstEval e = eval(false, false);
		if (e == null) {
			e = eval(false, true);
			if (e == null) {
				fail(START_EVAL_DEFERRED + "|" + START_EVAL_DYNAMIC);
			}
		}
		return e;
	}

	/**
	 * dynmamic := &lt;START_EVAL_DYNAMIC&gt; expr &lt;END_EVAL&gt;
	 * deferred := &lt;START_EVAL_DEFERRED&gt; expr &lt;END_EVAL&gt;.
	 *
	 * @param required the required
	 * @param deferred the deferred
	 * @return the ast eval
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstEval eval(boolean required, boolean deferred) throws ScanException, ParseException {
		AstEval v = null;
		Symbol start_eval = deferred ? START_EVAL_DEFERRED : START_EVAL_DYNAMIC;
		if (token.getSymbol() == start_eval) {
			consumeToken();
			v = new AstEval(expr(true), deferred);
			consumeToken(END_EVAL);
		} else if (required) {
			fail(start_eval);
		}
		return v;
	}

	/**
	 * expr := or (&lt;QUESTION&gt; expr &lt;COLON&gt; expr)?.
	 *
	 * @param required the required
	 * @return the ast node
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstNode expr(boolean required) throws ScanException, ParseException {
		AstNode v = or(required);
		if (v == null) {
			return null;
		}
		if (token.getSymbol() == QUESTION) {
			consumeToken();
			AstNode a = expr(true);
			consumeToken(COLON);
			AstNode b = expr(true);
			v = createAstChoice(v, a, b);
		}
		return v;
	}

	/**
	 * or := and (&lt;OR&gt; and)*.
	 *
	 * @param required the required
	 * @return the ast node
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstNode or(boolean required) throws ScanException, ParseException {
		AstNode v = and(required);
		if (v == null) {
			return null;
		}
		while (true) {
			switch (token.getSymbol()) {
				case OR:
					consumeToken();
					v = createAstBinary(v, and(true), AstBinary.OR);
					break;
				case EXTENSION:
					if (getExtensionHandler(token).getExtensionPoint() == ExtensionPoint.OR) {
						v = getExtensionHandler(consumeToken()).createAstNode(v, and(true));
						break;
					}
				default:
					return v;
			}
		}
	}

	/**
	 * and := eq (&lt;AND&gt; eq)*.
	 *
	 * @param required the required
	 * @return the ast node
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstNode and(boolean required) throws ScanException, ParseException {
		AstNode v = eq(required);
		if (v == null) {
			return null;
		}
		while (true) {
			switch (token.getSymbol()) {
				case AND:
					consumeToken();
					v = createAstBinary(v, eq(true), AstBinary.AND);
					break;
				case EXTENSION:
					if (getExtensionHandler(token).getExtensionPoint() == ExtensionPoint.AND) {
						v = getExtensionHandler(consumeToken()).createAstNode(v, eq(true));
						break;
					}
				default:
					return v;
			}
		}
	}

	/**
	 * eq := cmp (&lt;EQ&gt; cmp | &lt;NE&gt; cmp)*.
	 *
	 * @param required the required
	 * @return the ast node
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstNode eq(boolean required) throws ScanException, ParseException {
		AstNode v = cmp(required);
		if (v == null) {
			return null;
		}
		while (true) {
			switch (token.getSymbol()) {
				case EQ:
					consumeToken();
					v = createAstBinary(v, cmp(true), AstBinary.EQ);
					break;
				case NE:
					consumeToken();
					v = createAstBinary(v, cmp(true), AstBinary.NE);
					break;
				case EXTENSION:
					if (getExtensionHandler(token).getExtensionPoint() == ExtensionPoint.EQ) {
						v = getExtensionHandler(consumeToken()).createAstNode(v, cmp(true));
						break;
					}
				default:
					return v;
			}
		}
	}
	
	/**
	 * cmp := add (&lt;LT&gt; add | &lt;LE&gt; add | &lt;GE&gt; add | &lt;GT&gt; add)*.
	 *
	 * @param required the required
	 * @return the ast node
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstNode cmp(boolean required) throws ScanException, ParseException {
		AstNode v = add(required);
		if (v == null) {
			return null;
		}
		while (true) {
			switch (token.getSymbol()) {
				case LT:
					consumeToken();
					v = createAstBinary(v, add(true), AstBinary.LT);
					break;
				case LE:
					consumeToken();
					v = createAstBinary(v, add(true), AstBinary.LE);
					break;
				case GE:
					consumeToken();
					v = createAstBinary(v, add(true), AstBinary.GE);
					break;
				case GT:
					consumeToken();
					v = createAstBinary(v, add(true), AstBinary.GT);
					break;
				case EXTENSION:
					if (getExtensionHandler(token).getExtensionPoint() == ExtensionPoint.CMP) {
						v = getExtensionHandler(consumeToken()).createAstNode(v, add(true));
						break;
					}
				default:
					return v;
			}
		}
	}

	/**
	 * add := add (&lt;PLUS&gt; mul | &lt;MINUS&gt; mul)*.
	 *
	 * @param required the required
	 * @return the ast node
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstNode add(boolean required) throws ScanException, ParseException {
		AstNode v = mul(required);
		if (v == null) {
			return null;
		}
		while (true) {
			switch (token.getSymbol()) {
				case PLUS:
					consumeToken();
					v = createAstBinary(v, mul(true), AstBinary.ADD);
					break;
				case MINUS:
					consumeToken();
					v = createAstBinary(v, mul(true), AstBinary.SUB);
					break;
				case EXTENSION:
					if (getExtensionHandler(token).getExtensionPoint() == ExtensionPoint.ADD) {
						v = getExtensionHandler(consumeToken()).createAstNode(v, mul(true));
						break;
					}
				default:
					return v;
			}
		}
	}

	/**
	 * mul := unary (&lt;MUL&gt; unary | &lt;DIV&gt; unary | &lt;MOD&gt; unary)*.
	 *
	 * @param required the required
	 * @return the ast node
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstNode mul(boolean required) throws ScanException, ParseException {
		AstNode v = unary(required);
		if (v == null) {
			return null;
		}
		while (true) {
			switch (token.getSymbol()) {
				case MUL:
					consumeToken();
					v = createAstBinary(v, unary(true), AstBinary.MUL);
					break;
				case DIV:
					consumeToken();
					v = createAstBinary(v, unary(true), AstBinary.DIV);
					break;
				case MOD:
					consumeToken();
					v = createAstBinary(v, unary(true), AstBinary.MOD);
					break;
				case EXTENSION:
					if (getExtensionHandler(token).getExtensionPoint() == ExtensionPoint.MUL) {
						v = getExtensionHandler(consumeToken()).createAstNode(v, unary(true));
						break;
					}
				default:
					return v;
			}
		}
	}

	/**
	 * unary := &lt;NOT&gt; unary | &lt;MINUS&gt; unary | &lt;EMPTY&gt; unary | value.
	 *
	 * @param required the required
	 * @return the ast node
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstNode unary(boolean required) throws ScanException, ParseException {
		AstNode v = null;
		switch (token.getSymbol()) {
			case NOT:
				consumeToken();
				v = createAstUnary(unary(true), AstUnary.NOT);
				break;
			case MINUS:
				consumeToken();
				v = createAstUnary(unary(true), AstUnary.NEG);
				break;
			case EMPTY:
				consumeToken();
				v = createAstUnary(unary(true), AstUnary.EMPTY);
				break;
			case EXTENSION:
				if (getExtensionHandler(token).getExtensionPoint() == ExtensionPoint.UNARY) {
					v = getExtensionHandler(consumeToken()).createAstNode(unary(true));
					break;
				}
			default:
				v = value();
		}
		if (v == null && required) {
			fail(EXPR_FIRST);
		}
		return v;
	}

	/**
	 * value := (nonliteral | literal) (&lt;DOT&gt; &lt;IDENTIFIER&gt; | &lt;LBRACK&gt; expr &lt;RBRACK&gt;)*.
	 *
	 * @return the ast node
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstNode value() throws ScanException, ParseException {
		boolean lvalue = true;
		AstNode v = nonliteral();
		if (v == null) {
			v = literal();
			if (v == null) {
				return null;
			}
			lvalue = false;
		}
		while (true) {
			switch (token.getSymbol()) {
				case DOT:
					consumeToken();
					String name = consumeToken(IDENTIFIER).getImage();
					AstDot dot = createAstDot(v, name, lvalue);
					if (token.getSymbol() == LPAREN && context.isEnabled(METHOD_INVOCATIONS)) {
						v = createAstMethod(dot, params());
					} else {
						v = dot;
					}
					break;
				case LBRACK:
					consumeToken();
					AstNode property = expr(true);
					boolean strict = !context.isEnabled(NULL_PROPERTIES);
					consumeToken(RBRACK);
					AstBracket bracket = createAstBracket(v, property, lvalue, strict);
					if (token.getSymbol() == LPAREN && context.isEnabled(METHOD_INVOCATIONS)) {
						v = createAstMethod(bracket, params());
					} else {
						v = bracket;
					}
					break;
				default:
					return v;
			}
		}
	}

	/**
	 * nonliteral := &lt;IDENTIFIER&gt; | function | &lt;LPAREN&gt; expr &lt;RPAREN&gt;
	 * function   := (&lt;IDENTIFIER&gt; &lt;COLON&gt;)? &lt;IDENTIFIER&gt; &lt;LPAREN&gt; list? &lt;RPAREN&gt;.
	 *
	 * @return the ast node
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstNode nonliteral() throws ScanException, ParseException {
		AstNode v = null;
		switch (token.getSymbol()) {
			case IDENTIFIER:
				String name = consumeToken().getImage();
				if (token.getSymbol() == COLON && lookahead(0).getSymbol() == IDENTIFIER && lookahead(1).getSymbol() == LPAREN) { // ns:f(...)
					consumeToken();
					name += ":" + token.getImage();
					consumeToken();
				}
				if (token.getSymbol() == LPAREN) { // function
					v = function(name, params());
				} else { // identifier
					v = identifier(name);
				}
				break;
			case LPAREN:
				consumeToken();
				v = expr(true);
				consumeToken(RPAREN);
				v = new AstNested(v);
				break;
		}
		return v;
	}

	/**
	 * params := &lt;LPAREN&gt; (expr (&lt;COMMA&gt; expr)*)? &lt;RPAREN&gt;.
	 *
	 * @return the ast parameters
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstParameters params() throws ScanException, ParseException {
		consumeToken(LPAREN);
		List<AstNode> l = Collections.emptyList();
		AstNode v = expr(false);
		if (v != null) {
			l = new ArrayList<AstNode>();
			l.add(v);
			while (token.getSymbol() == COMMA) {
				consumeToken();
				l.add(expr(true));
			}
		}
		consumeToken(RPAREN);
		return new AstParameters(l);
	}
	
	/**
	 * literal := &lt;TRUE&gt; | &lt;FALSE&gt; | &lt;STRING&gt; | &lt;INTEGER&gt; | &lt;FLOAT&gt; | &lt;NULL&gt;.
	 *
	 * @return the ast node
	 * @throws ScanException the scan exception
	 * @throws ParseException the parse exception
	 */
	protected AstNode literal() throws ScanException, ParseException {
		AstNode v = null;
		switch (token.getSymbol()) {
			case TRUE:
				v = new AstBoolean(true);
				consumeToken();
				break;
			case FALSE:
				v = new AstBoolean(false);
				consumeToken();
				break;
			case STRING:
				v = new AstString(token.getImage());
				consumeToken();
				break;
			case INTEGER:
				v = new AstNumber(parseInteger(token.getImage()));
				consumeToken();
				break;
			case FLOAT:
				v = new AstNumber(parseFloat(token.getImage()));
				consumeToken();
				break;			
			case NULL:
				v = new AstNull();
				consumeToken();
				break;
			case EXTENSION:
				if (getExtensionHandler(token).getExtensionPoint() == ExtensionPoint.LITERAL) {
					v = getExtensionHandler(consumeToken()).createAstNode();
					break;
				}
		}
		return v;
	}

	/**
	 * Function.
	 *
	 * @param name the name
	 * @param params the params
	 * @return the ast function
	 */
	protected final AstFunction function(String name, AstParameters params) {
		if (functions.isEmpty()) {
			functions = new ArrayList<FunctionNode>(4);
		}
		AstFunction function = createAstFunction(name, functions.size(), params);
		functions.add(function);
		return function;
	}
	
	/**
	 * Identifier.
	 *
	 * @param name the name
	 * @return the ast identifier
	 */
	protected final AstIdentifier identifier(String name) {
		if (identifiers.isEmpty()) {
			identifiers = new ArrayList<IdentifierNode>(4);
		}
		AstIdentifier identifier = createAstIdentifier(name, identifiers.size());
		identifiers.add(identifier);
		return identifier;
	}
}