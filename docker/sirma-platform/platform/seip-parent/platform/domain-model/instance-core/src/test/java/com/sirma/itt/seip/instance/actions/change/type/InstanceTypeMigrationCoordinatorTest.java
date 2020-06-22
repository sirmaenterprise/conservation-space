package com.sirma.itt.seip.instance.actions.change.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openrdf.model.vocabulary.OWL;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.itt.seip.search.ResultItem;
import com.sirma.itt.seip.search.ResultValue;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.StateTransitionMock;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;

/**
 * Test for {@link InstanceTypeMigrationCoordinator}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 18/02/2019
 */
public class InstanceTypeMigrationCoordinatorTest {

	public static final String CURRENT_DEF = "currentDef";
	public static final String SIBLING_DEF = "siblingDef";
	public static final String SOME_OTHER_DEF = "someOtherDef";
	public static final String SOME_NOT_CREATEABLE_DEF = "someNotCreateableDef";
	public static final String SOME_ABSTRACT_DEF = "someAbstractDef";
	public static final String INVERSE_DEF = "inverseDef";

	private static ValueFactory valueFactory = SimpleValueFactory.getInstance();
	private static final IRI PTOP_INFORMATION_RESOURCE = valueFactory.createIRI(Proton.NAMESPACE, "InformationResource");

	@InjectMocks
	private InstanceTypeMigrationCoordinator migrationCoordinator;
	@Mock
	private DomainInstanceService domainInstanceService;
	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Mock
	private DefinitionService definitionService;
	@Mock
	private SearchService searchService;
	@Mock
	private InstanceService instanceService;
	@Mock
	private InstanceLoadDecorator loadDecorators;
	@Mock
	private CodelistService codelistService;
	@Spy
	private ConfigurationProperty<Set<String>> skippedClasses = new ConfigurationPropertyMock<>(
			new HashSet<>(Arrays.asList(Proton.DOCUMENT.toString(), EMF.MEDIA.toString())));

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(definitionService.getInstanceDefinition(any())).then(a -> {
			Instance instance = a.getArgumentAt(0, Instance.class);
			return definitionService.find(instance.getIdentifier());
		});

		initDefinitionModels();
		initSemanticHierarchy();

