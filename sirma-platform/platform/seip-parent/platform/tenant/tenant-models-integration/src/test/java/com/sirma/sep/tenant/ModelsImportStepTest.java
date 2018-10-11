package com.sirma.sep.tenant;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.definition.DefinitionImportService;
import com.sirma.sep.model.ModelImportService;

public class ModelsImportStepTest {

	@InjectMocks
	private ModelsImportStep step = new ModelsImportStep();

	@Mock
	private ModelImportService modelImportService;

	@Mock
	private DefinitionImportService definitionImportService;

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Captor
	private ArgumentCaptor<Map<String, InputStream>> captor;

	private static final String PATH = "/com/sirma/sep/tenant/";

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_ImportModelsProvidedByTenantConfiguration() {
		when(modelImportService.importModel(anyMap())).thenReturn(new ArrayList<>());

		executeStep("models");

		verifyModelsImported("models.zip");

		verifyTempFilesAreCleanedUp("models.zip");
	}

	@Test
	public void should_ImportModelsProvidedAsZipFile() {
		when(modelImportService.importModel(anyMap())).thenReturn(new ArrayList<>());

		executeStep("1.zip");

		verifyModelsImported("1.zip");
	}

	@Test(expected = TenantCreationException.class)
	public void should_ThrowExceptionIfThereAreModelValidationErrors() {
		when(modelImportService.importModel(anyMap())).thenReturn(Arrays.asList("Invalid data"));

		executeStep("models");
	}

	private void executeStep(String modelsPath) {
		TenantStepData data = new TenantStepData(null, null);

		File modelsDirectory = new File(getClass().getResource(PATH + modelsPath).getFile());
		data.getModels().add(modelsDirectory);

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test");

		context.setTenantInfo(info);

		step.execute(data, context);
	}

	private void verifyModelsImported(String expectedModelFile) {
		verify(modelImportService, times(1)).importModel(captor.capture());

		Map<String, InputStream> value = captor.getValue();

		assertTrue(value.containsKey(expectedModelFile));
	}

	private void verifyTempFilesAreCleanedUp(String tempFileName) {
		assertNull(getClass().getResource(PATH + tempFileName));
	}

}
