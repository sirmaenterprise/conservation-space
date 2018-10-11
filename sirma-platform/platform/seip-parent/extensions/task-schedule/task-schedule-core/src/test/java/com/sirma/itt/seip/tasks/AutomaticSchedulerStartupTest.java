package com.sirma.itt.seip.tasks;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Collection;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.build.ConfigurationProvider;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * The Class AutomaticSchedulerStartupTest.
 *
 * @author BBonev
 */
@Test
public class AutomaticSchedulerStartupTest {

	/** The scheduler cache. */
	@Mock
	private AutomaticSchedulerCache schedulerCache;

	/** The scheduler service. */
	@Mock
	private SchedulerService schedulerService;

	/** The provider. */
	@Mock
	ConfigurationProvider provider;

	/** The configuration provider. */
	@Spy
	private InstanceProxyMock<ConfigurationProvider> configurationProvider = new InstanceProxyMock<>(null);

	/** The startup. */
	@InjectMocks
	AutomaticSchedulerStartup startup;

	/**
	 * Inits the.
	 */
	@BeforeMethod
	public void init() {
		initMocks(this);
		configurationProvider.set(provider);
	}

	/**
	 * Schedule method.
	 */
	@Schedule(expression = "exp")
	protected void scheduleMethod() {
		//
	}

	/**
	 * Start on startup.
	 *
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 * @throws SecurityException
	 *             the security exception
	 */
	public void startOnStartup() throws NoSuchMethodException, SecurityException {
		Collection<SchedulerMethodCaller> callers = new ArrayList<>(1);
		SchedulerMethodCaller caller = new SchedulerMethodCaller(getClass(),
				getClass().getDeclaredMethod("scheduleMethod"), null);
		caller = spy(caller);
		callers.add(caller);
		doNothing().when(caller).schedule(schedulerService, provider);

		when(schedulerCache.getAll()).thenReturn(callers);

		startup.startSchedule();

		verify(caller).schedule(schedulerService, provider);
	}
}
