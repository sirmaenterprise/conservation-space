package com.sirma.itt.emf.label;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.definition.label.LabelBundleProvider;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.expressions.ExpressionEvaluatorManager;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Test the label provider.
 *
 * @author nvelkov
 */
public class LabelProviderTest {

	@Mock
	private LabelService labelService;

	@Mock
	private ExpressionEvaluatorManager manager;

	@Mock
	private UserPreferences userPreferences;

	@Mock
	private Iterable<LabelBundleProvider> labelProviders;

	/** Provided resource bundles mapped by language */
	@Mock
	private ContextualMap<String, List<ResourceBundle>> bundles;

	@InjectMocks
	private LabelProvider labelProvider = new LabelProviderImpl();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		mockLanguage();
	}

	/**
	 * Test the get bundle value method.
	 */
	@Test
	public void testGetBundleValue() {
		mockBundles(new HashSet<>(Arrays.asList("labelId")));
		assertEquals("label", labelProvider.getBundleValue("labelId"));
	}

	/**
	 * Test the get bundle value method with a missing label.
	 */
	@Test
	public void testGetBundleValueMissingLabel() {
		mockBundles(new HashSet<>(Arrays.asList("anotherLabel")));
		assertEquals("labelId", labelProvider.getBundleValue("labelId"));
	}

	@Test
	public void testGetLabel_Should_ReturnNoLabel_When_LabelIdIsNull() {
		mockBundles(new HashSet<>(Arrays.asList("anotherLabel")));
		assertEquals(LabelProvider.NO_LABEL, labelProvider.getLabel(null));
	}

	@Test
	public void testGetLabel_Should_ReturnPassedLabelId_When_NoLabelDefined() {
		mockBundles(new HashSet<>(Arrays.asList("anotherLabel")));
		assertEquals("notExistingLabel", labelProvider.getLabel("notExistingLabel"));
	}

	@Test
	public void testGetLabel_Should_ReturnLabel_When_LabelIsDefined() {
		mockBundles(new HashSet<>(Arrays.asList("anotherLabel")));
		assertEquals("label", labelProvider.getLabel("anotherLabel"));
	}

	@Test
	public void testGetLabel_Should_EvaluateLabel_When_IsExpression() {
		String labelId = "expLabel";
		String evaluatedLabel = "evaluated";

		mockBundles(new HashSet<>(Arrays.asList("anotherLabel")));
		when(manager.isExpression(labelId)).thenReturn(Boolean.TRUE);
		when(manager.evaluate(labelId, String.class)).thenReturn(evaluatedLabel);
		when(manager.isExpression(evaluatedLabel)).thenReturn(Boolean.FALSE);

		assertEquals(evaluatedLabel, labelProvider.getLabel(labelId));
	}

	@Test
	public void testGetLabel_Should_ReturnLabelId_When_ExpressionEvaluatedNull() {
		String labelId = "expLabel";

		mockBundles(new HashSet<>(Arrays.asList("anotherLabel")));
		when(manager.isExpression(labelId)).thenReturn(Boolean.TRUE);

		assertEquals(labelId, labelProvider.getLabel(labelId));
	}

	@Test
	public void testGetLabel_Should_ReturnLabelId_When_ExpressionEvaluatedAnotherExpression() {
		String labelId = "expLabel";
		String evaluatedLabel = "expression";

		mockBundles(new HashSet<>(Arrays.asList("anotherLabel")));
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

		mockBundles(new HashSet<>(Arrays.asList("anotherLabel")));
		when(labelDefinition.getLabels()).thenReturn(labels);
		when(labelService.getLabel(labelId)).thenReturn(labelDefinition);

		assertEquals(labelValue, labelProvider.getLabel(labelId));
	}

	@Test
	public void testGetLabel_Should_ReturnLabelFromBundle_When_ThereIsNoLabelDefinition() {
		String labelId = "labelId";
		LabelDefinition labelDefinition = mock(LabelDefinition.class);

		mockBundles(new HashSet<>(Arrays.asList(labelId)));
		when(labelDefinition.getLabels()).thenReturn(new HashMap<>());
		when(labelService.getLabel(labelId)).thenReturn(labelDefinition);

		assertEquals("label", labelProvider.getLabel(labelId));
	}

	@SuppressWarnings("unchecked")
	private void mockBundles(Set<String> keys) {
		ResourceBundle bundle = new ResourceBundle() {
			@Override
			protected Object handleGetObject(String key) {
				return "label";
			}

			@Override
			protected Set<String> handleKeySet() {
				return keys;
			}

			@Override
			public Enumeration<String> getKeys() {
				return Collections.emptyEnumeration();
			}
		};
		when(bundles.computeIfAbsent(Matchers.anyString(), Matchers.any(Function.class))).thenReturn(
				Arrays.asList(bundle));
	}

	private void mockLanguage() {
		when(userPreferences.getLanguage()).thenReturn("en");
	}
}
