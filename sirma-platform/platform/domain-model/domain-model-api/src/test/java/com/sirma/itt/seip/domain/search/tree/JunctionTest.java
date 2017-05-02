package com.sirma.itt.seip.domain.search.tree;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.domain.search.tree.Condition.Junction;

public class JunctionTest {

	@Test
	public void testFromString() {
		Assert.assertNull(Junction.fromString(null));
		Assert.assertNull(Junction.fromString("non-existent"));
		
		Assert.assertEquals(Junction.AND, Junction.fromString("AnD"));
		Assert.assertEquals(Junction.OR, Junction.fromString("Or"));
	}
}
 