package com.sirma.itt.seip.template.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplatePurposes;
import com.sirma.itt.seip.template.TemplateSearchCriteria;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.sep.instance.template.InstanceTemplateService;

/**
 * Test the template resource rest service.
 *
 * @author nvelkov
 */
public class TemplateResourceTest {

	@InjectMocks
	private TemplateResource resource = new TemplateResource();

	@Mock
	private TemplateService templateService;

	@Mock
	private InstanceTemplateService instanceTemplateService;

	@Captor
	private ArgumentCaptor<Template> captor;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(templateService.getTemplates(eq(new TemplateSearchCriteria("def-1", TemplatePurposes.CREATABLE, null))))
				.thenReturn(createList("1", "2"));

		Template template = new Template();
		template.setId("1");
		template.setContent("content");
	}

	@Test
	public void testFindAllNoGroupIdFilter() {
		assertEquals(resource.findAll(null, TemplatePurposes.CREATABLE), Collections.emptyList());
	}

	@Test
	public void testFindAllSingleGroupIdFilter() {
		List<Template> list = resource.findAll("def-1", TemplatePurposes.CREATABLE);
		assertEquals(list.size(), 2);
	}

	@Test
	public void testFindContent() {
		final String CONTENT = "content";
		final String TEMPLATE_ID = "def-1";
		Mockito.when(templateService.getContent(TEMPLATE_ID)).thenReturn(CONTENT);

		String content = resource.findContent(TEMPLATE_ID);
		assertEquals(content, CONTENT);
	}

	@Test
	public void testFindContent_WhenNoIdIsProvided() {
		String content = resource.findContent(null);
		assertNull(content);
	}

	@Test(expected = NullPointerException.class)
	public void should_NotCreateTemplate_When_NoSourceIsProvided() {
		resource.create(createTemplateCreateRequest("123", "testType", false, "creatable", null));
	}

	@Test(expected = NullPointerException.class)
	public void should_NotCreateTemplate_When_NoTitleIsProvided() {
		resource.create(createTemplateCreateRequest(null, "testType", false, "creatable", "id"));
	}

	@Test(expected = NullPointerException.class)
	public void should_NotCreateTemplate_When_NoForTypeIsProvided() {
		resource.create(createTemplateCreateRequest("123", null, false, "creatable", "id"));
	}

	@Test
	public void should_CreateTemplate() {
		final String TITLE = "123";
		final String FOR_TYPE = "testType";
		final String PURPOSE = "creatable";
		final String SOURCE_ID = "source";

		TemplateCreateRequest request = createTemplateCreateRequest(TITLE, FOR_TYPE, false, PURPOSE, SOURCE_ID);

		resource.create(request);

		verify(instanceTemplateService).createTemplate(captor.capture(), eq(SOURCE_ID));

		Template template = captor.getValue();

		assertEquals(TITLE, template.getTitle());
		assertEquals(FOR_TYPE, template.getForType());
		assertEquals(PURPOSE, template.getPurpose());
		assertEquals(false, template.getPrimary());
	}

	private static TemplateCreateRequest createTemplateCreateRequest(String title, String forType, Boolean primary, String purpose, String sourceInstance) {
		TemplateCreateRequest request = new TemplateCreateRequest();
		request.setTitle(title);
		request.setForType(forType);
		request.setPrimary(primary);
		request.setPurpose(purpose);
		request.setSourceInstance(sourceInstance);
		return request;
	}

	private static List<Template> createList(String... ids) {
		List<Template> list = new LinkedList<>();
		for (String id : ids) {
			Template instance = new Template();
			instance.setId(id);
			list.add(instance);
		}
		return list;
	}
}