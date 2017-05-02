package com.sirma.itt.seip.domain.definition;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.json.JsonRepresentable;

/**
 * The Interface ControlDefinition.
 *
 * @author BBonev
 */
public interface ControlDefinition extends PathElement, DefinitionModel, JsonRepresentable {

	/**
	 * Getter method for controlParams.
	 *
	 * @return the controlParams
	 */
	List<ControlParam> getControlParams();

	/**
	 * Getter method for uiParams.
	 *
	 * @return the uiParams
	 */
	List<ControlParam> getUiParams();

	@Override
	default String getType() {
		return null;
	}

	/**
	 * Getter method for parameters.
	 *
	 * @return stream with parameters
	 */
	default Stream<ControlParam> paramsStream() {
		return Stream.concat(getUiParams().stream(), getControlParams().stream());
	}

	/**
	 * Gets a control parameter by name. Parameter could be defined in control or ui parameters
	 *
	 * @param name
	 *            the name of the parameter to find
	 * @return the found parameter or empty optional
	 */
	default Optional<ControlParam> getParam(String name) {
		if (StringUtils.isBlank(name)) {
			return Optional.empty();
		}
		return paramsStream().filter(ControlParam.byName(name)).findAny();
	}

	/**
	 * Gets all parameters as mapping, mapped by {@link ControlParam#getName()}. Any duplicates will be overridden with
	 * the latest version of the parameter instance.
	 *
	 * @return the map
	 */
	default Map<String, ControlParam> asMapByName() {
		return paramsStream().collect(Collectors.toMap(ControlParam::getName, Function.identity(), (v1, v2) -> v2));
	}
}