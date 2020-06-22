package com.sirma.itt.seip.tenant.step;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.emf.cls.persister.SheetParser;
import com.sirma.itt.emf.cls.persister.CodeListPersister;
import com.sirma.itt.emf.cls.validator.SheetValidator;
import com.sirma.itt.emf.cls.validator.exception.CodeValidatorException;
import com.sirma.itt.emf.cls.validator.exception.SheetValidatorException;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.cls.parser.CodeListSheet;

/**
 * Tests for the {@link TenantCodelistStep}.
 *
 * @author velkov
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantCodelistStepTest {

	@Mock
	private CodeListPersister persister;

	@Mock
	private SheetValidator sheetValidator;

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Mock
	private SheetParser sheetParser;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@InjectMocks
	TenantCodelistStep step;

	@Test
	public void persistCodelist() throws URISyntaxException, SheetValidatorException, CodeValidatorException {
		TenantStepData data = new TenantStepData("DMSInit", null);
		File definitionsDir = new File(this.getClass().getResource("/definitions").toURI());
		data.addModel(definitionsDir);

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo tenantInfo = new TenantInfo("tenantId");
		context.setTenantInfo(tenantInfo);

		CodeListSheet sheet = new CodeListSheet();
		when(sheetParser.parseFromSheet(any())).thenReturn(sheet);

		assertTrue(step.execute(data, context));
		verify(persister, atLeastOnce()).override(any(CodeListSheet.class));
		verify(sheetValidator, atLeastOnce()).getValidatedCodeListSheet(any(InputStream.class));
	}

	@Test
	public void persistCodelistMissingCodelist()
			throws URISyntaxException, SheetValidatorException, CodeValidatorException {
		TenantStepData data = new TenantStepData("DMSInit", null);
		File definitionsDir = new File("emptyDefinitions");
		data.addModel(definitionsDir);

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo tenantInfo = new TenantInfo("tenantId");
		context.setTenantInfo(tenantInfo);

		assertTrue(step.execute(data, context));
		verify(persister, never()).override(any(CodeListSheet.class));
		verify(sheetValidator, never()).getValidatedCodeListSheet(any(InputStream.class));
	}

	@Test(expected = TenantCreationException.class)
	public void persistCodelistFailedPersist() throws URISyntaxException, CodeValidatorException {
		doThrow(IllegalArgumentException.class).when(persister).override(any(CodeListSheet.class));
		TenantStepData data = new TenantStepData("DMSInit", null);
		File definitionsDir = new File(this.getClass().getResource("/definitions").toURI());
		data.addModel(definitionsDir);

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo tenantInfo = new TenantInfo("tenantId");
		context.setTenantInfo(tenantInfo);

		CodeListSheet sheet = new CodeListSheet();
		when(sheetParser.parseFromSheet(any())).thenReturn(sheet);

		step.execute(data, context);
	}

	@Test(expected = TenantCreationException.class)
	public void persistCodelistInvalidSheet() throws URISyntaxException, SheetValidatorException {
		doThrow(SheetValidatorException.class).when(sheetValidator).getValidatedCodeListSheet(any(InputStream.class));
		TenantStepData data = new TenantStepData("DMSInit", null);
		File definitionsDir = new File(this.getClass().getResource("/definitions").toURI());
		data.addModel(definitionsDir);

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo tenantInfo = new TenantInfo("tenantId");
		context.setTenantInfo(tenantInfo);

		step.execute(data, context);

	}

	@Test(expected = TenantCreationException.class)
	public void persistCodelistValidationFail()
			throws URISyntaxException, SheetValidatorException, CodeValidatorException {
		TenantStepData data = new TenantStepData("DMSInit", null);
		File definitionsDir = new File(this.getClass().getResource("/definitions").toURI());
		data.addModel(definitionsDir);

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo tenantInfo = new TenantInfo("tenantId");
		context.setTenantInfo(tenantInfo);

		CodeListSheet sheet = new CodeListSheet();
		doThrow(new CodeValidatorException("Error")).when(persister).override(any());
		when(sheetParser.parseFromSheet(any())).thenReturn(sheet);

		step.execute(data, context);
	}

	public void delete_processorCalled() {
		step.delete(new TenantStepData(null, null), new TenantDeletionContext(new TenantInfo("tenantId"), false));
		verify(persister).delete();
	}
}
