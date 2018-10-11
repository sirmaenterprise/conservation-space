package com.sirma.itt.seip.search.facet;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.time.DateOffset;
import com.sirma.itt.seip.time.DateOffsetKeys;
import com.sirma.itt.seip.time.DateRangeConfig;

/**
 * Provides a way to parse and extract {@link DateRangeConfig} from {@link PropertyDefinition} if declared correctly.
 *
 * @author Mihail Radkov
 */
@Singleton
public class DateRangeConfigTransformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String START_FIELD = "start";
	private static final String END_FIELD = "end";
	private static final String INCLUDED_RANGES_FIELD = "includedRanges";

	/**
	 * Extracts any declared date ranges in the given property definition fields.
	 *
	 * @param rangeFields
	 *            - the given fields
	 * @return the extracted date ranges or empty list if no were present
	 */
	public List<DateRangeConfig> extractDateRanges(List<PropertyDefinition> rangeFields) {
		if (CollectionUtils.isEmpty(rangeFields)) {
			LOGGER.warn("No date ranges were provided for extraction.");
			return CollectionUtils.emptyList();
		}

		List<DateRangeConfig> extractedRanges = new ArrayList<>(rangeFields.size());

		for (PropertyDefinition rangeField : rangeFields) {
			DateRangeConfig dateRangeConfig = new DateRangeConfig();

			dateRangeConfig.setId(rangeField.getName());
			dateRangeConfig.setOrder(rangeField.getOrder());
			dateRangeConfig.setLabel(rangeField.getLabel());
			dateRangeConfig.setLabelId(rangeField.getLabelId());

			ControlDefinition rangeControlDefinition = rangeField.getControlDefinition();
			if (rangeControlDefinition == null) {
				LOGGER.warn("Date range field '{}' needs a control with fields!", rangeField.getName());
				continue;
			}

			List<PropertyDefinition> offsetFields = rangeControlDefinition.getFields();
			for (PropertyDefinition offsetField : offsetFields) {
				populateOffsets(dateRangeConfig, offsetField);
			}

			extractedRanges.add(dateRangeConfig);
		}

		return extractedRanges;
	}

	/**
	 * Extracts the date offset information from the provided property definition and populates the given date range
	 * configuration with it.
	 *
	 * @param dateRangeConfig
	 *            - the given date range
	 * @param offsetField
	 *            - the property definition with declared offsets
	 */
	private void populateOffsets(DateRangeConfig dateRangeConfig, PropertyDefinition offsetField) {
		if (START_FIELD.equalsIgnoreCase(offsetField.getName())) {
			ControlDefinition startOffsetControl = offsetField.getControlDefinition();
			DateOffset startOffset = getDateOffset(startOffsetControl);
			dateRangeConfig.setStartOffset(startOffset);
		} else if (END_FIELD.equalsIgnoreCase(offsetField.getName())) {
			ControlDefinition endOffsetControl = offsetField.getControlDefinition();
			DateOffset endOffset = getDateOffset(endOffsetControl);
			dateRangeConfig.setEndOffset(endOffset);
		} else if (INCLUDED_RANGES_FIELD.equalsIgnoreCase(offsetField.getName())) {
			ControlDefinition includedRangesContol = offsetField.getControlDefinition();
			List<String> includedRangesIds = getIncludedRanges(includedRangesContol);
			dateRangeConfig.setIncludedRangesIds(includedRangesIds);
		} else {
			LOGGER.warn("The field '{}' is not supported for parsing.", offsetField.getName());
		}
	}

	/**
	 * Creates a new date offset based on the defined control parameters in the provided control definition.
	 *
	 * @param control
	 *            - the provided control definition
	 * @return - the constructed date offset
	 */
	private DateOffset getDateOffset(ControlDefinition control) {
		DateOffset offset = new DateOffset();
		List<ControlParam> controlParams = control.getControlParams();
		for (ControlParam param : controlParams) {
			String name = param.getName();
			String value = param.getValue();
			// TODO: ENUM ?
			switch (name) {
				case DateOffsetKeys.YEAR:
					offset.put(name, Integer.parseInt(value));
					break;
				case DateOffsetKeys.DAY:
					offset.put(name, Integer.parseInt(value));
					break;
				case DateOffsetKeys.HOUR:
					offset.put(name, Integer.parseInt(value));
					break;
				case DateOffsetKeys.MINUTE:
					offset.put(name, Integer.parseInt(value));
					break;
				case DateOffsetKeys.MS:
					offset.put(name, Integer.parseInt(value));
					break;
				default:
					LOGGER.warn("The control parameter '{}' is not supported for parsing", name);
					break;
			}
		}
		return offset;
	}

	/**
	 * Collects the declared included ranges in the given control definition.
	 *
	 * @param includedRangesContol
	 *            - the given control definition
	 * @return the collected included ranges
	 */
	private List<String> getIncludedRanges(ControlDefinition includedRangesContol) {
		List<ControlParam> includedRanges = includedRangesContol.getControlParams();
		List<String> includedRangesIds = new ArrayList<>(includedRanges.size());
		for (ControlParam includedRange : includedRanges) {
			String name = includedRange.getName();
			if (StringUtils.isNotBlank(name)) {
				includedRangesIds.add(name);
			}
		}
		return includedRangesIds;
	}
}
