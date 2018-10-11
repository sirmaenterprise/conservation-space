package com.sirma.itt.seip.instance.version.revert;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.Serializable;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link SaveRevertedInstanceStep}.
 *
 * @author A. Kunchev
 */
public class SaveRevertedInstanceStepTest {

	@InjectMocks
	private SaveRevertedInstanceStep step;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Mock
	private InstanceContentService instanceContentService;

	@Before
	public void setup() {
		step = new SaveRevertedInstanceStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("saveRevertedInstance", step.getName());
	}

	@Test
	public void invoke_saveCalled() {
		RevertContext context = RevertContext
				.create("instance-id-v1.7")
					.setRevertResultInstance(new EmfInstance())
					.setOperation(new Operation());
		step.invoke(context);
		verify(domainInstanceService).save(any(InstanceSaveContext.class));
	}

	@Test
	public void rollback_noSaveContext_servicesNotCalled() {
		step.rollback(RevertContext.create("instance-id-v1.7"));
		verifyZeroInteractions(instanceContentService, instanceVersionService);
	}

	@Test
	public void rollback_withSaveContext() {
		RevertContext context = RevertContext.create("instance-id-v1.7");
		context.put("$saveContext$", InstanceSaveContext
				.create(new EmfInstance(), new Operation())
					.setViewId(Optional.of("instance-view-content-id")));
		step.rollback(context);

		verify(instanceContentService).deleteContent(any(Serializable.class), eq(null));
		verify(instanceVersionService).deleteVersion(any(Serializable.class));
	}

}
