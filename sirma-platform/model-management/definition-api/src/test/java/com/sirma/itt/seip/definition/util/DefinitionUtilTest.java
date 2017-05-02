package com.sirma.itt.seip.definition.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

/**
 * Tests for {@link DefinitionUtil}
 *
 * @author BBonev
 */
public class DefinitionUtilTest {

	@Test
	public void shouldSelectRnCExpressionFields() throws Exception {
		Set<String> set = DefinitionUtil.getRncFields("+[prop1] AND -[prop2] AND [prop3] IN ('val1', 'val2')");
		assertTrue(set.contains("prop1"));
		assertTrue(set.contains("prop2"));
		assertTrue(set.contains("prop3"));
		assertEquals(3, set.size());
	}
}
