package com.sirma.itt.seip.eai.model.error;

import static com.sirma.itt.seip.eai.service.model.EAIBaseConstants.NEW_LINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ErrorBuilderProviderTest {

	@Test
	public void testSeparator() throws Exception {
		ErrorBuilderProvider errorBuilderProvider = new ErrorBuilderProvider();
		errorBuilderProvider = errorBuilderProvider.separator();
		assertEquals("", errorBuilderProvider.build());

		errorBuilderProvider = errorBuilderProvider.append("test").separator();
		assertEquals("test" + NEW_LINE, errorBuilderProvider.build());
		assertEquals(errorBuilderProvider.build(), errorBuilderProvider.toString());
	}

	@Test
	public void testHasError() throws Exception {
		ErrorBuilderProvider errorBuilderProvider = new ErrorBuilderProvider();
		assertFalse(errorBuilderProvider.hasErrors());
		errorBuilderProvider = errorBuilderProvider.append("");
		assertFalse(errorBuilderProvider.hasErrors());
		errorBuilderProvider = errorBuilderProvider.append("error");
		assertTrue(errorBuilderProvider.hasErrors());
	}

	@Test
	public void testGetWithCapacity() throws Exception {
		ErrorBuilderProvider errorBuilderProvider = new ErrorBuilderProvider();
		assertFalse(errorBuilderProvider.hasErrors());
		StringBuilder builder = errorBuilderProvider.get(1024);
		assertEquals(1024, builder.capacity());
		builder = errorBuilderProvider.get();
		assertEquals(1024, builder.capacity());
	}
}
