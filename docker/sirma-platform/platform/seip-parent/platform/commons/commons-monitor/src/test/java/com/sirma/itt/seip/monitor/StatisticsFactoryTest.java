package com.sirma.itt.seip.monitor;

import javax.enterprise.inject.Instance;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsFactoryTest {

	@Mock
	ConfigurationProperty<Boolean> statisticsEnabledExternal;

	@Mock
	Instance<Statistics> implementations;

	@InjectMocks
	StatisticsFactory factory;

	@Before
	public void init() {
		Mockito.when(statisticsEnabledExternal.get()).thenReturn(Boolean.TRUE);
	}

	@Test
	public void testStatsDisabled() {
		Mockito.when(statisticsEnabledExternal.get()).thenReturn(Boolean.FALSE);

		Assert.assertEquals(NoOpStatistics.INSTANCE, factory.produce());
	}

	@Test
	public void testAmbiguous() {
		Mockito.when(implementations.isAmbiguous()).thenReturn(true);

		Assert.assertEquals(NoOpStatistics.INSTANCE, factory.produce());
	}

	@Test
	public void testUnsatisfied() {
		Mockito.when(implementations.isUnsatisfied()).thenReturn(true);

		Assert.assertEquals(NoOpStatistics.INSTANCE, factory.produce());
	}

	@Test
	public void testProduceAndCache() {
		Statistics impl = Mockito.mock(Statistics.class);
		Mockito.when(implementations.get()).thenReturn(impl);

		Assert.assertEquals(impl, factory.produce());
		// from cache
		Assert.assertEquals(impl, factory.produce());
	}
}
