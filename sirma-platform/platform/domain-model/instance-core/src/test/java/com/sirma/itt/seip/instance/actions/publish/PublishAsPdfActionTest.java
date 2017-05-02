package com.sirma.itt.seip.instance.actions.publish;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link PublishAsPdfAction}
 *
 * @author BBonev
 */
public class PublishAsPdfActionTest {

	@InjectMocks
	private PublishAsPdfAction publishAction;

	@Mock
	private RevisionService revisionService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldCallRevisionServiceForPublish() throws Exception {

		PublishAsPdfActionRequest request = new PublishAsPdfActionRequest();
		request.setTargetReference(InstanceReferenceMock.createGeneric("emf:instance"));
		request.setUserOperation(PublishAsPdfActionRequest.ACTION_NAME);

		publishAction.perform(request);

		verify(revisionService).publish(argThat(CustomMatcher.of((PublishInstanceRequest rq) -> {
			return nullSafeEquals(rq.getInstanceToPublish().getId(), "emf:instance") && rq.isAsPdf();
		})));
	}

	@Test
	public void shouldReportCorrectName() throws Exception {
		assertEquals("publishAsPdf", publishAction.getName());
	}
}
