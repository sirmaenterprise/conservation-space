/**
 *
 */
package com.sirma.itt.seip.instance.dao;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.version.VersionProperties;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Test for {@link ChainingInstanceLoadDecorator}.
 *
 * @author BBonev
 */
@RunWith(MockitoJUnitRunner.class)
public class ChainingInstanceLoadDecoratorTest extends EmfTest {

	@InjectMocks
	private ChainingInstanceLoadDecorator decorator;

	@Spy
	private ConfigurationProperty<Boolean> parallelPostInstanceLoading = new ConfigurationPropertyMock<>(Boolean.TRUE);

	@Mock
	private InstanceLoadDecorator asyncDecorator;

	@Mock
	private InstanceLoadDecorator syncDecorator;

	private List<InstanceLoadDecorator> plugins = new ArrayList<>();

	@Spy
	private Plugins<InstanceLoadDecorator> decorators = new Plugins<>(InstanceLoadDecorator.INSTANCE_DECORATOR,
			plugins);

	@Mock
	private InstanceLoadDecorator versionAsyncDecorator;

	@Mock
	private InstanceLoadDecorator versionSyncDecorator;

	private List<InstanceLoadDecorator> versionPlugins = new ArrayList<>();

	@Spy
	private Plugins<InstanceLoadDecorator> versionDecorators = new Plugins<>(
			InstanceLoadDecorator.VERSION_INSTANCE_DECORATOR, versionPlugins);

	@Before
	@Override
	public void beforeMethod() {
		super.beforeMethod();

		plugins.clear();
		plugins.add(asyncDecorator);
		plugins.add(syncDecorator);

		versionPlugins.clear();
		versionPlugins.add(versionAsyncDecorator);
		versionPlugins.add(versionSyncDecorator);

		when(syncDecorator.allowParallelProcessing()).thenReturn(Boolean.FALSE);
	}

	@Test
	public void decorateInstance_nullInstance() {
		decorator.decorateInstance(null);

		verifyZeroInteractions(decorators, versionDecorators);
	}

	@Test
	public void decorateInstance_alreadyDecorated() {
		EmfInstance instance = new EmfInstance("emf:instance");
		instance.add("$decorated$", Boolean.TRUE);
		decorator.decorateInstance(instance);

		verifyZeroInteractions(decorators, versionDecorators);
	}

	@Test
	public void decorateInstance() {
		EmfInstance instance = new EmfInstance("emf:instance");

		decorator.decorateInstance(instance);

		verify(asyncDecorator).decorateInstance(instance);
		verify(syncDecorator).decorateInstance(instance);
		assertNotNull(instance.get("$decorated$"));
		verifyZeroInteractions(versionDecorators);
	}

	@Test
	public void decorateVersion() {
		EmfInstance instance = new EmfInstance("emf:instance-v1.1");

		decorator.decorateInstance(instance);

		verify(versionAsyncDecorator).decorateInstance(instance);
		verify(versionSyncDecorator).decorateInstance(instance);
		assertNotNull(instance.get("$decorated$"));
		verifyZeroInteractions(decorators);
	}

	@Test
	public void decorateInstances_emptyCollection() {
		decorator.decorateResult(Collections.emptyList());

		verifyZeroInteractions(decorators);
	}

	@Test
	public void decorateInstances_allDecorated() {
		EmfInstance instance1 = new EmfInstance("emf:instance1-decorated");
		instance1.add("$decorated$", Boolean.TRUE);
		EmfInstance instance2 = new EmfInstance("emf:instance2-decorated");
		instance2.add("$decorated$", Boolean.TRUE);

		List<EmfInstance> list = Arrays.asList(instance1, instance2);
		decorator.decorateResult(list);

		verifyZeroInteractions(decorators);
	}

	@Test
	public void decorateInstances() {
		EmfInstance instance1 = new EmfInstance("emf:instance1-decorated");
		instance1.add("$decorated$", Boolean.TRUE);
		EmfInstance instance2 = new EmfInstance("emf:instance2");

		List<EmfInstance> list = Arrays.asList(instance1, instance2);
		decorator.decorateResult(list);

		verify(asyncDecorator).decorateResult(onlyOneDecorated());
		verify(syncDecorator).decorateResult(onlyOneDecorated());
		verifyZeroInteractions(versionDecorators);
	}

	private static Collection<Instance> onlyOneDecorated() {
		return argThat(CustomMatcher.ofPredicate(instances -> instances.size() == 1));
	}

	@Test
	public void decorateVersions() {
		EmfInstance decoratedVersion = new EmfInstance("emf:instance1-decorated-v1.2");
		decoratedVersion.add("$decorated$", Boolean.TRUE);
		EmfInstance version = new EmfInstance("emf:instance2-v2.2");
		version.add(VersionProperties.IS_VERSION, true);

		List<EmfInstance> list = Arrays.asList(decoratedVersion, version);
		decorator.decorateResult(list);

		verify(versionAsyncDecorator).decorateResult(onlyOneDecorated());
		verify(versionSyncDecorator).decorateResult(onlyOneDecorated());
		verifyZeroInteractions(decorators);
	}

	@Test
	public void decorateMixed() {
		EmfInstance decoratedInstance = new EmfInstance("emf:instance-decorated");
		decoratedInstance.add("$decorated$", Boolean.TRUE);
		EmfInstance instance = new EmfInstance("emf:instance");

		EmfInstance decoratedVersion = new EmfInstance("emf:instance-decorated-v1.2");
		decoratedVersion.add("$decorated$", Boolean.TRUE);
		EmfInstance version = new EmfInstance("emf:instance-v2.2");

		List<EmfInstance> list = Arrays.asList(decoratedInstance, instance, decoratedVersion, version);
		decorator.decorateResult(list);

		verify(versionAsyncDecorator).decorateResult(onlyOneDecorated());
		verify(versionSyncDecorator).decorateResult(onlyOneDecorated());
		verify(asyncDecorator).decorateResult(onlyOneDecorated());
		verify(syncDecorator).decorateResult(onlyOneDecorated());
	}
}