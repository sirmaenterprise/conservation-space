package com.sirma.itt.seip.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Test for {@link DefinitionHelperImpl}.
 *
 * @author A. Kunchev
 */
public class DefinitionHelperImplTest {

	@InjectMocks
	private DefinitionHelper helper;

	@Mock
	protected CodelistService codelistService;

	@Mock
	protected UserPreferences userPreferences;

	@Before
	public void setup() {
		helper = new DefinitionHelperImpl();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void collectAllFields_nullModel() {
		Collection<Ordinal> fields = helper.collectAllFields(null);
		assertTrue(fields.isEmpty());
	}

	@Test
	public void collectAllFields_simpleDefinitionModel() {
		DefinitionModel model = mock(DefinitionModel.class);
		List<PropertyDefinition> fields = Arrays.asList(buildPropertyDefinition(DefaultProperties.HEADER_DEFAULT),
				buildPropertyDefinition(DefaultProperties.TITLE), buildPropertyDefinition(DefaultProperties.STATUS));
		when(model.getFields()).thenReturn(fields);
		Collection<Ordinal> result = helper.collectAllFields(model);
		assertEquals(3, result.size());
	}

	@Test
	public void collectAllFields_regionDefinitionModel() {
		RegionDefinitionModel model = mock(RegionDefinitionModel.class);
		RegionDefinition region = buildRegionDefinition("desctiptionRegion");
		PropertyDefinition header = buildPropertyDefinition(DefaultProperties.HEADER_DEFAULT);
		PropertyDefinition title = buildPropertyDefinition(DefaultProperties.TITLE);
		PropertyDefinition status = buildPropertyDefinition(DefaultProperties.STATUS);
		when(model.getRegions()).thenReturn(Arrays.asList(region));
		when(model.getFields()).thenReturn(Arrays.asList(header, title, status));
		Collection<Ordinal> result = helper.collectAllFields(model);
		assertEquals(4, result.size());
	}

	private static PropertyDefinition buildPropertyDefinition(String identifier) {
		PropertyDefinition definition = new PropertyDefinitionProxy();
		definition.setIdentifier(identifier);
		return definition;
	}

	private static RegionDefinition buildRegionDefinition(String identifier) {
		RegionDefinition definition = new RegionDefinitionImpl();
		definition.setIdentifier(identifier);
		PropertyDefinition property = new PropertyDefinitionProxy();
		property.setIdentifier(DefaultProperties.DESCRIPTION);
		definition.getFields().add(property);
		return definition;
	}

	@Test
	public void getDefinitionLabel_nullDefinition() {
		assertNull(helper.getDefinitionLabel(null));
	}

	@Test
	public void getDefinitionLabel_nullProperyType() {
		DefinitionModel model = mock(DefinitionModel.class);
		when(model.getIdentifier()).thenReturn("identifier");
		when(model.getField("type")).thenReturn(Optional.empty());
		String definitionLabel = helper.getDefinitionLabel(model);
		assertEquals("identifier", definitionLabel);
	}

	@Test
	public void getDefinitionLabel_notNullTypeWithCodelist() {
		DefinitionModel model = mock(DefinitionModel.class);
		when(model.getIdentifier()).thenReturn("identifier");
		PropertyDefinitionProxy definition = new PropertyDefinitionProxy();
		definition.setIdentifier(DefaultProperties.TYPE);
		definition.setCodelist(100);
		definition.setValue("propValue");
		when(model.getField("type")).thenReturn(Optional.of(definition));
		CodeValue codeValue = new CodeValue();
		codeValue.add("lang", "type");
		when(codelistService.getCodeValue(100, "propValue")).thenReturn(codeValue);
		when(userPreferences.getLanguage()).thenReturn("lang");
		String definitionLabel = helper.getDefinitionLabel(model);
		assertEquals("type", definitionLabel);
	}

	@Test
	public void getDefinitionLabel_notNullTypeNoCodelist() {
		DefinitionModel model = mock(DefinitionModel.class);
		when(model.getIdentifier()).thenReturn("identifier");
		when(model.getField("type")).thenReturn(Optional.of(new PropertyDefinitionProxy()));
		String definitionLabel = helper.getDefinitionLabel(model);
		assertEquals("identifier", definitionLabel);
	}

}
