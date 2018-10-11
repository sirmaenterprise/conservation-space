package com.sirma.itt.seip.template;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT;
import static com.sirma.itt.seip.template.TemplateProperties.PURPOSE;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.template.utils.TemplateUtils;

/**
 * Validates input template data. </br>
 *
 * <b>Note:</b> The functionality is tested integrated in TemplateServiceImplTest from the entry point - the
 * templates reload.
 *
 * @author Vilizar Tsonev
 */
public class TemplateValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String EMAIL_TEMPLATE_TYPE = "emailTemplate";

	private TemplateValidator() {
		// disable instantiation
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
		List<String> mandatoryFieldErros = validateNoMissingMandatoryFields(templates);
		if (!mandatoryFieldErros.isEmpty()) {
			// Can not proceed with validation of the primary flags, if mandatory fields are missing
			return mandatoryFieldErros;
		}
		List<String> correspondingInstancesErrors = validateNoDuplicateCorreposndingInstances(templates);
		List<String> primaryFlagsErros = validatePrimaryFlags(templates);
		return Stream
				.concat(correspondingInstancesErrors.stream(), primaryFlagsErros.stream())
					.collect(Collectors.toList());
	}

	private static List<String> validateNoDuplicateCorreposndingInstances(
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
			logTemlateErrors(errors);
		}
		return errors;
	}

	private static List<String> validateNoMissingMandatoryFields(List<Template> templates) {
		List<String> errors = new ArrayList<>();
		for (Template template : templates) {
			boolean missingType = StringUtils.isBlank(template.getForType());
			boolean missingTitle = StringUtils.isBlank(template.getTitle());

			String errorMessage = "Template [%s] is missing a mandatory field: [%s]";

			// NOSONAR
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
			logTemlateErrors(errors);
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
			logTemlateErrors(errors);
		}
		return errors;
	}

	private static List<String> getPrimaryIds(List<Template> templates) {
		return templates.stream()
				.filter(Template::getPrimary)
				.map(Template::getId)
				.collect(Collectors.toList());
	}

	private static void logTemlateErrors(List<String> primaryFlagErrors) {
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
		LOGGER.error(builder.toString());
	}
}
