package com.sirma.itt.seip.definition.rest;

import java.util.Set;

import javax.json.stream.JsonGenerator;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Defines methods for converting {@link DefinitionModel} to JSON.
 *
 * @author A. Kunchev
 */
public interface DefinitionModelToJsonSerializer {

	/**
	 * Converts {@link DefinitionModel} to JSON model.
	 *
	 * @param model
	 *            the model to serialize
	 * @param instance
	 *            instance for which will be evaluated mandatory fields for the model
	 * @param operation
	 *            the executed operation, used to evaluate mandatory fields for the transitions
	 * @param generator
	 *            {@link JsonGenerator} in which to write the information
	 */
	void serialize(DefinitionModel model, Instance instance, String operation, JsonGenerator generator);

	/**
	 * Serializes only the specified fields from the given {@link DefinitionModel} to JSON model.
	 *
	 * @param model
	 *            the model to serialize
	 * @param instance
	 *            instance for which will be evaluated mandatory fields for the model
	 * @param operation
	 *            the executed operation, used to evaluate mandatory fields for the transitions
	 * @param fieldsToSerialize
	 *            the fields to serialize
	 * @param generator
	 *            {@link JsonGenerator} in which to write the information
	 */
	void serialize(DefinitionModel model, Instance instance, String operation, Set<String> fieldsToSerialize,
			JsonGenerator generator);

}
