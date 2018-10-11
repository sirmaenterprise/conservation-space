package com.sirmaenterprise.sep.roles;

import java.util.List;

/**
 * Provides functionality for validating and importing permission definitions from the file system.
 * 
 * @author Vilizar Tsonev
 */
public interface PermissionsImportService {

	/**
	 * Validates all permission definitions in the provided directory and returns the detected errors in a list (if
	 * any). </br>
	 * XSD schema validation is performed first. If it passes, the definitions get compiled and logical validations are
	 * performed - for circular dependencies, missing dependencies and more.
	 * 
	 * @param directoryPath is the directory path to load the definitions from
	 * @return the list of detected errors, or an empty list if none
	 */
	List<String> validate(String directoryPath);

	/**
	 * Imports all permission definitions from the provided directory path.</br>
	 * Should be first validated via {@link PermissionsImportService#validate(String)}
	 * 
	 * @param directoryPath is the directory path to load the definitions from
	 */
	void importPermissions(String directoryPath);
}
