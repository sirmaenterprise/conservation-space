package com.sirma.itt.emf.semantic.model.init;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.semantic.definitions.SemanticPropertyRegister;
import com.sirma.itt.seip.collections.ContextualSet;
import com.sirma.itt.seip.definition.DefinitionAccessor;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.definitions.SemanticDefinitionsModelProvider;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for DefinitionsLoadedObserver.
 *
 * @author A. Kunchev
 */
public class DefinitionsLoadedObserverTest {

	@InjectMocks
	private DefinitionsLoadedObserver observer = new DefinitionsLoadedObserver();

	@Mock
	private DefinitionAccessor definitionAccessor;

	@Mock
	private SemanticDefinitionsModelProvider semanticDefinitionsModelProvider;

	@Mock
	private RepositoryConnection repositoryConnection;

	@Mock
	private ContextualSet<String> contextualSet;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Mock
	private ValueFactory valueFactory;

	@InjectMocks
	private SemanticPropertyRegister semanticPropertyRegister = new SemanticPropertyRegister();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	// ----------------------------------------- onDefinitiionLoad ----------------------------------------

	@Test
	public void onDefinitiionLoad_emptyModel_repositoryConnectionNeverCalled() {
		when(definitionAccessor.getAllDefinitions()).thenReturn(Collections.singletonList(new DefinitionMock()));
		ReflectionUtils.setFieldValue(observer, "accessors", new InstanceProxyMock<>(definitionAccessor));
		observer.onDefinitionLoad(new DefinitionsChangedEvent());

		verify(semanticDefinitionsModelProvider, atLeastOnce()).provideModelStatements(any(DefinitionModel.class),
				any(Model.class));
		verify(repositoryConnection, never()).clear(EMF.DEFINITIONS_CONTEXT);
	}

	@Test
	public void onDefinitiionLoad_withElementsInModel_repositoryConnectionCalled() {
		when(definitionAccessor.getAllDefinitions()).thenReturn(buildTestDefinitions());
		ReflectionUtils.setFieldValue(observer, "accessors", new InstanceProxyMock<>(definitionAccessor));

		mockValueFactoryStatement();
		ReflectionUtils.setFieldValue(observer, "semanticDefinitionsModelProvider", semanticPropertyRegister);

		observer.onDefinitionLoad(new DefinitionsChangedEvent());
		verify(repositoryConnection).clear(EMF.DEFINITIONS_CONTEXT);
		verify(repositoryConnection).add(any(Iterable.class), eq(EMF.DEFINITIONS_CONTEXT));
	}

	// --------------------------------- common methods ---------------------------------------------

	private void mockValueFactoryStatement() {
		Statement statement = mock(Statement.class);
		when(statement.getSubject()).thenReturn(mock(Resource.class));
		when(statement.getPredicate()).thenReturn(mock(IRI.class));
		when(statement.getObject()).thenReturn(mock(Resource.class));
		when(valueFactory.createStatement(any(Resource.class), any(IRI.class), any(Value.class), any())).thenReturn(statement);
	}

	private static List<DefinitionModel> buildTestDefinitions() {
		GenericDefinition definitionImpl = new GenericDefinitionImpl();
		FieldDefinitionImpl definition = new FieldDefinitionImpl();
		definition.setIdentifier("test");
		definition.setUri("emf:test");
		definition.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.TEXT));
		definitionImpl.getFields().add(definition);
		return Collections.singletonList(definitionImpl);
	}

}
