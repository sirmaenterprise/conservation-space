package com.sirma.itt.seip.definition.label;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.model.LabelImpl;
import com.sirma.itt.seip.model.SerializableValue;

/**
 * @author Mihail Radkov
 */
public class LabelServiceImplTest {

	@Mock
	private DbDao dbDao;

	@Mock
	private EntityLookupCacheContext cacheContext;

	@InjectMocks
	private LabelServiceImpl labelService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldQueryLabelsDefinedIn() {
		LabelImpl labelWithoutValues = new LabelImpl();
		labelWithoutValues.setIdentifier("label.test");

		LabelImpl labelWithEmptyValues = new LabelImpl();
		labelWithEmptyValues.setIdentifier("label.test.empty");
		labelWithEmptyValues.setValue(new SerializableValue());

		HashMap<String, String> labels = new HashMap<>();
		labels.put("en", "Label");
		labels.put("bg", "Етикет");

		LabelImpl labelWithValues = new LabelImpl();
		labelWithValues.setIdentifier("label.test.values");
		labelWithValues.setValue(new SerializableValue(labels));

		when(dbDao.fetchWithNamed(any(), any())).thenReturn(Arrays.asList(labelWithoutValues, labelWithEmptyValues, labelWithValues));

		List<LabelDefinition> labelDefinitions = labelService.getLabelsDefinedIn("some_definition");
		assertNotNull(labelDefinitions);
		assertEquals(3, labelDefinitions.size());

		assertEquals("label.test", labelDefinitions.get(0).getIdentifier());
		assertTrue(CollectionUtils.isEmpty(labelDefinitions.get(0).getLabels()));

		assertEquals("label.test.empty", labelDefinitions.get(1).getIdentifier());
		assertTrue(CollectionUtils.isEmpty(labelDefinitions.get(1).getLabels()));

		assertEquals("label.test.values", labelDefinitions.get(2).getIdentifier());
		assertFalse(CollectionUtils.isEmpty(labelDefinitions.get(2).getLabels()));
		assertEquals(labels, labelDefinitions.get(2).getLabels());
	}

}
