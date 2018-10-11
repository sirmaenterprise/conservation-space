package com.sirma.itt.emf.semantic.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.semantic.persistence.PersistStep.PersistStepFactory;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.convert.DefaultTypeConverter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.InverseRelationProvider;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link PersistStep} functionality and it's sub classes
 *
 * @author BBonev
 */
public class PersistStepTest {

	private NamespaceRegistryService namespaceRegistryService;
	private ValueFactory valueFactory = SimpleValueFactory.getInstance();
	@Spy
	private StatementBuilderProvider statementBuilder;
	@Mock
	private InverseRelationProvider inverseRelationProvider;
	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Spy
	private TypeConverter typeConverter;

	@InjectMocks
	private PersistStepFactoryBuilder builder;

	@Before
	public void beforeMethod() {
		namespaceRegistryService = mock(NamespaceRegistryService.class);
		statementBuilder = new StatementBuilderProviderImpl(namespaceRegistryService, valueFactory);
		typeConverter = new TypeConverterImpl();

		MockitoAnnotations.initMocks(this);

		when(namespaceRegistryService.buildUri(anyString()))
				.then(a -> valueFactory.createURI(a.getArgumentAt(0, String.class)));
		new DefaultTypeConverter().register(typeConverter);
		new ValueConverter().register(typeConverter);

		TypeConverterUtil.setTypeConverter(typeConverter);

		when(semanticDefinitionService.getInverseRelationProvider()).thenReturn(inverseRelationProvider);

		when(inverseRelationProvider.inverseOf(anyString())).then(a -> a.getArgumentAt(0, String.class));
	}

