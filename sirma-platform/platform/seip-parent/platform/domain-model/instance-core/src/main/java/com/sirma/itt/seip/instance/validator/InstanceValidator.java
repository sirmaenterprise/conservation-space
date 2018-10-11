package com.sirma.itt.seip.instance.validator;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.seip.concurrent.collections.FixedBatchSpliterator;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.conditions.ConditionType;
import com.sirma.itt.seip.expressions.conditions.ConditionsManager;
import com.sirma.itt.seip.instance.properties.RichtextPropertiesHelper;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.InstanceValidationResult;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.time.TimeTracker;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does the actual instance validation. The service's only entry point gets the instance definition and executes the
 * following actions:
 * <ul>
 * <li>Collects the mandatory fields even those that are mandatory because of condition definition.</li>
 * <li>Collects the dynamic cl filters. This means when one cl field is filtered from another.</li>
 * <li>Gets all the definition fields and iterates them. Foe each field executes validation and if there's an error
 * collects it.</li>
 * </ul>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class InstanceValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstanceValidator.class);

	private static final int MAX_PARALLEL_FIELDS_PROCESSED = 20;
	private static final String RICHTEXT = "RICHTEXT";

	@Inject
	private DefinitionService definitionService;
	@Inject
	private DefinitionPropertyValidationService propertyValidationService;
	@Inject
	private StateService stateService;
	@Inject
	private StateTransitionManager stateTransitionManager;
	@Inject
	private ConditionsManager conditionsManager;
	@Inject
	private SecurityContextManager securityContextManager;

	/**
	 * Validates all fields in an instance for correctness.
	 *
	 * @param context object which contains the data needed for the validations see
	 * {@link ValidationContext}
	 * @return the result of the validation. A class that contains a list of
	 * failed validations if those are present.
	 */
	public InstanceValidationResult validate(ValidationContext context) {
		Instance instance = context.getInstance();
		LOGGER.debug("Starting validation of instance with id={} and definition={}", instance.getId(),
				instance.getIdentifier());
		TimeTracker timeTracker = TimeTracker.createAndStart();

		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(instance);

		FieldValidationContext validationContext = new FieldValidationContext();
		validationContext.setMandatoryFields(getMandatoryProperties(instanceDefinition, context));
		validationContext.setOptionalFields(getOptionalProperties(instanceDefinition, context));
		validationContext.setDynamicClFilters(
				DynamicCodelistFiltersExtractor.getDynamicClFilters(instanceDefinition, context.getInstance()));

		Function<PropertyDefinition, List<PropertyValidationError>> validateProperty = securityContextManager.wrap()
				.function(property -> validatePropertyDefinition(instance, property, validationContext));

		List<PropertyValidationError> errors = FixedBatchSpliterator
				.withBatchSize(instanceDefinition.fieldsStream(), MAX_PARALLEL_FIELDS_PROCESSED).map(validateProperty)
				.flatMap(Collection::stream).collect(Collectors.toList());

		InstanceValidationResult result = new InstanceValidationResult(errors);
		LOGGER.debug("Instance with id {} was validated in {}", instance.getId(), timeTracker.stopInSeconds());
		return result;
	}

	/**
	 * Method which returns the optional properties after the condition
	 * evaluation.
	 */
	private Set<String> getOptionalProperties(DefinitionModel model, ValidationContext context) {
		Set<String> result = new HashSet<>();

		Set<String> optionalPropertiesFromConditions = conditionsManager
				.getVerifiedFieldsByType(model, ConditionType.OPTIONAL, context.getInstance())
				.map(PropertyDefinition::getName).collect(Collectors.toSet());
		result.addAll(optionalPropertiesFromConditions);
		return Collections.unmodifiableSet(result);
	}

	/**
	 * Evaluates if a {@link PropertyDefinition} is mandatory. This method
	 * collects mandatory properties from two sources. From conditions and
	 * according to states, see
	 * {@link #getMandatoryFieldsForState(Instance, String)} and
	 * {@link ConditionsManager}.
	 */
	private Set<String> getMandatoryProperties(DefinitionModel model, ValidationContext context) {
		Set<String> result = new HashSet<>();

		// 1. Get all mandatory fields for the current state of the object
		result.addAll(getMandatoryFieldsForState(context.getInstance(), context.getOperation().getOperation()));

		// 2. Evaluate the conditions and get the mandatory properties from
		// there
		Set<String> mandatoryPropertiesFromConditions = conditionsManager
				.getVerifiedFieldsByType(model, ConditionType.MANDATORY, context.getInstance())
				.map(PropertyDefinition::getName).collect(Collectors.toSet());
		result.addAll(mandatoryPropertiesFromConditions);
		return Collections.unmodifiableSet(result);
	}

	private Set<String> getMandatoryFieldsForState(Instance instance, String operation) {
		if (instance == null || StringUtils.isBlank(operation)) {
			return Collections.emptySet();
		}
		String primaryState = stateService.getPrimaryState(instance);
		return stateTransitionManager.getRequiredFields(instance, primaryState, operation);
	}

	private List<PropertyValidationError> validatePropertyDefinition(Instance instance, PropertyDefinition property,
			FieldValidationContext validationContext) {
		Serializable value = sanitizeValue(instance.get(property.getName()), property);
		FieldValidationContext context = validationContext.copy().setPropertyDefinition(property).setValue(value)
				.setInstance(instance);
		return propertyValidationService.validate(context);
	}
	
	private static Serializable sanitizeValue(Serializable value, PropertyDefinition property) {
		if (value != null && isRichTextField(property)) {
			return RichtextPropertiesHelper.stripHTML((String) value);
		}
		return value;
	}
	
	private static boolean isRichTextField(PropertyDefinition property) {
		return PropertyDefinition.hasControl(RICHTEXT).test(property);
	}

}