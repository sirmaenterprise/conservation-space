package com.sirma.itt.seip.security.context;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ThreadFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link ThreadFactories}
 *
 * @author BBonev
 */
public class ThreadFactoriesTest {

	@Mock
	private SecurityContextManager manager;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createSystemThreadFactory() throws Exception {
		assertNotNull(ThreadFactories.createSystemThreadFactory(manager));
	}

	@Test
	public void createSystemThreadFactory_asDaemon() throws Exception {
		ThreadFactory threadFactory = ThreadFactories.createSystemThreadFactory(manager, ThreadFactories::asDaemon);
		assertNotNull(threadFactory);
		Thread thread = threadFactory.newThread(() -> {
			return;
		});
		assertTrue(thread.isDaemon());
	}
}
