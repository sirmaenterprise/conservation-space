package com.sirma.sep.model.management.deploy.definition.steps;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.cls.validator.exception.CodeValidatorException;
import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeValue;
import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.ModelField;
import com.sirma.sep.model.management.Path;
import com.sirma.sep.model.management.codelists.CodeListsProvider;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;

/**
 * Change set step that updates the labels of {@link ModelDefinition} by updating its related {@link CodeValue} via the {@link CodeListsProvider}.
 * <p>
 * This step does not actually make changes to the supplied {@link com.sirma.itt.seip.domain.definition.GenericDefinition}.
 *
 * @author Mihail Radkov
 */
public class DefinitionLabelsUpdateStep extends DefinitionChangeSetStep {

	private final CodeListsProvider codeListsProvider;

	/**
	 * Instantiates the step with the supplied code list provider.
	 *
	 * @param codeListsProvider the provided for updating {@link CodeValue}s.
	 */
	@Inject
	public DefinitionLabelsUpdateStep(CodeListsProvider codeListsProvider) {this.codeListsProvider = codeListsProvider;}

	@Override
	protected boolean checkPath(Path path) {
		Path head = path.head();
		Path next = head.next();
		return head.hasNext() && DefinitionModelAttributes.LABEL.equals(next.getValue());
	}

	@Override
	public List<ValidationMessage> validate(DefinitionChangeSetPayload definitionChangeSetPayload) {
		String definitionId = definitionChangeSetPayload.getDefinition().getId();
		CodeValue updatedValue = buildCodeValue(definitionChangeSetPayload);

		List<String> errors = validateCodeValue(updatedValue);
		return errors.stream()
				.map(error -> ValidationMessage.error(definitionId, error))
				.collect(Collectors.toList());
	}

	@Override
	public void handle(DefinitionChangeSetPayload definitionChangeSetPayload) {
		CodeValue updatedValue = buildCodeValue(definitionChangeSetPayload);
		codeListsProvider.validateValue(updatedValue);
		codeListsProvider.updateValue(updatedValue);
		// Signal that code lists should be reloaded after the deployment process
		definitionChangeSetPayload.getContext().codelistsUpdated();
	}

	private List<String> validateCodeValue(CodeValue value) {
		try {
			codeListsProvider.validateValue(value);
		} catch (CodeValidatorException e) {
			return e.getErrors();
		}
		return Collections.emptyList();
	}

	private static CodeValue buildCodeValue(DefinitionChangeSetPayload definitionChangeSetPayload) {
		ModelDefinition definition = definitionChangeSetPayload.getDefinition();

		ModelField typeField = definition.findFieldByName(DefinitionModelAttributes.TYPE)
				.orElseThrow(() -> new IllegalArgumentException("There is no type field for model definition " + definition.getId()));

		String type = typeField.getValue();
		if (StringUtils.isBlank(type)) {
			throw new IllegalArgumentException("There is no type value for model definition " + definition.getId());
		}

		ModelAttribute codeListAttribute = typeField.findAttribute(DefinitionModelAttributes.CODE_LIST)
				.orElseThrow(() -> new IllegalArgumentException(
						"There is no code list attribute for type field for model definition " + definition.getId()));

		CodeValue updatedValue = new CodeValue();
		updatedValue.setCodeListValue(codeListAttribute.getValue().toString());
		updatedValue.setValue(type);
		updatedValue.setDescriptions(getDescriptions(definition.getLabels()));

		return updatedValue;
	}

	private static List<CodeDescription> getDescriptions(Map<String, String> labels) {
		return labels.entrySet()
				.stream()
				.map(label -> new CodeDescription().setLanguage(label.getKey()).setName(label.getValue()))
				.collect(Collectors.toList());
	}
}
