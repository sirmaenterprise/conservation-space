package com.sirma.itt.commons.utils.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test overridden functionality of the BigDecimal class.
 * 
 * @author SKostadinov
 */
@Test
public class BigDecimalTest {
	/**
	 * Tests the code for null pointer.
	 */
	public void testForNull() {
		BigDecimal testOne = new BigDecimal("100");
		Assert.assertFalse(testOne.equals(null));
	}

	/**
	 * Tests the code for invalid value.
	 */
	public void testOne() {
		BigDecimal testOne = new BigDecimal("100");
		Assert.assertFalse(testOne.equals(java.math.BigDecimal.ZERO));
	}

	/**
	 * Tests the code for invalid value.
	 */
	public void testTwo() {
		BigDecimal testOne = new BigDecimal("100");
		Assert.assertFalse(testOne.equals(new BigDecimal("99.999999")));
	}

	/**
	 * Tests the code for invalid value.
	 */
	public void testThree() {
		BigDecimal testOne = new BigDecimal("100");
		Assert.assertFalse(testOne.equals(new BigDecimal("100.000001")));
	}

	/**
	 * Tests the code for valid value.
	 */
	public void testFour() {
		BigDecimal testOne = new BigDecimal("100");
		Assert.assertTrue(testOne.equals(new BigDecimal("100.000000")));
	}

	/**
	 * Tests the code for valid value.
	 */
	public void testFive() {
		BigDecimal testOne = new BigDecimal("100.");
		Assert.assertTrue(testOne.equals(new BigDecimal("100.000000")));
	}

	/**
	 * Tests the code for valid value.
	 */
	public void testSix() {
		BigDecimal testOne = new BigDecimal("100.00000");
		Assert.assertTrue(testOne.equals(new BigDecimal("100.000000")));
	}

	/**
	 * Tests the code for valid value.
	 */
	public void testSeven() {
		BigDecimal testOne = new BigDecimal("0.000000");
		Assert.assertTrue(testOne.equals(new BigDecimal(".000000")));
	}

	/**
	 * Tests the code for valid value.
	 */
	public void testEight() {
		BigDecimal testOne = new BigDecimal("0.000000");
		Assert.assertTrue(testOne.equals(java.math.BigDecimal.ZERO));
	}
}