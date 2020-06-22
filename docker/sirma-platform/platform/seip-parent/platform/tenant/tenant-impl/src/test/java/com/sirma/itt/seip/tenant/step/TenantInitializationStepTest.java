package com.sirma.itt.seip.tenant.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.tenant.context.Tenant;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tenant.context.TenantStatus;
import com.sirma.itt.seip.tenant.exception.TenantValidationException;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * Tests {@link TenantInitializationStep}.
 * 
 * @author smustafov
 */
public class TenantInitializationStepTest {

	@Mock
	private TenantManager tenantManager;

	@InjectMocks
	private TenantInitializationStep initializationStep = new TenantInitializationStep();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = TenantCreationException.class)
	public void testExecute_alreadyExistingActiveTenant() throws TenantValidationException {
		String tenantId = "tenant.com";
		TenantInitializationContext context = new TenantInitializationContext();

		TenantStepData data = TenantStepData.createEmpty(initializationStep.getIdentifier());
		data.getProperties().put("tenantid", tenantId);

		Tenant tenant = new Tenant(tenantId);
		tenant.setStatus(TenantStatus.ACTIVE);

		when(tenantManager.getTenant(tenantId)).thenReturn(Optional.of(tenant));

		initializationStep.execute(data, context);
	}

	@Test
	public void testExecute_shouldMarkNewTenantAsInactive() throws TenantValidationException {
		String tenantId = "tenant.com";
		TenantInitializationContext context = new TenantInitializationContext();

		TenantStepData data = TenantStepData.createEmpty(initializationStep.getIdentifier());
		data.getProperties().put("tenantid", tenantId);
		data.getProperties().put("tenantname", tenantId);

		when(tenantManager.getTenant(tenantId)).thenReturn(Optional.empty());

		initializationStep.execute(data, context);

		ArgumentCaptor<Tenant> tenantArgCaptor = ArgumentCaptor.forClass(Tenant.class);
		verify(tenantManager).addNewTenant(tenantArgCaptor.capture());

		assertTrue(TenantStatus.INACTIVE.equals(tenantArgCaptor.getValue().getStatus()));
		assertEquals(tenantId, tenantArgCaptor.getValue().getTenantId());
	}

	@Test
	public void tenantIdPattern_shouldAllowOnlyValidNames() throws JSONException {
		TenantStepData stepData = initializationStep.provide();
		String pattern = resolveTenantIdValidator(stepData);
		Pattern validator = Pattern.compile(pattern);

		verifyValidTenantId("a20190130.vr", validator);
		verifyValidTenantId("bb.com", validator);
		verifyValidTenantId("test20190130.vr", validator);
		verifyValidTenantId("test.20190130.vr", validator);
		verifyValidTenantId("test-20190130.vr", validator);
		verifyValidTenantId("test.0s-aa.vr", validator);
		verifyValidTenantId("test.as.aa.vr", validator);
		verifyValidTenantId("test.0s.vr", validator);
		verifyValidTenantId("tt.0s.vr", validator);
		verifyValidTenantId("tt0s.v2", validator);
		verifyValidTenantId("tt0s.v123456789v123456789", validator);

		// minimum 2 chars before the domain separator
		verifyInvalidTenantId("t.vr", validator);
		// should begin with character, not a number (the db does not allow users with leading digits)
		verifyInvalidTenantId("20190130.vr", validator);
		// cannot have intermediate domain with single character
		verifyInvalidTenantId("test.0.vr", validator);
		verifyInvalidTenantId("test.0s-a.vr", validator);
		// domain extension should be at least 2 chars
		verifyInvalidTenantId("test.v", validator);
		// domain extension should not have leading numbers
		verifyInvalidTenantId("test.2v", validator);
		// domain extension should not exceed 20 chars
		verifyInvalidTenantId("tt0s.v123456789v123456789v", validator);
	}

	private void verifyInvalidTenantId(String tenantId, Pattern validator) {
		assertFalse(tenantId + " should not be valid tenant identifier", validator.matcher(tenantId).matches());
	}

	private void verifyValidTenantId(String tenantId, Pattern validator) {
		assertTrue(tenantId + " should be valid tenant identifier", validator.matcher(tenantId).matches());
	}

	private String resolveTenantIdValidator(TenantStepData stepData) throws JSONException {
		JSONObject jsonObject = stepData.toJSONObject();
		JSONArray properties = jsonObject.getJSONArray(TenantStepData.KEY_PROPERTIES_LIST);
		for (int i = 0; i < properties.length(); i++) {
			JSONObject nextProperty = properties.getJSONObject(i);
			if (nextProperty.getString(TenantStepData.KEY_ID).equals("tenantid")) {
				return nextProperty.getString("validator");
			}
		}
		fail("Could not find tenant id validator pattern");
		return "";
	}
}