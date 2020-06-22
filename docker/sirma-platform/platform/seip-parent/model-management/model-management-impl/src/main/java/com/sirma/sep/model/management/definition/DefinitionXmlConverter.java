package com.sirma.sep.model.management.definition;

import static com.sirma.sep.model.management.definition.DefinitionSanitizer.sanitizeDefinition;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.jaxb.Definition;
import com.sirma.itt.seip.definition.jaxb.Label;
import com.sirma.itt.seip.definition.jaxb.LabelValue;
import com.sirma.itt.seip.definition.jaxb.Labels;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.mapping.ObjectMappingException;
import com.sirma.sep.xml.JAXBHelper;

/**
 * Utility service for converting XML {@link File} to {@link GenericDefinition} and vice versa.
 *
 * @author Mihail Radkov
 */
class DefinitionXmlConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ObjectMapper objectMapper;
	private final LabelProvider labelProvider;

	/**
	 * Constructs the converter with the supplied dependent services.
	 *
	 * @param objectMapper used to map different object types
	 * @param labelProvider used for retrieving {@link GenericDefinition} labels
	 */
	@Inject
	DefinitionXmlConverter(ObjectMapper objectMapper, LabelProvider labelProvider) {
		this.objectMapper = objectMapper;
		this.labelProvider = labelProvider;
	}

	/**
	 * Converts the provided XML files to non compiled {@link GenericDefinition}.
	 *
	 * @param definitionXMLs the files to convert
	 * @return list of converted {@link GenericDefinition}
	 */
	List<GenericDefinition> convertToGenericDefinitions(List<File> definitionXMLs) {
		List<GenericDefinition> converted = new LinkedList<>();
		definitionXMLs.forEach(definitionXML -> {
			Definition definition = JAXBHelper.load(definitionXML, Definition.class);
			GenericDefinitionImpl genericDefinition = objectMapper.map(definition, GenericDefinitionImpl.class);
			// Needed to know the original file name
			genericDefinition.setSourceFile(definitionXML.getName());
			converted.add(genericDefinition);
		});
		return converted;
	}

	/**
	 * Converts the provided {@link GenericDefinition} to XML {@link File} and then to a mapping between definition's file name and the
	 * XML as {@link InputStream}.
	 * <p>
	 * The produced XMLs will be clear of empty tags.
	 *
	 * @param definitions list of definitions to convert
	 * @return mapping between definition file names and their XML representation as {@link InputStream}
	 */
	Map<String, InputStream> convertToXMLs(Collection<GenericDefinition> definitions) {
		// The import service requires filename to xml stream mapping
		Map<String, InputStream> fileNameToXMLMapping = new LinkedHashMap<>(definitions.size());
		definitions.forEach(definition -> {
			if (definition instanceof GenericDefinitionImpl) {
				String xml = convertToXML(definition);
				String sourceFilename = ((GenericDefinitionImpl) definition).getSourceFile();
				InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
				fileNameToXMLMapping.put(sourceFilename, inputStream);
			} else {
				LOGGER.warn("Received GenericDefinition of unknown type {}, expected GenericDefinitionImpl",
						definition.getClass().getName());
			}
		});
		return fileNameToXMLMapping;
	}

	private String convertToXML(GenericDefinition genericDefinition) {
		// TODO: Use temp file and return stream
		try {
			Definition definition = objectMapper.map(genericDefinition, Definition.class);
			// Labels are not stored in GenericDefinition, they have to be manually re-populated.
			populateLabels(definition);
			sanitizeDefinition(definition);
			return JAXBHelper.toXml(definition);
		} catch (ObjectMappingException ex) {
			LOGGER.error("Cannot convert definition {} to XML", genericDefinition.getIdentifier());
			throw new IllegalArgumentException(ex);
		}
	}

	private void populateLabels(Definition definition) {
		Map<String, Map<String, String>> labelsMap = labelProvider.getDefinitionLabels(definition.getId());
		if (CollectionUtils.isNotEmpty(labelsMap)) {
			List<Label> labelsList = toLabels(labelsMap);
			Labels labels = new Labels();
			labels.setLabel(labelsList);
			definition.setLabels(labels);
		}
	}

	private static List<Label> toLabels(Map<String, Map<String, String>> labelsMap) {
		return labelsMap.entrySet().stream().map(entry -> {
			Label label = new Label();
			label.setId(entry.getKey());
			label.setValue(toLabelValues(entry.getValue()));
			return label;
		}).collect(Collectors.toList());
	}

	private static List<LabelValue> toLabelValues(Map<String, String> labels) {
		return labels.entrySet().stream().map(entry -> {
			LabelValue labelValue = new LabelValue();
			labelValue.setLang(entry.getKey());
			labelValue.setValue(entry.getValue());
			return labelValue;
		}).collect(Collectors.toList());
	}
}
