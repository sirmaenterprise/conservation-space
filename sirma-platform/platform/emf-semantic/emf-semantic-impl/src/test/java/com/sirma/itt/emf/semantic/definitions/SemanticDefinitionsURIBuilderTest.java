package com.sirma.itt.emf.semantic.definitions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;

import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.model.DefinitionEntry;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.TopLevelDefinition;
import com.sirma.itt.seip.testutil.mocks.ControlDefintionMock;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for SemanticDefinitionsURIBuilder.
 *
 * @author A. Kunchev
 */
public class SemanticDefinitionsURIBuilderTest {

	@InjectMocks
	private SemanticDefinitionsURIBuilder provider = new SemanticDefinitionsURIBuilder();

	@Mock
	private ValueFactory valueFactory;

	@Mock
	private DictionaryService dictionaryService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Mock
	private TypeMappingProvider typeProvider;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	// ----------------------------------- provideModelStatements -----------------------------

	@Test
	public void provideModelStatements_notTopLevelDefinition_valueFactoryNotCalled() {
		provider.provideModelStatements(new ControlDefintionMock(), new LinkedHashModel());
		verifyValueFactoryNeverCalled();
	}

	@Test
	public void provideModelStatements_abstractDefinition_valueFactoryNotCalled() {
		TopLevelDefinition definition = mock(TopLevelDefinition.class);
		when(definition.isAbstract()).thenReturn(true);
		provider.provideModelStatements(definition, new LinkedHashModel());
		verifyValueFactoryNeverCalled();
	}

	@Test
	public void provideModelStatements_nullTypeDefinition_valueFactoryNotCalled() {
		provider.provideModelStatements(new DefinitionEntry(), new LinkedHashModel());
		verifyValueFactoryNeverCalled();
	}

	private void verifyValueFactoryNeverCalled() {
		verify(valueFactory, never()).createURI(anyString(), anyString());
		verify(valueFactory, never()).createStatement(any(Resource.class), any(URI.class), any(Resource.class));
	}

	@Test
	public void provideModelStatements_caseDefinition_valueFactoryCalled() {
		LinkedHashModel model = mock(LinkedHashModel.class);
		when(model.add(any(Statement.class))).thenReturn(true);
		when(dictionaryService.getDefinitionIdentifier(any(DefinitionModel.class))).thenReturn("definitionId");
		TypeConverter typeConverter = mock(TypeConverter.class);
		when(typeConverter.convert(eq(Value.class), any()))
				.then(a -> valueFactory.createLiteral(a.getArgumentAt(1, Serializable.class).toString()));
		TypeConverterUtil.setTypeConverter(typeConverter);
		when(typeProvider.getDataType(anyString())).then(a -> {
			DataTypeDefinition definition = Mockito.mock(DataTypeDefinition.class);
			final Set<String> uries = new HashSet<>();
			uries.add(EMF.NAMESPACE + EMF.PROJECT.getLocalName());
			when(definition.getUries()).then(answer -> uries);

			return definition;
		});
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setType("case");
		provider.provideModelStatements(definition, model);
		verify(namespaceRegistryService, times(4)).buildUri(anyString());
		verify(valueFactory, times(4)).createStatement(any(Resource.class), any(URI.class), any(Resource.class), any());
	}

}
