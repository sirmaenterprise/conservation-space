package com.sirma.itt.seip.domain.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sirma.itt.seip.IntegerPair;

/**
 * Test for {@link VersionUtil}.
 *
 * @author A. Kunchev
 */
public class VersionUtilTest {

	@Test(expected = IllegalArgumentException.class)
	public void split_nullParameter() {
		VersionUtil.split(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void split_emptyParameter() {
		VersionUtil.split("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void split_doesntContainsDot() {
		VersionUtil.split("10");
	}

	public void split_withCorrectData() {
		IntegerPair pair = VersionUtil.split("1.0");
		assertEquals(1, pair.getFirst().intValue());
		assertEquals(0, pair.getSecond().intValue());
	}

	@Test(expected = NumberFormatException.class)
	public void combine_negativeMajorVersion() {
		VersionUtil.combine(-2, 3);
	}

	@Test(expected = NumberFormatException.class)
	public void combine_negativeMinorVersion() {
		VersionUtil.combine(2, -3);
	}

	@Test(expected = NumberFormatException.class)
	public void combine_bothNegative() {
		VersionUtil.combine(-2, -3);
	}

	@Test
	public void combine_withCorrectData() {
		String version = VersionUtil.combine(2, 3);
		assertEquals("2.3", version);
	}

}
