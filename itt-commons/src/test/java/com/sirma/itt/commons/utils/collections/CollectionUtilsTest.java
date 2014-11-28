package com.sirma.itt.commons.utils.collections;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.collections.CollectionUtils;

/**
 * Tests for CollectionUtils.
 * 
 * @author Adrian Mitev
 */
@Test
public class CollectionUtilsTest {

	/**
	 * Tests findInsertionIndex().
	 */
	public void testFindInsertionIndex() {
		List<String> list = new ArrayList<String>();
		list.add("AA");
		list.add("BB");
		list.add("CC");
		list.add("CC");
		list.add("CC");
		list.add("DD");
		list.add("DD");
		list.add("EE");

		// test with value that is presented in the list
		String newValue = "CC";
		int result = CollectionUtils.findInsertionIndex(list, newValue);
		Assert.assertEquals(result, 5);

		// test with value that is not presented in the list
		newValue = "C2";
		result = CollectionUtils.findInsertionIndex(list, newValue);

		Assert.assertEquals(result, 2);

		// test with only one record - presented in the list
		list.clear();
		list.add("AD");
		newValue = "AD";

		result = CollectionUtils.findInsertionIndex(list, newValue);

		Assert.assertEquals(result, 1);

		// test with empty list
		list.clear();
		newValue = "AD";

		result = CollectionUtils.findInsertionIndex(list, newValue);

		Assert.assertEquals(result, 0);
	}

}
