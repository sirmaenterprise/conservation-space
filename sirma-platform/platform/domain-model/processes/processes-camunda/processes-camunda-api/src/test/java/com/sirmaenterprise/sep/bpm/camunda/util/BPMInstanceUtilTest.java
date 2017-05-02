package com.sirmaenterprise.sep.bpm.camunda.util;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link BPMInstanceUtil}.
 *
 * @author hlungov
 */
public class BPMInstanceUtilTest {

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void resolveInstanceTest_null_check() {
		InstanceTypeResolver instanceTypeResolver = mock(InstanceTypeResolver.class);
		BPMInstanceUtil.resolveInstance(null, instanceTypeResolver);
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void resolveInstanceTest_empty_check() {
		InstanceTypeResolver instanceTypeResolver = mock(InstanceTypeResolver.class);
		BPMInstanceUtil.resolveInstance("", instanceTypeResolver);
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void resolveReferenceTest_null_check() {
		InstanceTypeResolver instanceTypeResolver = mock(InstanceTypeResolver.class);
		BPMInstanceUtil.resolveReference(null, instanceTypeResolver);
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void resolveReferenceTest_empty_check() {
		InstanceTypeResolver instanceTypeResolver = mock(InstanceTypeResolver.class);
		BPMInstanceUtil.resolveReference("", instanceTypeResolver);
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void resolveReferenceTest_instance_not_found() {
		InstanceTypeResolver instanceResolver = mock(InstanceTypeResolver.class);
		when(instanceResolver.resolveReference("testId")).thenReturn(Optional.empty());
		BPMInstanceUtil.resolveReference("testId", instanceResolver);
	}

	@Test
	public void resolveReferenceTest() {
		InstanceTypeResolver instanceResolver = mock(InstanceTypeResolver.class);
		InstanceReference found = mock(InstanceReference.class);
		when(instanceResolver.resolveReference("testId")).thenReturn(Optional.of(found));
		Assert.assertEquals(found,BPMInstanceUtil.resolveReference("testId", instanceResolver));
	}

}