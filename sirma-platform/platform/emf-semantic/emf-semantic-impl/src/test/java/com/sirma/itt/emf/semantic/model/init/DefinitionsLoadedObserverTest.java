package com.sirma.itt.emf.semantic.model.init;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;

import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.semantic.definitions.SemanticPropertyRegister;
import com.sirma.itt.seip.collections.ContextualSet;
import com.sirma.itt.seip.definition.DefinitionAccessor;
import com.sirma.itt.seip.definition.event.AllDefinitionsLoaded;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.definitions.SemanticDefinitionsModelProvider;

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
	private Instance<RepositoryConnection> repositoryConnection;

	@Mock
	private ContextualSet<String> contextualSet;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Mock
	private ValueFactory valueFactory;

	@Mock
	private TransactionSupport dbDao;

	@InjectMocks
	private SemanticPropertyRegister semanticPropertyRegister = new SemanticPropertyRegister();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	// ----------------------------------------- onDefinitiionLoad ----------------------------------------

	@Test
	public void onDefinitiionLoad_emptyModel_repositoryConnectionNeverCalled() {
		when(definitionAccessor.getAllDefinitions()).thenReturn(Arrays.asList(new DefinitionMock()));
		ReflectionUtils.setField(observer, "accessors", new InstanceProxyMock<>(definitionAccessor));
		observer.onDefinitionLoad(new AllDefinitionsLoaded());

		verify(semanticDefinitionsModelProvider, atLeastOnce()).provideModelStatements(any(DefinitionModel.class),
				any(Model.class));
		verify(repositoryConnection, never()).get();
	}

	@Test
	public void onDefinitiionLoad_withElementsInModel_repositoryConnectionCalled() {
		when(definitionAccessor.getAllDefinitions()).thenReturn(buildTestDefinitions());
		ReflectionUtils.setField(observer, "accessors", new InstanceProxyMock<>(definitionAccessor));

		mockValueFactoryStatement();
		ReflectionUtils.setField(observer, "semanticDefinitionsModelProvider", semanticPropertyRegister);

		when(repositoryConnection.get()).thenReturn(mock(RepositoryConnection.class));
		observer.onDefinitionLoad(new AllDefinitionsLoaded());
		verify(repositoryConnection).get();
	}

	// --------------------------------- common methods ---------------------------------------------

	private void mockValueFactoryStatement() {
		Statement statement = mock(Statement.class);
		when(statement.getSubject()).thenReturn(mock(Resource.class));
		when(statement.getPredicate()).thenReturn(mock(URI.class));
		when(statement.getObject()).thenReturn(mock(Resource.class));
		when(valueFactory.createStatement(any(Resource.class), any(URI.class), any(Value.class), any())).thenReturn(statement);
	}

	private static List<DefinitionModel> buildTestDefinitions() {
		GenericDefinition definitionImpl = new GenericDefinitionImpl();
		FieldDefinitionImpl definition = new FieldDefinitionImpl();
		definition.setIdentifier("test");
		definition.setUri("emf:test");
		definition.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.TEXT));
		definitionImpl.getFields().add(definition);
		return Arrays.asList(definitionImpl);
	}

}
