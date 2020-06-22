package com.sirma.sep.model.management;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.seip.domain.validation.ValidationReport;
import com.sirma.sep.model.ModelImportService;

/**
 * Stubs the behaviour of {@link ModelImportService} {@link org.mockito.Mock}
 *
 * @author Mihail Radkov
 */
public class ModelImportServiceStub {

	private final ModelImportService modelImportService;
	private final Map<String, InputStream> importedModels;

	/**
	 * Constructs the stub with the provided service mock.
	 *
	 * @param modelImportService mock of the import service
	 */
	public ModelImportServiceStub(ModelImportService modelImportService) {
		this.modelImportService = modelImportService;
		this.importedModels = new HashMap<>();

		when(modelImportService.importModel(anyMap())).thenAnswer(invocation -> {
			Map models = invocation.getArgumentAt(0, Map.class);
			importedModels.putAll(models);
			return ValidationReport.valid();
		});
	}

	public void validModels() {
		when(modelImportService.validateModel(any())).thenReturn(ValidationReport.valid());
	}

	public void withInvalidModels(String... errors) {
		ValidationReport validationReport = new ValidationReport();
		validationReport.addErrors(Arrays.asList(errors));
		when(modelImportService.validateModel(any())).thenReturn(validationReport);
	}

	public Map<String, InputStream> getImportedModels() {
		return importedModels;
	}
}
