package com.sirma.sep.model.management.deploy.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.model.management.deploy.configuration.ModelManagementDeploymentConfigurations;
import com.sirma.sep.model.management.deploy.configuration.ModelManagementDeploymentConfigurationsImpl;

/**
 * Tests the deploying logic in {@link SemanticClassDeployer}.
 *
 * @author Mihail Radkov
 */
public class SemanticClassDeployerTest {

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private ModelManagementDeploymentConfigurations deploymentConfigurations;

	@InjectMocks
	private SemanticClassDeployer semanticClassDeployer;

	private IRI context = SimpleValueFactory.getInstance().createIRI(ModelManagementDeploymentConfigurationsImpl.DEFAULT_SEMANTIC_CONTEXT);

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(deploymentConfigurations.getSemanticContext()).thenReturn(new ConfigurationPropertyMock<>(context));
	}

	@Test
	public void shouldPersistTheClassPropertiesInConfiguredGraph() {
		withClassInstance("emf:123");

		SemanticClassDeploymentPayload payload = new SemanticClassDeploymentPayload("emf:123", Collections.emptyMap());

		semanticClassDeployer.deploy(payload);

		InstanceSaveContext saveContext = getSaveContext();
		assertNotNull(saveContext);
		assertEquals(context, saveContext.get("SEMANTIC_PERSISTENCE_CONTEXT"));
	}

	private void withClassInstance(String id) {
		Instance classInstance = new EmfInstance(id);
		when(domainInstanceService.loadInstance(eq(id))).thenReturn(classInstance);
	}

	private InstanceSaveContext getSaveContext() {
		ArgumentCaptor<InstanceSaveContext> saveContextCaptor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService).save(saveContextCaptor.capture());
		return saveContextCaptor.getValue();
	}

}
