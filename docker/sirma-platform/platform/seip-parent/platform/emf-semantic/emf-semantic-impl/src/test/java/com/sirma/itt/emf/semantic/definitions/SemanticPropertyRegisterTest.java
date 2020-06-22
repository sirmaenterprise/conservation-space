package com.sirma.itt.emf.semantic.definitions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.semantic.model.Rdf4JStringUriProxy;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Test for SemanticPropertyRegister.
 *
 * @author BBonev
 * @author A. Kunchev
 */
public class SemanticPropertyRegisterTest {

	@InjectMocks
	private SemanticPropertyRegister register = new SemanticPropertyRegister();

	@Mock
	private NamespaceRegistryService registryService;

	@Spy
	private ValueFactory valueFactory = SimpleValueFactory.getInstance();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		when(registryService.buildFullUri(anyString())).thenAnswer(a -> a.getArgumentAt(0, String.class));
		when(registryService.getShortUri(any(Uri.class))).then(a -> {
			return "emf:" + a.getArgumentAt(0, Uri.class).getLocalName();
		});
		when(registryService.buildUri(anyString()))
				.then(a -> new Rdf4JStringUriProxy(a.getArgumentAt(0, String.class)));
	}

	@Test
	@SuppressWarnings("synthetic-access")
	public void provideModelStatements() {

		register.provideModelStatements(buildTestDefinition(), new LinkedHashModel());

		verify(registryService, times(14)).buildUri(anyString());
		verify(valueFactory, times(14)).createStatement(any(Resource.class), any(IRI.class), any(Resource.class),
				eq(null));
	}

	@Test
	public void provideModelStatements_nullDefinition_servicesNotCalled() {
		register.provideModelStatements(null, new LinkedHashModel());
		verify(registryService, never()).buildUri(anyString());
		verify(valueFactory, never()).createStatement(any(Resource.class), any(IRI.class), any(Resource.class));
	}

	@Test
	public void provideModelStatements_nullModel_servicesNotCalled() {
		register.provideModelStatements(mock(DefinitionModel.class), null);
		verify(registryService, never()).buildUri(anyString());
		verify(valueFactory, never()).createStatement(any(Resource.class), any(IRI.class), any(Resource.class));
	}

	// ----------------------------------------- common methods --------------------------------------------------

	/**
	 * Builds the test definitions.
	 *
	 * @return the list
	 */
	private static DefinitionModel buildTestDefinition() {
		GenericDefinition definitionImpl = new GenericDefinitionImpl();
		definitionImpl.getFields().add(createField("test"));
		definitionImpl.getFields().add(createObjectProperty("test1"));
		definitionImpl.getFields().add(createObjectProperty("test2"));
		definitionImpl.getRegions().add(createRegion("region1"));
		definitionImpl.getRegions().add(createRegion("region2"));
		return definitionImpl;
	}

	/**
	 * Creates the region.
	 *
	 * @param string
	 *            the string
	 * @return the region definition
	 */
	private static RegionDefinition createRegion(String string) {
		RegionDefinitionImpl impl = new RegionDefinitionImpl();
		impl.setIdentifier(string);
		impl.getFields().add(createField(string + "Field1"));
		impl.getFields().add(createField(string + "Field2"));
		return impl;
	}

	private static PropertyDefinition createField(String string) {
		FieldDefinitionImpl definition = new FieldDefinitionImpl();
		definition.setIdentifier(string);
		definition.setUri("emf:" + string);
		definition.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.TEXT));
		definition.setDisplayType(DisplayType.EDITABLE);
		return definition;
	}

	private static PropertyDefinition createObjectProperty(String string) {
		FieldDefinitionImpl definition = new FieldDefinitionImpl();
		definition.setIdentifier(string);
		definition.setUri("emf:" + string);
		definition.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));
		definition.setType(DataTypeDefinition.URI);
		definition.setDisplayType(DisplayType.EDITABLE);
		return definition;
	}
}
