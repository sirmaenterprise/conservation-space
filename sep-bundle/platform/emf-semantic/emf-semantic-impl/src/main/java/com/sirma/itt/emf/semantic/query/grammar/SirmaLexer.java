// Generated from
// A:/Development/Workspaces/IDoc/sep-bundle/platform/emf-semantic/emf-semantic-impl/src/main/resources/com/sirma/itt/emf/semantic/query/grammar/Sirma.g4
// by ANTLR 4.1

package com.sirma.itt.emf.semantic.query.grammar;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNSimulator;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

import com.sirma.itt.emf.semantic.query.PredicateValidator;

// TODO: Auto-generated Javadoc
/**
 * The Class SirmaLexer.
 */
@SuppressWarnings({ "all", "warnings", "unchecked", "unused", "cast" })
public class SirmaLexer extends Lexer {

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

	/** The mode names. */
	public static String[] modeNames = { "DEFAULT_MODE" };

	/** The Constant tokenNames. */
	public static final String[] tokenNames = { "<INVALID>", "'has Relation'", "'has Design'",
			"LPAREN", "')'", "','", "'['", "']'", "FIND", "WHERE", "'!'", "'<'", "'>'", "'>='",
			"'<='", "'='", "'!='", "LIKE", "'!~'", "IN", "IS", "AND", "OR", "NOT", "EMPTY", "WORD",
			"INTEGER", "DATEFULL", "DATE", "TIME", "ALL", "HAS", "WS" };

