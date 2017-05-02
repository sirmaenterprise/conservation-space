package com.sirma.itt.seip.tenant.step;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.emf.cls.entity.CodeListSheet;
import com.sirma.itt.emf.cls.persister.PersisterException;
import com.sirma.itt.emf.cls.persister.SheetParser;
import com.sirma.itt.emf.cls.persister.XLSProcessor;
import com.sirma.itt.emf.cls.validator.XLSValidator;
import com.sirma.itt.emf.cls.validator.XLSValidatorException;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * Tests for the {@link TenantCreationCodelistStep}.
 *
 * @author velkov
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantCreationCodelistStepTest {

	@Mock
	private XLSProcessor processor;
	@Mock
	private XLSValidator validator;
	@Mock
	private SecurityContextManager securityContextManager;
	@Mock
	private SheetParser sheetParser;

	@InjectMocks
	TenantCreationCodelistStep step;

	/**
	 * Test the codelist persist.
	 *
	 * @throws URISyntaxException
	 *             if the codelists file couldn't be opened
	 * @throws PersisterException
	 *             if the sheet couldn't be persisted
	 * @throws XLSValidatorException
	 *             if the sheet couldn't be validated
	 */
	@Test
	public void testPersistCodelist() throws URISyntaxException, PersisterException, XLSValidatorException {
		TenantStepData data = new TenantStepData("DMSInit", null);
		File definitionsDir = new File(this.getClass().getResource("/definitions").toURI());
		data.addModel(definitionsDir);

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo tenantInfo = new TenantInfo("tenantId");
		context.setTenantInfo(tenantInfo);

		Assert.assertTrue(step.execute(data, context));
		Mockito.verify(processor, Mockito.atLeastOnce()).persistSheet(Matchers.any(CodeListSheet.class));
		Mockito.verify(validator, Mockito.atLeastOnce()).getValidatedCodeListSheet(Matchers.any(InputStream.class));
	}

	/**
	 * Test the step with a valid definitions model but missing codelists file.
	 *
	 * @throws URISyntaxException
	 *             if the codelists file couldn't be opened
	 * @throws PersisterException
	 *             if the sheet couldn't be persisted
	 * @throws XLSValidatorException
	 *             if the sheet couldn't be validated
	 */
	@Test
	public void testPersistCodelistMissingCodelist()
			throws URISyntaxException, PersisterException, XLSValidatorException {
		TenantStepData data = new TenantStepData("DMSInit", null);
		File definitionsDir = new File("emptyDefinitions");
		data.addModel(definitionsDir);

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo tenantInfo = new TenantInfo("tenantId");
		context.setTenantInfo(tenantInfo);

		Assert.assertTrue(step.execute(data, context));
		Mockito.verify(processor, Mockito.never()).persistSheet(Matchers.any(CodeListSheet.class));
		Mockito.verify(validator, Mockito.never()).getValidatedCodeListSheet(Matchers.any(InputStream.class));
	}

	/**
	 * Test the step with a failed codelist persist.
	 *
	 * @throws URISyntaxException
	 *             if the codelists file couldn't be opened
	 * @throws PersisterException
	 *             if the sheet couldn't be persisted
	 */
	@Test(expected = TenantCreationException.class)
	public void testPersistCodelistFailedPersist() throws URISyntaxException, PersisterException {
		Mockito.doThrow(PersisterException.class).when(processor).persistSheet(Matchers.any(CodeListSheet.class));
		TenantStepData data = new TenantStepData("DMSInit", null);
		File definitionsDir = new File(this.getClass().getResource("/definitions").toURI());
		data.addModel(definitionsDir);

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo tenantInfo = new TenantInfo("tenantId");
		context.setTenantInfo(tenantInfo);

		step.execute(data, context);

	}

	/**
	 * Test the step with a malformed sheet
	 *
	 * @throws URISyntaxException
	 *             if the codelists file couldn't be opened
	 * @throws XLSValidatorException
	 *             if the sheet couldn't be validated
	 */
	@Test(expected = TenantCreationException.class)
	public void testPersistCodelistInvalidSheet() throws URISyntaxException, XLSValidatorException {
		Mockito.doThrow(XLSValidatorException.class).when(validator)
				.getValidatedCodeListSheet(Matchers.any(InputStream.class));
		TenantStepData data = new TenantStepData("DMSInit", null);
		File definitionsDir = new File(this.getClass().getResource("/definitions").toURI());
		data.addModel(definitionsDir);

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo tenantInfo = new TenantInfo("tenantId");
		context.setTenantInfo(tenantInfo);

		step.execute(data, context);

	}
}
