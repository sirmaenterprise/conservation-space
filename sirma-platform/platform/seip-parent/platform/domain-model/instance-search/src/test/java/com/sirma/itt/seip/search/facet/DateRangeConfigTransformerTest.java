package com.sirma.itt.seip.search.facet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.testutil.mocks.ControlDefintionMock;
import com.sirma.itt.seip.testutil.mocks.ControlParamMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.time.DateOffset;
import com.sirma.itt.seip.time.DateOffsetKeys;
import com.sirma.itt.seip.time.DateRangeConfig;

/**
 * Tests the logic in {@link DateRangeConfigTransformer}.
 *
 * @author Mihail Radkov
 */
public class DateRangeConfigTransformerTest {

	/** The class under tests. */
	private DateRangeConfigTransformer transformer;

	/**
	 * Creates new transformer before every test to ensure equal conditions.
	 */
	@BeforeTest
	public void beforeTest() {
		transformer = new DateRangeConfigTransformer();
	}

	/**
	 * Tests the logic behind {@link DateRangeConfigTransformer#extractDateRanges(List)} when no ranges are provided.
	 */
	@Test
	public void testWithoutFields() {
		List<DateRangeConfig> ranges = transformer.extractDateRanges(null);
		Assert.assertEquals(0, ranges.size());

		ranges = transformer.extractDateRanges(CollectionUtils.<PropertyDefinition> emptyList());
		Assert.assertEquals(0, ranges.size());
	}

	/**
	 * Tests the logic behind {@link DateRangeConfigTransformer#extractDateRanges(List)} when a range is provided but
	 * without an offset control definition.
	 */
	@Test
	public void testExtractionWithoutOffsetControl() {
		List<PropertyDefinition> rangeFields = new ArrayList<>();
		PropertyDefinitionMock todayField = new PropertyDefinitionMock();
		todayField.setName("today");
		todayField.setOrder(100);
		rangeFields.add(todayField);

		List<DateRangeConfig> ranges = transformer.extractDateRanges(rangeFields);
		Assert.assertEquals(0, ranges.size());
	}

