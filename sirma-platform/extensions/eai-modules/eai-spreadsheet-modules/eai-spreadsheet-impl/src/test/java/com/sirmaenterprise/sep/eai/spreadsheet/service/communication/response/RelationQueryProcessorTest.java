package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Condition.Junction;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.eai.service.model.transform.EAIModelConverter;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider;
import com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.RelationQueryProcessor.ContextProcessorParameters;

/**
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class RelationQueryProcessorTest {
	@Mock
	private EAIModelConverter modelConverter;
	@Mock
	private ModelConfiguration modelConfiguration;

	@Mock
	private SpreadsheetIntegrationConfiguration eaiConfiguration;
	@Mock
	private ModelService modelService;
	@Mock
	private DateConverter dateConverter;
	@InjectMocks
	private RelationQueryProcessor contextQueryProcessor;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		when(modelService.getModelConfiguration(eq(SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID)))
				.thenReturn(modelConfiguration);

		modelConverter = mock(EAIModelConverter.class);
		when(modelService.provideModelConverter(eq(SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID)))
				.thenReturn(modelConverter);

		when(eaiConfiguration.getTypePropertyURI()).thenReturn(new ConfigurationPropertyMock<String>("emf:type"));

		when(dateConverter.getDateFormat()).thenReturn(new SimpleDateFormat("dd.MM.yyyy"));
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.RelationQueryProcessor#prepareParameters(java.lang.String)}.
	 */
	@Test
	public void testPrepareParameters() throws Exception {
		ContextProcessorParameters prepareParameters = createNextStatement(
				"emf:type=\"Drawing\" and (dcterms:creator=\"Creator1\" or  dcterms:creator=\"Creator2\") and emf:externalID=\"extId\" and (chd:trackingCodeId=\"track-01\" or chd:trackingCodeId=\"track-02\" or chd:trackingCodeId=\"track-03\") and chd:accessionNumber=?");
		assertNotNull(prepareParameters);
		assertNotNull(prepareParameters.getQuery());

		prepareParameters = createNextStatement("emf:type=\"Drawing\" OR emf:type=\"Painting\"");
		assertNotNull(prepareParameters);
		assertNotNull(prepareParameters.getQuery());
		prepareParameters = createNextStatement("emf:date=[* TO 10.03.2017]");
		assertNotNull(prepareParameters);
		assertNotNull(prepareParameters.getQuery());
		prepareParameters = createNextStatement("emf:range=[1 TO 0]");
		assertNotNull(prepareParameters);
		assertNotNull(prepareParameters.getQuery());
		prepareParameters = createNextStatement("emf:type=(100 or 1010 or 1200)");
		assertNotNull(prepareParameters);
		assertNotNull(prepareParameters.getQuery());
	}

	@Test
	public void testConvertToCondtionComplex() throws Exception {
		when(modelConverter.convertExternaltoSEIPProperty(any(), any(), any()))
				.thenAnswer(e -> new Pair<>(e.getArgumentAt(0, String.class), e.getArgumentAt(1, Serializable.class)));

		ContextProcessorParameters prepareParameters = createNextStatement(
				"emf:type=\"Drawing\" and (dcterms:creator=\"Creator1\" or  dcterms:creator=\"Creator2\") and emf:externalID=\"extId\" and (chd:trackingCodeId=\"track-01\" or chd:trackingCodeId=\"track-02\" or chd:trackingCodeId=\"track-03\") and chd:accessionNumber=?");
		EntityType entityType = mock(EntityType.class);
		when(entityType.getIdentifier()).thenReturn("objectId");
		when(modelConfiguration.getTypeByExternalName(eq("Drawing"))).thenReturn(entityType);
		EntityProperty property1 = mock(EntityProperty.class);
		EntityProperty property2 = mock(EntityProperty.class);
		EntityProperty property3 = mock(EntityProperty.class);
		EntityProperty property4 = mock(EntityProperty.class);
		when(modelConfiguration.getPropertyByExternalName(eq("objectId"), eq("dcterms:creator"))).thenReturn(property1);
		when(modelConfiguration.getPropertyByExternalName(eq("objectId"), eq("emf:externalID"))).thenReturn(property2);
		when(modelConfiguration.getPropertyByExternalName(eq("objectId"), eq("chd:trackingCodeId")))
				.thenReturn(property3);
		when(modelConfiguration.getPropertyByExternalName(eq("objectId"), eq("chd:accessionNumber")))
				.thenReturn(property4);

		PropertyDefinition propDefinition = mock(PropertyDefinition.class);
		when(propDefinition.isMultiValued()).thenReturn(Boolean.FALSE);
		when(modelConverter.findInternalFieldForType(eq("objectId"), any())).thenReturn(Optional.of(propDefinition));

		Condition convertToCondtion = contextQueryProcessor.convertToCondtion(prepareParameters);
		assertNotNull(convertToCondtion);
		assertEquals(5, convertToCondtion.getRules().size());
		assertEquals(Junction.AND, convertToCondtion.getCondition());
		// converted to types instead of emf:type
		assertTrue(convertToCondtion.getRules().get(0) instanceof Rule);
		assertEquals("types", ((Rule) convertToCondtion.getRules().get(0)).getField());

		assertTrue(convertToCondtion.getRules().get(1) instanceof Rule);
		assertEquals(2, ((Rule) convertToCondtion.getRules().get(1)).getValues().size());

		assertTrue(convertToCondtion.getRules().get(2) instanceof Rule);
		assertEquals("emf:externalID", ((Rule) convertToCondtion.getRules().get(2)).getField());

		assertTrue(convertToCondtion.getRules().get(3) instanceof Rule);
		assertEquals(3, ((Rule) convertToCondtion.getRules().get(3)).getValues().size());

		assertTrue(convertToCondtion.getRules().get(4) instanceof Rule);
		assertEquals("chd:accessionNumber", ((Rule) convertToCondtion.getRules().get(4)).getField());
	}

	@Test
	public void testConvertToCondtionOrClause() throws Exception {
		when(modelConverter.convertExternaltoSEIPProperty(any(), any(), any()))
				.thenAnswer(e -> new Pair<>(e.getArgumentAt(0, String.class), e.getArgumentAt(1, Serializable.class)));
		EntityType entityType = mock(EntityType.class);
		when(entityType.getIdentifier()).thenReturn("objectId2");
		when(modelConfiguration.getTypeByExternalName(eq("Drawing"))).thenReturn(entityType);

		ContextProcessorParameters prepareParameters = createNextStatement(
				"(emf:type=\"Drawing\" OR emf:type=\"Painting\") AND emf:externalID=?");
		EntityType entityType2 = mock(EntityType.class);
		when(entityType2.getIdentifier()).thenReturn("objectId2");
		when(modelConfiguration.getTypeByExternalName(eq("Painting"))).thenReturn(entityType2);
		EntityProperty property1 = mock(EntityProperty.class);
		EntityProperty property2 = mock(EntityProperty.class);
		when(modelConfiguration.getPropertyByExternalName(eq("objectId2"), eq("emf:externalID"))).thenReturn(property1);
		when(modelConfiguration.getPropertyByExternalName(eq("objectId2"), eq("emf:partOf"))).thenReturn(property2);
		Condition convertToCondtion = contextQueryProcessor.convertToCondtion(prepareParameters);
		assertNotNull(convertToCondtion);
		assertEquals(2, convertToCondtion.getRules().size());
		assertEquals(Junction.AND, convertToCondtion.getCondition());
		// converted to types instead of emf:type
		assertTrue(convertToCondtion.getRules().get(0) instanceof Rule);
		assertEquals(2, ((Rule) convertToCondtion.getRules().get(0)).getValues().size());

		assertTrue(convertToCondtion.getRules().get(1) instanceof Rule);
		assertEquals("emf:externalID", ((Rule) convertToCondtion.getRules().get(1)).getField());

		// next statement
		prepareParameters = createNextStatement(
				"emf:partOf=current and emf:type=\"Drawing\" and (emf:externalID=? or chd:accessionNumber=?)");
		EntityProperty property3 = mock(EntityProperty.class);
		when(modelConfiguration.getPropertyByExternalName(eq("objectId2"), eq("chd:accessionNumber")))
				.thenReturn(property3);
		convertToCondtion = contextQueryProcessor.convertToCondtion(prepareParameters);
		assertNotNull(convertToCondtion);

		assertEquals(3, convertToCondtion.getRules().size());
		assertEquals(Junction.AND, convertToCondtion.getCondition());
		// converted to types instead of emf:type
		assertTrue(convertToCondtion.getRules().get(0) instanceof Rule);
		assertEquals("emf:partOf", ((Rule) convertToCondtion.getRules().get(0)).getField());

		assertTrue(convertToCondtion.getRules().get(1) instanceof Rule);
		assertEquals("types", ((Rule) convertToCondtion.getRules().get(1)).getField());

		assertTrue(convertToCondtion.getRules().get(2) instanceof Condition);
		assertEquals(2, ((Condition) convertToCondtion.getRules().get(2)).getRules().size());
		assertEquals(Junction.OR, ((Condition) convertToCondtion.getRules().get(2)).getCondition());
	}

	@Test
	public void testConvertToCondtionRanges() throws Exception {

		ContextProcessorParameters prepareParameters = createNextStatement("emf:date=[* TO 10.03.2017]");
		Condition convertToCondtion = contextQueryProcessor.convertToCondtion(prepareParameters);
		assertEquals(1, convertToCondtion.getRules().size());
		assertEquals(Junction.AND, convertToCondtion.getCondition());
		assertTrue(convertToCondtion.getRules().get(0) instanceof Rule);
		assertEquals("emf:date", ((Rule) convertToCondtion.getRules().get(0)).getField());
		assertEquals(2, ((Rule) convertToCondtion.getRules().get(0)).getValues().size());
		assertNull(((Rule) convertToCondtion.getRules().get(0)).getValues().get(0));
		// second argument is valid date
		assertNotNull(ISO8601DateFormat.parse(((Rule) convertToCondtion.getRules().get(0)).getValues().get(1)));

		prepareParameters = createNextStatement("emf:range=[1 TO *]");
		convertToCondtion = contextQueryProcessor.convertToCondtion(prepareParameters);
		assertNotNull(convertToCondtion);
		assertTrue(convertToCondtion.getRules().get(0) instanceof Rule);
		assertEquals("emf:range", ((Rule) convertToCondtion.getRules().get(0)).getField());
		assertEquals(2, ((Rule) convertToCondtion.getRules().get(0)).getValues().size());
		assertEquals("1", ((Rule) convertToCondtion.getRules().get(0)).getValues().get(0));
		assertNull(((Rule) convertToCondtion.getRules().get(0)).getValues().get(1));

		prepareParameters = createNextStatement("emf:property=(100 or 1010 or 1200)");
		convertToCondtion = contextQueryProcessor.convertToCondtion(prepareParameters);
		assertNotNull(convertToCondtion);

		prepareParameters = createNextStatement("emf:range=[* TO 1]");
		convertToCondtion = contextQueryProcessor.convertToCondtion(prepareParameters);
		assertNotNull(convertToCondtion);
	}

	private ContextProcessorParameters createNextStatement(String query) throws EAIException {
		ContextProcessorParameters prepareParameters = RelationQueryProcessor.prepareParameters(query);
		prepareParameters.setContext(mock(Instance.class));
		prepareParameters.setProvidedValue("");
		return prepareParameters;
	}

}
