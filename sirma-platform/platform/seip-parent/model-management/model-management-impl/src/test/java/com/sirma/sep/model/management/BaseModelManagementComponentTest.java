package com.sirma.sep.model.management;

import static com.sirma.sep.model.management.ModelsFakeCreator.createStringMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.Message;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.jglue.cdiunit.ActivatedAlternatives;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.jglue.cdiunit.AdditionalPackages;
import org.jglue.cdiunit.CdiRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.sirma.itt.emf.cls.persister.CodeListPersister;
import com.sirma.itt.emf.cls.validator.CodeValidator;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.dozer.DefinitionsDozerProvider;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.validation.InstanceValidationResult;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.sep.cls.CodeListService;
import com.sirma.sep.definition.DefinitionImportService;
import com.sirma.sep.model.ModelImportService;
import com.sirma.sep.model.management.codelists.CodeListsProvider;
import com.sirma.sep.model.management.definition.DefinitionModelConverter;
import com.sirma.sep.model.management.definition.export.GenericDefinitionConverter;
import com.sirma.sep.model.management.deploy.definition.steps.DefinitionChangeSetStep;
import com.sirma.sep.model.management.operation.ModifyAttributeChangeSetOperation;
import com.sirma.sep.model.management.stubs.LabelServiceStub;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Base component test for the model management functionality.
 * <p>
 * Includes the mandatory stubbed services and mocks to be able to run tests.
 *
 * @author Mihail Radkov
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ ModelManagementServiceImpl.class, DefinitionsDozerProvider.class, ModelUpdater.class, ModelPersistence.class,
		ModelUpdateHandler.class, ModelsResetObserver.class, ContextualFakeProducer.class })
@AdditionalPackages(
		{ DefinitionModelConverter.class, CodeListsProvider.class, ModifyAttributeChangeSetOperation.class, SenderService.class,
				DefinitionChangeSetStep.class, GenericDefinitionConverter.class })
@AdditionalClasspaths({ ObjectMapper.class, Extension.class, EventService.class, Message.class })
@ActivatedAlternatives({ ModelManagementDeploymentConfigurationsFake.class })
public abstract class BaseModelManagementComponentTest {

	@Produces
	@Mock
	protected SemanticDefinitionService semanticDefinitionService;
	protected SemanticDefinitionServiceStub semanticDefinitionServiceStub;

	@Produces
	@Mock
	protected DefinitionImportService definitionImportService;
	protected DefinitionImportServiceStub definitionImportServiceStub;

	@Produces
	@Mock
	protected ModelImportService modelImportService;
	protected ModelImportServiceStub modelImportServiceStub;

	@Produces
	@Mock
	protected CodelistService codelistService;

	@Produces
	@Mock
	protected CodeListPersister codeListPersister;

	@Produces
	@Mock
	protected CodeListService codeListService;
	protected CodelistServiceStub codelistServiceStub;

	@Produces
	@Mock
	protected CodeValidator codeValidator;

	@Produces
	@Mock
	protected LabelService labelService;
	protected LabelServiceStub labelServiceStub;

	@Produces
	protected NamespaceRegistryService namespaceRegistryService = new NamespaceRegistryFake();

	@Produces
	@Mock
	protected SecurityContext securityContext;

	@Produces
	protected DbDao dbDao = new DbDaoFake();

	@Produces
	@Mock
	protected SenderService senderService;
	protected SenderServiceStub senderServiceStub;

	@Inject
	protected EventService eventService;

	@Produces
	protected RepositoryConnection semanticDatabase = new RepositoryConnectionFake();

	@Produces
	protected ValueFactory valueFactory = SimpleValueFactory.getInstance();

	@Produces
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Produces
	@Mock
	protected DomainInstanceService domainInstanceService;
	protected DomainInstanceServiceStub domainInstanceServiceStub;

	@Produces
	@Mock
	protected InstanceValidationService instanceValidationService;

	@Produces
	@Configuration
	protected ConfigurationPropertyMock<IRI> deploymentContext = new ConfigurationPropertyMock<>();

	@Produces
	@Mock
	protected LabelProvider labelProvider;

	@Before
	public void baseBefore() {
		semanticDefinitionServiceStub = new SemanticDefinitionServiceStub(semanticDefinitionService, eventService);
		labelServiceStub = new LabelServiceStub(labelService);
		definitionImportServiceStub = new DefinitionImportServiceStub(definitionImportService, labelServiceStub);
		codelistServiceStub = new CodelistServiceStub(codeListService);
		senderServiceStub = new SenderServiceStub(senderService);
		mockSecurityContext();
	}

	@Before
	public void stubModelImportService() {
		modelImportServiceStub = new ModelImportServiceStub(modelImportService);
		// All validation will be valid unless re-stubbed
		modelImportServiceStub.validModels();
	}

	@Before
	public void stubInstanceValidation() {
		// All validation will be valid unless re-stubbed
		InstanceValidationResult valid = new InstanceValidationResult(Collections.emptyList());
		when(instanceValidationService.validate(any(ValidationContext.class))).thenReturn(valid);
	}

	@Before
	public void stubDomainInstanceService() {
		domainInstanceServiceStub = new DomainInstanceServiceStub(domainInstanceService);
	}

	@Before
	public void stubConfigurations() {
		deploymentContext.setValue(EMF.DATA_CONTEXT);
	}

	@Before
	public void stubLabelProvider() {
		when(labelProvider.getLabel(anyString())).then(invocation -> invocation.getArgumentAt(0, String.class) + "_translated");
		when(labelProvider.getLabel(anyString(), anyString())).then(
				invocation -> invocation.getArgumentAt(0, String.class) + "_translated_in_" + invocation.getArgumentAt(1, String.class));
	}

	@After
	public void cleanUp() {
		// Remove temporary files/dirs
		definitionImportServiceStub.clear();
	}

	/**
	 * Stubs the import/export service with the provided definition file names.
	 *
	 * @param definitions file names of definition XMLs
	 */
	protected void withDefinitions(String... definitions) {
		Arrays.stream(definitions).forEach(definitionImportServiceStub::withDefinition);
	}

	protected void withLabelDefinitionFor(String labelId, String... labels) {
		LabelDefinition labelDefinition = LabelServiceStub.build(labelId, createStringMap(labels));
		labelServiceStub.withLabelDefinition(labelDefinition);
	}

	protected void withLabelDefinitionDefinedIn(String labelId, String definedIn, String... labels) {
		LabelDefinition labelDefinition = LabelServiceStub.build(labelId, definedIn, createStringMap(labels));
		labelServiceStub.withLabelDefinition(labelDefinition);
	}

	protected void mockSecurityContext() {
		when(securityContext.getCurrentTenantId()).thenReturn("test.tenant");
		EmfUser user = new EmfUser("admin@test.tenant");
		user.setId("emf:admin-test.tenant");
		user.setActive(true);
		when(securityContext.getAuthenticated()).thenReturn(user);
		when(securityContext.getEffectiveAuthentication()).thenReturn(user);
		when(securityContext.getRequestId()).thenReturn(UUID.randomUUID().toString());
	}
}
