package com.sirma.itt.seip.template;

import java.io.File;
import java.util.List;

/**
 * Provides functionality to validate and import template definitions to the system.
 * 
 * @author Vilizar Tsonev
 */
public interface TemplateImportService {

	/**
	 * Loads all template definitions from the provided path and validates them. </br>
	 * Files' extensions are validated. XSD schema validation is performed for well-structured xml and tags, as well as
	 * business validation for mandatory fields, duplicate instance IDs, duplicate primary templates and more.
	 * 
	 * @param validationRequest request containing the directory path to load the templates from and the available definitions for validation
	 * @return a list of all detected errors in the templates
	 */
	List<String> validate(TemplateValidationRequest validationRequest);

	/**
	 * Imports all templates from the given local file directory.
	 * 
	 * @param directoryPath is the directory path to load the templates from
	 */
	void importTemplates(String directoryPath);

	/**
	 * Exports the activated templates with the given ids. Returns them as a list of xml files created in a temp
	 * directory on the file system.
	 *
	 * @param ids the ids of the templates to export (works with both template ids and corresponding instance ids)
	 * @return the list of template files
	 */
	List<File> exportTemplates(List<String> ids);

	/**
	 * Exports all activated templates available in the system. Returns them as a list of xml files created in a temp
	 * directory on the file system.
	 * 
	 * @return the list of all activated template files
	 */
	List<File> exportAllTemplates();
}
