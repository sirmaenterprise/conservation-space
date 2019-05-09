package com.sirma.itt.seip.instance.validator.validators;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.instance.validation.DynamicCodeListFilter;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.PropertyFieldValidator;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.instance.validator.exceptions.InstanceValidationException;

/**
 * Class that contains logic that validates code list {@link com.sirma.itt.seip.domain.instance.Instance} fields. Here
 * we take into consideration if the value set for the field is actually valid. This is done by:
 * <ul>
 * <li>Validate if the value exists in the code list server.</li>
 * <li>Validate if the value should be filtered from a definition filter.</li>
 * <li>Validate if the value is filtered from a condition from another field.</li>
 * </ul>
 * <p/>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
@Extension(target = PropertyFieldValidator.TARGET_NAME, order = 7)
public class CodelistFieldValidator extends PropertyFieldValidator {

	@Inject
	private CodelistService codelistService;

	@Inject
	private FieldValidationErrorBuilder builder;

	@Override
	public Stream<PropertyValidationError> validate(FieldValidationContext context) {
		Map<String, CodeValue> codeValues = getCodeValues(context);
		return collectValues(context).map(value -> {
			if (!(value instanceof String)) {
				return builder.buildCodelistFieldError(context.getPropertyDefinition(), value.toString(), codeValues);
			}
			if (!codeValues.containsKey(value)) {
				return builder.buildCodelistFieldError(context.getPropertyDefinition(), value.toString(), codeValues);
			}
			return null;
		}).filter(Objects::nonNull);
	}

	@Override
	public boolean isApplicable(FieldValidationContext context) {
		return PropertyDefinition.hasCodelist().test(context.getPropertyDefinition()) && context.getValue() != null;
	}

	private Map<String, CodeValue> getCodeValues(FieldValidationContext context) {
		Integer codeList = context.getPropertyDefinition().getCodelist();

		// handles filters that are set directly in the definition
		Set<String> filters = context.getPropertyDefinition().getFilters();
		if (CollectionUtils.isNotEmpty(filters)) {
			return codelistService.getFilteredCodeValues(codeList, filters.toArray(new String[filters.size()]));
		}

		// Handles code list filters that are set through conditions.
		DynamicCodeListFilter filter = context.getDynamicClFilters().get(context.getPropertyDefinition().getName());
		if (filter != null) {
			validateMandatoryParams(filter, context.getPropertyDefinition());
			// Filter a field depending on supplied custom filter
			if (filter.getReRenderFieldName().equals(filter.getSourceFilterFieldName())) {
				String[] values = filter.getFilterSource().replaceAll("\\s+", "").split(",");
				return codelistService.filterCodeValues(codeList, filter.isInclusive(), Arrays.asList(values));
			}
			Collection<String> values = filter.getValues();
			String[] valuesArray = values.toArray(new String[values.size()]);
			// Filter a field depending on another field value (restrictions are described in codelist "extra" columns)
			return codelistService.filterCodeValues(codeList, filter.isInclusive(), filter.getFilterSource(),
					valuesArray);
		}
		return codelistService.getFilteredCodeValues(codeList);
	}
	
	private void validateMandatoryParams(DynamicCodeListFilter filter, PropertyDefinition property) {
		if (!filter.isFilterValid()) {
			throw new InstanceValidationException(builder.buildMandatoryControlParamError(property).toString());
		}
	}
}