		when(codelistService.getCodeValue(10, "DRAFT")).thenReturn(new CodeValue());
		when(codelistService.getFilteredCodeValues(15)).thenReturn(
				Stream.of(CURRENT_DEF, SIBLING_DEF, SOME_OTHER_DEF, SOME_NOT_CREATEABLE_DEF, SOME_ABSTRACT_DEF,
						INVERSE_DEF).collect(Collectors.toMap(Function.identity(), a -> new CodeValue())));
	}

	private void initDefinitionModels() {
		withDefinition(CURRENT_DEF)
				.withDataProperty().name(DefaultProperties.STATUS).uri("emf:status").codeList(10).add()
				.withDataProperty().name(DefaultProperties.TYPE).uri("emf:type").codeList(15).value(CURRENT_DEF).add()
				.withObjectProperty().name(DefaultProperties.SEMANTIC_TYPE).uri(DefaultProperties.SEMANTIC_TYPE)
				.range(OWL.CLASS.toString()).value(EMF.CASE.toString()).add()
				.withObjectProperty().name(LinkConstants.HAS_TEMPLATE).uri(LinkConstants.HAS_TEMPLATE).range(EMF.TEMPLATE.toString()).add()
				.withObjectProperty().name("relation1").uri("emf:relation1").domain(EMF.CASE.toString()).range(EMF.DOCUMENT.toString()).add()
				.withObjectProperty().name("relation2").uri("emf:relation2").domain(EMF.CASE.toString()).range(EMF.TASK.toString()).add()
				.withObjectProperty().name("relation3").uri("emf:relation3").domain(EMF.ACTIVITY.toString()).range(EMF.DOCUMENT.toString()).add()
				.withObjectProperty().name("relation4").uri("emf:relation4").domain(Proton.ENTITY.toString()).range(Proton.ENTITY.toString()).add()
				.withDataProperty().name("property1").uri("emf:property1").domain(Proton.ENTITY.toString()).add()
				.withDataProperty().name("property2").uri("emf:property2").domain(Proton.OBJECT.toString()).add()
				.withDataProperty().name("property3").uri("emf:property3").domain(EMF.CASE.toString()).add()
				.withDataProperty().name("property4").uri("emf:property4").domain(EMF.ACTIVITY.toString()).add()
				.withDataProperty().name("property6").uri("emf:property6").domain(EMF.ACTIVITY.toString()).add()
				.done();

		withDefinition(SIBLING_DEF)
				.withDataProperty().name(DefaultProperties.STATUS).uri("emf:status").codeList(10).add()
				.withDataProperty().name(DefaultProperties.TYPE).uri("emf:type").codeList(15).value(SIBLING_DEF).add()
				.withObjectProperty().name(DefaultProperties.SEMANTIC_TYPE).uri(DefaultProperties.SEMANTIC_TYPE).range(OWL.CLASS.toString()).value(EMF.PROJECT.toString()).add()
				.withObjectProperty().name(LinkConstants.HAS_TEMPLATE).uri(LinkConstants.HAS_TEMPLATE).range(EMF.TEMPLATE.toString()).add()
				.withObjectProperty().name("relation1").uri("emf:relation1").domain(EMF.CASE.toString()).range(EMF.DOCUMENT.toString()).add()
				.withObjectProperty().name("otherRelation2").uri("emf:relation22").domain(EMF.PROJECT.toString()).range(EMF.TASK.toString()).add()
				.withObjectProperty().name("relation3").uri("emf:relation3").domain(EMF.ACTIVITY.toString()).range(EMF.DOCUMENT.toString()).add()
				.withObjectProperty().name("otherRelation4").uri("emf:relation4").domain(Proton.ENTITY.toString()).range(Proton.ENTITY.toString()).add()
				.withDataProperty().name("otherProperty1").uri("emf:property1").domain(Proton.ENTITY.toString()).add()
				.withDataProperty().name("property2").uri("emf:property2").domain(Proton.OBJECT.toString()).add()
				.withDataProperty().name("property3").uri("emf:property33").domain(EMF.PROJECT.toString()).add()
				.withDataProperty().name("otherProperty4").uri("emf:property4").domain(EMF.ACTIVITY.toString()).add()
				.withDataProperty().name("property5").uri("emf:property5").domain(EMF.ACTIVITY.toString()).add()
				.withStateTransition(PrimaryStates.INITIAL_KEY, ActionTypeConstants.CHANGE_TYPE, PrimaryStates.APPROVED_KEY)
				.done();

		withDefinition(SOME_OTHER_DEF)
				.withDataProperty().name(DefaultProperties.STATUS).uri("emf:status").codeList(10).add()
				.withDataProperty().name(DefaultProperties.TYPE).uri("emf:type").codeList(15).value(SOME_OTHER_DEF).add()
				.withObjectProperty().name(DefaultProperties.SEMANTIC_TYPE).uri(DefaultProperties.SEMANTIC_TYPE).range(OWL.CLASS.toString()).value(EMF.DOCUMENT.toString()).add()
				.done();

		withDefinition(SOME_NOT_CREATEABLE_DEF)
				.withDataProperty().name(DefaultProperties.STATUS).uri("emf:status").codeList(10).add()
				.withDataProperty().name(DefaultProperties.TYPE).uri("emf:type").codeList(15).value(SOME_NOT_CREATEABLE_DEF).add()
				.withObjectProperty().name(DefaultProperties.SEMANTIC_TYPE).uri(DefaultProperties.SEMANTIC_TYPE).range(OWL.CLASS.toString()).value(EMF.ACTIVITY.toString()).add()
				.done();

		withDefinition(SOME_ABSTRACT_DEF)
				.withDataProperty().name(DefaultProperties.STATUS).uri("emf:status").codeList(10).add()
				.withDataProperty().name(DefaultProperties.TYPE).uri("emf:type").codeList(15).value(SOME_ABSTRACT_DEF).add()
				.withObjectProperty().name(DefaultProperties.SEMANTIC_TYPE).uri(DefaultProperties.SEMANTIC_TYPE).range(OWL.CLASS.toString()).value(Proton.ENTITY.toString()).add()
				.done().setAbstract(true);

		withDefinition(INVERSE_DEF)
				.withDataProperty().name(DefaultProperties.STATUS).uri("emf:status").codeList(10).add()
				.withDataProperty().name(DefaultProperties.TYPE).uri("emf:type").codeList(15).value(INVERSE_DEF).add()
				.withObjectProperty().name(DefaultProperties.SEMANTIC_TYPE).uri(DefaultProperties.SEMANTIC_TYPE).range(OWL.CLASS.toString()).value(EMF.TASK.toString()).add()
				.withObjectProperty().name(LinkConstants.HAS_TEMPLATE).uri(LinkConstants.HAS_TEMPLATE).range(EMF.TEMPLATE.toString()).add()
				.withObjectProperty().name("relation100").uri("emf:relation100").domain(EMF.TASK.toString()).range(EMF.DOCUMENT.toString()).add()
				.withObjectProperty().name("relation101").uri("emf:relation101").domain(EMF.TASK.toString()).range(EMF.CASE.toString()).add()
				.withObjectProperty().name("relation102").uri("emf:relation102").domain(EMF.ACTIVITY.toString()).range(EMF.PROJECT.toString()).add()
				.withObjectProperty().name("relation103").uri("emf:relation103").domain(Proton.ENTITY.toString()).range(Proton.ENTITY.toString()).add()
				.withObjectProperty().name("relation104").uri("emf:relation104").domain(Proton.ENTITY.toString()).add()
				.done();
	}

	private void initSemanticHierarchy() {
		defineClass(Proton.ENTITY.toString(), null, false, false);
		defineClass(Proton.NAMESPACE + "Happening", Proton.ENTITY.toString(), false, false);
		defineClass(Proton.NAMESPACE + "Event", Proton.NAMESPACE + "Happening", false, false);
		defineClass(EMF.ACTIVITY.toString(), Proton.NAMESPACE + "Event", false, false);
		defineClass(EMF.CASE.toString(), EMF.ACTIVITY.toString(), true, false);
		defineClass(EMF.PROJECT.toString(), EMF.ACTIVITY.toString(), true, false);

		defineClass(Proton.OBJECT.toString(), Proton.ENTITY.toString(), false, false);
		// skipped ptop:Statement and ptop:InformationResource as it's not needed for the current test
		defineClass(PTOP_INFORMATION_RESOURCE.toString(), Proton.OBJECT.toString(), false, false);
		defineClass(EMF.DOCUMENT.toString(), PTOP_INFORMATION_RESOURCE.toString(), true, true);
		defineClass(EMF.MEDIA.toString(), PTOP_INFORMATION_RESOURCE.toString(), false, true);
		defineClass(EMF.IMAGE.toString(), EMF.MEDIA.toString(), false, true);
	}

	private void defineClass(String id, String parent, boolean createable, boolean uploadable) {
		ClassInstance classInstance = new ClassInstance();
		classInstance.setId(id);
		classInstance.add("createable", createable);
		classInstance.add("uploadable", uploadable);
		if (parent != null) {
			ClassInstance parentClass = semanticDefinitionService.getClassInstance(parent);
			assertNotNull("Parent class " + parent + " not defined", parentClass);
			classInstance.setSuperClasses(Collections.singletonList(parentClass));
			parentClass.getSubClasses().put(id, classInstance);
		}
		when(semanticDefinitionService.getClassInstance(id)).thenReturn(classInstance);
	}

	@Test
	public void getInstanceAs_shouldReturnSameInstanceIfTargetTypeIsTheSame() throws Exception {
		ObjectInstance instance = createInstance();

		withCurrentInstance(instance);

		Instance result = migrationCoordinator.getInstanceAs("emf:instanceId", CURRENT_DEF);
		assertSame(result, instance);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getInstanceAs_shouldNotAcceptNotValidDestinationType() throws Exception {
		withCurrentInstance(createInstance());

		migrationCoordinator.getInstanceAs("emf:instanceId", SOME_OTHER_DEF);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void getInstanceAs_shouldFailIfCouldNotLoadInstance() throws Exception {
		withCurrentInstance(null);
		migrationCoordinator.getInstanceAs("emf:notFoundInstance", SOME_NOT_CREATEABLE_DEF);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getInstanceAs_shouldNotAcceptNotValidDestinationType_notSameCreationType() throws Exception {
		ObjectInstance instance = createInstance();

		withCurrentInstance(instance);

		migrationCoordinator.getInstanceAs("emf:instanceId", SOME_NOT_CREATEABLE_DEF);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void getInstanceAs_shouldFailIfUndefinedDefinitionIsPassed() throws Exception {
		withCurrentInstance(createInstance());
		migrationCoordinator.getInstanceAs("emf:instanceId", "someNonDefinedDefinition");
	}

	@Test(expected = IllegalArgumentException.class)
	public void getInstanceAs_shouldFailAbstractDefinitionIsPassed() throws Exception {
		withCurrentInstance(createInstance());
		migrationCoordinator.getInstanceAs("emf:instanceId", SOME_ABSTRACT_DEF);
	}

	@Test
	public void getInstanceAs_shouldRemoveNonApplicableProperties() throws Exception {
		ObjectInstance instance = createInstance();
		withCurrentInstance(instance);
		Instance result = migrationCoordinator.getInstanceAs("emf:instanceId", SIBLING_DEF);
		assertNotSame(result, instance);

		assertNull("The property2 should not be copied: not valid domain class", result.get("property2"));
		assertNull("The property6 should not be copied: not defined in the target model", result.get("property6"));
		assertNull("The property3 should not be copied: different URI mapping in the target model", result.get("property3"));
		assertNull("The property2 should not be copied: not valid domain class", result.get("relation1"));
		assertNull("The relation2 should not be copied: not defined in the target model", result.get("relation2"));
		assertNull("The otherRelation2 should not be copied: not in the source model", result.get("otherRelation2"));

		// the following properties should be copied
		assertNotNull(result.get("relation3"));
		// changed property names
		assertNotNull(result.get("otherProperty1"));
		assertNotNull(result.get("otherProperty4"));
		assertNotNull(result.get("otherRelation4"));
	}

	@Test
	public void getInstanceAs_shouldResetAssignedTemplate() throws Exception {
		ObjectInstance instance = createInstance();
		withCurrentInstance(instance);
		Instance result = migrationCoordinator.getInstanceAs("emf:instanceId", SIBLING_DEF);
		assertNotSame(result, instance);

		assertNull(result.get(LinkConstants.HAS_TEMPLATE));
	}

	@Test
	public void getInstanceAs_shouldChangeInstanceTypes() throws Exception {
		ObjectInstance instance = createInstance();
		withCurrentInstance(instance);
		Instance result = migrationCoordinator.getInstanceAs("emf:instanceId", SIBLING_DEF);
		assertNotSame(result, instance);

		assertEquals(SIBLING_DEF, result.getIdentifier());
		assertEquals(SIBLING_DEF, result.get(DefaultProperties.TYPE));
		assertEquals(EMF.PROJECT.toString(), result.get(DefaultProperties.SEMANTIC_TYPE));
		assertEquals(EMF.PROJECT.toString(), result.type().getId());
	}

	@Test
	public void getInstanceAs_shouldResetStateIfNotCompatible_andThereIsDefinedTransitionFromInitState() throws Exception {
		ObjectInstance instance = createInstance();
		instance.add(DefaultProperties.STATUS, "SOME_NOT_DEFINED_IN_TARGET_MODEL_STATUS");
		withCurrentInstance(instance);
		Instance result = migrationCoordinator.getInstanceAs("emf:instanceId", SIBLING_DEF);
		assertNotSame(result, instance);

		assertEquals("INIT", result.get(DefaultProperties.STATUS));
	}

	@Test
	public void getInstanceAs_shouldKeepStateIfCompatible_andThereIsDefinedTransitionFromThatState() throws Exception {
		when(codelistService.getFilteredCodeValues(10)).thenReturn(Collections.singletonMap("DRAFT", new CodeValue()));

		ObjectInstance instance = createInstance();
		DefinitionMock instanceDefinition = (DefinitionMock) definitionService.find(SIBLING_DEF);
		instanceDefinition.getStateTransitions().add(createStateTransition("DRAFT", ActionTypeConstants.CHANGE_TYPE, "DRAFT"));
		withCurrentInstance(instance);
		Instance result = migrationCoordinator.getInstanceAs("emf:instanceId", SIBLING_DEF);
		assertNotSame(result, instance);

		assertEquals("DRAFT", result.get(DefaultProperties.STATUS));
	}

	@Test
	public void getInstanceAs_shouldRemoveInvalidCodelistValueIfNotAllowedByFilters() throws Exception {
		ObjectInstance instance = createInstance();
		DefinitionMock instanceDefinition = (DefinitionMock) definitionService.find(SIBLING_DEF);
		instanceDefinition.getStateTransitions().add(createStateTransition("DRAFT", ActionTypeConstants.CHANGE_TYPE, "DRAFT"));
		withCurrentInstance(instance);
		Instance result = migrationCoordinator.getInstanceAs("emf:instanceId", SIBLING_DEF);
		assertNotSame(result, instance);

		// the state value is not valid so it's reset to its default value
		assertEquals("INIT", result.get(DefaultProperties.STATUS));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getInstanceAs_shouldFailIfNoStateTransitionFoundInTargetType() throws Exception {
		ObjectInstance instance = createInstance();
		DefinitionMock instanceDefinition = (DefinitionMock) definitionService.find(SIBLING_DEF);
		instanceDefinition.getStateTransitions().clear();
		instance.add(DefaultProperties.STATUS, "SOME_NOT_DEFINED_IN_TARGET_MODEL_STATUS");
		withCurrentInstance(instance);
		Instance result = migrationCoordinator.getInstanceAs("emf:instanceId", SIBLING_DEF);
		assertNotSame(result, instance);

		assertEquals("INIT", result.get(DefaultProperties.STATUS));
	}

	@Test
	public void getInstanceAs_shouldDecorateReturnedInstance() throws Exception {
		withCurrentInstance(createInstance());
		Instance result = migrationCoordinator.getInstanceAs("emf:instanceId", SIBLING_DEF);
		verify(loadDecorators).decorateInstance(result);
	}

	private ObjectInstance createInstance() {
		ObjectInstance instance = new ObjectInstance();
		instance.setId("emf:instanceId");
		instance.setIdentifier(CURRENT_DEF);
		instance.add(DefaultProperties.SEMANTIC_TYPE, EMF.CASE.toString());
		instance.add(DefaultProperties.STATUS, "DRAFT");
		instance.add(DefaultProperties.TYPE, CURRENT_DEF);
		instance.add(LinkConstants.HAS_TEMPLATE, "emf:currentDefTemplateId");

		instance.add("relation1", "emf:relation1-value");
		instance.append("relation2", "emf:relation2-1-value");
		instance.append("relation2", "emf:relation2-2-value");
		instance.add("relation3", "emf:relation3-value");
		instance.append("relation4", "emf:relation4-1-value");
		instance.append("relation4", "emf:relation4-2-value");
		instance.append("relation4", "emf:relation4-3-value");

		instance.add("property1", "property 1 value");
		instance.add("property2", "property 2 value");
		instance.append("property3", "property 3.1 value");
		instance.append("property3", "property 3.2 value");
		instance.add("property4", "property 4 value");
		instance.add("property6", "property 6 value");
		return instance;
	}

	private void withCurrentInstance(Instance instance) {
		if (instance == null) {
			when(domainInstanceService.loadInstance(any())).thenThrow(IllegalArgumentException.class);
		} else {
			when(domainInstanceService.loadInstance(instance.getId().toString())).thenReturn(instance);
			ClassInstance type = getMockedInstanceType(instance);
			instance.setType(type);
		}
	}

	private ClassInstance getMockedInstanceType(Instance instance) {
		DefinitionModel definition = definitionService.getInstanceDefinition(instance);
		String rdfType = definition.getField(DefaultProperties.SEMANTIC_TYPE)
				.map(PropertyDefinition::getDefaultValue)
				.orElseThrow(() -> new IllegalStateException("rdf:type should be present in the definition"));
		return semanticDefinitionService.getClassInstance(rdfType);
	}

	@Test
	public void getAffectedInstanceOfTypeChangeOf() throws Exception {
		mockRelatingInstances();
		Collection<Instance> instances = migrationCoordinator.getAffectedInstanceOfTypeChangeOf(
				"emf:instanceId", InstanceType.create(EMF.PROJECT.toString()));

		// range is Document - wrong range in the first place
		assertTrue(instances.stream().noneMatch(instance -> instance.containsValue("relation100", "emf:instanceId")));
		// range is Case - range not compatible with the new Type
		assertTrue(instances.stream().noneMatch(instance -> instance.containsValue("relation101", "emf:instanceId")));
		// range is Project - as as the new type
		assertTrue(instances.stream().allMatch(instance -> instance.containsValue("relation102", "emf:instanceId")));
		// range is Entity - parent of the new type
		assertTrue(instances.stream().allMatch(instance -> instance.containsValue("relation103", "emf:instanceId")));
		// no range defined - all good
		assertTrue(instances.stream().allMatch(instance -> instance.containsValue("relation104", "emf:instanceId")));
	}

	@Test
	public void countAffectedInstanceOfTypeChangeOf() throws Exception {
		mockRelatingInstances();
		int instanceCount = migrationCoordinator.countAffectedInstanceOfTypeChangeOf("emf:instanceId",
				InstanceType.create(EMF.PROJECT.toString()));

		assertEquals(9, instanceCount);
	}

	@Test
	public void getAllowedSuperTypes_shouldConsiderSkippedClassesWenCalculatingHierarchy() {
		ClassInstance imageType = semanticDefinitionService.getClassInstance(EMF.IMAGE.toString());

		Collection<InstanceType> allowedSuperTypes = migrationCoordinator.getAllowedSuperTypes(imageType);
		assertEquals(2, allowedSuperTypes.size());
		assertTrue(allowedSuperTypes.stream().map(InstanceType::getId).anyMatch(id -> id.equals(EMF.MEDIA.toString())));
		assertTrue(allowedSuperTypes.stream().map(InstanceType::getId).anyMatch(id -> id.equals(PTOP_INFORMATION_RESOURCE.toString())));
	}

	private void mockRelatingInstances() {
		when(searchService.stream(any(), any())).thenReturn(IntStream.range(1, 10).boxed().map(idx -> "emf:instance-" + idx));

		List<EmfInstance> instances = IntStream.range(1, 10).boxed().map(idx -> {
			EmfInstance instance = new EmfInstance();
			instance.setId("emf:instance-" + idx);
			instance.setIdentifier(INVERSE_DEF);
			instance.setType(getMockedInstanceType(instance));
			instance.add("relation100", "emf:instanceId");
			instance.append("relation101", "emf:instanceId");
			instance.append("relation101", "emf:instanceId-1");
			instance.add("relation102", "emf:instanceId");
			instance.add("relation103", "emf:instanceId");
			instance.add("relation104", "emf:instanceId");
			return instance;
		}).collect(Collectors.toList());

		when(instanceService.loadByDbId(anyList())).thenReturn(instances);
	}

	private DefinitionModelBuilder withDefinition(String defId) {
		return new DefinitionModelBuilder(defId);
	}

	private class DefinitionModelBuilder {

		private final String defId;
		private List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		private List<StateTransition> stateTransitions = new ArrayList<>();

		DefinitionModelBuilder(String defId) {
			this.defId = defId;
		}

		private PropertyBuilder withDataProperty() {
			return new PropertyBuilder(true, this);
		}

		private PropertyBuilder withObjectProperty() {
			return new PropertyBuilder(false, this);
		}

		private DefinitionModelBuilder addProperty(PropertyDefinitionMock property) {
			propertyDefinitions.add(property);
			return this;
		}

		private DefinitionModelBuilder withStateTransition(String fromState, String operation, String toState) {
			stateTransitions.add(createStateTransition(fromState, operation, toState));
			return this;
		}

		DefinitionMock done() {
			DefinitionMock definitionMock = new DefinitionMock();
			definitionMock.setIdentifier(defId);
			definitionMock.getFields().addAll(propertyDefinitions);
			definitionMock.getStateTransitions().addAll(stateTransitions);
			when(definitionService.find(defId)).thenReturn(definitionMock);
			return definitionMock;
		}
	}

	private StateTransitionMock createStateTransition(String fromState, String operation, String toState) {
		StateTransitionMock stateTransition = new StateTransitionMock();
		stateTransition.setFromState(fromState);
		stateTransition.setTransitionId(operation);
		stateTransition.setToState(toState);
		return stateTransition;
	}

	private class PropertyBuilder {
		private final DefinitionModelBuilder modelBuilder;
		private String name;
		private String uri;
		private Integer codeList;
		private String domain;
		private String range;
		private String value;
		private boolean isDataProperty;

		private PropertyBuilder(boolean isDataProperty, DefinitionModelBuilder modelBuilder) {
			this.isDataProperty = isDataProperty;
			this.modelBuilder = modelBuilder;
		}

		PropertyBuilder name(String name) {
			this.name = name;
			return this;
		}

		PropertyBuilder uri(String uri) {
			this.uri = uri;
			return this;
		}

		PropertyBuilder codeList(int codeList) {
			this.codeList = codeList;
			return this;
		}

		PropertyBuilder value(String value) {
			this.value = value;
			return this;
		}

		PropertyBuilder domain(String domain) {
			this.domain = domain;
			return this;
		}

		PropertyBuilder range(String range) {
			this.range = range;
			return this;
		}

		DefinitionModelBuilder add() {
			PropertyDefinitionMock property = new PropertyDefinitionMock();
			property.setName(name);
			property.setCodelist(codeList);
			property.setDefaultValue(value);
			if (uri != null) {
				property.setUri(uri);
			} else {
				property.setUri(DefaultProperties.NOT_USED_PROPERTY_VALUE);
			}
			if (uri != null) {
				PropertyInstance instance = new PropertyInstance();
				instance.setId(uri);
				instance.setRangeClass(range);
				instance.setDomainClass(domain);
				if (isDataProperty) {
					when(semanticDefinitionService.getProperty(uri)).thenReturn(instance);
				} else {
					when(semanticDefinitionService.getRelation(uri)).thenReturn(instance);
				}
			}
			return modelBuilder.addProperty(property);
		}
	}

	private static class SimpleResultItem implements ResultItem {

		private final Serializable value;

		private SimpleResultItem(Serializable value) {
			this.value = value;
		}

		@Override
		public Iterator<ResultValue> iterator() {
			return Collections.emptyIterator();
		}

		@Override
		public Set<String> getValueNames() {
			return Collections.emptySet();
		}

		@Override
		public boolean hasValue(String name) {
			return false;
		}

		@Override
		public ResultValue getValue(String name) {
			return null;
		}

		@Override
		public Serializable getResultValue(String name) {
			return value;
		}

		@Override
		public int size() {
			return 0;
		}
	}
}