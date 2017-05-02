package com.sirma.itt.seip.template.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirma.itt.seip.template.TemplatePurposes;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;

/**
 * Test the template resource rest service.
 *
 * @author nvelkov
 */
public class TemplateResourceTest {

	@Spy
	private SecurityContextManager securityManager = new SecurityContextManagerFake();

	@Mock
	private TemplateService templateService;

	@Mock
	private InstanceContentService instanceContentService;

	@InjectMocks
	private TemplateResource resource = new TemplateResource();

	@Captor
	private ArgumentCaptor<TemplateInstance> templateInstanceCaptor;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(templateService.getTemplates("def-1", TemplatePurposes.CREATABLE)).thenReturn(
				createList("1", "2"));
		Mockito.when(templateService.getTemplates("def-2", TemplatePurposes.CREATABLE)).thenReturn(createList("3"));

		TemplateInstance template = new TemplateInstance();
		template.setId(1);
		template.add(DefaultProperties.CONTENT, "content");
		Mockito.when(templateService.getTemplateWithContent("def-1")).thenReturn(template);
		Mockito.when(templateService.getDefaultTemplateContent()).thenReturn("defaultContent");
	}

	@Test
	public void testFindAllNoGroupIdFilter() {
		Assert.assertEquals(resource.findAll(null, TemplatePurposes.CREATABLE), Collections.emptyList());
		Assert.assertEquals(resource.findAll(Collections.emptyList(), TemplatePurposes.CREATABLE),
				Collections.emptyList());
	}

	@Test
	public void testFindAllSingleGroupIdFilter() {
		List<TemplateInstance> list = resource.findAll(Arrays.asList("def-2"), TemplatePurposes.CREATABLE);
		Assert.assertEquals(list.size(), 2);
		Assert.assertEquals(list.get(0).getId(), "3");
	}

	@Test
	public void testFindAllTwoGroupIdFilter() {
		List<TemplateInstance> list = resource.findAll(Arrays.asList("def-1", "def-2"), TemplatePurposes.CREATABLE);

		Assert.assertEquals(list.size(), 4);
		Assert.assertEquals(list.get(0).getId(), "1");
		Assert.assertEquals(list.get(1).getId(), "2");
		Assert.assertEquals(list.get(2).getId(), "3");
	}

	@Test
	public void testFindContent() {
		String content = resource.findContent("def-1");
		Assert.assertEquals(content, "content");
	}

	@Test
	public void testFindDefaultContent() {
		String content = resource.findContent("def-2");
		Assert.assertEquals(content, "defaultContent");
	}

	private List<TemplateInstance> createList(String... ids) {
		List<TemplateInstance> list = new LinkedList<>();
		for (String id : ids) {
			TemplateInstance instance = new TemplateInstance();
			instance.setId(id);
			list.add(instance);
		}
		return list;
	}
}