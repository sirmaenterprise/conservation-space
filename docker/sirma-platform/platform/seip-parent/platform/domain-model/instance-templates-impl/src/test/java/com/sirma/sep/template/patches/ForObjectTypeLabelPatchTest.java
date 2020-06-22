package com.sirma.sep.template.patches;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.template.TemplateProperties;
import com.sirma.itt.seip.template.observers.TemplateInstanceSaveObserver;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;

import liquibase.exception.CustomChangeException;

public class ForObjectTypeLabelPatchTest {

	@InjectMocks
	private ForObjectTypeLabelPatch patch;

	@Spy
	private TransactionSupportFake transactionSupport;

	@Mock
	private SearchService searchService;

	@Mock
	private TemplateInstanceSaveObserver instanceSaveObserver;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Captor
	private ArgumentCaptor<InstanceSaveContext> captor;

	@Test
	public void should_UpdateForObjectTypeLabelProperty() throws CustomChangeException {
		withTemplateInstance("book", null);

		withForObjecTypeLabel("Book");

		patch.execute(null);

		verify(domainInstanceService, times(1)).save(captor.capture());

		InstanceSaveContext saveContext = captor.getValue();

		assertNotNull(saveContext.getDisableValidationReason(), "The instance validation should be disabled");

		Instance saveInstance = saveContext.getInstance();

		assertEquals(saveInstance.getString(TemplateProperties.FOR_OBJECT_TYPE_LABEL), "Book");
	}

	@Test
	public void should_NotUpdateForObjectTypeLabelProperty_IfAlreadySet() throws CustomChangeException {
		withTemplateInstance("book", "Book");

		patch.execute(null);

		verify(domainInstanceService, times(0)).save(any());
	}

	@Before
	@SuppressWarnings("unchecked")
	public void init() {
		patch = new ForObjectTypeLabelPatch();
		MockitoAnnotations.initMocks(this);

		doAnswer(invokation -> {
			SearchArguments<Instance> searchArguments = invokation.getArgumentAt(1, SearchArguments.class);

			if (!TemplateProperties.TEMPLATE_CLASS_ID
					.equals(searchArguments.getArguments().get(DefaultProperties.SEMANTIC_TYPE))) {
				fail("Template instances should be fetched");
			}

			searchArguments.setResult(instances);

			return null;
		}).when(searchService).searchAndLoad(any(), any());
	}

	private List<Instance> instances = new ArrayList<>();

	private void withForObjecTypeLabel(String forObjectTypeLabel) {
		doAnswer(invokation -> {
			Instance instance = invokation.getArgumentAt(0, Instance.class);
			instance.add(TemplateProperties.FOR_OBJECT_TYPE_LABEL, forObjectTypeLabel);

			return null;
		}).when(instanceSaveObserver).setForObjectTypeLabel(any());
	}

	private void withTemplateInstance(String forObjectType, String forObjectTypeLabel) {
		EmfInstance instance = new EmfInstance();
		instance.add(TemplateProperties.FOR_OBJECT_TYPE, forObjectType);
		instance.add(TemplateProperties.FOR_OBJECT_TYPE_LABEL, forObjectTypeLabel);

		instances.add(instance);
	}

}