	@Test
	public void noStatements_OnNullValues() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null).create(RDF.TYPE, null, null);
		assertEquals(0, step.getStatements().count());

		step = builder.build(EMF.CASE, null, null).create("rdf:type");
		assertEquals(0, step.getStatements().count());

		step = builder.build(EMF.CASE, new EmfInstance(), new EmfInstance()).create("rdf:type");
		assertEquals(0, step.getStatements().count());

		step = builder.build(EMF.CASE, new EmfInstance(), new EmfInstance()).create(
				mockDefinitionAsObjectProperty("rdf:type"));
		assertEquals(0, step.getStatements().count());

		step = builder.build(EMF.CASE, new EmfInstance(), null).create(mockDefinitionAsObjectProperty("rdf:type"));
		assertEquals(0, step.getStatements().count());
	}

	@Test
	public void noStatements_OnEqualValues() throws Exception {
		PersistStep step = builder.build(EMF.CASE, new EmfInstance(), new EmfInstance())
					.create(RDF.TYPE, EMF.CLASS_DESCRIPTION, EMF.CLASS_DESCRIPTION);
		assertEquals(0, step.getStatements().count());

		EmfInstance instance1 = new EmfInstance();
		instance1.add("rdf:type", EMF.CLASS_DESCRIPTION);
		EmfInstance instance2 = new EmfInstance();
		instance2.add("rdf:type", EMF.CLASS_DESCRIPTION);

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);
		step = stepFactory.create(mockDefinitionAsObjectProperty("rdf:type"));
		assertEquals(0, step.getStatements().count());

		step = stepFactory.create("rdf:type");
		assertEquals(0, step.getStatements().count());
	}

	@Test
	public void noStatements_OnEqualValues_WithDifferentTypes() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(RDF.TYPE, EMF.CLASS_DESCRIPTION, EMF.CLASS_DESCRIPTION.toString());
		assertEquals(0, step.getStatements().count());

		EmfInstance instance1 = new EmfInstance();
		instance1.add("rdf:type", EMF.CLASS_DESCRIPTION);
		EmfInstance instance2 = new EmfInstance();
		instance2.add("rdf:type", EMF.CLASS_DESCRIPTION.toString());

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);
		step = stepFactory.create(mockDefinitionAsObjectProperty("rdf:type"));
		assertEquals(0, step.getStatements().count());

		step = stepFactory.create("rdf:type");
		assertEquals(0, step.getStatements().count());
	}

	@Test
	public void noStatements_OnEqualMultiValues_WithDifferentTypes() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(RDF.TYPE, (Serializable) Arrays.asList(EMF.CLASS_DESCRIPTION),
							EMF.CLASS_DESCRIPTION.toString());
		assertEquals(0, step.getStatements().count());

		EmfInstance instance1 = new EmfInstance();
		instance1.add("rdf:type", (Serializable) Arrays.asList(EMF.CLASS_DESCRIPTION));
		EmfInstance instance2 = new EmfInstance();
		instance2.add("rdf:type", EMF.CLASS_DESCRIPTION.toString());

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsObjectProperty("rdf:type"));
		assertEquals(0, step.getStatements().count());

		step = stepFactory.create("rdf:type");
		assertEquals(0, step.getStatements().count());
	}

	@Test
	public void inverseRelationStatements_OnDifferentValues() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(RDF.TYPE, EMF.CLASS_DESCRIPTION, EMF.SAVED_SEARCH.toString());
		assertEquals(4, step.getStatements().count());

		EmfInstance instance1 = new EmfInstance();
		instance1.add("rdf:type", EMF.CLASS_DESCRIPTION);
		EmfInstance instance2 = new EmfInstance();
		instance2.add("rdf:type", EMF.SAVED_SEARCH.toString());

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsObjectProperty("rdf:type"));
		assertEquals(4, step.getStatements().count());

		step = stepFactory.create("rdf:type");
		assertEquals(4, step.getStatements().count());
	}

	@Test
	public void noInverseRelationStatements_OnDifferentValues() throws Exception {
		reset(inverseRelationProvider);
		when(inverseRelationProvider.inverseOf(anyString())).then(a -> null);

		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(RDF.TYPE, EMF.CLASS_DESCRIPTION, EMF.SAVED_SEARCH.toString());
		assertEquals(2, step.getStatements().count());

		EmfInstance instance1 = new EmfInstance();
		instance1.add("rdf:type", EMF.CLASS_DESCRIPTION);
		EmfInstance instance2 = new EmfInstance();
		instance2.add("rdf:type", EMF.SAVED_SEARCH.toString());

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsObjectProperty("rdf:type"));
		assertEquals(2, step.getStatements().count());

		step = stepFactory.create("rdf:type");
		assertEquals(2, step.getStatements().count());
	}

	@Test
	public void inverseRelationStatements_OnDifferentMultiValues() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(RDF.TYPE, (Serializable) Arrays.asList(EMF.CLASS_DESCRIPTION), EMF.SAVED_SEARCH.toString());
		assertEquals(4, step.getStatements().count());

		EmfInstance instance1 = new EmfInstance();
		instance1.add("rdf:type", (Serializable) Arrays.asList(EMF.CLASS_DESCRIPTION));
		EmfInstance instance2 = new EmfInstance();
		instance2.add("rdf:type", EMF.SAVED_SEARCH.toString());

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsObjectProperty("rdf:type"));
		assertEquals(4, step.getStatements().count());

		step = stepFactory.create("rdf:type");
		assertEquals(4, step.getStatements().count());
	}

	@Test
	public void noInverseRelationStatements_OnDifferentMultiValues() throws Exception {
		reset(inverseRelationProvider);
		when(inverseRelationProvider.inverseOf(anyString())).then(a -> null);

		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(RDF.TYPE, (Serializable) Arrays.asList(EMF.CLASS_DESCRIPTION), EMF.SAVED_SEARCH.toString());
		assertEquals(2, step.getStatements().count());

		EmfInstance instance1 = new EmfInstance();
		instance1.add("rdf:type", (Serializable) Arrays.asList(EMF.CLASS_DESCRIPTION));
		EmfInstance instance2 = new EmfInstance();
		instance2.add("rdf:type", EMF.SAVED_SEARCH.toString());

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsObjectProperty("rdf:type"));
		assertEquals(2, step.getStatements().count());

		step = stepFactory.create("rdf:type");
		assertEquals(2, step.getStatements().count());
	}

	@Test
	public void generateAddRevemoStatements_OnDifferentLiterals() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(EMF.STATUS, "OPEN", "CLOSED");
		assertEquals(2, step.getStatements().count());

		EmfInstance instance1 = new EmfInstance();
		instance1.add("emf:status", "OPEN");
		EmfInstance instance2 = new EmfInstance();
		instance2.add("emf:status", "CLOSED");

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsLiteralProperty("status"));
		assertEquals(2, step.getStatements().count());

		step = stepFactory.create("emf:status");
		assertEquals(2, step.getStatements().count());
	}

	@Test
	public void generateAddRemoveStatements_OnDifferentMultiLiterals() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null).create(EMF.STATUS, (Serializable) Arrays.asList("OPEN"),
				"CLOSED");
		assertEquals(2, step.getStatements().count());

		EmfInstance instance1 = new EmfInstance();
		instance1.add("emf:status", (Serializable) Arrays.asList("OPEN"));
		EmfInstance instance2 = new EmfInstance();
		instance2.add("emf:status", "CLOSED");

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsLiteralProperty("status"));
		assertEquals(2, step.getStatements().count());

		step = stepFactory.create("emf:status");
		assertEquals(2, step.getStatements().count());
	}

	@Test
	public void generateAddStatement_onAddOnly_literal() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(EMF.STATUS, "OPEN", null);
		assertEquals(1, step.getStatements().count());
		assertTrue(step.getStatements().allMatch(LocalStatement::isToAdd));

		EmfInstance instance1 = new EmfInstance();
		instance1.add("emf:status", "OPEN");
		EmfInstance instance2 = new EmfInstance();

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsLiteralProperty("status"));
		assertEquals(1, step.getStatements().count());
		assertTrue(step.getStatements().allMatch(LocalStatement::isToAdd));

		step = stepFactory.create("emf:status");
		assertEquals(1, step.getStatements().count());
		assertTrue(step.getStatements().allMatch(LocalStatement::isToAdd));
	}

	@Test
	public void generateAddStatement_onAddOnly_multiLiteral() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(EMF.STATUS, (Serializable) Arrays.asList("OPEN"), null);
		assertEquals(1, step.getStatements().count());
		assertTrue(step.getStatements().allMatch(LocalStatement::isToAdd));

		EmfInstance instance1 = new EmfInstance();
		instance1.add("emf:status", (Serializable) Arrays.asList("OPEN"));
		EmfInstance instance2 = new EmfInstance();

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsLiteralProperty("status"));
		assertEquals(1, step.getStatements().count());
		assertTrue(step.getStatements().allMatch(LocalStatement::isToAdd));

		step = stepFactory.create("emf:status");
		assertEquals(1, step.getStatements().count());
		assertTrue(step.getStatements().allMatch(LocalStatement::isToAdd));
	}

	@Test
	public void generateRemoveStatement_onRemoveOnly_literal() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(EMF.STATUS, null, "OPEN");
		assertEquals(1, step.getStatements().count());
		assertTrue(step.getStatements().noneMatch(LocalStatement::isToAdd));

		EmfInstance instance1 = new EmfInstance();
		EmfInstance instance2 = new EmfInstance();
		instance2.add("emf:status", "OPEN");

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsLiteralProperty("status"));
		assertEquals(1, step.getStatements().count());
		assertTrue(step.getStatements().noneMatch(LocalStatement::isToAdd));

		step = stepFactory.create("emf:status");
		assertEquals(1, step.getStatements().count());
		assertTrue(step.getStatements().noneMatch(LocalStatement::isToAdd));
	}

	@Test
	public void generateRemoveStatement_onRemoveOnly_multiLiteral() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(EMF.STATUS, null, (Serializable) Arrays.asList("OPEN"));
		assertEquals(1, step.getStatements().count());
		assertTrue(step.getStatements().noneMatch(LocalStatement::isToAdd));

		EmfInstance instance1 = new EmfInstance();
		EmfInstance instance2 = new EmfInstance();
		instance2.add("emf:status", (Serializable) Arrays.asList("OPEN"));

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsLiteralProperty("status"));
		assertEquals(1, step.getStatements().count());
		assertTrue(step.getStatements().noneMatch(LocalStatement::isToAdd));

		step = stepFactory.create("emf:status");
		assertEquals(1, step.getStatements().count());
		assertTrue(step.getStatements().noneMatch(LocalStatement::isToAdd));
	}

	@Test
	public void generateAddStatement_onAddOnly_objectProperty() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(RDF.TYPE, EMF.CLASS_DESCRIPTION, null);
		assertEquals(2, step.getStatements().count());
		assertTrue(step.getStatements().allMatch(LocalStatement::isToAdd));

		EmfInstance instance1 = new EmfInstance();
		instance1.add("rdf:type", EMF.CLASS_DESCRIPTION);
		EmfInstance instance2 = new EmfInstance();

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsObjectProperty("rdf:type"));
		assertEquals(2, step.getStatements().count());
		assertTrue(step.getStatements().allMatch(LocalStatement::isToAdd));

		step = stepFactory.create("rdf:type");
		assertEquals(2, step.getStatements().count());
		assertTrue(step.getStatements().allMatch(LocalStatement::isToAdd));
	}

	@Test
	public void generateAddStatement_onAddOnly_multiObjectProperty() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(RDF.TYPE, (Serializable) Arrays.asList(EMF.CLASS_DESCRIPTION), null);
		assertEquals(2, step.getStatements().count());
		assertTrue(step.getStatements().allMatch(LocalStatement::isToAdd));

		EmfInstance instance1 = new EmfInstance();
		instance1.add("rdf:type", (Serializable) Arrays.asList(EMF.CLASS_DESCRIPTION));
		EmfInstance instance2 = new EmfInstance();

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsObjectProperty("rdf:type"));
		assertEquals(2, step.getStatements().count());
		assertTrue(step.getStatements().allMatch(LocalStatement::isToAdd));

		step = stepFactory.create("rdf:type");
		assertEquals(2, step.getStatements().count());
		assertTrue(step.getStatements().allMatch(LocalStatement::isToAdd));
	}

	@Test
	public void generateRemoveStatement_onRemoveOnly_objectProperty() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(RDF.TYPE, null, EMF.CLASS_DESCRIPTION);
		assertEquals(2, step.getStatements().count());
		assertTrue(step.getStatements().noneMatch(LocalStatement::isToAdd));

		EmfInstance instance1 = new EmfInstance();
		EmfInstance instance2 = new EmfInstance();
		instance2.add("rdf:type", EMF.CLASS_DESCRIPTION);

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsObjectProperty("rdf:type"));
		assertEquals(2, step.getStatements().count());
		assertTrue(step.getStatements().noneMatch(LocalStatement::isToAdd));

		step = stepFactory.create("rdf:type");
		assertEquals(2, step.getStatements().count());
		assertTrue(step.getStatements().noneMatch(LocalStatement::isToAdd));
	}

	@Test
	public void generateRemoveStatement_onRemoveOnly_multiObjectProperty() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null)
					.create(RDF.TYPE, null, (Serializable) Arrays.asList(EMF.CLASS_DESCRIPTION));
		assertEquals(2, step.getStatements().count());
		assertTrue(step.getStatements().noneMatch(LocalStatement::isToAdd));

		EmfInstance instance1 = new EmfInstance();
		EmfInstance instance2 = new EmfInstance();
		instance2.add("rdf:type", (Serializable) Arrays.asList(EMF.CLASS_DESCRIPTION));

		PersistStepFactory stepFactory = builder.build(EMF.CASE, instance1, instance2);

		step = stepFactory.create(mockDefinitionAsObjectProperty("rdf:type"));
		assertEquals(2, step.getStatements().count());
		assertTrue(step.getStatements().noneMatch(LocalStatement::isToAdd));

		step = stepFactory.create("rdf:type");
		assertEquals(2, step.getStatements().count());
		assertTrue(step.getStatements().noneMatch(LocalStatement::isToAdd));
	}

	@Test
	public void detectAsObjectProperty_Instance() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.setId(EMF.CLASS_DESCRIPTION);
		PersistStep step = builder.build(EMF.CASE, null, null).create(RDF.TYPE, instance, EMF.CLASS_DESCRIPTION);
		assertEquals(0, step.getStatements().count());
	}

	@Test
	public void detectAsObjectProperty_InstanceReference() throws Exception {
		InstanceReference reference = new InstanceReferenceMock(EMF.CLASS_DESCRIPTION.toString(), ClassInstance.class);
		PersistStep step = builder.build(EMF.CASE, null, null).create(RDF.TYPE, reference, EMF.CLASS_DESCRIPTION);
		assertEquals(0, step.getStatements().count());
	}

	@Test
	public void detectAsObjectProperty_Uri() throws Exception {
		Uri uri = mock(Uri.class);
		when(uri.toString()).thenReturn(EMF.CLASS_DESCRIPTION.toString());
		PersistStep step = builder.build(EMF.CASE, null, null).create(RDF.TYPE, uri, EMF.CLASS_DESCRIPTION);
		assertEquals(0, step.getStatements().count());
	}

	@Test
	public void detectAsObjectProperty_FoundInSemanticDefinitionService() throws Exception {
		when(semanticDefinitionService.getRelation(anyString())).thenReturn(new PropertyInstance());
		PersistStep step = builder.build(EMF.CASE, null, null).create("rdf:type", "emf:hasReferences", null);
		List<LocalStatement> collect = step.getStatements().collect(Collectors.toList());
		assertEquals(2, collect.size());
		assertTrue(collect.iterator().next().getStatement().getObject() instanceof IRI);
	}

	@Test
	public void detectAsObjectProperty_FullUriAsString() throws Exception {
		PersistStep step = builder.build(EMF.CASE, null, null).create(RDF.TYPE, EMF.CLASS_DESCRIPTION.toString(),
				EMF.CLASS_DESCRIPTION);
		assertEquals(0, step.getStatements().count());
	}

	private static PropertyDefinition mockDefinitionAsObjectProperty(String name) {
		PropertyDefinition definition = mock(PropertyDefinition.class);
		when(definition.getUri()).thenReturn(name);
		when(definition.getName()).thenReturn(name);
		when(definition.getIdentifier()).thenReturn(name);
		when(definition.getType()).thenReturn(DataTypeDefinition.URI);
		when(definition.getDataType()).thenReturn(new DataTypeDefinitionMock(DataTypeDefinition.URI));
		return definition;
	}

	private static PropertyDefinition mockDefinitionAsLiteralProperty(String name) {
		PropertyDefinition definition = mock(PropertyDefinition.class);
		when(definition.getUri()).thenReturn("emf:" + name);
		when(definition.getName()).thenReturn(name);
		when(definition.getIdentifier()).thenReturn(name);
		when(definition.getType()).thenReturn(DataTypeDefinition.TEXT);
		when(definition.getDataType()).thenReturn(new DataTypeDefinitionMock(DataTypeDefinition.TEXT));
		return definition;
	}
}
