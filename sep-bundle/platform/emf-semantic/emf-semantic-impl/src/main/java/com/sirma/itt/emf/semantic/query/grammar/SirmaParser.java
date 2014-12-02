// Generated from
// A:/Development/Workspaces/IDoc/sep-bundle/platform/emf-semantic/emf-semantic-impl/src/main/resources/com/sirma/itt/emf/semantic/query/grammar/Sirma.g4
// by ANTLR 4.1

package com.sirma.itt.emf.semantic.query.grammar;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNSimulator;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.sirma.itt.emf.semantic.query.Operator;
import com.sirma.itt.emf.semantic.query.PredicateValidator;

// TODO: Auto-generated Javadoc
/**
 * The Class SirmaParser.
 */
@SuppressWarnings({ "all", "warnings", "unchecked", "unused", "cast" })
public class SirmaParser extends Parser {

	/** The Constant _decisionToDFA. */
	protected static final DFA[] _decisionToDFA;

	/** The Constant _sharedContextCache. */
	protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();

	/** The Constant WS. */
	public static final int T__1 = 1, T__0 = 2, LPAREN = 3, RPAREN = 4, COMMA = 5, LBRACKET = 6,
			RBRACKET = 7, FIND = 8, WHERE = 9, BANG = 10, LT = 11, GT = 12, GTEQ = 13, LTEQ = 14,
			EQUALS = 15, NOT_EQUALS = 16, LIKE = 17, NOT_LIKE = 18, IN = 19, IS = 20, AND = 21,
			OR = 22, NOT = 23, EMPTY = 24, WORD = 25, INTEGER = 26, DATEFULL = 27, DATE = 28,
			TIME = 29, ALL = 30, HAS = 31, WS = 32;

	/** The Constant tokenNames. */
	public static final String[] tokenNames = { "<INVALID>", "'has Relation'", "'has Design'",
			"LPAREN", "')'", "','", "'['", "']'", "FIND", "WHERE", "'!'", "'<'", "'>'", "'>='",
			"'<='", "'='", "'!='", "LIKE", "'!~'", "IN", "IS", "AND", "OR", "NOT", "EMPTY", "WORD",
			"INTEGER", "DATEFULL", "DATE", "TIME", "ALL", "HAS", "WS" };

	/** The Constant RULE_date. */
	public static final int RULE_query = 0, RULE_subStatement = 1, RULE_clause = 2,
			RULE_orClause = 3, RULE_booleanClause = 4, RULE_listOfValues = 5, RULE_object = 6,
			RULE_subject = 7, RULE_value = 8, RULE_operator = 9, RULE_relation = 10,
			RULE_date = 11;

