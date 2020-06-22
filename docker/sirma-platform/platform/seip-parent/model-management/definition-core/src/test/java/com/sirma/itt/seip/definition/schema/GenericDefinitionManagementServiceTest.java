package com.sirma.itt.seip.definition.schema;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Test for {@link GenericDefinitionManagementService}.
 *
 * @author A. Kunchev
 */
public class GenericDefinitionManagementServiceTest {

	private GenericDefinitionManagementService service;

	@Before
	public void setup() {
		service = new GenericDefinitionManagementService();
	}

	@Test
	public void getTypeDefinitions_resourceLoaded() throws IOException {
		List<FileDescriptor> definitions = service.getTypeDefinitions();
		assertNotNull(definitions.iterator().next().asString());
	}
}