	/** The Constant ruleNames. */
	public static final String[] ruleNames = { "T__1", "T__0", "LPAREN", "RPAREN", "COMMA",
			"LBRACKET", "RBRACKET", "FIND", "WHERE", "BANG", "LT", "GT", "GTEQ", "LTEQ", "EQUALS",
			"NOT_EQUALS", "LIKE", "NOT_LIKE", "IN", "IS", "AND", "OR", "NOT", "EMPTY", "WORD",
			"INTEGER", "DATEFULL", "DATE", "TIME", "ALL", "HAS", "QUOTE", "SQUOTE", "BSLASH", "NL",
			"CR", "SPACE", "AMPER", "AMPER_AMPER", "PIPE", "PIPE_PIPE", "DIGIT", "WS", "NEWLINE" };

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
	 * Instantiates a new sirma lexer.
	 * 
	 * @param input
	 *            the input
	 */
	public SirmaLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
	}

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
	public String[] getModeNames() {
		return modeNames;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ATN getATN() {
		return _ATN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
			case 42:
				WS_action((RuleContext) _localctx, actionIndex);
				break;
		}
	}

	/**
	 * W s_action.
	 * 
	 * @param _localctx
	 *            the _localctx
	 * @param actionIndex
	 *            the action index
	 */
	private void WS_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
			case 0:
				skip();
				break;
		}
	}

	/** The Constant _serializedATN. */
	public static final String _serializedATN = "\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\"\u010a\b\1\4\2\t"
			+ "\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"
			+ "\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"
			+ "\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"
			+ "\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"
			+ "\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"
			+ ",\t,\4-\t-\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3"
			+ "\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3"
			+ "\b\3\b\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\f\3\f\3"
			+ "\r\3\r\3\16\3\16\3\16\3\17\3\17\3\17\3\20\3\20\3\21\3\21\3\21\3\22\3\22"
			+ "\3\22\3\22\3\22\5\22\u009f\n\22\3\23\3\23\3\23\3\24\3\24\3\24\3\25\3\25"
			+ "\3\25\3\26\3\26\3\26\3\26\3\26\5\26\u00af\n\26\3\27\3\27\3\27\3\27\5\27"
			+ "\u00b5\n\27\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31"
			+ "\3\31\5\31\u00c4\n\31\3\32\3\32\7\32\u00c8\n\32\f\32\16\32\u00cb\13\32"
			+ "\3\33\6\33\u00ce\n\33\r\33\16\33\u00cf\3\34\3\34\3\34\3\34\3\35\6\35\u00d7"
			+ "\n\35\r\35\16\35\u00d8\3\36\6\36\u00dc\n\36\r\36\16\36\u00dd\3\37\3\37"
			+ "\3\37\3\37\3 \3 \3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3\'\3\'\3"
			+ "(\3(\3(\3)\3)\3*\3*\3*\3+\3+\3,\3,\3,\5,\u0103\n,\3,\3,\3-\3-\5-\u0109"
			+ "\n-\2.\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23\13\1\25\f"
			+ "\1\27\r\1\31\16\1\33\17\1\35\20\1\37\21\1!\22\1#\23\1%\24\1\'\25\1)\26"
			+ "\1+\27\1-\30\1/\31\1\61\32\1\63\33\1\65\34\1\67\35\19\36\1;\37\1= \1?"
			+ "!\1A\2\1C\2\1E\2\1G\2\1I\2\1K\2\1M\2\1O\2\1Q\2\1S\2\1U\2\1W\"\2Y\2\1\3"
			+ "\2\30\4\2HHhh\4\2KKkk\4\2PPpp\4\2FFff\4\2YYyy\4\2JJjj\4\2GGgg\4\2TTtt"
			+ "\4\2NNnn\4\2MMmm\4\2UUuu\4\2CCcc\4\2QQqq\4\2VVvv\4\2OOoo\4\2RRrr\4\2["
			+ "[{{\4\2WWww\4\2C\\c|\5\2\62<C\\c|\4\2//\61<\3\2\62<\u010a\2\3\3\2\2\2"
			+ "\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2"
			+ "\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2"
			+ "\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2"
			+ "\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2"
			+ "\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2"
			+ "\2\2\2?\3\2\2\2\2W\3\2\2\2\3[\3\2\2\2\5h\3\2\2\2\7s\3\2\2\2\tu\3\2\2\2"
			+ "\13w\3\2\2\2\ry\3\2\2\2\17{\3\2\2\2\21}\3\2\2\2\23\u0082\3\2\2\2\25\u0088"
			+ "\3\2\2\2\27\u008a\3\2\2\2\31\u008c\3\2\2\2\33\u008e\3\2\2\2\35\u0091\3"
			+ "\2\2\2\37\u0094\3\2\2\2!\u0096\3\2\2\2#\u009e\3\2\2\2%\u00a0\3\2\2\2\'"
			+ "\u00a3\3\2\2\2)\u00a6\3\2\2\2+\u00ae\3\2\2\2-\u00b4\3\2\2\2/\u00b6\3\2"
			+ "\2\2\61\u00c3\3\2\2\2\63\u00c5\3\2\2\2\65\u00cd\3\2\2\2\67\u00d1\3\2\2"
			+ "\29\u00d6\3\2\2\2;\u00db\3\2\2\2=\u00df\3\2\2\2?\u00e3\3\2\2\2A\u00e7"
			+ "\3\2\2\2C\u00e9\3\2\2\2E\u00eb\3\2\2\2G\u00ed\3\2\2\2I\u00ef\3\2\2\2K"
			+ "\u00f1\3\2\2\2M\u00f3\3\2\2\2O\u00f5\3\2\2\2Q\u00f8\3\2\2\2S\u00fa\3\2"
			+ "\2\2U\u00fd\3\2\2\2W\u0102\3\2\2\2Y\u0108\3\2\2\2[\\\7j\2\2\\]\7c\2\2"
			+ "]^\7u\2\2^_\7\"\2\2_`\7T\2\2`a\7g\2\2ab\7n\2\2bc\7c\2\2cd\7v\2\2de\7k"
			+ "\2\2ef\7q\2\2fg\7p\2\2g\4\3\2\2\2hi\7j\2\2ij\7c\2\2jk\7u\2\2kl\7\"\2\2"
			+ "lm\7F\2\2mn\7g\2\2no\7u\2\2op\7k\2\2pq\7i\2\2qr\7p\2\2r\6\3\2\2\2st\7"
			+ "*\2\2t\b\3\2\2\2uv\7+\2\2v\n\3\2\2\2wx\7.\2\2x\f\3\2\2\2yz\7]\2\2z\16"
			+ "\3\2\2\2{|\7_\2\2|\20\3\2\2\2}~\t\2\2\2~\177\t\3\2\2\177\u0080\t\4\2\2"
			+ "\u0080\u0081\t\5\2\2\u0081\22\3\2\2\2\u0082\u0083\t\6\2\2\u0083\u0084"
			+ "\t\7\2\2\u0084\u0085\t\b\2\2\u0085\u0086\t\t\2\2\u0086\u0087\t\b\2\2\u0087"
			+ "\24\3\2\2\2\u0088\u0089\7#\2\2\u0089\26\3\2\2\2\u008a\u008b\7>\2\2\u008b"
			+ "\30\3\2\2\2\u008c\u008d\7@\2\2\u008d\32\3\2\2\2\u008e\u008f\7@\2\2\u008f"
			+ "\u0090\7?\2\2\u0090\34\3\2\2\2\u0091\u0092\7>\2\2\u0092\u0093\7?\2\2\u0093"
			+ "\36\3\2\2\2\u0094\u0095\7?\2\2\u0095 \3\2\2\2\u0096\u0097\7#\2\2\u0097"
			+ "\u0098\7?\2\2\u0098\"\3\2\2\2\u0099\u009f\7\u0080\2\2\u009a\u009b\t\n"
			+ "\2\2\u009b\u009c\t\3\2\2\u009c\u009d\t\13\2\2\u009d\u009f\t\b\2\2\u009e"
			+ "\u0099\3\2\2\2\u009e\u009a\3\2\2\2\u009f$\3\2\2\2\u00a0\u00a1\7#\2\2\u00a1"
			+ "\u00a2\7\u0080\2\2\u00a2&\3\2\2\2\u00a3\u00a4\t\3\2\2\u00a4\u00a5\t\4"
			+ "\2\2\u00a5(\3\2\2\2\u00a6\u00a7\t\3\2\2\u00a7\u00a8\t\f\2\2\u00a8*\3\2"
			+ "\2\2\u00a9\u00aa\t\r\2\2\u00aa\u00ab\t\4\2\2\u00ab\u00af\t\5\2\2\u00ac"
			+ "\u00af\5M\'\2\u00ad\u00af\5O(\2\u00ae\u00a9\3\2\2\2\u00ae\u00ac\3\2\2"
			+ "\2\u00ae\u00ad\3\2\2\2\u00af,\3\2\2\2\u00b0\u00b1\t\16\2\2\u00b1\u00b5"
			+ "\t\t\2\2\u00b2\u00b5\5Q)\2\u00b3\u00b5\5S*\2\u00b4\u00b0\3\2\2\2\u00b4"
			+ "\u00b2\3\2\2\2\u00b4\u00b3\3\2\2\2\u00b5.\3\2\2\2\u00b6\u00b7\t\4\2\2"
			+ "\u00b7\u00b8\t\16\2\2\u00b8\u00b9\t\17\2\2\u00b9\60\3\2\2\2\u00ba\u00bb"
			+ "\t\b\2\2\u00bb\u00bc\t\20\2\2\u00bc\u00bd\t\21\2\2\u00bd\u00be\t\17\2"
			+ "\2\u00be\u00c4\t\22\2\2\u00bf\u00c0\t\4\2\2\u00c0\u00c1\t\23\2\2\u00c1"
			+ "\u00c2\t\n\2\2\u00c2\u00c4\t\n\2\2\u00c3\u00ba\3\2\2\2\u00c3\u00bf\3\2"
			+ "\2\2\u00c4\62\3\2\2\2\u00c5\u00c9\t\24\2\2\u00c6\u00c8\t\25\2\2\u00c7"
			+ "\u00c6\3\2\2\2\u00c8\u00cb\3\2\2\2\u00c9\u00c7\3\2\2\2\u00c9\u00ca\3\2"
			+ "\2\2\u00ca\64\3\2\2\2\u00cb\u00c9\3\2\2\2\u00cc\u00ce\5U+\2\u00cd\u00cc"
			+ "\3\2\2\2\u00ce\u00cf\3\2\2\2\u00cf\u00cd\3\2\2\2\u00cf\u00d0\3\2\2\2\u00d0"
			+ "\66\3\2\2\2\u00d1\u00d2\59\35\2\u00d2\u00d3\5W,\2\u00d3\u00d4\5;\36\2"
			+ "\u00d48\3\2\2\2\u00d5\u00d7\t\26\2\2\u00d6\u00d5\3\2\2\2\u00d7\u00d8\3"
			+ "\2\2\2\u00d8\u00d6\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9:\3\2\2\2\u00da\u00dc"
			+ "\t\27\2\2\u00db\u00da\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd\u00db\3\2\2\2"
			+ "\u00dd\u00de\3\2\2\2\u00de<\3\2\2\2\u00df\u00e0\t\r\2\2\u00e0\u00e1\t"
			+ "\n\2\2\u00e1\u00e2\t\n\2\2\u00e2>\3\2\2\2\u00e3\u00e4\t\7\2\2\u00e4\u00e5"
			+ "\t\r\2\2\u00e5\u00e6\t\f\2\2\u00e6@\3\2\2\2\u00e7\u00e8\7$\2\2\u00e8B"
			+ "\3\2\2\2\u00e9\u00ea\7)\2\2\u00eaD\3\2\2\2\u00eb\u00ec\7^\2\2\u00ecF\3"
			+ "\2\2\2\u00ed\u00ee\7\17\2\2\u00eeH\3\2\2\2\u00ef\u00f0\7\f\2\2\u00f0J"
			+ "\3\2\2\2\u00f1\u00f2\7\"\2\2\u00f2L\3\2\2\2\u00f3\u00f4\7(\2\2\u00f4N"
			+ "\3\2\2\2\u00f5\u00f6\7(\2\2\u00f6\u00f7\7(\2\2\u00f7P\3\2\2\2\u00f8\u00f9"
			+ "\7~\2\2\u00f9R\3\2\2\2\u00fa\u00fb\7~\2\2\u00fb\u00fc\7~\2\2\u00fcT\3"
			+ "\2\2\2\u00fd\u00fe\4\62;\2\u00feV\3\2\2\2\u00ff\u0103\5K&\2\u0100\u0103"
			+ "\7\13\2\2\u0101\u0103\5Y-\2\u0102\u00ff\3\2\2\2\u0102\u0100\3\2\2\2\u0102"
			+ "\u0101\3\2\2\2\u0103\u0104\3\2\2\2\u0104\u0105\b,\2\2\u0105X\3\2\2\2\u0106"
			+ "\u0109\5G$\2\u0107\u0109\5I%\2\u0108\u0106\3\2\2\2\u0108\u0107\3\2\2\2"
			+ "\u0109Z\3\2\2\2\r\2\u009e\u00ae\u00b4\u00c3\u00c9\u00cf\u00d8\u00dd\u0102"
			+ "\u0108";

	/** The Constant _ATN. */
	public static final ATN _ATN = ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}