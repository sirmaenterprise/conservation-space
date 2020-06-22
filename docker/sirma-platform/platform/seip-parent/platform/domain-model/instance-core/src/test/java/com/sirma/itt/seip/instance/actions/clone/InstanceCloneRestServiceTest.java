package com.sirma.itt.seip.instance.actions.clone;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.DomainInstanceServiceImpl;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Tests for {@link InstanceCloneRestService}.
 *
 * @author Ivo Rusev on 20.12.2016
 */
public class InstanceCloneRestServiceTest {

    private static final String EMF_ID = "emf:id";

    @InjectMocks
    private InstanceCloneRestService service;

    @Mock
    private Actions actions;

    @Mock
    private DomainInstanceServiceImpl domainInstanceService;


    @Before
    public void setup() {
        service = new InstanceCloneRestService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteCloneAction() {
        InstanceCloneRequest request = new InstanceCloneRequest();
        service.executeCloneAction(request);
        verify(actions).callAction(request);
    }

    @Test
    public void testClone() {
        Instance instance = Mockito.mock(Instance.class);
        Mockito.when(domainInstanceService.clone(EMF_ID, new Operation(ActionTypeConstants.CLONE, true))).thenReturn(instance);
		assertNotNull(service.clone(EMF_ID));
    }
}