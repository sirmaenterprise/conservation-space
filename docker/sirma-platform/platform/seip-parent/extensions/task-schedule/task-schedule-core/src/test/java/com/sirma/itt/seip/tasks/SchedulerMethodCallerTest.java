package com.sirma.itt.seip.tasks;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationProvider;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.util.CDI;

/**
 * @author BBonev
 */
@Test
public class SchedulerMethodCallerTest {

	@Mock
	BeanManager beanManager;
	@SuppressWarnings("rawtypes")
	@Mock
	Bean bean;
	@SuppressWarnings("rawtypes")
	@Mock
	CreationalContext creationalContext;
	@Mock
	SchedulerService schedulerService;
	@Mock
	ConfigurationProvider configurationProvider;
	private DefaultSchedulerConfiguration configuration;

	private boolean scheduledMethodCalled = false;

	@Test(enabled = false)
	@Schedule(expression = "expression", transactionMode = TransactionMode.NOT_SUPPORTED, identifier = "customId", incrementalDelay = true, maxRetries = 2, retryDelay = 10)
	void scheduledMethod() {
		scheduledMethodCalled = true;
	}

	@Test(enabled = false)
	@ConfigurationPropertyDefinition(name = "config.key")
	@Schedule(transactionMode = TransactionMode.NOT_SUPPORTED, identifier = "customId", incrementalDelay = true, maxRetries = 2, retryDelay = 10)
	void scheduledMethodByConfig() {
		//
	}

	@ConfigurationPropertyDefinition(name = "config.key")
	@Schedule(transactionMode = TransactionMode.NOT_SUPPORTED, identifier = "customId", incrementalDelay = true, maxRetries = 2, retryDelay = 10)
	void scheduleWithArg(SchedulerContext context, SchedulerMethodCallerTest other) {
		assertNotNull(context);
		context.put("key", "value");
		assertNotNull(other);
	}

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod() throws ConfigurationException {
		initMocks(this);

		Set<Bean<?>> beans = new HashSet<>();
		beans.add(bean);
		doReturn(bean).when(beanManager).resolve(beans);
		when(beanManager.getBeans(getClass(), new Annotation[0])).thenReturn(beans);
		when(beanManager.getBeans(getClass(), CDI.getDefaultLiteral())).thenReturn(beans);
		when(beanManager.createCreationalContext(bean)).thenReturn(creationalContext);
		when(bean.create(creationalContext)).thenReturn(this);

		configuration = new DefaultSchedulerConfiguration();
		when(schedulerService.buildEmptyConfiguration(SchedulerEntryType.CRON)).thenReturn(configuration);

		when(beanManager.getReference(bean, getClass(), creationalContext)).thenReturn(this);

		ConfigurationPropertyMock<Object> configuration = new ConfigurationPropertyMock<>("configValue");
		ConfigurationInstance definition = mock(ConfigurationInstance.class);
		when(definition.isSystemConfiguration()).thenReturn(Boolean.TRUE);
		configuration.setDefinition(definition);
		when(configurationProvider.getProperty("config.key")).thenReturn(configuration);
	}

	public void testDefaultSchedule() throws NoSuchMethodException, SecurityException {
		SchedulerMethodCaller caller = new SchedulerMethodCaller(getClass(), getMethod("scheduledMethod"), beanManager);

		caller.schedule(schedulerService, configurationProvider);

		assertEquals(configuration.getCronExpression(), "expression");
		assertEquals(configuration.getTransactionMode(), TransactionMode.NOT_SUPPORTED);
		assertEquals(configuration.getIdentifier(), "customId");
		assertEquals(configuration.isIncrementalDelay(), true);
		assertEquals(configuration.getMaxRetryCount(), 2);
		assertEquals(configuration.getRetryDelay().longValue(), 10L);

	}

	public void testScheduleByConfig() throws NoSuchMethodException, SecurityException {
		SchedulerMethodCaller caller = new SchedulerMethodCaller(getClass(), getMethod("scheduledMethodByConfig"),
				beanManager);

		caller.schedule(schedulerService, configurationProvider);

		assertEquals(configuration.getCronExpression(), "configValue");
		assertEquals(configuration.getTransactionMode(), TransactionMode.NOT_SUPPORTED);
		assertEquals(configuration.getIdentifier(), "customId");
		assertEquals(configuration.isIncrementalDelay(), true);
		assertEquals(configuration.getMaxRetryCount(), 2);
		assertEquals(configuration.getRetryDelay().longValue(), 10L);
	}

	@Test(expectedExceptions = ConfigurationException.class)
	public void testScheduleByConfig_notSystemConfig() throws NoSuchMethodException, SecurityException {
		when(configurationProvider.getProperty("config.key")).thenReturn(
				new ConfigurationPropertyMock<Object>("configValue").setDefinition(mock(ConfigurationInstance.class)));

		SchedulerMethodCaller caller = new SchedulerMethodCaller(getClass(), getMethod("scheduledMethodByConfig"),
				beanManager);

		caller.schedule(schedulerService, configurationProvider);
	}

	public void testInvoke() throws NoSuchMethodException, SecurityException {
		SchedulerMethodCaller caller = new SchedulerMethodCaller(getClass(), getMethod("scheduledMethod"), beanManager);
		caller.invoke(null);

		assertTrue(scheduledMethodCalled);
	}

	public void testInvokeWithArg() throws NoSuchMethodException, SecurityException {
		SchedulerMethodCaller caller = new SchedulerMethodCaller(getClass(), getMethod("scheduleWithArg"), beanManager);
		SchedulerContext context = new SchedulerContext();
		caller.invoke(context);

		assertFalse(context.isEmpty());
	}

	private Method getMethod(String methodName) throws NoSuchMethodException, SecurityException {
		try {
			return getClass().getDeclaredMethod(methodName, (Class[]) null);
		} catch (NoSuchMethodException | SecurityException e) {
			return getClass().getDeclaredMethod(methodName,
					new Class[] { SchedulerContext.class, SchedulerMethodCallerTest.class });
		}
	}

}
