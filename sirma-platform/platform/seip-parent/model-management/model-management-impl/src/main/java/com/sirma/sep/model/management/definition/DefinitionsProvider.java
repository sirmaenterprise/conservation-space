package com.sirma.sep.model.management.definition;

import com.sirma.itt.seip.definition.jaxb.Definition;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.sep.definition.DefinitionImportService;
import com.sirma.sep.xml.JAXBHelper;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides the unprocessed (not compiled) {@link GenericDefinition} loaded in the application.
 *
 * @author Mihail Radkov
 */
@Singleton
public class DefinitionsProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DefinitionImportService definitionImportService;

	@Inject
	private ObjectMapper objectMapper;

	/**
	 * Retrieves the available {@link GenericDefinition} before compilation.
	 *
	 * @return list of not compiled {@link GenericDefinition}
	 */
	public List<GenericDefinition> getUnprocessedDefinitions() {
		TimeTracker tracker = TimeTracker.createAndStart();
		List<File> definitionXMLs = definitionImportService.exportAllDefinitions();
		try {
			return definitionXMLs.stream()
					.map(file -> JAXBHelper.load(file, Definition.class))
					.map(def -> objectMapper.map(def, GenericDefinitionImpl.class))
					.collect(Collectors.toList());
		} finally {
			definitionXMLs.forEach(FileUtils::deleteQuietly);
			LOGGER.debug("Extracting {} unprocessed definitions took {} ms", definitionXMLs.size(), tracker.stop());
		}
	}
}
