package com.sirma.itt.emf.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * {@link EqualsHelper} tests.
 * 
 * @author BBonev
 */
@Test
public class EqualsHelperTest {

	/**
	 * Test null compare.
	 */
	public void testNullCompare() {

		List<Integer> list = Arrays.asList(5, 3, 1, 2, 7, null, 3);
		Collections.sort(list, new Comparator<Integer>() {

			@Override
			public int compare(Integer arg0, Integer arg1) {
				int compare = EqualsHelper.nullCompare(arg0, arg1);
				if (compare != 2) {
					return compare;
				}
				return arg0.compareTo(arg1);
			}
		});

		Assert.assertNotNull(list.get(0));
		Assert.assertNull(list.get(list.size() - 1));

		Assert.assertEquals(EqualsHelper.nullCompare(null, null), 0);
		Assert.assertEquals(EqualsHelper.nullCompare("", null), -1);
		Assert.assertEquals(EqualsHelper.nullCompare(null, ""), 1);
		Assert.assertEquals(EqualsHelper.nullCompare("", ""), 2);
	}

	/**
	 * Test null safe compare.
	 */
	public void testNullSafeCompare() {

		List<Integer> list = Arrays.asList(5, 3, 1, 2, 7, null, 3);
		Collections.sort(list, new Comparator<Integer>() {

			@Override
			public int compare(Integer arg0, Integer arg1) {
				int compare = EqualsHelper.nullSafeCompare(arg0, arg1);
				return compare;
			}
		});

		Assert.assertNotNull(list.get(0));
		Assert.assertEquals(list.get(0).intValue(), 1);
		Assert.assertNull(list.get(list.size() - 1));

		Assert.assertEquals(EqualsHelper.nullSafeCompare(null, null), 0);
		Assert.assertEquals(EqualsHelper.nullSafeCompare("2", null), -1);
		Assert.assertEquals(EqualsHelper.nullSafeCompare(null, "2"), 1);
		Assert.assertEquals(EqualsHelper.nullSafeCompare("22", "22"), 0);
	}
}
