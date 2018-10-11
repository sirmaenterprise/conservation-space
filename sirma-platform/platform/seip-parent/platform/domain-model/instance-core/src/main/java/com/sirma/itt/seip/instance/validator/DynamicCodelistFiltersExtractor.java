package com.sirma.itt.seip.instance.validator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.validation.DynamicCodeListFilter;

/**
 * Helper class used to extract all dynamic code lists filters from a definition model.
 * <p/>
 * Dynamic code list filters are those that are specified with control-param tags. For example:
 * <pre>
 * {@code
 * <control id="RELATED_FIELDS">
 *    <control-param type="related_field1" id="fieldsToRerender" name="RERENDER">functional</control-param>
 *    <control-param type="related_field1" id="filterSource" name="FILTER_SOURCE">extra1</control-param>
 *    <control-param type="related_field1" id="filterInclusive" name="FILTER_SOURCE">true</control-param>
 *
 *    <control-param type="related_field2" id="fieldsToRerender" name="RERENDER">documentISOType</control-param>
 *    <control-param type="related_field2" id="filterSource" name="FILTER_SOURCE">extra1</control-param>
 *    <control-param type="related_field2" id="filterInclusive" name="INCLUSIVE">true</control-param>
 * </control>
 * }
 * </pre>
 * In order a filter to be valid each should have control-param 3 tags with names respectively - RERENDER,
 * FILTER_SOURCE and INCLUSIVE.
 * <ul>
 * <li>RERENDER - points which other field should we filter based on the current filed;</li>
 * <li>FILTER_SOURCE - shows which code list part should be used for filtering (for example extra1);</li>
 * <li>INCLUSIVE - if the filtering should include or exclude the values.</li>
 * </ul>
 * <p>
 * In this control we can see that there are two filters which are distinguished by their type. Besides the code
 * list filters, there can be other control-params (for example for the default value suggest). This class can traverse
 * the all control-params and extracts all the <b>valid</b> filters.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 08/11/2017
 */
public class DynamicCodelistFiltersExtractor {

	private static final String RE_RENDER_PARAM_NAME = "RERENDER";
	private static final String FILTER_SOURCE_PARAM_NAME = "FILTER_SOURCE";
	private static final String RELATED_FIELDS_PARAM_NAME = "RELATED_FIELDS";
	private static final String INCLUSIVE = "INCLUSIVE";

	private DynamicCodelistFiltersExtractor() {
		// Disallow instantiation.
	}

	/**
	 * Extracts the dynamic code list filters for a definition.
	 *
	 * @param model the definition model.
	 * @param instance the instance, which we need to extract the value for the filer.
	 * @return a map containing as a key the field which should be filtered. The values in the map contains
	 * {@link DynamicCodeListFilter} objects.
	 * @see DynamicCodeListFilter
	 */
	static Map<String, DynamicCodeListFilter> getDynamicClFilters(DefinitionModel model,
			Instance instance) {
		return model.fieldsStream()
				.filter(PropertyDefinition.hasCodelist().and(hasControlDefinitionWithRelatedFields()))
				.map(propertyDefinition -> getFiltersForSinglePropertyDefinition(propertyDefinition, instance))
				.filter(filters -> !filters.isEmpty())
				.flatMap(map -> map.entrySet().stream())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private static Predicate<PropertyDefinition> hasControlDefinitionWithRelatedFields() {
		return propertyDefinition -> propertyDefinition.getControlDefinition() != null
				&& RELATED_FIELDS_PARAM_NAME.equalsIgnoreCase(
				propertyDefinition.getControlDefinition().getIdentifier());
	}

	/**
	 * Gets the dynamic filter(s) for a single field from the definition model.
	 */
	private static Map<String, DynamicCodeListFilter> getFiltersForSinglePropertyDefinition(
			PropertyDefinition propertyDefinition,
			Instance instance) {
		// This logic handles fields that have multiple filters. Basically we group the filter controls by their
		// types. For example codelist filters for control params with one and the same type form one filter.
		Map<String, List<ControlParam>> controlParamsByTheirType = new HashMap<>();
		propertyDefinition.getControlDefinition().getControlParams()
				.stream()
				.filter(isControlPartOfClFilter())
				.forEach(controlParam -> insertFilterControl(controlParamsByTheirType, controlParam));

		return controlParamsByTheirType.keySet()
				.stream()
				.map(controlParamsByTheirType::get)
				.map(list -> getDynamicFilter(propertyDefinition, list, instance))
				.filter(DynamicCodeListFilter::isFilterValid)
				.collect(Collectors.toMap(DynamicCodeListFilter::getReRenderFieldName, Function.identity()));
	}

	// We can have control-params that are used for different things. When the control has a name from one of those
	// then it is a part of CL filter.
	private static Predicate<ControlParam> isControlPartOfClFilter() {
		return controlParam -> RE_RENDER_PARAM_NAME.equalsIgnoreCase(controlParam.getName()) || FILTER_SOURCE_PARAM_NAME
				.equalsIgnoreCase(controlParam.getName()) || INCLUSIVE.equalsIgnoreCase(controlParam
				.getName());
	}

	/**
	 * Inserts a filter control to the map given as argument based on the control type field.
	 */
	private static void insertFilterControl(Map<String, List<ControlParam>> map, ControlParam param) {
		if (map.containsKey(param.getType())) {
			map.get(param.getType()).add(param);
		} else {
			List<ControlParam> params = new ArrayList<>();
			params.add(param);
			map.put(param.getType(), params);
		}
	}

	/**
	 * Constructs a dynamic filter from control-params. This is called with control-params which have one and the
	 * same type.
	 */
	private static DynamicCodeListFilter getDynamicFilter(PropertyDefinition property, List<ControlParam>
			controlParameters, Instance instance) {
		// Object used to store linked CLS fields.
		DynamicCodeListFilter clFilter = new DynamicCodeListFilter();
		for (ControlParam parameter : controlParameters) {
			if (RE_RENDER_PARAM_NAME.equalsIgnoreCase(parameter.getName())) {
				clFilter.setReRenderFieldName(parameter.getValue());
			} else if (FILTER_SOURCE_PARAM_NAME.equalsIgnoreCase(parameter.getName())) {
				clFilter.setFilterSource(parameter.getValue());
				clFilter.setValues(getFieldValues(instance, property.getName()));
			} else if (INCLUSIVE.equalsIgnoreCase(parameter.getName())) {
				clFilter.setInclusive(Boolean.parseBoolean(parameter.getValue().toLowerCase()));
			}
		}
		return clFilter;
	}

	/**
	 * Gets from the instance the values, based on which we should filter.
	 */
	private static Collection<String> getFieldValues(Instance instance, String propertyName) {
		Serializable value = instance.get(propertyName);
		if (value instanceof Collection) {
			//noinspection unchecked
			return (Collection) value;
		}
		// if the source value is null then no filtering should be applied (the
		// value from which we want to filter is
		// empty). We return an empty list so that the filter can be ignored.
		return value != null ? Collections.singletonList(value.toString()) : Collections.emptyList();
	}
}
