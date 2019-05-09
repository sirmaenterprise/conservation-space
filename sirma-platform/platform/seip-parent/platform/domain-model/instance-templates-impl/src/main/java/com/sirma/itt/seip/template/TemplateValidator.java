package com.sirma.itt.seip.template;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT;
import static com.sirma.itt.seip.template.TemplateProperties.PURPOSE;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.template.rules.TemplateRuleUtils;
import com.sirma.itt.seip.template.utils.TemplateUtils;

/**
 * Validates input template data. </br>
 * <p>
 * <b>Note:</b> The functionality is tested integrated in TemplateServiceImplTest from the entry point - the
 * templates reload.
 *
 * @author Vilizar Tsonev
 */
public class TemplateValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String EMAIL_TEMPLATE_TYPE = "emailTemplate";

	private static final String MISSING_FIELD_ERROR = "Field=%s in type=%s for template=%s is missing";
	private static final String INVALID_FIELD_ERROR = "Field=%s in type=%s for template=%s is not of code list type";
	private static final String MISSING_VALUE_ERROR = "Code value=%s for field=%s in type=%s for template=%s is missing or disabled";

	private final CodelistService codelistService;

	@Inject
	public TemplateValidator(CodelistService codelistService) {
		this.codelistService = codelistService;
	}

	/**
	 * Verifies that the given templates are valid.</br>
	 * The following validations are performed:
	 * <ul>
	 * <li>that there are no missing mandatory fields and the titles correspond to the identifiers.</li>
	 * <li>that there are no duplicated corresponding instances in 2 or more templates.</li>
	 * <li>that there is one and no more than one primary template per group (forType + purpose + rule (if any))</li>
	 * </ul>
	 * Logs all errors that have been identified (if any) and returns the validation result.
	 *
	 * @param templates are the templates to validate
	 * @return a list of all collected errors
	 */
	public static List<String> validate(List<Template> templates) {
		List<String> mandatoryFieldErrors = validateNoMissingMandatoryFields(templates);
		if (!mandatoryFieldErrors.isEmpty()) {
			// Can not proceed with validation of the primary flags, if mandatory fields are missing
			return mandatoryFieldErrors;
		}
		List<String> correspondingInstancesErrors = validateNoDuplicateCorrespondingInstances(templates);
		List<String> primaryFlagsErrors = validatePrimaryFlags(templates);
		return Stream
				.concat(correspondingInstancesErrors.stream(), primaryFlagsErrors.stream())
				.collect(Collectors.toList());
	}

	/**
	 * Validates that all of the provided templates have a corresponding {@link GenericDefinition} (unless it's an email template)
	 *
	 * @param templates the templates to validate
	 * @param definitionResolver function resolving {@link GenericDefinition} for provided identifier or <code>null</code> if none corresponds
	 * @return list of errors for templates lacking a {@link GenericDefinition} or empty list if all of them are valid
	 */
	public List<String> hasDefinition(List<Template> templates, Function<String, GenericDefinition> definitionResolver) {
		return templates.stream()
				.filter(template -> !EMAIL_TEMPLATE_TYPE.equals(template.getForType()))
				.filter(template -> definitionResolver.apply(template.getForType()) == null)
				.map(template -> "Missing definition id=" + template.getForType() + " for template=" + template.getId())
				.collect(Collectors.toList());
	}

	/**
	 * Validates that the provided templates have valid rules (if any).
	 * The following validations are performed:
	 * <ul>
	 * <li>that there is a definition property for each rule</li>
	 * <li>that each definition property has a defined code list</li>
	 * <li>that each rule's code values exist and are enabled</li>
	 * </ul>
	 *
	 * @param templates the templates which rules will be validated
	 * @param definitionResolver function resolving {@link GenericDefinition} for provided identifier or <code>null</code> if none corresponds
	 * @return list of validation errors or empty list if no errors are detected
	 */
	public List<String> validateRules(List<Template> templates, Function<String, GenericDefinition> definitionResolver) {
		return templates.stream()
				.map(t -> this.validateRules(t, definitionResolver))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	private List<String> validateRules(Template template, Function<String, GenericDefinition> definitionResolver) {
		String templateId = template.getId();
		String rule = template.getRule();
		String forType = template.getForType();

		if (StringUtils.isBlank(rule) || StringUtils.isBlank(forType)) {
			return Collections.emptyList();
		}

		List<String> errors = new LinkedList<>();
		Map<String, Serializable> parsedRules = TemplateRuleUtils.parseRule(rule);
		DefinitionModel definition = definitionResolver.apply(forType);

		parsedRules.forEach((field, value) -> {
			Optional<PropertyDefinition> propertyDefinition = definition.getField(field);

			if (!propertyDefinition.isPresent()) {
				errors.add(String.format(MISSING_FIELD_ERROR, field, forType, templateId));
			} else {
				Integer codelist = propertyDefinition.get().getCodelist();
				if (codelist == null) {
					errors.add(String.format(INVALID_FIELD_ERROR, field, forType, templateId));
				} else if (value instanceof Collection) {
					((Collection) value).forEach(v -> validateCodeValue(templateId, codelist, v.toString(), field, forType, errors));
				} else {
					validateCodeValue(templateId, codelist, value.toString(), field, forType, errors);
				}
			}
		});

		return errors;
	}

	private void validateCodeValue(String templateId, Integer codeList, String value, String field, String forType, List<String> errors) {
		CodeValue codeValue = codelistService.getCodeValue(codeList, value);
		if (codeValue == null) {
			errors.add(String.format(MISSING_VALUE_ERROR, value, field, forType, templateId));
		}
	}

	private static List<String> validateNoDuplicateCorrespondingInstances(
			List<Template> templates) {
		// maps a corresponding instance to the template id
		Map<String, String> correspondingInstancesUnique = new HashMap<>();
		List<String> errors = new ArrayList<>();
		for (Template template : templates) {
			if (StringUtils.isNotBlank(template.getCorrespondingInstance())) {
				String correspondingInstanceId = template.getCorrespondingInstance();
				if (correspondingInstancesUnique.containsKey(correspondingInstanceId)) {
					errors.add("Template [" + template.getId()
							+ "] has corresponding instance which duplicates in ["
							+ correspondingInstancesUnique.get(correspondingInstanceId) + "].");
					continue;
				}
				correspondingInstancesUnique.put(correspondingInstanceId, template.getId());
			}
		}
		if (!errors.isEmpty()) {
			logTemplateErrors(errors);
		}
		return errors;
	}

	private static List<String> validateNoMissingMandatoryFields(List<Template> templates) {
		List<String> errors = new ArrayList<>();
		for (Template template : templates) {
			boolean missingType = StringUtils.isBlank(template.getForType());
			boolean missingTitle = StringUtils.isBlank(template.getTitle());

			String errorMessage = "Template [%s] is missing a mandatory field: [%s]";

			if (missingType) {
				errors.add(String.format(errorMessage, template.getId(), DefaultProperties.TYPE));
			} else if (!EMAIL_TEMPLATE_TYPE.equals(template.getForType())) {
				validateNonMailTemplate(template, errors, errorMessage, missingTitle);
			}
			if (missingTitle) {
				errors.add(String.format(errorMessage, template.getId(), DefaultProperties.TITLE));
			}
			if (StringUtils.isBlank(template.getContent())) {
				errors.add(String.format(errorMessage, template.getId(), CONTENT));
			}
		}
		if (!errors.isEmpty()) {
			logTemplateErrors(errors);
		}
		return errors;
	}

	private static void validateNonMailTemplate(Template template, List<String> errors, String errorMessage,
			boolean missingTitle) {
		if (StringUtils.isBlank(template.getPurpose())) {
			errors.add(String.format(errorMessage, template.getId(), PURPOSE));
		}
		if (!missingTitle && template.getId() != null && !template
				.getId()
				.equalsIgnoreCase(TemplateUtils.buildIdFromTitle(template.getTitle()))) {
			errors.add("Template [" + template.getId()
					+ "] has a title which does not correspond to its identifier. "
					+ "Identifier should be built by the system as the title is converted to lower case and the whitespaces are removed");
		}
	}

	private static List<String> validatePrimaryFlags(List<Template> templates) {
		List<String> errors = new ArrayList<>();
		Map<String, List<Template>> groupedTemplates = TemplateUtils.groupNonMailTemplatesByHash(templates);

		for (Map.Entry<String, List<Template>> entry : groupedTemplates.entrySet()) {
			List<String> primaryIds = getPrimaryIds(entry.getValue());
			if (primaryIds.size() > 1) {
				errors.add(
						"There are more than one primary templates for the same type/purpose/rule. The duplicated templates are: "
								+ primaryIds);
			} else if (primaryIds.isEmpty()) {
				Template template = entry.getValue().get(0);

				String ruleClause = "";
				if (StringUtils.isNotBlank(template.getRule())) {
					ruleClause = ", rule: " + template.getRule();
				}
				errors.add("There is missing primary template for type: " + template.getForType() + ", purpose: "
						+ template.getPurpose() + ruleClause);
			}
		}
		if (!errors.isEmpty()) {
			logTemplateErrors(errors);
		}
		return errors;
	}

	private static List<String> getPrimaryIds(List<Template> templates) {
		return templates.stream()
				.filter(Template::getPrimary)
				.map(Template::getId)
				.collect(Collectors.toList());
	}

	private static void logTemplateErrors(List<String> primaryFlagErrors) {
		String endLine = System.lineSeparator();
		String separator = "====================================================================================================================";
		StringBuilder builder = new StringBuilder().append(endLine)
				.append(endLine)
				.append(separator)
				.append(endLine)
				.append(separator)
				.append(endLine)
				.append("=============== Template reload was aborted. The Following errors were found in the templates ===============")
				.append(endLine);

		primaryFlagErrors.forEach(error -> builder.append(error).append(endLine));
		builder.append(endLine).append(separator).append(endLine).append(separator).append(endLine).append(endLine);
		LOGGER.error("{}", builder);
	}
}
