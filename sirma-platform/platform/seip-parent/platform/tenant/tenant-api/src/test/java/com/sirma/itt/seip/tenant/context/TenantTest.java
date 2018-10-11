package com.sirma.itt.seip.tenant.context;

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

/**
 * Test the tenant json conversion.
 * 
 * @author nvelkov
 */
public class TenantTest {

	@Test
	public void should_convertToJSONObject() {
		Tenant tenant = new Tenant("tenantId");
		JSONObject jsonObject = tenant.toJSONObject();
		assertEquals("{\"id\":\"tenantId\"}", jsonObject.toString());

		tenant.setStatus(TenantStatus.ACTIVE);
		jsonObject = tenant.toJSONObject();
		assertEquals("{\"id\":\"tenantId\",\"status\":\"ACTIVE\"}", jsonObject.toString());
	}

	@Test
	public void should_convertFromJSONObject() throws JSONException {
		Tenant tenant = new Tenant();
		tenant.fromJSONObject(new JSONObject("{\"id\":\"tenantId\",\"status\":\"1\"}"));
		assertEquals(TenantStatus.ACTIVE, tenant.getStatus());
		assertEquals("tenantId", tenant.getTenantId());

		tenant.fromJSONObject(new JSONObject("{\"id\":\"tenantId\"}"));
		assertEquals("tenantId", tenant.getTenantId());
	}
}