	/**
	 * Tests the logic behind {@link DateRangeConfigTransformer#extractDateRanges(List)} when all fields and controls
	 * are correctly set.
	 */
	@Test
	public void testWithCorrectlyProvidedFields() {
		List<PropertyDefinition> rangeFields = new ArrayList<>();

		PropertyDefinitionMock todayField = new PropertyDefinitionMock();
		todayField.setName("today");
		todayField.setOrder(100);
		todayField.setLabelId("date.today");
		todayField.setLabelProvider(new UpperCaseTestLabelProvider());

		ControlDefintionMock offsetsControl = new ControlDefintionMock();

		PropertyDefinitionMock todayStartField = new PropertyDefinitionMock();
		todayStartField.setName("start");
		PropertyDefinitionMock todayEndField = new PropertyDefinitionMock();
		todayEndField.setName("end");
		PropertyDefinitionMock includedRangesField = new PropertyDefinitionMock();
		includedRangesField.setName("includedRanges");

		ControlDefintionMock todayStartOffsetControl = new ControlDefintionMock();
		ControlDefintionMock todayEndOffsetControl = new ControlDefintionMock();
		ControlDefintionMock todayincludedRangesControl = new ControlDefintionMock();

		// Offsets for today's start
		List<ControlParam> todayStartOffsets = new ArrayList<>();
		todayStartOffsets.add(getControlParameter("yearOffset", "1"));
		todayStartOffsets.add(getControlParameter("dayOffset", "-2"));
		todayStartOffsets.add(getControlParameter("hourOffset", "3"));
		todayStartOffsets.add(getControlParameter("minuteOffset", "-4"));
		todayStartOffsets.add(getControlParameter("msOffset", "5"));
		todayStartOffsetControl.setControlParams(todayStartOffsets);
		todayStartField.setControlDefinition(todayStartOffsetControl);

		// Offsets for today's end
		List<ControlParam> todayEndOffsets = new ArrayList<>();
		todayEndOffsets.add(getControlParameter("yearOffset", "11"));
		todayEndOffsets.add(getControlParameter("blaaa", "-6"));
		todayEndOffsetControl.setControlParams(todayEndOffsets);
		todayEndField.setControlDefinition(todayEndOffsetControl);

		// Included ranges
		List<ControlParam> includedRanges = new ArrayList<>();
		includedRanges.add(getControlParameter("yesterday", null));
		includedRanges.add(getControlParameter(null, null));
		todayincludedRangesControl.setControlParams(includedRanges);
		includedRangesField.setControlDefinition(todayincludedRangesControl);

		// Non supported field
		PropertyDefinitionMock nonSupportedField = new PropertyDefinitionMock();
		nonSupportedField.setName("nonSupportedField");

		offsetsControl.setFields(Arrays.asList((PropertyDefinition) todayStartField, todayEndField, includedRangesField,
				nonSupportedField));
		todayField.setControlDefinition(offsetsControl);
		rangeFields.add(todayField);

		List<DateRangeConfig> ranges = transformer.extractDateRanges(rangeFields);

		Assert.assertEquals(1, ranges.size());

		DateRangeConfig rangeConfig = ranges.get(0);

		Assert.assertEquals("today", rangeConfig.getId());
		Assert.assertEquals(100, rangeConfig.getOrder());
		Assert.assertEquals("date.today", rangeConfig.getLabelId());
		Assert.assertEquals("DATE.TODAY", rangeConfig.getLabel());

		DateOffset startOffset = rangeConfig.getStartOffset();
		Assert.assertNotNull(startOffset);
		Assert.assertEquals(Integer.valueOf(1), startOffset.get(DateOffsetKeys.YEAR));
		Assert.assertEquals(Integer.valueOf(-2), startOffset.get(DateOffsetKeys.DAY));
		Assert.assertEquals(Integer.valueOf(3), startOffset.get(DateOffsetKeys.HOUR));
		Assert.assertEquals(Integer.valueOf(-4), startOffset.get(DateOffsetKeys.MINUTE));
		Assert.assertEquals(Integer.valueOf(5), startOffset.get(DateOffsetKeys.MS));

		DateOffset endOffset = rangeConfig.getEndOffset();
		Assert.assertNotNull(endOffset);
		Assert.assertEquals(Integer.valueOf(11), endOffset.get(DateOffsetKeys.YEAR));

		List<String> includedRangesIds = rangeConfig.getIncludedRangesIds();
		Assert.assertEquals(1, includedRangesIds.size());
		String includedRange = includedRangesIds.get(0);
		Assert.assertEquals("yesterday", includedRange);

	}

	/**
	 * Constructs a control parameter out of the provided name and value.
	 *
	 * @param name
	 *            the provided name
	 * @param value
	 *            the provided value
	 * @return the constructed control parameter
	 */
	private ControlParam getControlParameter(String name, String value) {
		ControlParamMock controlParam = new ControlParamMock();
		controlParam.setName(name);
		controlParam.setValue(value);
		return controlParam;
	}

	/**
	 * Custom label provider used in the above test.
	 *
	 * @author Mihail Radkov
	 */
	private class UpperCaseTestLabelProvider implements LabelProvider {
		@Override
		public String getLabel(String labelId) {
			return labelId.toUpperCase();
		}

		@Override
		public String getLabel(String labelId, String language) {
			return null;
		}

		@Override
		public String getValue(String key) {
			return null;
		}

		@Override
		public String getBundleValue(String key) {
			return null;
		}

		@Override
		public String getPropertyLabel(PropertyDefinition propertyDefinition) {
			return null;
		}

		@Override
		public String getPropertyTooltip(PropertyDefinition propertyDefinition) {
			return null;
		}
	}
}
