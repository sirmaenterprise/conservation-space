package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirmaenterprise.sep.bpm.camunda.actions.BPMActionRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;

/**
 * Tests for {@link BPMActionRestService}
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMActionRestServiceTest {

    @Mock
    private Actions actions;

    @InjectMocks
    private BPMActionRestService bpmActionRestService;

    @Test
    public void should_ReturnInstancesLoadResponseWithInstances_When_ExecuteClaimMethodIsCalled() {
        BPMClaimRequest request = Mockito.mock(BPMClaimRequest.class);
        Collection<Instance> instances = setupActions(request);

        Assert.assertEquals(instances, bpmActionRestService.executeClaim(request).getInstances());
    }

    @Test
    public void should_ReturnInstancesLoadResponseWithInstances_When_ExecuteReleaseMethodIsCalled() {
        BPMReleaseRequest request = Mockito.mock(BPMReleaseRequest.class);
        Collection<Instance> instances = setupActions(request);

        Assert.assertEquals(instances, bpmActionRestService.executeRelease(request).getInstances());
    }

    @Test
    public void should_ReturnInstancesLoadResponseWithInstances_When_ExecuteStopMethodIsCalled() {
        BPMStopRequest request = Mockito.mock(BPMStopRequest.class);
        Collection<Instance> instances = setupActions(request);

        Assert.assertEquals(instances, bpmActionRestService.executeStop(request).getInstances());
    }

    @Test
    public void should_ReturnInstancesLoadResponseWithInstances_When_ExecuteStartMethodIsCalled() {
        BPMStartRequest request = Mockito.mock(BPMStartRequest.class);
        Collection<Instance> instances = setupActions(request);

        Assert.assertEquals(instances, bpmActionRestService.executeStart(request).getInstances());
    }

    @Test
    public void should_ReturnInstancesLoadResponseWithInstances_When_ExecuteTransitionMethodIsCalled() {
        BPMTransitionRequest request = Mockito.mock(BPMTransitionRequest.class);
        Collection<Instance> instances = setupActions(request);

        Assert.assertEquals(instances, bpmActionRestService.executeTransition(request).getInstances());
    }

    private Collection<Instance> setupActions(BPMActionRequest request) {
        Collection<Instance> instances = Collections.singleton(Mockito.mock(Instance.class));
        Mockito.when(actions.callAction(request)).thenReturn(instances);
        return instances;
    }
}