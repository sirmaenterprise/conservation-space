/**
 * Copyright (c) 2014 10.02.2014 , Sirma ITT. /* /**
 */
package com.sirma.cmf.web.template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.javacrumbs.jsonunit.JsonAssert;

import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.services.DocumentTemplateService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.template.TemplateInstance;
import com.sirma.itt.emf.template.TemplateProperties;

/**
 * Tests for {@link DocumentTemplateRestService}.
 * 
 * @author Adrian Mitev
 */
@Test
public class DocumentTemplateRestServiceTest {

	private DocumentTemplateRestService templateRestService;

	private static final String DEFINITION_ID = "TEST_DEFINITION";

	/**
	 * Initializes test class.
	 */
	@BeforeClass
	public void init() {
		templateRestService = new DocumentTemplateRestService();

		List<TemplateInstance> templates = new ArrayList<>();
		templates.add(createTemplateInstance("1", "TEMPLATE", "First template", false));
		templates.add(createTemplateInstance("2", "TEMPLATE", "Second template", true));
		templates.add(createTemplateInstance("3", "TEMPLATE", "Third template", false));

		DocumentTemplateService templateService = Mockito.mock(DocumentTemplateService.class);
		Mockito.when(templateService.getTemplates(DEFINITION_ID)).thenReturn(templates);

		ReflectionUtils.setField(templateRestService, "templateService", templateService);
	}

	/**
	 * Tests {@link DocumentTemplateRestService#getTemplatesForType(String)}.
	 */
	public void testGetTemplatesForType() {
		String result = templateRestService.getTemplatesForType(DEFINITION_ID);

		JsonAssert
				.assertJsonEquals(
						"[{\"id\":\"2\",\"title\":\"Second template\",\"type\":\"TEMPLATE\"},{\"id\":\"1\",\"title\":\"First template\",\"type\":\"TEMPLATE\"},{\"id\":\"3\",\"title\":\"Third template\",\"type\":\"TEMPLATE\"}]",
						result);
	}

	/**
	 * Creates an instance of TemplateInstance and populate it's properties.
	 * 
	 * @param id
	 *            template id.
	 * @param type
	 *            template type.
	 * @param title
	 *            template title.
	 * @param primary
	 *            is template primary.
	 * @return constructed instance.
	 */
	private TemplateInstance createTemplateInstance(String id, String type, String title,
			Boolean primary) {
		TemplateInstance instance = new TemplateInstance();
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(TemplateProperties.TYPE, type);
		properties.put(TemplateProperties.TITLE, title);
		instance.setProperties(properties);

		instance.setPrimary(primary);
		instance.setId(id);

		return instance;
	}

}
