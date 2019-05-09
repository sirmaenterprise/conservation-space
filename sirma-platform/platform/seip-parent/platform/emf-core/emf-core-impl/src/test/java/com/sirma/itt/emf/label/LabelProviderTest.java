package com.sirma.itt.emf.label;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelResolverProvider;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.definition.label.LabelResolver;
import com.sirma.itt.seip.expressions.ExpressionEvaluatorManager;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test the label provider.
 *
 * @author nvelkov
 */
public class LabelProviderTest {

	public static final String LABEL_ID = "labelId";
	public static final String PROPERTY_URI = "emf:references";
	@Mock
	private LabelService labelService;

	@Mock
	private ExpressionEvaluatorManager manager;

	@Mock
	private UserPreferences userPreferences;

	@Spy
	private List<LabelResolverProvider> labelProviders = new ArrayList<>();

	@Mock
	private LabelResolverProvider bundleProvider;

	/** Provided resource bundles mapped by language */
	@Spy
	private ContextualMap<String, List<ResourceBundle>> bundles = ContextualMap.create();

	@InjectMocks
	private LabelProviderImpl labelProvider = new LabelProviderImpl();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		setEnglishAsSystemLanguage();
		bundles.reset();
		labelProviders.clear();
		labelProviders.add(bundleProvider);

