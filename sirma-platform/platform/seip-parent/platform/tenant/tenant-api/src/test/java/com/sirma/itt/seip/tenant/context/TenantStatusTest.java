package com.sirma.itt.seip.tenant.context;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test the tenant status conversion from integer.
 * 
 * @author nvelkov
 */
public class TenantStatusTest {

	@Test
	public void should_createTenantStatus_fromInteger() {
		TenantStatus status = TenantStatus.fromInteger(0);
		assertEquals(null, status);

		status = TenantStatus.fromInteger(1);
		assertEquals(TenantStatus.ACTIVE, status);

		status = TenantStatus.fromInteger(2);
		assertEquals(TenantStatus.INACTIVE, status);

		status = TenantStatus.fromInteger(3);
		assertEquals(TenantStatus.DELETED, status);
		assertEquals(3, status.getValue());
	}
}
