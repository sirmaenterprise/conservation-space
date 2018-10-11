package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.bpm.exception.BPMException;
import com.sirmaenterprise.sep.bpm.model.ProcessConstants;

/**
 * Test for {@link BPMClaimAction}.
 *
 * @author hlungov
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMClaimActionTest {

	@Mock
	private InstanceTypeResolver instanceResolver;
	@Mock
	private DomainInstanceService domainInstanceService;
	@Mock
	private CamundaBPMNService camundaBPMNService;
	@Mock
	private SecurityContext securityContext;

	@InjectMocks
	private BPMClaimAction bpmClaimAction;

	private static final String INSTANCE_ID = "emf:instanceId";
	private static final String ACTIVITI_ID = "emf:activitiId";
	private static final String EMF_USER_ID = "emf:userId";

	@Test
	public void executeBPMActionTest_Successful() throws BPMException {
		User user = Mockito.mock(User.class);
		when(user.getSystemId()).thenReturn(EMF_USER_ID);
		when(securityContext.getAuthenticated()).thenReturn(user);
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(ProcessConstants.ACTIVITY_ID, ACTIVITI_ID);
		Instance instance = buildInstance(INSTANCE_ID, properties);
		when(instance.isValueNotNull(Mockito.anyString())).thenReturn(Boolean.TRUE);
		BPMClaimRequest request = new BPMClaimRequest();
		request.setTargetId(INSTANCE_ID);
		request.setTargetReference(instance.toReference());
		bpmClaimAction.executeBPMAction(request);
		Mockito.verify(domainInstanceService).save(Mockito.any(InstanceSaveContext.class));
		Mockito.verify(camundaBPMNService).claimTask(instance, EMF_USER_ID);
	}

	@Test
	public void executeBPMActionTest_Not_An_Activity() throws BPMException {
		Instance instance = buildInstance(INSTANCE_ID, Collections.emptyMap());
		when(instance.isValueNotNull(Mockito.anyString())).thenReturn(Boolean.FALSE);
		BPMClaimRequest request = new BPMClaimRequest();
		request.setTargetId(INSTANCE_ID);
		request.setTargetReference(instance.toReference());
		bpmClaimAction.executeBPMAction(request);
		Mockito.verify(domainInstanceService, Mockito.never()).save(Mockito.any(InstanceSaveContext.class));
		Mockito.verify(camundaBPMNService, Mockito.never()).claimTask(instance, EMF_USER_ID);
	}

	@Test
	public void getNameTest() {
		Assert.assertEquals(BPMClaimRequest.CLAIM_OPERATION, bpmClaimAction.getName());
	}

	private Instance buildInstance(String targetId, Map<String, Serializable> properties) {
		Instance instance = mock(Instance.class);
		when(instance.getId()).thenReturn(targetId);
		when(instance.getOrCreateProperties()).thenReturn(properties);
		when(instance.getProperties()).thenReturn(properties);
		InstanceReference instanceRef = mock(InstanceReference.class);
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.is(Mockito.anyString())).thenReturn(Boolean.TRUE);
		when(instance.type()).thenReturn(instanceType);
		when(instance.toReference()).thenReturn(instanceRef);
		when(instanceRef.toInstance()).thenReturn(instance);
		when(instanceRef.getId()).thenReturn(targetId);
		Collection<Instance> instances = Collections.singletonList(instance);
		when(instanceResolver.resolveInstances(eq(Collections.singletonList(targetId)))).thenReturn(instances);
		return instance;
	}

}
