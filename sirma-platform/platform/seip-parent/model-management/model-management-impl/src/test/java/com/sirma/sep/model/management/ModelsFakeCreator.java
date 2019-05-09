package com.sirma.sep.model.management;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;

import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeValue;

/**
 * Contains methods for creating various model objects for test purposes
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 02/08/2018
 */
public class ModelsFakeCreator {

	static ClassInstance createClass(IRI iri, String... labels) {
		ClassInstance classInstance = new ClassInstance();
		classInstance.setId(iri.toString());

		classInstance.setSuperClasses(new ArrayList<>());
		classInstance.setSubClasses(new HashMap<>());
		classInstance.setFields(new HashMap<>());
		classInstance.add("description", iri.getLocalName() + " description");

		createStringMap(labels).forEach(classInstance::setLabel);

		return classInstance;
	}

	static PropertyInstance createProperty(IRI iri, String... labels) {
		PropertyInstance propertyInstance = new PropertyInstance();
		propertyInstance.setId(iri.toString());

		createStringMap(labels).forEach(propertyInstance::setLabel);

		return propertyInstance;
	}

	static CodeValue createCodeValue(String id, Integer codeListId, String... descriptions) {
		CodeValue value = new CodeValue();
		value.setValue(id);
		value.setCodeListValue(codeListId.toString());
		Map<String, String> descriptionsMap = createStringMap(descriptions);
		value.setDescriptions(descriptionsMap.entrySet()
				.stream()
				.map(description -> new CodeDescription().setLanguage(description.getKey()).setName(description.getValue()))
				.collect(Collectors.toList()));
		return value;
	}

	public static Map<String, Serializable> createMap(String... keyValue) {
		if (keyValue == null) {
			return Collections.emptyMap();
		}
		return Arrays.stream(keyValue)
				.collect(Collectors.toMap(d -> d.substring(0, d.indexOf('=')), d -> d.substring(d.indexOf('=') + 1, d.length())));
	}

	public static Map<String, String> createStringMap(String... keyValue) {
		if (keyValue == null) {
			return Collections.emptyMap();
		}
		return Arrays.stream(keyValue)
				.collect(Collectors.toMap(getKey(), getValue(), (u, u2) -> u2, LinkedHashMap::new));
	}

	private static Function<String, String> getKey() {
		return s -> s.substring(0, s.indexOf('='));
	}

	private static Function<String, String> getValue() {
		return s -> s.substring(s.indexOf('=') + 1, s.length());
	}
}
