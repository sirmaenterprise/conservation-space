package com.sirmaenterprise.sep.eai.spreadsheet.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sirma.itt.seip.domain.instance.DefaultProperties;

/**
 * Tests for {@link EAISystemProperties}.
 * 
 * @author gshevkedov
 */
public class EAISystemPropertiesTest {

	@Test
	public void testGetValue() throws Exception {
		assertEquals("contentSource", EAISystemProperties.CONTENT_SOURCE);
		assertEquals("ImportStatus", EAISystemProperties.IMPORT_STATUS);
		assertEquals(DefaultProperties.PRIMARY_CONTENT_ID, EAISystemProperties.PRIMARY_CONTENT_ID);
	}

}