		labelProvider.init();
	}

	/**
	 * Test the get bundle value method.
	 */
	@Test
	public void testGetBundleValue() {
		mockBundleLanguage(Collections.singleton(LABEL_ID));
		assertEquals("label", labelProvider.getBundleValue(LABEL_ID));
	}

	/**
	 * Test the get bundle value method with a missing label.
	 */
	@Test
	public void testGetBundleValueMissingLabel() {
		mockBundleLanguage(Collections.singleton("anotherLabel"));
		assertEquals(LABEL_ID, labelProvider.getBundleValue(LABEL_ID));
	}

	@Test
	public void testGetLabel_Should_ReturnNoLabel_When_LabelIdIsNull() {
		mockBundleLanguage(Collections.singleton("anotherLabel"));
		assertEquals(LabelProvider.NO_LABEL, labelProvider.getLabel(null));
	}

	@Test
	public void testGetLabel_Should_ReturnPassedLabelId_When_NoLabelDefined() {
		mockBundleLanguage(Collections.singleton("anotherLabel"));
		assertEquals("notExistingLabel", labelProvider.getLabel("notExistingLabel"));
	}

	@Test
	public void testGetLabel_Should_ReturnLabel_When_LabelIsDefined() {
		mockBundleLanguage(Collections.singleton("anotherLabel"));
		assertEquals("label", labelProvider.getLabel("anotherLabel"));
	}

	@Test
	public void testGetLabel_Should_EvaluateLabel_When_IsExpression() {
		String labelId = "expLabel";
		String evaluatedLabel = "evaluated";

		mockBundleLanguage(new HashSet<>(Collections.singletonList("anotherLabel")));
		when(manager.isExpression(labelId)).thenReturn(Boolean.TRUE);
		when(manager.evaluate(labelId, String.class)).thenReturn(evaluatedLabel);
		when(manager.isExpression(evaluatedLabel)).thenReturn(Boolean.FALSE);

		assertEquals(evaluatedLabel, labelProvider.getLabel(labelId));
	}

	@Test
	public void testGetLabel_Should_ReturnLabelId_When_ExpressionEvaluatedNull() {
		String labelId = "expLabel";

		mockBundleLanguage(new HashSet<>(Collections.singletonList("anotherLabel")));
		when(manager.isExpression(labelId)).thenReturn(Boolean.TRUE);

		assertEquals(labelId, labelProvider.getLabel(labelId));
	}

	@Test
	public void testGetLabel_Should_ReturnLabelId_When_ExpressionEvaluatedAnotherExpression() {
		String labelId = "expLabel";
		String evaluatedLabel = "expression";

		mockBundleLanguage(Collections.singleton("anotherLabel"));
		when(manager.isExpression(labelId)).thenReturn(Boolean.TRUE);
		when(manager.evaluate(labelId, String.class)).thenReturn(evaluatedLabel);
		when(manager.isExpression(evaluatedLabel)).thenReturn(Boolean.TRUE);

		assertEquals(labelId, labelProvider.getLabel(labelId));
	}

	@Test
	public void testGetLabel_Should_ReturnLabelFromDefinition_When_ThereIsLabelDefinition() {
		LabelDefinition labelDefinition = mock(LabelDefinition.class);
		String labelId = "labelFromDef";
		String labelValue = "labelDefValue";
		Map<String, String> labels = new HashMap<>();
		labels.put(userPreferences.getLanguage(), labelValue);

		mockBundleLanguage(Collections.singleton("anotherLabel"));
		when(labelDefinition.getLabels()).thenReturn(labels);
		when(labelService.getLabel(labelId)).thenReturn(labelDefinition);

		assertEquals(labelValue, labelProvider.getLabel(labelId));
	}

	@Test
	public void testGetLabel_Should_ReturnLabelFromBundle_When_ThereIsNoLabelDefinition() {
		String labelId = LABEL_ID;
		LabelDefinition labelDefinition = mock(LabelDefinition.class);
		when(labelDefinition.getLabels()).thenReturn(new HashMap<>());
		when(labelService.getLabel(labelId)).thenReturn(labelDefinition);

		mockBundleLanguage(Collections.singleton(labelId));

		assertEquals("label", labelProvider.getLabel(labelId));
	}

	@Test
	public void getLabelShouldReturnEnglishLabelIfNotDefiniedRequestedLangInDefinition() {
		String labelId = LABEL_ID;
		LabelDefinition labelDefinition = mock(LabelDefinition.class);
		Map<String, String> labels = new HashMap<>();
		labels.put("de", "Some German label");
		labels.put("en", "Some English label");
		when(labelDefinition.getLabels()).thenReturn(labels);
		when(labelService.getLabel(labelId)).thenReturn(labelDefinition);

		assertEquals("Some English label", labelProvider.getLabel(labelId, "en"));
		assertEquals("Some German label", labelProvider.getLabel(labelId, "de"));
		assertEquals("Some English label", labelProvider.getLabel(labelId, "fr"));
	}

	@Test
	public void getLabelShouldReturnEnglishLabelIfDefinedButHasEmptyValueInDefinition() {
		String labelId = LABEL_ID;
		LabelDefinition labelDefinition = mock(LabelDefinition.class);
		Map<String, String> labels = new HashMap<>();
		labels.put("de", "");
		labels.put("en", "Some English label");
		when(labelDefinition.getLabels()).thenReturn(labels);
		when(labelService.getLabel(labelId)).thenReturn(labelDefinition);

		assertEquals("Some English label", labelProvider.getLabel(labelId, "en"));
		assertEquals("Some English label", labelProvider.getLabel(labelId, "de"));
	}

	@Test
	public void getLabelShouldReturnEnglishLabelIfNotDefiniedRequestedLangInDefinitionAndBundles() {
		String labelId = LABEL_ID;
		LabelDefinition labelDefinition = mock(LabelDefinition.class);
		mockBundleLanguage("en", Collections.singletonMap(labelId, "Some English label"));
		mockBundleLanguage("de", Collections.singletonMap(labelId, "Some German label"));
		when(labelDefinition.getLabels()).thenReturn(new HashMap<>());
		when(labelService.getLabel(labelId)).thenReturn(labelDefinition);

		assertEquals("Some English label", labelProvider.getLabel(labelId, "en"));
		assertEquals("Some German label", labelProvider.getLabel(labelId, "de"));
		assertEquals("Some English label", labelProvider.getLabel(labelId, "fr"));
	}

	@Test
	public void getLabelShouldReturnEnglishLabelIfBundleValueIsEmpty() {
		String labelId = LABEL_ID;
		LabelDefinition labelDefinition = mock(LabelDefinition.class);
		mockBundleLanguage("en", Collections.singletonMap(labelId, "Some English label"));
		mockBundleLanguage("de", Collections.singletonMap(labelId, ""));
		when(labelDefinition.getLabels()).thenReturn(new HashMap<>());
		when(labelService.getLabel(labelId)).thenReturn(labelDefinition);

		assertEquals("Some English label", labelProvider.getLabel(labelId, "en"));
		assertEquals("Some English label", labelProvider.getLabel(labelId, "de"));
	}

	@Test
	public void getValueShouldReturnEnglishLabelIfBundleValueIsEmpty() {
		String labelId = LABEL_ID;
		LabelDefinition labelDefinition = mock(LabelDefinition.class);
		mockBundleLanguage("en", Collections.singletonMap(labelId, "Some English label"));
		mockBundleLanguage("de", Collections.singletonMap(labelId, ""));
		when(labelDefinition.getLabels()).thenReturn(new HashMap<>());
		when(labelService.getLabel(labelId)).thenReturn(labelDefinition);

		assertEquals("Some English label", labelProvider.getValue(labelId));
	}

	@Test
	public void getPropertyLabel_shouldReturnDefinitionLabelWhenPresent() {
		setGermanAsSystemLanguage();
		withLabelDefinition("Some English definition label", "German definition label");
		withSemanticLabel("de", "Some German semantic label");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();

		assertEquals("German definition label", labelProvider.getPropertyLabel(propertyMock));
	}

	@Test
	public void getPropertyLabel_shouldReturnSemanticLabelIfDefinitionIsNotPresent_noLanguageProvider() {
		setGermanAsSystemLanguage();
		withLabelDefinition("Some English definition label", null);
		withSemanticLabel("de", "Some German semantic label");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();

		assertEquals("Some German semantic label", labelProvider.getPropertyLabel(propertyMock));
	}

	@Test
	public void getPropertyLabel_shouldReturnDefinitionLabelForDefaultLanguageWhenRequestedAndSemanticAreNotPresent() {
		setGermanAsSystemLanguage();
		withLabelDefinition("Some English definition label", null);
		withSemanticLabel("en", "Some English semantic label");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();

		assertEquals("Some English definition label", labelProvider.getPropertyLabel(propertyMock));
	}

	@Test
	public void getPropertyLabel_shouldReturnSemanticLabelForDefaultLanguageWhenRequestedAndDefinitionAreNotPresent() {
		setGermanAsSystemLanguage();
		withLabelDefinition(null, null);
		withSemanticLabel("en", "Some English semantic label");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();

		assertEquals("Some English semantic label", labelProvider.getPropertyLabel(propertyMock));
	}

	@Test
	public void getPropertyLabel_shouldReturnLabelIdWhenNoLabelAreDefined() {
		setGermanAsSystemLanguage();
		withLabelDefinition(null, null);
		withSemanticLabel("en", "");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();

		assertEquals("emf:references.label/labelId", labelProvider.getPropertyLabel(propertyMock));
	}

	@Test
	public void getPropertyLabel_shouldReturnDefaultLanguageLabelIfNotSemanticProperty() {
		setGermanAsSystemLanguage();
		withLabelDefinition("Some English definition label", null);
		withSemanticLabel("en", "");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();
		propertyMock.setUri(null);

		assertEquals("Some English definition label", labelProvider.getPropertyLabel(propertyMock));
	}

	@Test
	public void getPropertyLabel_shouldFallBackToSemanticIfLabelIdIsNotDefined() {
		setGermanAsSystemLanguage();
		withLabelDefinition("Some English definition label", null);
		withSemanticLabel("en", "Some English semantic label");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();
		propertyMock.setLabelId(null);

		assertEquals("Some English semantic label", labelProvider.getPropertyLabel(propertyMock));
	}

	@Test
	public void getPropertyLabel_shouldFallBackToSemanticIfLabelDefinitionIsNotDefined() {
		setGermanAsSystemLanguage();
		withSemanticLabel("en", "Some English semantic label");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();

		assertEquals("Some English semantic label", labelProvider.getPropertyLabel(propertyMock));
	}

	/// Tooltip methods

	@Test
	public void getPropertyTooltip_shouldReturnDefinitionLabelWhenPresent() {
		setGermanAsSystemLanguage();
		withLabelDefinition("Some English definition label", "German definition label");
		withSemanticDescription("de", "Some German semantic label");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();

		assertEquals("German definition label", labelProvider.getPropertyLabel(propertyMock));
	}

	@Test
	public void getPropertyTooltip_shouldReturnSemanticLabelIfDefinitionIsNotPresent_noLanguageProvider() {
		setGermanAsSystemLanguage();
		withLabelDefinition("Some English definition label", null);
		withSemanticDescription("de", "Some German semantic label");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();

		assertEquals("Some German semantic label", labelProvider.getPropertyTooltip(propertyMock));
	}

	@Test
	public void getPropertyTooltip_shouldReturnDefinitionLabelForDefaultLanguageWhenRequestedAndSemanticAreNotPresent() {
		setGermanAsSystemLanguage();
		withLabelDefinition("Some English definition label", null);
		withSemanticDescription("en", "Some English semantic label");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();

		assertEquals("Some English definition label", labelProvider.getPropertyTooltip(propertyMock));
	}

	@Test
	public void getPropertyTooltip_shouldReturnSemanticLabelForDefaultLanguageWhenRequestedAndDefinitionAreNotPresent() {
		setGermanAsSystemLanguage();
		withLabelDefinition(null, null);
		withSemanticDescription("en", "Some English semantic label");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();

		assertEquals("Some English semantic label", labelProvider.getPropertyTooltip(propertyMock));
	}

	@Test
	public void getPropertyTooltip_shouldReturnLabelIdWhenNoLabelAreDefined() {
		setGermanAsSystemLanguage();
		withLabelDefinition(null, null);
		withSemanticDescription("en", "");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();

		assertEquals("emf:references.tooltip/labelId", labelProvider.getPropertyTooltip(propertyMock));
	}

	@Test
	public void getPropertyTooltip_shouldReturnDefaultLanguageLabelIfNotSemanticProperty() {
		setGermanAsSystemLanguage();
		withLabelDefinition("Some English definition label", null);
		withSemanticDescription("en", "");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();
		propertyMock.setUri(null);

		assertEquals("Some English definition label", labelProvider.getPropertyTooltip(propertyMock));
	}

	@Test
	public void getPropertyTooltip_shouldFallBackToSemanticIfLabelIdIsNotDefined() {
		setGermanAsSystemLanguage();
		withLabelDefinition("Some English definition label", null);
		withSemanticDescription("en", "Some English semantic label");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();
		propertyMock.setTooltipId(null);

		assertEquals("Some English semantic label", labelProvider.getPropertyTooltip(propertyMock));
	}

	@Test
	public void getPropertyTooltip_shouldFallBackToSemanticIfLabelDefinitionIsNotDefined() {
		setGermanAsSystemLanguage();
		withSemanticDescription("en", "Some English semantic label");

		PropertyDefinitionMock propertyMock = createPropertyDefinition();

		assertEquals("Some English semantic label", labelProvider.getPropertyTooltip(propertyMock));
	}

	private PropertyDefinitionMock createPropertyDefinition() {
		PropertyDefinitionMock propertyMock = new PropertyDefinitionMock();
		propertyMock.setIdentifier("someProperty");
		propertyMock.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));
		propertyMock.setType(DataTypeDefinition.URI);
		propertyMock.setUri(PROPERTY_URI);
		propertyMock.setLabelId(LABEL_ID);
		propertyMock.setTooltipId(LABEL_ID);
		return propertyMock;
	}

	private void withLabelDefinition(String engLabel, String germanLabel) {
		LabelDefinition labelDefinition = mock(LabelDefinition.class);
		Map<String, String> map = new HashMap<>();
		CollectionUtils.addNonNullValue(map, "en", engLabel);
		CollectionUtils.addNonNullValue(map, "de", germanLabel);
		when(labelDefinition.getLabels()).thenReturn(map);
		when(labelService.getLabel(LABEL_ID)).thenReturn(labelDefinition);
	}

	private void mockBundleLanguage(Set<String> keys) {
		keys.forEach(key -> mockBundleLanguage("en", Collections.singletonMap(key, "label")));
	}

	private void withSemanticLabel(String lang, String label) {
		mockBundleLanguage(lang, Collections.singletonMap(LabelProvider.buildUriLabelId(PROPERTY_URI), label));
	}
	private void withSemanticDescription(String lang, String label) {
		mockBundleLanguage(lang, Collections.singletonMap(LabelProvider.buildUriTooltipId(PROPERTY_URI), label));
	}

	private void mockBundleLanguage(String locale, Map<String, String> keyValues) {
		ResourceBundle bundle = new ResourceBundle() {
			@Override
			protected Object handleGetObject(String key) {
				return keyValues.get(key);
			}

			@Override
			protected Set<String> handleKeySet() {
				return keyValues.keySet();
			}

			@Override
			public Enumeration<String> getKeys() {
				return Collections.emptyEnumeration();
			}
		};
		when(bundleProvider.getLabelResolver(Locale.forLanguageTag(locale))).thenReturn(LabelResolver.wrap(bundle));
	}

	private void setEnglishAsSystemLanguage() {
		reset(userPreferences);
		when(userPreferences.getLanguage()).thenReturn("en");
	}

	private void setGermanAsSystemLanguage() {
		reset(userPreferences);
		when(userPreferences.getLanguage()).thenReturn("de");
	}
}
