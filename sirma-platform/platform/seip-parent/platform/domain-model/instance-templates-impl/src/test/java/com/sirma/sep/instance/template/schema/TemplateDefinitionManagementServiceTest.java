package com.sirma.sep.instance.template.schema;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Test for {@link TemplateDefinitionManagementService}.
 *
 * @author A. Kunchev
 */
public class TemplateDefinitionManagementServiceTest {

	private TemplateDefinitionManagementService service;

	@Before
	public void setup() {
		service = new TemplateDefinitionManagementService();
	}

	@Test
	public void getTypeDefinitions_resourceLoaded() throws IOException {
		List<FileDescriptor> definitions = service.getTypeDefinitions();
		assertNotNull(definitions.iterator().next().asString());
	}
}
