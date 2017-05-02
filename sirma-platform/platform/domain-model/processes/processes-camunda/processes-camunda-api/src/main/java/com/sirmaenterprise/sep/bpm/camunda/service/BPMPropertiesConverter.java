package com.sirmaenterprise.sep.bpm.camunda.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.variable.VariableMap;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Defines a properties conversion between SEIP model and external process engine model.
 * 
 * @author bbanchev
 */
public interface BPMPropertiesConverter {
	/**
	 * Converts a map of properties to Camunda style map with optional additional processing.
	 * 
	 * @param properties
	 *            is a mandatory parameter that is used as source of conversion.
	 * @param formFields
	 *            is provided filters out only those properties to the result map. Might be null or empty
	 * @return the converted map with optionally reduced set of properties
	 */
	Map<String, Object> convertDataFromSEIPtoCamunda(Map<String, Serializable> properties, List<FormField> formFields);

	/**
	 * Converts a map of properties from Camunda style map with optional additional processing.
	 * 
	 * @param source
	 *            is the source task containing the properties. Might be null
	 * @param destination
	 *            is the destination instance to convert properties for. Might be null
	 * @return the converted map of task properties or null on empty or null source data
	 */
	Map<String, Serializable> convertDataFromCamundaToSEIP(VariableMap source, Instance destination);
}
