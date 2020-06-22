package com.sirma.sep.instance.batch;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.batch.runtime.context.JobContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Test for {@link SecurityJobListener}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/06/2017
 */
public class SecurityJobListenerTest {

	@InjectMocks
	private SecurityJobListener securityJobListener;
	@Mock
	private SecurityContextManager securityContextManager;
	@Mock
	private JobContext context;
	@Mock
	private BatchProperties batchProperties;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(context.getExecutionId()).thenReturn(1L);
		when(batchProperties.getTenantId(1L)).thenReturn("test.tenant");
		when(batchProperties.getRequestId(1L)).thenReturn("request-id");
	}

	@Test
	public void beforeJob_shouldInitializeSecurityContext() throws Exception {
		securityJobListener.beforeJob();
		verify(securityContextManager).initializeTenantContext("test.tenant", "request-id");
	}

	@Test
	public void afterJob_shouldEndSecurityContext() throws Exception {
		securityJobListener.beforeJob();
		securityJobListener.afterJob();
		verify(securityContextManager).endExecution();
	}

}
