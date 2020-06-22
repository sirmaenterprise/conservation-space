package com.sirmaenterprise.sep.bpm.camunda.service;

/**
 * Provides functionality for importing BPM models to the system.
 * 
 * @author Vilizar Tsonev
 */
public interface BPMDefinitionImportService {

	/**
	 * Imports all BPM models from the provided directory.
	 * 
	 * @param directoryPath is the path of the directory to import from
	 */
	void importDefinitions(String directoryPath);
}
