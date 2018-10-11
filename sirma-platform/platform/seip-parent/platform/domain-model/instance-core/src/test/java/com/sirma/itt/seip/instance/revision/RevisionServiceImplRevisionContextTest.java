package com.sirma.itt.seip.instance.revision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplateSearchCriteria;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests for {@link RevisionServiceImpl} revision context functionality.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class RevisionServiceImplRevisionContextTest {

	private static final String REVISION_CONTEXT_DEFINITION_ID = "recordContextDefinitionId";
	private static final String REVISION_CONTEXT_TITLE = "Revision context title";
	private static final String INSTANCE_DEFINITION_ID = "instanceDefinitionId";
	private static final String RDF_TYPE_EMF_DOCUMENT = "emf:Document";

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Mock
	private SearchService searchService;

	@Mock
	private TemplateService templateService;

	@Mock
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@InjectMocks
	private RevisionServiceImpl revisionService;

	@Spy
	private InstancePropertyNameResolver fieldConverter = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@Before
	public void init() {
		Mockito.when(namespaceRegistryService.getShortUri(RDF_TYPE_EMF_DOCUMENT)).thenReturn(RDF_TYPE_EMF_DOCUMENT);
		Mockito.when(namespaceRegistryService.getShortUri(RevisionServiceImpl.RECORD_RDF_TYPE))
				.thenReturn(RevisionServiceImpl.RECORD_RDF_TYPE);
	}

	@Test
	public void should_CreateAllRecordHolders_When_DefinitionAreReloaded() {
		Instance revisionContext = new InstanceBuilder(REVISION_CONTEXT_DEFINITION_ID).addField(
				DefaultProperties.SEMANTIC_TYPE, RevisionServiceImpl.RECORD_RDF_TYPE).build();

		String configurationValue = createConfigurationValue(REVISION_CONTEXT_DEFINITION_ID, REVISION_CONTEXT_TITLE);
		DefinitionModel definitionOne = new InstanceBuilder(INSTANCE_DEFINITION_ID).addConfiguration(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME, configurationValue).buildDefinitionModel();

		setupSearchResult(Collections.emptyList());
		Mockito.when(domainInstanceService.createInstance(REVISION_CONTEXT_DEFINITION_ID, null)).thenReturn(revisionContext);

		Mockito.when(definitionService.getAllDefinitions()).thenReturn(Stream.of(definitionOne));

		revisionService.createRevisionsContexts();

		Mockito.verify(domainInstanceService).createInstance(Matchers.anyString(), Matchers.any());
		Mockito.verify(revisionContext).add(DefaultProperties.TITLE, REVISION_CONTEXT_TITLE);
	}

	@Test
	public void should_ReturnFoundRevisionContext_When_SearchReturnIt() {
		Instance revisionContext = new InstanceBuilder(REVISION_CONTEXT_DEFINITION_ID).addField(
				DefaultProperties.SEMANTIC_TYPE, RevisionServiceImpl.RECORD_RDF_TYPE).build();

		String configurationValue = createConfigurationValue(REVISION_CONTEXT_DEFINITION_ID, REVISION_CONTEXT_TITLE);
		Instance revision = new InstanceBuilder(INSTANCE_DEFINITION_ID).addConfiguration(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME, configurationValue).build();

		setupSearchResult(Collections.singletonList(revisionContext));


		Assert.assertEquals(revisionContext, revisionService.getOrCreateContextForRevision(revision).get());

	}

	@Test
	public void should_CreateNewRevisionContextWithTemplate_When_SearchNotFoundRecordSpace() {
		Instance revisionContext = new InstanceBuilder(REVISION_CONTEXT_DEFINITION_ID).addField(
				DefaultProperties.SEMANTIC_TYPE, RevisionServiceImpl.RECORD_RDF_TYPE).build();

		String configurationValue = createConfigurationValue(REVISION_CONTEXT_DEFINITION_ID, REVISION_CONTEXT_TITLE);
		Instance revision = new InstanceBuilder(INSTANCE_DEFINITION_ID).addConfiguration(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME, configurationValue).build();

		setupSearchResult(Collections.emptyList());
		setupTemplateService("someTemplateId", "emf:correspondingInstance");
		Mockito.when(domainInstanceService.createInstance(REVISION_CONTEXT_DEFINITION_ID, null)).thenReturn(revisionContext);

		revisionService.getOrCreateContextForRevision(revision);

		Mockito.verify(domainInstanceService).createInstance(Matchers.anyString(), Matchers.any());
		Mockito.verify(revisionContext).add(DefaultProperties.TITLE, REVISION_CONTEXT_TITLE);
		Mockito.verify(revisionContext).add(LinkConstants.HAS_TEMPLATE, "emf:correspondingInstance", fieldConverter);
	}

	@Test
	public void should_CreateNewRevisionContextWithoutTemplate_When_SearchNotFoundRecordSpaceAndTemplate() {
		Instance revisionContext = new InstanceBuilder(REVISION_CONTEXT_DEFINITION_ID).addField(
				DefaultProperties.SEMANTIC_TYPE, RevisionServiceImpl.RECORD_RDF_TYPE).build();

		String configurationValue = createConfigurationValue(REVISION_CONTEXT_DEFINITION_ID, REVISION_CONTEXT_TITLE);
		Instance revision = new InstanceBuilder(INSTANCE_DEFINITION_ID).addConfiguration(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME, configurationValue).build();

		setupSearchResult(Collections.emptyList());
		Mockito.when(domainInstanceService.createInstance(REVISION_CONTEXT_DEFINITION_ID, null)).thenReturn(revisionContext);

		revisionService.getOrCreateContextForRevision(revision);

		Mockito.verify(domainInstanceService).createInstance(Matchers.anyString(), Matchers.any());
		Mockito.verify(revisionContext).add(DefaultProperties.TITLE, REVISION_CONTEXT_TITLE);
		Mockito.verify(revisionContext, Mockito.never()).add(Matchers.eq(LinkConstants.HAS_TEMPLATE), Matchers.any());
	}

	@Test
	public void should_CreateNewRevisionContextWithoutTemplate_When_SearchNotFoundRecordSpaceAndTemplateIDefaultTemplate() {
		Instance revisionContext = new InstanceBuilder(REVISION_CONTEXT_DEFINITION_ID).addField(
				DefaultProperties.SEMANTIC_TYPE, RevisionServiceImpl.RECORD_RDF_TYPE).build();

		String configurationValue = createConfigurationValue(REVISION_CONTEXT_DEFINITION_ID, REVISION_CONTEXT_TITLE);
		Instance revision = new InstanceBuilder(INSTANCE_DEFINITION_ID).addConfiguration(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME, configurationValue).build();

		setupSearchResult(Collections.emptyList());
		setupTemplateService(TemplateService.DEFAULT_TEMPLATE_ID, "emf:correspondingInstance");
		Mockito.when(domainInstanceService.createInstance(REVISION_CONTEXT_DEFINITION_ID, null)).thenReturn(revisionContext);

		revisionService.getOrCreateContextForRevision(revision);

		Mockito.verify(domainInstanceService).createInstance(Matchers.anyString(), Matchers.any());
		Mockito.verify(revisionContext).add(DefaultProperties.TITLE, REVISION_CONTEXT_TITLE);
		Mockito.verify(revisionContext, Mockito.never()).add(LinkConstants.HAS_TEMPLATE, "emf:correspondingInstance");
	}

	private void setupTemplateService(String templateId, String correspondingInstance) {
		Template template = new Template();
		template.setId(templateId);
		template.setCorrespondingInstance(correspondingInstance);
		Mockito.when(templateService.getTemplate(Matchers.any(TemplateSearchCriteria.class))).thenReturn(template);
	}

	private void setupSearchResult(List<Instance> result) {
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				invocation.getArgumentAt(1, SearchArguments.class).setResult(result);
				return null;
			}
		}).when(searchService).searchAndLoad(Matchers.any(), Matchers.any());
	}

	@Test
	public void should_NotCreateContext_When_DefinitionOfRevisionContextRdfTypeIsNotSet() {
		new InstanceBuilder(REVISION_CONTEXT_DEFINITION_ID).addField(DefaultProperties.SEMANTIC_TYPE);

		String configurationValue = createConfigurationValue(REVISION_CONTEXT_DEFINITION_ID, REVISION_CONTEXT_TITLE);
		Instance revision = new InstanceBuilder(INSTANCE_DEFINITION_ID).addConfiguration(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME, configurationValue).build();

		executeTestWithoutCreationOfRevisionContext(revision);
	}

	@Test
	public void should_NotCreateContext_When_DefinitionOfRevisionContextIsNotRdfTypeEmf_RecordSpace() {
		new InstanceBuilder(REVISION_CONTEXT_DEFINITION_ID).addField(DefaultProperties.SEMANTIC_TYPE,
																	 RDF_TYPE_EMF_DOCUMENT);

		String configurationValue = createConfigurationValue(REVISION_CONTEXT_DEFINITION_ID, REVISION_CONTEXT_TITLE);
		Instance revision = new InstanceBuilder(INSTANCE_DEFINITION_ID).addConfiguration(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME, configurationValue).build();

		executeTestWithoutCreationOfRevisionContext(revision);
	}

	@Test
	public void should_NotCreateContext_When_DefinitionOfRevisionContextIsNotFound() {

		String configurationValue = createConfigurationValue(REVISION_CONTEXT_DEFINITION_ID, REVISION_CONTEXT_TITLE);
		Instance revision = new InstanceBuilder(INSTANCE_DEFINITION_ID).addConfiguration(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME, configurationValue).build();

		executeTestWithoutCreationOfRevisionContext(revision);
	}

	@Test
	public void should_NotCreateContext_When_RevisionContextNameMissingInConfiguration() {
		String configurationValue = createConfigurationValue(REVISION_CONTEXT_DEFINITION_ID, null);
		Instance revision = new InstanceBuilder(INSTANCE_DEFINITION_ID).addConfiguration(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME, configurationValue).build();

		executeTestWithoutCreationOfRevisionContext(revision);
	}

	@Test
	public void should_NotCreateContext_When_RevisionContextDefinitionIdMissingInConfiguration() {
		String configurationValue = createConfigurationValue(null, REVISION_CONTEXT_TITLE);
		Instance revision = new InstanceBuilder(INSTANCE_DEFINITION_ID).addConfiguration(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME, configurationValue).build();

		executeTestWithoutCreationOfRevisionContext(revision);
	}

	@Test
	public void should_NotCreateContext_When_ConfigurationIsInvalidJson() {
		Instance revision = new InstanceBuilder(INSTANCE_DEFINITION_ID).addField(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME, "{").build();

		executeTestWithoutCreationOfRevisionContext(revision);
	}

	@Test
	public void should_NotCreateContext_When_ConfigurationIsEmpty() {
		Instance revision = new InstanceBuilder(INSTANCE_DEFINITION_ID).addField(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME, "").build();

		executeTestWithoutCreationOfRevisionContext(revision);
	}

	@Test
	public void should_NotCreateContext_When_ConfigurationIsNull() {
		Instance revision = new InstanceBuilder(INSTANCE_DEFINITION_ID).addField(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME, null).build();

		executeTestWithoutCreationOfRevisionContext(revision);
	}

	@Test
	public void should_NotCreateContext_When_ConfigurationNotExist() {
		Instance revision = new InstanceBuilder(INSTANCE_DEFINITION_ID).addField(
				RevisionServiceImpl.REVISION_CONTEXT_CONFIGURATION_FIELD_NAME).build();

		executeTestWithoutCreationOfRevisionContext(revision);
	}

	private void executeTestWithoutCreationOfRevisionContext(Instance revision) {
		revisionService.getOrCreateContextForRevision(revision);

		Mockito.verify(domainInstanceService, Mockito.never()).createInstance(Matchers.anyString(), Matchers.any());
	}

	private class InstanceBuilder {
		Instance instance;
		DefinitionMock instanceDefinition = new DefinitionMock();

		List<PropertyDefinition> configurations = new ArrayList<>();
		List<PropertyDefinition> fields = new ArrayList<>();

		InstanceBuilder(String instanceIdentifier) {
			instance = Mockito.mock(Instance.class);
			instanceDefinition.setIdentifier(instanceIdentifier);
			Mockito.when(instance.getIdentifier()).thenReturn(instanceIdentifier);
			Mockito.when(definitionService.find(instanceIdentifier)).thenReturn(instanceDefinition);
			Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(instanceDefinition);
		}

		InstanceBuilder addField(String fieldName) {
			return addField(fieldName, null);
		}

		InstanceBuilder addField(String fieldName, String defaultValue) {
			fields.add(createPropertyDefinition(fieldName, defaultValue));
			return this;
		}

		InstanceBuilder addConfiguration(String fieldName, String defaultValue) {
			configurations.add(createPropertyDefinition(fieldName, defaultValue));
			return this;
		}

		private PropertyDefinition createPropertyDefinition(String fieldName, String defaultValue) {
			PropertyDefinitionProxy propertyDefinition = new PropertyDefinitionProxy();
			propertyDefinition.setName(fieldName);
			propertyDefinition.setValue(defaultValue);
			return propertyDefinition;
		}

		DefinitionModel buildDefinitionModel() {
			instanceDefinition.setConfigurations(configurations);
			instanceDefinition.setFields(fields);
			return instanceDefinition;
		}

		Instance build() {
			buildDefinitionModel();
			return instance;
		}
	}

	private String createConfigurationValue(String revisionContextDefinitionId, String revisionContextName) {
		JsonObjectBuilder configurationBuilder = Json.createObjectBuilder();
		if (revisionContextDefinitionId != null) {
			configurationBuilder.add("recordContextDefinitionId", revisionContextDefinitionId);
		}
		if (revisionContextName != null) {
			configurationBuilder.add("recordContextName", revisionContextName);
		}
		return configurationBuilder.build().toString();
	}

}
