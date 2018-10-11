package com.sirma.sep.definition;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * Imports model definitions into the system.
 *
 * @author Adrian Mitev
 */
public interface DefinitionImportService {

	/**
	 * Validates definition files within a directory tree.
	 *
	 * @param path directory tree where definition files are stored.
	 * @return list of validation errors.
	 */
	Collection<String> validate(Path path);

	/**
	 * Compiles and persists definitions within a directory tree.
	 * WARNING: before invoking the import operation, the definition have to be
	 * validated using the validate method.
	 *
	 * @param path directory tree where definition files are stored.
	 */
	void importDefinitions(Path path);

	/**
	 * Initializes the data type definitions.
	 */
	void initializeDataTypes();

	/**
	 * Exports all definitions available in the system. They are returned as xml files contained within a temp directory
	 * on the file system.
	 *
	 * @return all definitions available in the system
	 */
	List<File> exportAllDefinitions();

	/**
	 * Exports the definitions with the given ids. They are returned as xml files contained within a temp directory on
	 * the file system.
	 *
	 * @param ids the definition ids to export
	 * @return Exports the definitions with the given ids
	 */
	List<File> exportDefinitions(List<String> ids);

	/**
	 * Gets a data about the imported definitions
	 *
	 * @return list for data describing the imported definitions.
	 */
	List<DefinitionInfo> getImportedDefinitions();

}
