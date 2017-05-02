/**
 *
 */
package com.sirma.itt.seip.instance.dao;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * @author BBonev
 */
public class ChainingInstanceLoadDecoratorTest extends EmfTest {

	@InjectMocks
	ChainingInstanceLoadDecorator decorator;

	@Spy
	ConfigurationProperty<Boolean> parallelPostInstanceLoading = new ConfigurationPropertyMock<>(Boolean.TRUE);

	@Spy
	List<InstanceLoadDecorator> decorators = new ArrayList<>();

	@Mock
	InstanceLoadDecorator asyncDecorator;
	@Mock
	InstanceLoadDecorator syncDecorator;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
		decorators.clear();
		decorators.add(asyncDecorator);
		decorators.add(syncDecorator);
		when(syncDecorator.allowParallelProcessing()).thenReturn(Boolean.FALSE);
	}

	@Test
	public void test_decorateInstance() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");

		decorator.decorateInstance(instance);

		verify(asyncDecorator).decorateInstance(instance);
		verify(syncDecorator).decorateInstance(instance);
	}

	@Test
	public void test_decorateInstances() {
		EmfInstance instance1 = new EmfInstance();
		instance1.setId("emf:instance1");
		EmfInstance instance2 = new EmfInstance();
		instance2.setId("emf:instance2");

		List<EmfInstance> list = Arrays.asList(instance1, instance2);
		decorator.decorateResult(list);

		verify(asyncDecorator).decorateResult(list);
		verify(syncDecorator).decorateResult(list);
	}
}
