package com.sirma.sep.model.management;

import com.sirma.sep.definition.DefinitionImportService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Stubs the definition export functionality in a {@link DefinitionImportService} {@link org.mockito.Mock}.
 *
 * @author Mihail Radkov
 */
public class DefinitionImportServiceStub {

	private List<File> filesToExport;

	public DefinitionImportServiceStub(DefinitionImportService definitionImportService) {
		filesToExport = new ArrayList<>();
		when(definitionImportService.exportAllDefinitions()).thenReturn(filesToExport);
	}

	public DefinitionImportServiceStub withDefinition(File definitionXml) {
		filesToExport.add(definitionXml);
		return this;
	}

	public void clearDefinitions() {
		filesToExport.clear();
	}

}
