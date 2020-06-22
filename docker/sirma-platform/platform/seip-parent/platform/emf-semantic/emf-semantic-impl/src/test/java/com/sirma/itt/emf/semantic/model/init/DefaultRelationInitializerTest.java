package com.sirma.itt.emf.semantic.model.init;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.seip.definition.DefinitionAccessor;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * @author kirq4e
 */
public class DefaultRelationInitializerTest extends GeneralSemanticTest<DefaultRelationInitializer> {

	private SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
	private NamespaceRegistryService namespaceRegistry;
	private RepositoryConnection connection;

	@BeforeMethod
	public void init() {
		service = new DefaultRelationInitializer();
		connection = spy(connectionFactory.produceManagedConnection());
		namespaceRegistry = new NamespaceRegistryMock(context);
		ReflectionUtils.setFieldValue(service, "repositoryConnection", connection);
		ReflectionUtils.setFieldValue(service, "valueFactory", valueFactory);
		ReflectionUtils.setFieldValue(service, "registryService", namespaceRegistry);
	}

	/**
	 * Tests the initialization with no definitions
	 */
	@Test
	public void onDefinitiionLoad_emptyModel() {
		noTransaction();
		DefinitionAccessor definitionAccessor = Mockito.mock(DefinitionAccessor.class);
		when(definitionAccessor.getAllDefinitions()).thenReturn(Arrays.asList(new DefinitionMock()));
		ReflectionUtils.setFieldValue(service, "accessors", new InstanceProxyMock<>(definitionAccessor));

		service.onDefinitionLoad(new DefinitionsChangedEvent());

		verify(connection, times(1)).prepareTupleQuery(eq(QueryLanguage.SPARQL), anyString());
		verify(connection, never()).add(any(Iterable.class), any());
	}

	/**
	 * Tests initialization with added new relation
	 */
	@Test
	public void onDefinitiionLoad_withElementsInModel_addNewRelation() {
		DefinitionAccessor definitionAccessor = Mockito.mock(DefinitionAccessor.class);
		when(definitionAccessor.getAllDefinitions()).thenReturn(buildTestDefinitions("emf:createdBy"));
		ReflectionUtils.setFieldValue(service, "accessors", new InstanceProxyMock<>(definitionAccessor));

		connection.add(SemanticPersistenceHelper.createStatement("emf:blocks", "rdfs:subPropertyOf", "emf:hasRelation",
				namespaceRegistry, valueFactory), (Resource) null);

		service.onDefinitionLoad(new DefinitionsChangedEvent());

		verify(connection, times(1)).prepareTupleQuery(eq(QueryLanguage.SPARQL), anyString());
		verify(connection, times(1)).add(any(Iterable.class), any());
	}

	/**
	 * Tests adding existing relation
	 */
	@Test
	public void onDefinitiionLoad_withElementsInModel_addExistingRelation() {
		DefinitionAccessor definitionAccessor = Mockito.mock(DefinitionAccessor.class);
		when(definitionAccessor.getAllDefinitions()).thenReturn(buildTestDefinitions("isBlockedBy"));
		ReflectionUtils.setFieldValue(service, "accessors", new InstanceProxyMock<>(definitionAccessor));

		connection.add(SemanticPersistenceHelper.createStatement("emf:isBlockedBy", "rdfs:subPropertyOf",
				"emf:hasRelation", namespaceRegistry, valueFactory), (Resource) null);
		commitTransaction();
		noTransaction();

		service.onDefinitionLoad(new DefinitionsChangedEvent());

		verify(connection, times(1)).prepareTupleQuery(eq(QueryLanguage.SPARQL), anyString());
		verify(connection, never()).add(any(Iterable.class), any());
	}

	private static List<DefinitionModel> buildTestDefinitions(String id) {
		GenericDefinition definitionImpl = new GenericDefinitionImpl();
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		definition.setIdentifier(id);
		definition.setUri("emf:" + id);
		definition.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));
		definitionImpl.getFields().add(definition);
		return Arrays.asList(definitionImpl);
	}

	@Override
	protected String getTestDataFile() {
		return null;
	}

}