	/** The Constant ruleNames. */
	public static final String[] ruleNames = { "query", "subStatement", "clause", "orClause",
			"booleanClause", "listOfValues", "object", "subject", "value", "operator", "relation",
			"date" };

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getGrammarFileName() {
		return "Sirma.g4";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getTokenNames() {
		return tokenNames;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getRuleNames() {
		return ruleNames;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ATN getATN() {
		return _ATN;
	}

	/** The predicate validator. */
	private PredicateValidator predicateValidator;

	/**
	 * Sets the predicate validator.
	 * 
	 * @param validator
	 *            the new predicate validator
	 */
	public void setPredicateValidator(PredicateValidator validator) {
		this.predicateValidator = validator;
	}

	/**
	 * Instantiates a new sirma parser.
	 * 
	 * @param input
	 *            the input
	 */
	public SirmaParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
	}

	/**
	 * The Class QueryContext.
	 */
	public static class QueryContext extends ParserRuleContext {

		/**
		 * Where.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode WHERE() {
			return getToken(SirmaParser.WHERE, 0);
		}

		/**
		 * Object.
		 * 
		 * @return the object context
		 */
		public ObjectContext object() {
			return getRuleContext(ObjectContext.class, 0);
		}

		/**
		 * Or clause.
		 * 
		 * @return the list
		 */
		public List<OrClauseContext> orClause() {
			return getRuleContexts(OrClauseContext.class);
		}

		/**
		 * Find.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode FIND() {
			return getToken(SirmaParser.FIND, 0);
		}

		/**
		 * Or clause.
		 * 
		 * @param i
		 *            the i
		 * @return the or clause context
		 */
		public OrClauseContext orClause(int i) {
			return getRuleContext(OrClauseContext.class, i);
		}

		/**
		 * Instantiates a new query context.
		 * 
		 * @param parent
		 *            the parent
		 * @param invokingState
		 *            the invoking state
		 */
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRuleIndex() {
			return RULE_query;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).enterQuery(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).exitQuery(this);
		}
	}

	/**
	 * Query.
	 * 
	 * @return the query context
	 * @throws RecognitionException
	 *             the recognition exception
	 */
	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_query);
		int _la;
		try {
			setState(37);
			switch (_input.LA(1)) {
				case FIND:
					enterOuterAlt(_localctx, 1);
					{
						setState(24);
						match(FIND);
						setState(25);
						object();
						setState(26);
						match(WHERE);
						setState(28);
						_errHandler.sync(this);
						_la = _input.LA(1);
						do {
							{
								{
									setState(27);
									orClause();
								}
							}
							setState(30);
							_errHandler.sync(this);
							_la = _input.LA(1);
						} while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 1) | (1L << 2) | (1L << WORD))) != 0));
					}
					break;
				case 1:
				case 2:
				case WORD:
					enterOuterAlt(_localctx, 2);
					{
						setState(33);
						_errHandler.sync(this);
						_la = _input.LA(1);
						do {
							{
								{
									setState(32);
									orClause();
								}
							}
							setState(35);
							_errHandler.sync(this);
							_la = _input.LA(1);
						} while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 1) | (1L << 2) | (1L << WORD))) != 0));
					}
					break;
				default:
					throw new NoViableAltException(this);
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class SubStatementContext.
	 */
	public static class SubStatementContext extends ParserRuleContext {

		/**
		 * Or clause.
		 * 
		 * @return the list
		 */
		public List<OrClauseContext> orClause() {
			return getRuleContexts(OrClauseContext.class);
		}

		/**
		 * Rparen.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode RPAREN() {
			return getToken(SirmaParser.RPAREN, 0);
		}

		/**
		 * Or clause.
		 * 
		 * @param i
		 *            the i
		 * @return the or clause context
		 */
		public OrClauseContext orClause(int i) {
			return getRuleContext(OrClauseContext.class, i);
		}

		/**
		 * Lparen.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode LPAREN() {
			return getToken(SirmaParser.LPAREN, 0);
		}

		/**
		 * Instantiates a new sub statement context.
		 * 
		 * @param parent
		 *            the parent
		 * @param invokingState
		 *            the invoking state
		 */
		public SubStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRuleIndex() {
			return RULE_subStatement;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).enterSubStatement(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).exitSubStatement(this);
		}
	}

	/**
	 * Sub statement.
	 * 
	 * @return the sub statement context
	 * @throws RecognitionException
	 *             the recognition exception
	 */
	public final SubStatementContext subStatement() throws RecognitionException {
		SubStatementContext _localctx = new SubStatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_subStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(39);
				match(LPAREN);
				setState(41);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
						{
							setState(40);
							orClause();
						}
					}
					setState(43);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 1) | (1L << 2) | (1L << WORD))) != 0));
				setState(45);
				match(RPAREN);
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ClauseContext.
	 */
	public static class ClauseContext extends ParserRuleContext {

		/**
		 * Boolean clause.
		 * 
		 * @param i
		 *            the i
		 * @return the boolean clause context
		 */
		public BooleanClauseContext booleanClause(int i) {
			return getRuleContext(BooleanClauseContext.class, i);
		}

		/**
		 * And.
		 * 
		 * @return the list
		 */
		public List<TerminalNode> AND() {
			return getTokens(SirmaParser.AND);
		}

		/**
		 * Boolean clause.
		 * 
		 * @return the list
		 */
		public List<BooleanClauseContext> booleanClause() {
			return getRuleContexts(BooleanClauseContext.class);
		}

		/**
		 * And.
		 * 
		 * @param i
		 *            the i
		 * @return the terminal node
		 */
		public TerminalNode AND(int i) {
			return getToken(SirmaParser.AND, i);
		}

		/**
		 * Instantiates a new clause context.
		 * 
		 * @param parent
		 *            the parent
		 * @param invokingState
		 *            the invoking state
		 */
		public ClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRuleIndex() {
			return RULE_clause;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).enterClause(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).exitClause(this);
		}
	}

	/**
	 * Clause.
	 * 
	 * @return the clause context
	 * @throws RecognitionException
	 *             the recognition exception
	 */
	public final ClauseContext clause() throws RecognitionException {
		ClauseContext _localctx = new ClauseContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(47);
				booleanClause();
				setState(52);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la == AND) {
					{
						{
							setState(48);
							match(AND);
							setState(49);
							booleanClause();
						}
					}
					setState(54);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class OrClauseContext.
	 */
	public static class OrClauseContext extends ParserRuleContext {

		/**
		 * Clause.
		 * 
		 * @param i
		 *            the i
		 * @return the clause context
		 */
		public ClauseContext clause(int i) {
			return getRuleContext(ClauseContext.class, i);
		}

		/**
		 * Or.
		 * 
		 * @return the list
		 */
		public List<TerminalNode> OR() {
			return getTokens(SirmaParser.OR);
		}

		/**
		 * Or.
		 * 
		 * @param i
		 *            the i
		 * @return the terminal node
		 */
		public TerminalNode OR(int i) {
			return getToken(SirmaParser.OR, i);
		}

		/**
		 * Clause.
		 * 
		 * @return the list
		 */
		public List<ClauseContext> clause() {
			return getRuleContexts(ClauseContext.class);
		}

		/**
		 * Instantiates a new or clause context.
		 * 
		 * @param parent
		 *            the parent
		 * @param invokingState
		 *            the invoking state
		 */
		public OrClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRuleIndex() {
			return RULE_orClause;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).enterOrClause(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).exitOrClause(this);
		}
	}

	/**
	 * Or clause.
	 * 
	 * @return the or clause context
	 * @throws RecognitionException
	 *             the recognition exception
	 */
	public final OrClauseContext orClause() throws RecognitionException {
		OrClauseContext _localctx = new OrClauseContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_orClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(55);
				clause();
				setState(60);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la == OR) {
					{
						{
							setState(56);
							match(OR);
							setState(57);
							clause();
						}
					}
					setState(62);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class BooleanClauseContext.
	 */
	public static class BooleanClauseContext extends ParserRuleContext {

		/** The subject. */
		public SubjectContext subject;

		/** The operator. */
		public OperatorContext operator;

		/** The value. */
		public ValueContext value;

		/**
		 * In.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode IN() {
			return getToken(SirmaParser.IN, 0);
		}

		/**
		 * Relation.
		 * 
		 * @return the relation context
		 */
		public RelationContext relation() {
			return getRuleContext(RelationContext.class, 0);
		}

		/**
		 * List of values.
		 * 
		 * @return the list of values context
		 */
		public ListOfValuesContext listOfValues() {
			return getRuleContext(ListOfValuesContext.class, 0);
		}

		/**
		 * Value.
		 * 
		 * @return the value context
		 */
		public ValueContext value() {
			return getRuleContext(ValueContext.class, 0);
		}

		/**
		 * Operator.
		 * 
		 * @return the operator context
		 */
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class, 0);
		}

		/**
		 * Subject.
		 * 
		 * @return the subject context
		 */
		public SubjectContext subject() {
			return getRuleContext(SubjectContext.class, 0);
		}

		/**
		 * Instantiates a new boolean clause context.
		 * 
		 * @param parent
		 *            the parent
		 * @param invokingState
		 *            the invoking state
		 */
		public BooleanClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRuleIndex() {
			return RULE_booleanClause;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).enterBooleanClause(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).exitBooleanClause(this);
		}
	}

	/**
	 * Boolean clause.
	 * 
	 * @return the boolean clause context
	 * @throws RecognitionException
	 *             the recognition exception
	 */
	public final BooleanClauseContext booleanClause() throws RecognitionException {
		BooleanClauseContext _localctx = new BooleanClauseContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_booleanClause);
		try {
			setState(75);
			switch (getInterpreter().adaptivePredict(_input, 6, _ctx)) {
				case 1:
					enterOuterAlt(_localctx, 1);
					{
						setState(63);
						((BooleanClauseContext) _localctx).subject = subject();
						setState(64);
						((BooleanClauseContext) _localctx).operator = operator();
						setState(65);
						((BooleanClauseContext) _localctx).value = value();
						System.out.println(((BooleanClauseContext) _localctx).subject.v);
						System.out.println(((BooleanClauseContext) _localctx).operator.op);
						System.out.println(((BooleanClauseContext) _localctx).value.v);
					}
					break;

				case 2:
					enterOuterAlt(_localctx, 2);
					{
						setState(68);
						subject();
						setState(69);
						match(IN);
						setState(70);
						listOfValues();
					}
					break;

				case 3:
					enterOuterAlt(_localctx, 3);
					{
						setState(72);
						relation();
						setState(73);
						value();
					}
					break;
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ListOfValuesContext.
	 */
	public static class ListOfValuesContext extends ParserRuleContext {

		/**
		 * Value.
		 * 
		 * @param i
		 *            the i
		 * @return the value context
		 */
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class, i);
		}

		/**
		 * Value.
		 * 
		 * @return the list
		 */
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}

		/**
		 * Rparen.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode RPAREN() {
			return getToken(SirmaParser.RPAREN, 0);
		}

		/**
		 * Lparen.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode LPAREN() {
			return getToken(SirmaParser.LPAREN, 0);
		}

		/**
		 * Instantiates a new list of values context.
		 * 
		 * @param parent
		 *            the parent
		 * @param invokingState
		 *            the invoking state
		 */
		public ListOfValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRuleIndex() {
			return RULE_listOfValues;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).enterListOfValues(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).exitListOfValues(this);
		}
	}

	/**
	 * List of values.
	 * 
	 * @return the list of values context
	 * @throws RecognitionException
	 *             the recognition exception
	 */
	public final ListOfValuesContext listOfValues() throws RecognitionException {
		ListOfValuesContext _localctx = new ListOfValuesContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_listOfValues);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(77);
				match(LPAREN);
				setState(78);
				value();
				setState(83);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la == COMMA) {
					{
						{
							setState(79);
							match(COMMA);
							setState(80);
							value();
						}
					}
					setState(85);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(86);
				match(RPAREN);
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ObjectContext.
	 */
	public static class ObjectContext extends ParserRuleContext {

		/** The v. */
		public String v;

		/** The word. */
		public Token WORD;

		/**
		 * Word.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode WORD() {
			return getToken(SirmaParser.WORD, 0);
		}

		/**
		 * All.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode ALL() {
			return getToken(SirmaParser.ALL, 0);
		}

		/**
		 * Instantiates a new object context.
		 * 
		 * @param parent
		 *            the parent
		 * @param invokingState
		 *            the invoking state
		 */
		public ObjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRuleIndex() {
			return RULE_object;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).enterObject(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).exitObject(this);
		}
	}

	/**
	 * Object.
	 * 
	 * @return the object context
	 * @throws RecognitionException
	 *             the recognition exception
	 */
	public final ObjectContext object() throws RecognitionException {
		ObjectContext _localctx = new ObjectContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_object);
		try {
			setState(92);
			switch (_input.LA(1)) {
				case ALL:
					enterOuterAlt(_localctx, 1);
					{
						setState(88);
						match(ALL);
						((ObjectContext) _localctx).v = "ALL";
					}
					break;
				case WORD:
					enterOuterAlt(_localctx, 2);
					{
						setState(90);
						((ObjectContext) _localctx).WORD = match(WORD);
						((ObjectContext) _localctx).v = predicateValidator
								.getClassURI((((ObjectContext) _localctx).WORD != null ? ((ObjectContext) _localctx).WORD
										.getText() : null));
					}
					break;
				default:
					throw new NoViableAltException(this);
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class SubjectContext.
	 */
	public static class SubjectContext extends ParserRuleContext {

		/** The v. */
		public String v;

		/** The word. */
		public Token WORD;

		/**
		 * Word.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode WORD() {
			return getToken(SirmaParser.WORD, 0);
		}

		/**
		 * Instantiates a new subject context.
		 * 
		 * @param parent
		 *            the parent
		 * @param invokingState
		 *            the invoking state
		 */
		public SubjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRuleIndex() {
			return RULE_subject;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).enterSubject(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).exitSubject(this);
		}
	}

	/**
	 * Subject.
	 * 
	 * @return the subject context
	 * @throws RecognitionException
	 *             the recognition exception
	 */
	public final SubjectContext subject() throws RecognitionException {
		SubjectContext _localctx = new SubjectContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_subject);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(94);
				((SubjectContext) _localctx).WORD = match(WORD);
				((SubjectContext) _localctx).v = predicateValidator
						.getPredicateURI((((SubjectContext) _localctx).WORD != null ? ((SubjectContext) _localctx).WORD
								.getText() : null));
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ValueContext.
	 */
	public static class ValueContext extends ParserRuleContext {

		/** The v. */
		public Serializable v;

		/** The word. */
		public Token WORD;

		/** The date. */
		public DateContext date;

		/**
		 * Word.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode WORD() {
			return getToken(SirmaParser.WORD, 0);
		}

		/**
		 * Date.
		 * 
		 * @return the date context
		 */
		public DateContext date() {
			return getRuleContext(DateContext.class, 0);
		}

		/**
		 * Instantiates a new value context.
		 * 
		 * @param parent
		 *            the parent
		 * @param invokingState
		 *            the invoking state
		 */
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRuleIndex() {
			return RULE_value;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).enterValue(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).exitValue(this);
		}
	}

	/**
	 * Value.
	 * 
	 * @return the value context
	 * @throws RecognitionException
	 *             the recognition exception
	 */
	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_value);
		try {
			setState(102);
			switch (_input.LA(1)) {
				case WORD:
					enterOuterAlt(_localctx, 1);
					{
						setState(97);
						((ValueContext) _localctx).WORD = match(WORD);
						((ValueContext) _localctx).v = (((ValueContext) _localctx).WORD != null ? ((ValueContext) _localctx).WORD
								.getText() : null);
					}
					break;
				case DATEFULL:
				case DATE:
					enterOuterAlt(_localctx, 2);
					{
						setState(99);
						((ValueContext) _localctx).date = date();
						((ValueContext) _localctx).v = ((ValueContext) _localctx).date.dValue;
					}
					break;
				default:
					throw new NoViableAltException(this);
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class OperatorContext.
	 */
	public static class OperatorContext extends ParserRuleContext {

		/** The op. */
		public Operator op;

		/**
		 * In.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode IN() {
			return getToken(SirmaParser.IN, 0);
		}

		/**
		 * Gteq.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode GTEQ() {
			return getToken(SirmaParser.GTEQ, 0);
		}

		/**
		 * Equals.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode EQUALS() {
			return getToken(SirmaParser.EQUALS, 0);
		}

		/**
		 * Lt.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode LT() {
			return getToken(SirmaParser.LT, 0);
		}

		/**
		 * Not.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode NOT() {
			return getToken(SirmaParser.NOT, 0);
		}

		/**
		 * Not equals.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode NOT_EQUALS() {
			return getToken(SirmaParser.NOT_EQUALS, 0);
		}

		/**
		 * Gt.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode GT() {
			return getToken(SirmaParser.GT, 0);
		}

		/**
		 * Not like.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode NOT_LIKE() {
			return getToken(SirmaParser.NOT_LIKE, 0);
		}

		/**
		 * Like.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode LIKE() {
			return getToken(SirmaParser.LIKE, 0);
		}

		/**
		 * Lteq.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode LTEQ() {
			return getToken(SirmaParser.LTEQ, 0);
		}

		/**
		 * Instantiates a new operator context.
		 * 
		 * @param parent
		 *            the parent
		 * @param invokingState
		 *            the invoking state
		 */
		public OperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRuleIndex() {
			return RULE_operator;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).enterOperator(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).exitOperator(this);
		}
	}

	/**
	 * Operator.
	 * 
	 * @return the operator context
	 * @throws RecognitionException
	 *             the recognition exception
	 */
	public final OperatorContext operator() throws RecognitionException {
		OperatorContext _localctx = new OperatorContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_operator);
		try {
			setState(125);
			switch (_input.LA(1)) {
				case EQUALS:
					enterOuterAlt(_localctx, 1);
					{
						setState(104);
						match(EQUALS);
						((OperatorContext) _localctx).op = Operator.EQUALS;
					}
					break;
				case NOT_EQUALS:
					enterOuterAlt(_localctx, 2);
					{
						setState(106);
						match(NOT_EQUALS);
						((OperatorContext) _localctx).op = Operator.NOT_EQUALS;
					}
					break;
				case LIKE:
					enterOuterAlt(_localctx, 3);
					{
						setState(108);
						match(LIKE);
						((OperatorContext) _localctx).op = Operator.LIKE;
					}
					break;
				case NOT_LIKE:
					enterOuterAlt(_localctx, 4);
					{
						setState(110);
						match(NOT_LIKE);
						((OperatorContext) _localctx).op = Operator.NOT_LIKE;
					}
					break;
				case LT:
					enterOuterAlt(_localctx, 5);
					{
						setState(112);
						match(LT);
						((OperatorContext) _localctx).op = Operator.LESS_THAN;
					}
					break;
				case GT:
					enterOuterAlt(_localctx, 6);
					{
						setState(114);
						match(GT);
						((OperatorContext) _localctx).op = Operator.GREATER_THAN;
					}
					break;
				case LTEQ:
					enterOuterAlt(_localctx, 7);
					{
						setState(116);
						match(LTEQ);
						((OperatorContext) _localctx).op = Operator.LESS_THAN_EQUALS;
					}
					break;
				case GTEQ:
					enterOuterAlt(_localctx, 8);
					{
						setState(118);
						match(GTEQ);
						((OperatorContext) _localctx).op = Operator.GREATER_THAN_EQUALS;
					}
					break;
				case NOT:
					enterOuterAlt(_localctx, 9);
					{
						setState(120);
						match(NOT);
						setState(121);
						match(IN);
						((OperatorContext) _localctx).op = Operator.NOT_IN;
					}
					break;
				case IN:
					enterOuterAlt(_localctx, 10);
					{
						setState(123);
						match(IN);
						((OperatorContext) _localctx).op = Operator.IN;
					}
					break;
				default:
					throw new NoViableAltException(this);
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class RelationContext.
	 */
	public static class RelationContext extends ParserRuleContext {

		/** The rel. */
		public String rel;

		/**
		 * Instantiates a new relation context.
		 * 
		 * @param parent
		 *            the parent
		 * @param invokingState
		 *            the invoking state
		 */
		public RelationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRuleIndex() {
			return RULE_relation;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).enterRelation(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).exitRelation(this);
		}
	}

	/**
	 * Relation.
	 * 
	 * @return the relation context
	 * @throws RecognitionException
	 *             the recognition exception
	 */
	public final RelationContext relation() throws RecognitionException {
		RelationContext _localctx = new RelationContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_relation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(127);
				_la = _input.LA(1);
				if (!(_la == 1 || _la == 2)) {
					_errHandler.recoverInline(this);
				}
				consume();
				((RelationContext) _localctx).rel = "ALL";
				System.out.println(_input.getText(_localctx.start, _input.LT(-1)));
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class DateContext.
	 */
	public static class DateContext extends ParserRuleContext {

		/** The d value. */
		public Date dValue;

		/**
		 * Date.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode DATE() {
			return getToken(SirmaParser.DATE, 0);
		}

		/**
		 * Datefull.
		 * 
		 * @return the terminal node
		 */
		public TerminalNode DATEFULL() {
			return getToken(SirmaParser.DATEFULL, 0);
		}

		/**
		 * Instantiates a new date context.
		 * 
		 * @param parent
		 *            the parent
		 * @param invokingState
		 *            the invoking state
		 */
		public DateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRuleIndex() {
			return RULE_date;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).enterDate(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof SirmaListener)
				((SirmaListener) listener).exitDate(this);
		}
	}

	/**
	 * Date.
	 * 
	 * @return the date context
	 * @throws RecognitionException
	 *             the recognition exception
	 */
	public final DateContext date() throws RecognitionException {
		DateContext _localctx = new DateContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_date);
		try {
			setState(134);
			switch (_input.LA(1)) {
				case DATE:
					enterOuterAlt(_localctx, 1);
					{
						setState(130);
						match(DATE);

						if (_input
								.getText(_localctx.start, _input.LT(-1))
								.matches(
										"^(0?[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\\d\\d$")) {
							SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
							try {
								((DateContext) _localctx).dValue = formatter.parse(_input.getText(
										_localctx.start, _input.LT(-1)));
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}

					}
					break;
				case DATEFULL:
					enterOuterAlt(_localctx, 2);
					{
						setState(132);
						match(DATEFULL);

						if (_input
								.getText(_localctx.start, _input.LT(-1))
								.matches(
										"^(?=\\d)(?:(?!(?:(?:0?[5-9]|1[0-4])(?:\\.|-|\\/)10(?:\\.|-|\\/)(?:1582))|(?:(?:0?[3-9]|1[0-3])(?:\\.|-|\\/)0?9(?:\\.|-|\\/)(?:1752)))(31(?!(?:\\.|-|\\/)(?:0?[2469]|11))|30(?!(?:\\.|-|\\/)0?2)|(?:29(?:(?!(?:\\.|-|\\/)0?2(?:\\.|-|\\/))|(?=\\D0?2\\D(?:(?!000[04]|(?:(?:1[^0-6]|[2468][^048]|[3579][^26])00))(?:(?:(?:\\d\\d)(?:[02468][048]|[13579][26])(?!\\x20BC))|(?:00(?:42|3[0369]|2[147]|1[258]|09)\\x20BC))))))|2[0-8]|1\\d|0?[1-9])([-.\\/])(1[012]|(?:0?[1-9]))\\2((?=(?:00(?:4[0-5]|[0-3]?\\d)\\x20BC)|(?:\\d{4}(?:$|(?=\\x20\\d)\\x20)))\\d{4}(?:\\x20BC)?)(?:$|(?=\\x20\\d)\\x20))?((?:(?:0?[1-9]|1[012])(?::[0-5]\\d){0,2}(?:\\x20[aApP][mM]))|(?:[01]\\d|2[0-3])(?::[0-5]\\d){1,2})?$")) {
							SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
							try {
								((DateContext) _localctx).dValue = formatter.parse(_input.getText(
										_localctx.start, _input.LT(-1)));
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}

					}
					break;
				default:
					throw new NoViableAltException(this);
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/** The Constant _serializedATN. */
	public static final String _serializedATN = "\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\3\"\u008b\4\2\t\2\4"
			+ "\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"
			+ "\13\4\f\t\f\4\r\t\r\3\2\3\2\3\2\3\2\6\2\37\n\2\r\2\16\2 \3\2\6\2$\n\2"
			+ "\r\2\16\2%\5\2(\n\2\3\3\3\3\6\3,\n\3\r\3\16\3-\3\3\3\3\3\4\3\4\3\4\7\4"
			+ "\65\n\4\f\4\16\48\13\4\3\5\3\5\3\5\7\5=\n\5\f\5\16\5@\13\5\3\6\3\6\3\6"
			+ "\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\5\6N\n\6\3\7\3\7\3\7\3\7\7\7T\n\7"
			+ "\f\7\16\7W\13\7\3\7\3\7\3\b\3\b\3\b\3\b\5\b_\n\b\3\t\3\t\3\t\3\n\3\n\3"
			+ "\n\3\n\3\n\5\ni\n\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13"
			+ "\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u0080\n\13"
			+ "\3\f\3\f\3\f\3\r\3\r\3\r\3\r\5\r\u0089\n\r\3\r\2\16\2\4\6\b\n\f\16\20"
			+ "\22\24\26\30\2\3\3\2\3\4\u0093\2\'\3\2\2\2\4)\3\2\2\2\6\61\3\2\2\2\b9"
			+ "\3\2\2\2\nM\3\2\2\2\fO\3\2\2\2\16^\3\2\2\2\20`\3\2\2\2\22h\3\2\2\2\24"
			+ "\177\3\2\2\2\26\u0081\3\2\2\2\30\u0088\3\2\2\2\32\33\7\n\2\2\33\34\5\16"
			+ "\b\2\34\36\7\13\2\2\35\37\5\b\5\2\36\35\3\2\2\2\37 \3\2\2\2 \36\3\2\2"
			+ "\2 !\3\2\2\2!(\3\2\2\2\"$\5\b\5\2#\"\3\2\2\2$%\3\2\2\2%#\3\2\2\2%&\3\2"
			+ "\2\2&(\3\2\2\2\'\32\3\2\2\2\'#\3\2\2\2(\3\3\2\2\2)+\7\5\2\2*,\5\b\5\2"
			+ "+*\3\2\2\2,-\3\2\2\2-+\3\2\2\2-.\3\2\2\2./\3\2\2\2/\60\7\6\2\2\60\5\3"
			+ "\2\2\2\61\66\5\n\6\2\62\63\7\27\2\2\63\65\5\n\6\2\64\62\3\2\2\2\658\3"
			+ "\2\2\2\66\64\3\2\2\2\66\67\3\2\2\2\67\7\3\2\2\28\66\3\2\2\29>\5\6\4\2"
			+ ":;\7\30\2\2;=\5\6\4\2<:\3\2\2\2=@\3\2\2\2><\3\2\2\2>?\3\2\2\2?\t\3\2\2"
			+ "\2@>\3\2\2\2AB\5\20\t\2BC\5\24\13\2CD\5\22\n\2DE\b\6\1\2EN\3\2\2\2FG\5"
			+ "\20\t\2GH\7\25\2\2HI\5\f\7\2IN\3\2\2\2JK\5\26\f\2KL\5\22\n\2LN\3\2\2\2"
			+ "MA\3\2\2\2MF\3\2\2\2MJ\3\2\2\2N\13\3\2\2\2OP\7\5\2\2PU\5\22\n\2QR\7\7"
			+ "\2\2RT\5\22\n\2SQ\3\2\2\2TW\3\2\2\2US\3\2\2\2UV\3\2\2\2VX\3\2\2\2WU\3"
			+ "\2\2\2XY\7\6\2\2Y\r\3\2\2\2Z[\7 \2\2[_\b\b\1\2\\]\7\33\2\2]_\b\b\1\2^"
			+ "Z\3\2\2\2^\\\3\2\2\2_\17\3\2\2\2`a\7\33\2\2ab\b\t\1\2b\21\3\2\2\2cd\7"
			+ "\33\2\2di\b\n\1\2ef\5\30\r\2fg\b\n\1\2gi\3\2\2\2hc\3\2\2\2he\3\2\2\2i"
			+ "\23\3\2\2\2jk\7\21\2\2k\u0080\b\13\1\2lm\7\22\2\2m\u0080\b\13\1\2no\7"
			+ "\23\2\2o\u0080\b\13\1\2pq\7\24\2\2q\u0080\b\13\1\2rs\7\r\2\2s\u0080\b"
			+ "\13\1\2tu\7\16\2\2u\u0080\b\13\1\2vw\7\20\2\2w\u0080\b\13\1\2xy\7\17\2"
			+ "\2y\u0080\b\13\1\2z{\7\31\2\2{|\7\25\2\2|\u0080\b\13\1\2}~\7\25\2\2~\u0080"
			+ "\b\13\1\2\177j\3\2\2\2\177l\3\2\2\2\177n\3\2\2\2\177p\3\2\2\2\177r\3\2"
			+ "\2\2\177t\3\2\2\2\177v\3\2\2\2\177x\3\2\2\2\177z\3\2\2\2\177}\3\2\2\2"
			+ "\u0080\25\3\2\2\2\u0081\u0082\t\2\2\2\u0082\u0083\b\f\1\2\u0083\27\3\2"
			+ "\2\2\u0084\u0085\7\36\2\2\u0085\u0089\b\r\1\2\u0086\u0087\7\35\2\2\u0087"
			+ "\u0089\b\r\1\2\u0088\u0084\3\2\2\2\u0088\u0086\3\2\2\2\u0089\31\3\2\2"
			+ "\2\16 %\'-\66>MU^h\177\u0088";

	/** The Constant _ATN. */
	public static final ATN _ATN = ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}