package com.sirma.sep.model.management.definition;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.validation.ValidationReport;
import com.sirma.itt.seip.domain.validation.ValidationReportTranslator;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.sep.definition.DefinitionImportService;
import com.sirma.sep.model.ModelImportService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.File;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Provides a way to get and update the unprocessed (not compiled) {@link GenericDefinition} in the application.
 *
 * @author Mihail Radkov
 */
@Singleton
public class DefinitionsProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DefinitionImportService definitionImportService;

	@Inject
	private ModelImportService modelImportService;

	@Inject
	private DefinitionXmlConverter xmlConverter;

	@Inject
	private LabelProvider labelProvider;

	/**
	 * Retrieves the available {@link GenericDefinition} before compilation.
	 *
	 * @return list of not compiled {@link GenericDefinition}
	 */
	public List<GenericDefinition> getUnprocessedDefinitions() {
		return getAndConvert(definitionImportService::exportAllDefinitions);
	}

	/**
	 * Retrieves the specified {@link GenericDefinition} before compilation.
	 *
	 * @param identifiers the IDs of {@link GenericDefinition} to retrieve
	 * @return list of not compiled {@link GenericDefinition}
	 */
	public List<GenericDefinition> getUnprocessedDefinitions(List<String> identifiers) {
		if (CollectionUtils.isEmpty(identifiers)) {
			return Collections.emptyList();
		}
		return getAndConvert(() -> definitionImportService.exportDefinitions(identifiers));
	}

	private List<GenericDefinition> getAndConvert(Supplier<List<File>> definitionSupplier) {
		TimeTracker tracker = TimeTracker.createAndStart();
		List<File> definitionXMLs = definitionSupplier.get();
		try {
			return xmlConverter.convertToGenericDefinitions(definitionXMLs);
		} finally {
			definitionXMLs.forEach(FileUtils::deleteQuietly);
			LOGGER.debug("Extracting {} unprocessed definitions took {} ms", definitionXMLs.size(), tracker.stop());
		}
	}

	/**
	 * Validates the provided definitions by converting them to XMLs and passing those to {@link ModelImportService#validateModel(Map)}
	 *
	 * @param definitions the definitions to validate
	 * @return list of validation error messages or empty list if everything is valid
	 */
	public ValidationReport validateDefinitions(Collection<GenericDefinition> definitions) {
		Map<String, InputStream> definitionsForValidation = xmlConverter.convertToXMLs(definitions);
		return modelImportService.validateModel(definitionsForValidation);
	}

	/**
	 * Updates the provided definition by converting them to XMLs and then invoking the validate & import process via
	 * {@link ModelImportService#importModel(Map)}
	 *
	 * @param definitions the definition to import
	 */
	public void updateDefinitions(Collection<GenericDefinition> definitions) {
		TimeTracker tracker = TimeTracker.createAndStart();
		Map<String, InputStream> definitionsForImport = xmlConverter.convertToXMLs(definitions);
		try {
			ValidationReport validationReport = modelImportService.importModel(definitionsForImport);
			if (validationReport.isValid()) {
				LOGGER.debug("Updating {} definition models took {} ms", definitionsForImport.size(), tracker.stop());
			} else {
				LOGGER.error("Couldn't update definitions because of validation errors:");
				ValidationReportTranslator reportTranslator = new ValidationReportTranslator(labelProvider, validationReport);
				reportTranslator.getErrors(Locale.ENGLISH.getLanguage()).forEach(LOGGER::warn);
				throw new RollbackedRuntimeException("Provided definitions for import should have been valid but are not");
			}
		} finally {
			definitionsForImport.values().forEach(IOUtils::closeQuietly);
		}
	}

}
