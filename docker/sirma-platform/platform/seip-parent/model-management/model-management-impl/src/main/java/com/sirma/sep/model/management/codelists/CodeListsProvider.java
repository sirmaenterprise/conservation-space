package com.sirma.sep.model.management.codelists;

import com.sirma.itt.emf.cls.persister.CodeListPersister;
import com.sirma.itt.emf.cls.validator.CodeValidator;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.sep.cls.CodeListService;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeValue;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Encapsulates the {@link CodeListService} from the model management functionality.
 *
 * @author Mihail Radkov
 */
@Singleton
public class CodeListsProvider {

	@Inject
	private CodelistService codelistService;

	@Inject
	private CodeListService readService;

	@Inject
	private CodeListPersister codeListPersister;

	@Inject
	private CodeValidator codeValidator;

	/**
	 * Retrieves the values for the specified code list identifier.
	 *
	 * @param codelistId the code list identifier for which values belong to
	 * @return map with the code values
	 */
	public Map<String, CodeValue> getValues(Integer codelistId) {
		return readService.getCodeValues(codelistId.toString())
				.stream()
				.collect(Collectors.toMap(CodeValue::getValue, Function.identity()));
	}

	/**
	 * Validates the provided {@link CodeValue}.
	 *
	 * @param value the value to validate
	 */
	public void validateValue(CodeValue value) {
		Objects.requireNonNull(value.getCodeListValue(), "Non null code list identifier is required");

		CodeValue codeValue = getCodeValue(value);
		mergeCodeDescriptions(value, codeValue);
		codeValidator.validateCodeValue(codeValue);
	}

	/**
	 * Uses the provided {@link CodeValue} to fetch and update its corresponding {@link CodeValue} by merging
	 * their descriptions.
	 *
	 * @param value the value with updated descriptions to save
	 */
	public void updateValue(CodeValue value) {
		Objects.requireNonNull(value.getCodeListValue(), "Non null code list identifier is required");

		CodeValue codeValue = getCodeValue(value);
		mergeCodeDescriptions(value, codeValue);
		codeListPersister.persist(codeValue);
	}

	/**
	 * Triggers cache reloading in the code list service without notifying the whole application to avoid models recalculation.
	 */
	public void reloadCodelists() {
		codelistService.refreshCache();
	}

	private CodeValue getCodeValue(CodeValue value) {
		return readService.getCodeValue(value.getCodeListValue(), value.getValue())
				.orElseThrow(() -> new IllegalArgumentException(
						"Missing code value " + value.getValue() + " for code list with id=" + value.getCodeListValue()));
	}

	private static void mergeCodeDescriptions(CodeValue source, CodeValue target) {
		Map<String, CodeDescription> codeDescriptionMap = target.getDescriptions()
				.stream()
				.collect(Collectors.toMap(d -> d.getLanguage().toUpperCase(), Function.identity()));

		source.getDescriptions().forEach(description -> {
			String localLang = description.getLanguage().toUpperCase();
			String localName = sanitizeName(description.getName());
			if (codeDescriptionMap.containsKey(localLang)) {
				codeDescriptionMap.get(localLang).setName(localName);
			} else {
				target.getDescriptions().add(new CodeDescription().setLanguage(localLang).setName(localName));
			}
		});
	}

	private static String sanitizeName(Serializable name) {
		return Objects.toString(name, "").trim();
	}

}
