package com.sirma.itt.emf.audit.observer;

import javax.interceptor.InvocationContext;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.Options;

/**
 * Test the {@link AuditObserverHelper}.
 *
 * @author nvelkov
 */
public class AuditObserverInterceptorTest {

	@InjectMocks
	private AuditObserverInterceptor auditObserverInterceptor = new AuditObserverInterceptor();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test interceptor with audit enabled.
	 *
	 * @throws Exception
	 */
	@Test
	public void testAuditDisabled() throws Exception {
		Options.DISABLE_AUDIT_LOG.enable();
		try {
			InvocationContext invocationContext = EasyMock.createMock(InvocationContext.class);
			Object result = auditObserverInterceptor.manageContext(invocationContext);
			Assert.assertNull(result);
		} finally {
			Options.DISABLE_AUDIT_LOG.disable();
		}
	}

	/**
	 * Test interceptor with audit disabled.
	 *
	 * @throws Exception
	 */
	@Test
	public void testAuditEnabled() throws Exception {
		InvocationContext invocationContext = EasyMock.createMock(InvocationContext.class);
		Object result = auditObserverInterceptor.manageContext(invocationContext);
		Assert.assertNotEquals(invocationContext, result);

	}

}
