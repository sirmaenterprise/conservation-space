package com.sirma.itt.seip.instance.actions.save;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Test for {@link CreateOrUpdateAction}.
 *
 * @author A. Kunchev
 */
public class CreateOrUpdateActionTest {

	@InjectMocks
	private CreateOrUpdateAction action;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Before
	public void setup() {
		action = new CreateOrUpdateAction();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals(CreateOrUpdateRequest.OPERATION_NAME, action.getName());
	}

	@Test
	public void perform_successful() {
		EmfInstance instance = new EmfInstance();
		Date versionCreatedOn = new Date();
		CreateOrUpdateRequest request = new CreateOrUpdateRequest();
		request.setTarget(instance);
		request.setVersionCreatedOn(versionCreatedOn);
		request.setUserOperation(ActionTypeConstants.CREATE);

		action.perform(request);

		Mockito.verify(domainInstanceService).save(argThat(CustomMatcher.of((InstanceSaveContext context) -> {
			assertEquals(instance, context.getInstance());
			assertEquals(versionCreatedOn, context.getVersionCreationDate());
			assertEquals(ActionTypeConstants.CREATE, context.getOperation().getOperation());
			assertEquals(ActionTypeConstants.CREATE, context.getOperation().getUserOperationId());
		})));
	}

}
