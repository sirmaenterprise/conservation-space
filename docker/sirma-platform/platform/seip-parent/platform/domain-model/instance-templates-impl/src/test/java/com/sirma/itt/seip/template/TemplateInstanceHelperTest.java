package com.sirma.itt.seip.template;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Tests the functionality of {@link TemplateInstanceHelper}.
 * 
 * @author Vilizar Tsonev
 */
public class TemplateInstanceHelperTest {

	@InjectMocks
	private TemplateInstanceHelper templateInstanceHelper;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Save_Instance_With_Correct_Arguments_When_Demoting_It() {
		Instance instanceToDemote = new EmfInstance();
		instanceToDemote.setId("instanceToDemote");

		templateInstanceHelper.demoteInstance(instanceToDemote, "test");

		ArgumentCaptor<InstanceSaveContext> captor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService).save(captor.capture());
		assertEquals(instanceToDemote, captor.getValue().getInstance());
		assertEquals(Operation.NO_OPERATION, captor.getValue().getOperation());
	}
}
