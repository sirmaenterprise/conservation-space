package com.sirma.itt.seip.instance.version.revert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.VersionProperties;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link InitialRevertStep}.
 *
 * @author A. Kunchev
 */
public class InitialRevertStepTest {

	@InjectMocks
	private InitialRevertStep step;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private InstanceService instanceService;

	@Before
	public void setup() {
		step = new InitialRevertStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("initRevert", step.getName());
	}

	@Test(expected = InstanceNotFoundException.class)
	public void invoke_currentInstanceNotFound() {
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.empty());
		step.invoke(RevertContext.create("instance-id-v1.3"));
	}

	@Test
	public void invoke_instancesLoaded_cloneCalled() {
		Instance version = new EmfInstance("instance-id-v1.3");
		version.add(VersionProperties.IS_VERSION, true);
		when(instanceVersionService.loadVersion("instance-id-v1.3")).thenReturn(version);
		when(instanceTypeResolver.resolveReference("instance-id"))
				.thenReturn(Optional.of(InstanceReferenceMock.createGeneric("instnace-id")));
		when(instanceService.deepClone(eq(version), any(Operation.class)))
				.then(a -> a.getArgumentAt(0, Instance.class));

		RevertContext context = RevertContext.create("instance-id-v1.3");
		step.invoke(context);

		verify(instanceService).deepClone(eq(version), any(Operation.class));
		assertNotNull(context.getCurrentInstance());
		Instance resultInstance = context.getRevertResultInstance();
		assertNotNull(resultInstance);
		assertFalse(containsVersionProperty(resultInstance));
	}

	private static boolean containsVersionProperty(Instance resultInstance) {
		return resultInstance.getProperties().keySet().stream().anyMatch(VersionProperties.getVersionProperties(
				Collections.singleton(VersionProperties.DEFINITION_ID))::contains);
	}
}