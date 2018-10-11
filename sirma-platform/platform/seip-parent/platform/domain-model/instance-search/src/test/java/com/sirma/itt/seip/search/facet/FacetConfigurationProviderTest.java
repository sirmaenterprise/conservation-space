package com.sirma.itt.seip.search.facet;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.search.facet.FacetConfiguration;

/**
 * Testing the logic in {@link FacetConfigurationProvider} for handling configuration definitions of
 * {@link ObjectType#FACET_CONFIG} type.
 *
 * @author nvelkov
 * @author Mihail Radkov
 */
public class FacetConfigurationProviderTest {

	private static final String FACET_ID = "rdfType";

	@Mock
	private DefinitionService definitionService;
	@Spy
	ContextualMap<String, FacetConfiguration> facetConfigurations = ContextualMap.create();

	@InjectMocks
	private FacetConfigurationProviderImpl facetConfigurationProvider = new FacetConfigurationProviderImpl();

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		facetConfigurations.clearContextValue();
		facetConfigurationProvider.init();
	}

	/**
	 * Tests the logic behind {@link FacetConfigurationProvider#reset()} when there is no configuration definitions.
	 */
	@Test
	public void testWithoutDefinitions() {
		facetConfigurationProvider.reset();
		FacetConfiguration facetConfigField = facetConfigurationProvider.getFacetConfigField(FACET_ID);
		Assert.assertNull(facetConfigField);
	}

	/**
	 * Tests the logic behind {@link FacetConfigurationProvider#reset()} when there is a configuration definitions but
	 * without configuration field.
	 */
	@Test
	public void testWithoutConfigurationField() {
		GenericDefinition facetConfigDefinition = mockGenericDefinition(FacetConfigurationProviderImpl.FACET_CONFIG,
				FACET_ID);

		List<DefinitionModel> genericDefinitions = new ArrayList<>();
		genericDefinitions.add(facetConfigDefinition);
		Mockito.when(definitionService.getAllDefinitions(GenericDefinition.class)).thenReturn(genericDefinitions);

		facetConfigurationProvider.reset();

		FacetConfiguration facetConfigField = facetConfigurationProvider.getFacetConfigField(FACET_ID);
		Assert.assertNull(facetConfigField);
	}

	/**
	 * Tests the logic behind {@link FacetConfigurationProvider#reset()} when there is a configuration definitions with
	 * all the correct properties and controls.
	 */
	@Test
	public void testFacetConfig() {
		GenericDefinition facetConfigDefinition = mockGenericDefinition(FacetConfigurationProviderImpl.FACET_CONFIG,
				FACET_ID);
		addFieldsToDefinition(facetConfigDefinition, false, true, false);

		List<DefinitionModel> genericDefinitions = new ArrayList<>();
		genericDefinitions.add(facetConfigDefinition);
		Mockito.when(definitionService.getAllDefinitions(GenericDefinition.class)).thenReturn(genericDefinitions);

		facetConfigurationProvider.reset();

		FacetConfiguration facetConfiguration = facetConfigurationProvider.getFacetConfigField(FACET_ID);
		Assert.assertEquals(facetConfiguration.getSort(), "alphabetical");
		Assert.assertEquals(facetConfiguration.isDefault(), false);
		Assert.assertEquals(facetConfiguration.getOrder(), Integer.valueOf(1));
	}

	/**
	 * Tests the logic behind {@link FacetConfigurationProvider#reset()} when there is a configuration definitions with
	 * all the correct properties and controls and it is a <i>default</i> facet.
	 */
	@Test
	public void testDefaultFacetConfig() {
		GenericDefinition facetConfigDefinition = mockGenericDefinition(FacetConfigurationProviderImpl.FACET_CONFIG,
				FACET_ID);
		addFieldsToDefinition(facetConfigDefinition, true, true, false);

		List<DefinitionModel> genericDefinitions = new ArrayList<>();
		genericDefinitions.add(facetConfigDefinition);
		Mockito.when(definitionService.getAllDefinitions(GenericDefinition.class)).thenReturn(genericDefinitions);

		facetConfigurationProvider.reset();

		FacetConfiguration facetConfiguration = facetConfigurationProvider.getFacetConfigField(FACET_ID);
		Assert.assertEquals(facetConfiguration.getSort(), "alphabetical");
		Assert.assertEquals(facetConfiguration.isDefault(), true);
		Assert.assertEquals(facetConfiguration.getOrder(), Integer.valueOf(1));
	}

	/**
	 * Tests the logic behind {@link FacetConfigurationProvider#reset()} when there is a configuration definitions with
	 * all the correct properties but without controls.
	 */
	@Test
	public void testFacetConfigMissingConfigControl() {
		GenericDefinition facetConfigDefinition = mockGenericDefinition(FacetConfigurationProviderImpl.FACET_CONFIG,
				FACET_ID);
		addFieldsToDefinition(facetConfigDefinition, true, false, false);

		List<DefinitionModel> genericDefinitions = new ArrayList<>();
		genericDefinitions.add(facetConfigDefinition);
		Mockito.when(definitionService.getAllDefinitions(GenericDefinition.class)).thenReturn(genericDefinitions);

		facetConfigurationProvider.reset();

		FacetConfiguration facetConfiguration = facetConfigurationProvider.getFacetConfigField(FACET_ID);
		Assert.assertNull(facetConfiguration.getSort());
	}

	/**
	 * Tests the logic behind {@link FacetConfigurationProvider#reset()} when there is a configuration definition but
	 * its display type is {@link DisplayType#HIDDEN}.
	 */
	@Test
	public void testHiddenFacetConfiguration() {
		GenericDefinition facetConfigDefinition = mockGenericDefinition(FacetConfigurationProviderImpl.FACET_CONFIG,
				FACET_ID);
		addFieldsToDefinition(facetConfigDefinition, false, true, true);

		List<DefinitionModel> genericDefinitions = new ArrayList<>();
		genericDefinitions.add(facetConfigDefinition);
		Mockito.when(definitionService.getAllDefinitions(GenericDefinition.class)).thenReturn(genericDefinitions);

		facetConfigurationProvider.reset();

		FacetConfiguration facetConfiguration = facetConfigurationProvider.getFacetConfigField(FACET_ID);
		Assert.assertNull(facetConfiguration);
	}

	/**
	 * Mock a generic definition that will return the specified definition type.
	 *
	 * @param definitionType
	 *            the definition type
	 * @param definitionId
	 *            - the ID of the definition
	 * @return the mocked generic definition
	 */
	private static GenericDefinition mockGenericDefinition(String definitionType, String definitionId) {
		GenericDefinition definition = Mockito.mock(GenericDefinition.class);
		Mockito.when(definition.getType()).thenReturn(definitionType);
		Mockito.when(definition.getIdentifier()).thenReturn(definitionId);
		return definition;
	}

	/**
	 * Add one test field to a generic definition.
	 *
	 * @param definition
	 *            the definition
	 * @param defaultFacet
	 *            indicates whether the field should be default or not
	 * @param controlExists
	 *            indicates whether there are controls in the field
	 * @param hidden
	 *            - if true, makes the facet configuration hidden
	 */
	private static void addFieldsToDefinition(GenericDefinition definition, boolean defaultFacet, boolean controlExists,
			boolean hidden) {
		List<PropertyDefinition> properties = new ArrayList<>();
		PropertyDefinition property = Mockito.mock(PropertyDefinition.class);
		Mockito.when(property.getOrder()).thenReturn(Integer.valueOf(1));
		Mockito.when(property.getName()).thenReturn("configuration");

		if (hidden) {
			Mockito.when(property.getDisplayType()).thenReturn(DisplayType.HIDDEN);
		}

		if (controlExists) {
			ControlDefinition controlDefinition = Mockito.mock(ControlDefinition.class);
			ControlParam sortNode = Mockito.mock(ControlParam.class);
			ControlParam orderNode = Mockito.mock(ControlParam.class);
			ControlParam pageSizeNode = Mockito.mock(ControlParam.class);
			ControlParam stateParamNode = Mockito.mock(ControlParam.class);

			Mockito.when(sortNode.getValue()).thenReturn("alphabetical");
			Mockito.when(orderNode.getValue()).thenReturn("ascending");
			Mockito.when(pageSizeNode.getValue()).thenReturn("22");
			Mockito.when(stateParamNode.getValue()).thenReturn("expanded");

			Mockito.when(controlDefinition.getChild("sort")).thenReturn(sortNode);
			Mockito.when(controlDefinition.getChild("order")).thenReturn(orderNode);
			Mockito.when(controlDefinition.getChild("pageSize")).thenReturn(pageSizeNode);
			Mockito.when(controlDefinition.getChild("state")).thenReturn(stateParamNode);

			if (defaultFacet) {
				ControlParam defaultNode = Mockito.mock(ControlParam.class);
				Mockito.when(defaultNode.getValue()).thenReturn("true");
				Mockito.when(controlDefinition.getChild("default")).thenReturn(defaultNode);
			}

			Mockito.when(property.getControlDefinition()).thenReturn(controlDefinition);
		}
		String defId = definition.getIdentifier();
		when(property.getPath()).thenReturn(defId);
		properties.add(property);
		Mockito.when(definition.getFields()).thenReturn(properties);
	}
}
