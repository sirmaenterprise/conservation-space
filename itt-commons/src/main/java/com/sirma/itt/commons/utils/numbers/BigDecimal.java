package com.sirma.itt.commons.utils.numbers;

import java.math.BigInteger;
import java.math.MathContext;

/**
 * Custom big decimal that overrides problem with comparing two values with
 * {@link #equals(Object)}.
 * 
 * @author SKostadinov
 */
public class BigDecimal extends java.math.BigDecimal {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Translates a character array representation of a <tt>BigDecimal</tt> into
	 * a <tt>BigDecimal</tt>, accepting the same sequence of characters as the
	 * {@link #BigDecimal(String)} constructor.
	 * <p>
	 * Note that if the sequence of characters is already available as a
	 * character array, using this constructor is faster than converting the
	 * <tt>char</tt> array to string and using the <tt>BigDecimal(String)</tt>
	 * constructor .
	 * 
	 * @param in
	 *            <tt>char</tt> array that is the source of characters.
	 */
	public BigDecimal(char[] in) {
		super(in);
		// Implicit definition of constructor.
	}

/**
     * Translates the string representation of a <tt>BigDecimal</tt>
     * into a <tt>BigDecimal</tt>.  The string representation consists
     * of an optional sign, <tt>'+'</tt> (<tt>'&#92;u002B'</tt>) or
     * <tt>'-'</tt> (<tt>'&#92;u002D'</tt>), followed by a sequence of
     * zero or more decimal digits ("the integer"), optionally
     * followed by a fraction, optionally followed by an exponent.
     *
     * <p>The fraction consists of a decimal point followed by zero
     * or more decimal digits.  The string must contain at least one
     * digit in either the integer or the fraction.  The number formed
     * by the sign, the integer and the fraction is referred to as the
     * <i>significand</i>.
     *
     * <p>The exponent consists of the character <tt>'e'</tt>
     * (<tt>'&#92;u0065'</tt>) or <tt>'E'</tt> (<tt>'&#92;u0045'</tt>)
     * followed by one or more decimal digits.  The value of the
     * exponent must lie between -{@link Integer#MAX_VALUE} ({@link
     * Integer#MIN_VALUE}+1) and {@link Integer#MAX_VALUE}, inclusive.
     *
     * <p>More formally, the strings this constructor accepts are
     * described by the following grammar:
     * <blockquote>
     * <dl>
     * <dt><i>BigDecimalString:</i>
     * <dd><i>Sign<sub>opt</sub> Significand Exponent<sub>opt</sub></i>
     * <p>
     * <dt><i>Sign:</i>
     * <dd><tt>+</tt>
     * <dd><tt>-</tt>
     * <p>
     * <dt><i>Significand:</i>
     * <dd><i>IntegerPart</i> <tt>.</tt> <i>FractionPart<sub>opt</sub></i>
     * <dd><tt>.</tt> <i>FractionPart</i>
     * <dd><i>IntegerPart</i>
     * <p>
     * <dt><i>IntegerPart:
     * <dd>Digits</i>
     * <p>
     * <dt><i>FractionPart:
     * <dd>Digits</i>
     * <p>
     * <dt><i>Exponent:
     * <dd>ExponentIndicator SignedInteger</i>
     * <p>
     * <dt><i>ExponentIndicator:</i>
     * <dd><tt>e</tt>
     * <dd><tt>E</tt>
     * <p>
     * <dt><i>SignedInteger:
     * <dd>Sign<sub>opt</sub> Digits</i>
     * <p>
     * <dt><i>Digits:
     * <dd>Digit
     * <dd>Digits Digit</i>
     * <p>
     * <dt><i>Digit:</i>
     * <dd>any character for which {@link Character#isDigit}
     * returns <tt>true</tt>, including 0, 1, 2 ...
     * </dl>
     * </blockquote>
     *
     * <p>The scale of the returned <tt>BigDecimal</tt> will be the
     * number of digits in the fraction, or zero if the string
     * contains no decimal point, subject to adjustment for any
     * exponent; if the string contains an exponent, the exponent is
     * subtracted from the scale.  The value of the resulting scale
     * must lie between <tt>Integer.MIN_VALUE</tt> and
     * <tt>Integer.MAX_VALUE</tt>, inclusive.
     *
     * <p>The character-to-digit mapping is provided by {@link
     * java.lang.Character#digit} set to convert to radix 10.  The
     * String may not contain any extraneous characters (whitespace,
     * for example).
     *
     * <p><b>Examples:</b><br>
     * The value of the returned <tt>BigDecimal</tt> is equal to
     * <i>significand</i> &times; 10<sup>&nbsp;<i>exponent</i></sup>.
     * For each string on the left, the resulting representation
     * [<tt>BigInteger</tt>, <tt>scale</tt>] is shown on the right.
     * <pre>
     * "0"            [0,0]
     * "0.00"         [0,2]
     * "123"          [123,0]
     * "-123"         [-123,0]
     * "1.23E3"       [123,-1]
     * "1.23E+3"      [123,-1]
     * "12.3E+7"      [123,-6]
     * "12.0"         [120,1]
     * "12.3"         [123,1]
     * "0.00123"      [123,5]
     * "-1.23E-12"    [-123,14]
     * "1234.5E-4"    [12345,5]
     * "0E+7"         [0,-7]
     * "-0"           [0,0]
     * </pre>
     *
     * <p>Note: For values other than <tt>float</tt> and
     * <tt>double</tt> NaN and &plusmn;Infinity, this constructor is
     * compatible with the values returned by {@link Float#toString}
     * and {@link Double#toString}.  This is generally the preferred
     * way to convert a <tt>float</tt> or <tt>double</tt> into a
     * BigDecimal, as it doesn't suffer from the unpredictability of
     * the {@link #BigDecimal(double)} constructor.
     *
     * @param val String representation of <tt>BigDecimal</tt>.
     *
     */
	public BigDecimal(String val) {
		super(val);
		// Implicit definition of constructor.
	}

	/**
	 * Translates a <tt>double</tt> into a <tt>BigDecimal</tt> which is the
	 * exact decimal representation of the <tt>double</tt>'s binary
	 * floating-point value. The scale of the returned <tt>BigDecimal</tt> is
	 * the smallest value such that <tt>(10<sup>scale</sup> &times; val)</tt> is
	 * an integer.
	 * <p>
	 * <b>Notes:</b>
	 * <ol>
	 * <li>The results of this constructor can be somewhat unpredictable. One
	 * might assume that writing <tt>new BigDecimal(0.1)</tt> in Java creates a
	 * <tt>BigDecimal</tt> which is exactly equal to 0.1 (an unscaled value of
	 * 1, with a scale of 1), but it is actually equal to
	 * 0.1000000000000000055511151231257827021181583404541015625. This is
	 * because 0.1 cannot be represented exactly as a <tt>double</tt> (or, for
	 * that matter, as a binary fraction of any finite length). Thus, the value
	 * that is being passed <i>in</i> to the constructor is not exactly equal to
	 * 0.1, appearances notwithstanding.
	 * <li>The <tt>String</tt> constructor, on the other hand, is perfectly
	 * predictable: writing <tt>new BigDecimal("0.1")</tt> creates a
	 * <tt>BigDecimal</tt> which is <i>exactly</i> equal to 0.1, as one would
	 * expect. Therefore, it is generally recommended that the
	 * {@linkplain #BigDecimal(String) <tt>String</tt> constructor} be used in
	 * preference to this one.
	 * <li>When a <tt>double</tt> must be used as a source for a
	 * <tt>BigDecimal</tt>, note that this constructor provides an exact
	 * conversion; it does not give the same result as converting the
	 * <tt>double</tt> to a <tt>String</tt> using the
	 * {@link Double#toString(double)} method and then using the
	 * {@link #BigDecimal(String)} constructor. To get that result, use the
	 * <tt>static</tt> {@link #valueOf(double)} method.
	 * </ol>
	 * 
	 * @param val
	 *            <tt>double</tt> value to be converted to <tt>BigDecimal</tt>.
	 */
	public BigDecimal(double val) {
		super(val);
		// Implicit definition of constructor.
	}

	/**
	 * Translates a <tt>BigInteger</tt> into a <tt>BigDecimal</tt>. The scale of
	 * the <tt>BigDecimal</tt> is zero.
	 * 
	 * @param val
	 *            <tt>BigInteger</tt> value to be converted to
	 *            <tt>BigDecimal</tt>.
	 */
	public BigDecimal(BigInteger val) {
		super(val);
		// Implicit definition of constructor.
	}

	/**
	 * Translates an <tt>int</tt> into a <tt>BigDecimal</tt>. The scale of the
	 * <tt>BigDecimal</tt> is zero.
	 * 
	 * @param val
	 *            <tt>int</tt> value to be converted to <tt>BigDecimal</tt>.
	 */
	public BigDecimal(int val) {
		super(val);
		// Implicit definition of constructor.
	}

	/**
	 * Translates a <tt>long</tt> into a <tt>BigDecimal</tt>. The scale of the
	 * <tt>BigDecimal</tt> is zero.
	 * 
	 * @param val
	 *            <tt>long</tt> value to be converted to <tt>BigDecimal</tt>.
	 */
	public BigDecimal(long val) {
		super(val);
		// Implicit definition of constructor.
	}

	/**
	 * Translates a character array representation of a <tt>BigDecimal</tt> into
	 * a <tt>BigDecimal</tt>, accepting the same sequence of characters as the
	 * {@link #BigDecimal(String)} constructor and with rounding according to
	 * the context settings.
	 * <p>
	 * Note that if the sequence of characters is already available as a
	 * character array, using this constructor is faster than converting the
	 * <tt>char</tt> array to string and using the <tt>BigDecimal(String)</tt>
	 * constructor .
	 * 
	 * @param in
	 *            <tt>char</tt> array that is the source of characters.
	 * @param mc
	 *            the context to use.
	 */
	public BigDecimal(char[] in, MathContext mc) {
		super(in, mc);
		// Implicit definition of constructor.
	}

	/**
	 * Translates the string representation of a <tt>BigDecimal</tt> into a
	 * <tt>BigDecimal</tt>, accepting the same strings as the
	 * {@link #BigDecimal(String)} constructor, with rounding according to the
	 * context settings.
	 * 
	 * @param val
	 *            string representation of a <tt>BigDecimal</tt>.
	 * @param mc
	 *            the context to use.
	 */
	public BigDecimal(String val, MathContext mc) {
		super(val, mc);
		// Implicit definition of constructor.
	}

	/**
	 * Translates a <tt>double</tt> into a <tt>BigDecimal</tt>, with rounding
	 * according to the context settings. The scale of the <tt>BigDecimal</tt>
	 * is the smallest value such that <tt>(10<sup>scale</sup> &times; val)</tt>
	 * is an integer.
	 * <p>
	 * The results of this constructor can be somewhat unpredictable and its use
	 * is generally not recommended; see the notes under the
	 * {@link #BigDecimal(double)} constructor.
	 * 
	 * @param val
	 *            <tt>double</tt> value to be converted to <tt>BigDecimal</tt>.
	 * @param mc
	 *            the context to use.
	 */
	public BigDecimal(double val, MathContext mc) {
		super(val, mc);
		// Implicit definition of constructor.
	}

	/**
	 * Translates a <tt>BigInteger</tt> into a <tt>BigDecimal</tt> rounding
	 * according to the context settings. The scale of the <tt>BigDecimal</tt>
	 * is zero.
	 * 
	 * @param val
	 *            <tt>BigInteger</tt> value to be converted to
	 *            <tt>BigDecimal</tt>.
	 * @param mc
	 *            the context to use.
	 */
	public BigDecimal(BigInteger val, MathContext mc) {
		super(val, mc);
		// Implicit definition of constructor.
	}

	/**
	 * Translates a <tt>BigInteger</tt> unscaled value and an <tt>int</tt> scale
	 * into a <tt>BigDecimal</tt>. The value of the <tt>BigDecimal</tt> is
	 * <tt>(unscaledVal &times; 10<sup>-scale</sup>)</tt>.
	 * 
	 * @param unscaledVal
	 *            unscaled value of the <tt>BigDecimal</tt>.
	 * @param scale
	 *            scale of the <tt>BigDecimal</tt>.
	 */
	public BigDecimal(BigInteger unscaledVal, int scale) {
		super(unscaledVal, scale);
		// Implicit definition of constructor.
	}

	/**
	 * Translates an <tt>int</tt> into a <tt>BigDecimal</tt>, with rounding
	 * according to the context settings. The scale of the <tt>BigDecimal</tt>,
	 * before any rounding, is zero.
	 * 
	 * @param val
	 *            <tt>int</tt> value to be converted to <tt>BigDecimal</tt>.
	 * @param mc
	 *            the context to use.
	 */
	public BigDecimal(int val, MathContext mc) {
		super(val, mc);
		// Implicit definition of constructor.
	}

	/**
	 * Translates a <tt>long</tt> into a <tt>BigDecimal</tt>, with rounding
	 * according to the context settings. The scale of the <tt>BigDecimal</tt>,
	 * before any rounding, is zero.
	 * 
	 * @param val
	 *            <tt>long</tt> value to be converted to <tt>BigDecimal</tt>.
	 * @param mc
	 *            the context to use.
	 */
	public BigDecimal(long val, MathContext mc) {
		super(val, mc);
		// Implicit definition of constructor.
	}

	/**
	 * Translates a character array representation of a <tt>BigDecimal</tt> into
	 * a <tt>BigDecimal</tt>, accepting the same sequence of characters as the
	 * {@link #BigDecimal(String)} constructor, while allowing a sub-array to be
	 * specified.
	 * <p>
	 * Note that if the sequence of characters is already available within a
	 * character array, using this constructor is faster than converting the
	 * <tt>char</tt> array to string and using the <tt>BigDecimal(String)</tt>
	 * constructor .
	 * 
	 * @param in
	 *            <tt>char</tt> array that is the source of characters.
	 * @param offset
	 *            first character in the array to inspect.
	 * @param len
	 *            number of characters to consider.
	 */
	public BigDecimal(char[] in, int offset, int len) {
		super(in, offset, len);
		// Implicit definition of constructor.
	}

	/**
	 * Translates a <tt>BigInteger</tt> unscaled value and an <tt>int</tt> scale
	 * into a <tt>BigDecimal</tt>, with rounding according to the context
	 * settings. The value of the <tt>BigDecimal</tt> is
	 * <tt>(unscaledVal &times;
	 * 10<sup>-scale</sup>)</tt>, rounded according to the <tt>precision</tt>
	 * and rounding mode settings.
	 * 
	 * @param unscaledVal
	 *            unscaled value of the <tt>BigDecimal</tt>.
	 * @param scale
	 *            scale of the <tt>BigDecimal</tt>.
	 * @param mc
	 *            the context to use.
	 */
	public BigDecimal(BigInteger unscaledVal, int scale, MathContext mc) {
		super(unscaledVal, scale, mc);
		// Implicit definition of constructor.
	}

	/**
	 * Translates a character array representation of a <tt>BigDecimal</tt> into
	 * a <tt>BigDecimal</tt>, accepting the same sequence of characters as the
	 * {@link #BigDecimal(String)} constructor, while allowing a sub-array to be
	 * specified and with rounding according to the context settings.
	 * <p>
	 * Note that if the sequence of characters is already available within a
	 * character array, using this constructor is faster than converting the
	 * <tt>char</tt> array to string and using the <tt>BigDecimal(String)</tt>
	 * constructor .
	 * 
	 * @param in
	 *            <tt>char</tt> array that is the source of characters.
	 * @param offset
	 *            first character in the array to inspect.
	 * @param len
	 *            number of characters to consider..
	 * @param mc
	 *            the context to use.
	 */
	public BigDecimal(char[] in, int offset, int len, MathContext mc) {
		super(in, offset, len, mc);
		// Implicit definition of constructor.
	}

	@Override
	public int hashCode() {
		// Implicit definition of hashCode method - defined because of the
		// equals method.
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof java.math.BigDecimal)) {
			return false;
		}
		return compareTo((java.math.BigDecimal) obj) == 0;
	}
}