package com.sirma.itt.seip.template;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sirma.itt.seip.domain.definition.GenericDefinition;

/**
 * DTO carrying the folder path with templates for validation and the available definitions that can be used for template validation.
 *
 * @author Mihail Radkov
 */
public class TemplateValidationRequest {

	private final String path;
	private final Map<String, GenericDefinition> definitionsMap;

	public TemplateValidationRequest(String path, List<GenericDefinition> definitions) {
		this.path = path;
		this.definitionsMap = definitions.stream().collect(Collectors.toMap(GenericDefinition::getIdentifier, Function.identity()));
	}

	public String getPath() {
		return path;
	}

	public List<GenericDefinition> getDefinitions() {
		return new LinkedList<>(definitionsMap.values());
	}

	/**
	 * Retrieves a {@link GenericDefinition} from the available in the request.
	 *
	 * @param identifier definition identifier with which to fetch from the available
	 * @return the {@link GenericDefinition} or <code>null</code> if none corresponds to the identifier
	 */
	public GenericDefinition getDefinition(String identifier) {
		return definitionsMap.get(identifier);
	}
}
