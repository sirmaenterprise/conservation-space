package com.sirma.itt.seip.annotations.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test for {@link ContextStore}
 *
 * @author BBonev
 */
public class ContextStoreTest {

	@Test
	public void fromLocalCache() throws Exception {
		ContextStore.get("http://iiif.io/api/presentation/1/context.json", uri -> {
			assertTrue(uri.toString().endsWith("annotations/iiif-context-1.json"));
			return null;
		});
	}

	@Test
	public void notFromLocalCache() throws Exception {
		ContextStore.get("test", uri -> {
			assertEquals("test", uri);
			return null;
		});
	}
}
