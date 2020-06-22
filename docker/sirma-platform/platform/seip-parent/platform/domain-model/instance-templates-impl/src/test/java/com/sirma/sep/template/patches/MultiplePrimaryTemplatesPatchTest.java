package com.sirma.sep.template.patches;

import static com.sirma.itt.seip.template.TemplateProperties.IS_PRIMARY_TEMPLATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.db.TemplateDao;
import com.sirma.itt.seip.template.exceptions.MissingTemplateException;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;

import liquibase.exception.CustomChangeException;

public class MultiplePrimaryTemplatesPatchTest {

	@InjectMocks
	private MultiplePrimaryTemplatesPatch patch;
	@Spy
	private TransactionSupportFake transactionSupport;
	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private TemplateDao templateDao;

	@Test
	public void should_Save_Duplicate_Primaries_As_Secondary() throws CustomChangeException {
		Template tmpl1 = construct("tmpl1", "task", "creatable", null, true, "emf:Instance1");
		Template tmpl2 = construct("tmpl2", "task", "creatable", null, true, "emf:Instance2");

		Template tmpl3 = construct("tmpl3", "case", "creatable", "(department == \"DEV\" || department == \"QA\")",
				true, "emf:Instance3");
		Template tmpl4 = construct("tmpl4", "case", "creatable", null, false, "emf:Instance4");
		Template tmpl5 = construct("tmpl5", "case", "creatable", "(department == \"QA\" || department == \"DEV\")",
				true, "emf:Instance5");
		withExistingActivatedTemplates(tmpl1, tmpl2, tmpl3, tmpl4, tmpl5);

		Template instance1 = construct("emf:Instance1", null, null, null, true, null);
		Template instance2 = construct("emf:Instance2", null, null, null, false, null);
		Template instance3 = construct("emf:Instance3", null, null, null, true, null);
		Template instance5 = construct("emf:Instance5", null, null, null, false, null);
		withExistingTemplateInstances(instance1, instance2, instance3, instance5);

		patch.execute(null);

		verifyTemplatesSavedAsSecondary("tmpl2", "tmpl5");
	}

	@Test
	public void should_Not_Save_Templates_If_Same_PrimaryFlag_As_CorrespondingInstance() throws CustomChangeException {
		Template tmpl1 = construct("tmpl1", "task", "creatable", null, true, "emf:Instance1");
		Template tmpl2 = construct("tmpl2", "task", "creatable", null, true, "emf:Instance2");
		Template tmpl3 = construct("tmpl3", "task", "creatable", null, true, "emf:Instance3");

		withExistingActivatedTemplates(tmpl1, tmpl2, tmpl3);

		Template instance1 = construct("emf:Instance1", null, null, null, true, null);
		Template instance2 = construct("emf:Instance2", null, null, null, true, null);
		Template instance3 = construct("emf:Instance3", null, null, null, true, null);
		withExistingTemplateInstances(instance1, instance2, instance3);

		patch.execute(null);

		verifyNoTemplatesSaved();
	}

	@Test
	public void should_Not_Save_Templates_If_One_Primary_Per_Group() throws CustomChangeException {
		Template tmpl1 = construct("tmpl1", "task", "creatable", null, true, "emf:Instance1");
		Template tmpl2 = construct("tmpl2", "task", "creatable", null, false, "emf:Instance2");
		Template tmpl3 = construct("tmpl3", "task", "creatable", null, false, "emf:Instance3");

		Template tmpl4 = construct("tmpl4", "case", "creatable", "(department == \"DEV\" || department == \"QA\")",
				true, "emf:Instance4");
		Template tmpl5 = construct("tmpl5", "case", "creatable", "(department == \"QA\" || department == \"DEV\")",
				false, "emf:Instance5");
		Template tmpl6 = construct("tmpl6", "case", "creatable", "(department == \"ENG\" || department == \"DEV\")",
				true, "emf:Instance6");
		withExistingActivatedTemplates(tmpl1, tmpl2, tmpl3, tmpl4, tmpl5, tmpl6);

		patch.execute(null);

		verifyNoTemplatesSaved();
	}

	@Test(expected = MissingTemplateException.class)
	public void should_Throw_Exception_If_Template_Instance_Not_Found() throws CustomChangeException {
		Template tmpl1 = construct("tmpl1", "task", "creatable", null, true, "emf:Instance1");
		Template tmpl2 = construct("tmpl2", "task", "creatable", null, true, "emf:Instance2");
		withExistingActivatedTemplates(tmpl1, tmpl2);

		Template instance1 = construct("emf:Instance1", null, null, null, true, null);
		withExistingTemplateInstances(instance1);
		when(instanceTypeResolver.resolveReference(eq("emf:Instance2"))).thenReturn(Optional.empty());

		patch.execute(null);
	}

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	private void withExistingActivatedTemplates(Template... templates) {
		when(templateDao.getAllTemplates()).thenReturn(Arrays.asList(templates));
	}

	private void withExistingTemplateInstances(Template... templates) {
		for (Template template : templates) {
			Instance instance = mock(Instance.class);
			when(instance.getBoolean(eq(IS_PRIMARY_TEMPLATE))).thenReturn(template.getPrimary());
			InstanceReference reference = mock(InstanceReference.class);
			when(reference.toInstance()).thenReturn(instance);
			when(instanceTypeResolver.resolveReference(eq(template.getId()))).thenReturn(Optional.of(reference));
		}
	}

	private void verifyTemplatesSavedAsSecondary(String... ids) {
		ArgumentCaptor<Template> captor = ArgumentCaptor.forClass(Template.class);
		verify(templateDao, times(ids.length)).saveOrUpdate(captor.capture());
		List<Template> savedTemplates = captor.getAllValues();

		assertEquals("Number of saved templates was not as expected", ids.length, savedTemplates.size());

		Set<String> savedIds = savedTemplates
				.stream()
				.map(Template::getId)
				.collect(Collectors.toSet());
		Set<String> expectedids = Stream.of(ids).collect(Collectors.toSet());
		assertEquals(expectedids, savedIds);

		for (int i = 0; i < savedTemplates.size(); i++) {
			Template savedTemplate = savedTemplates.get(i);
			assertFalse("template " + savedTemplate.getId() + "was not saved a secondary", savedTemplate.getPrimary());
		}
	}

	private void verifyNoTemplatesSaved() {
		verify(templateDao, never()).saveOrUpdate(any());
	}

	private Template construct(String id, String forType, String purpose, String rule, boolean primary, String instanceId) {
		Template template = new Template();
		template.setId(id);
		template.setForType(forType);
		template.setPurpose(purpose);
		template.setRule(rule);
		template.setPrimary(primary);
		template.setCorrespondingInstance(instanceId);
		return template;
	}
}
