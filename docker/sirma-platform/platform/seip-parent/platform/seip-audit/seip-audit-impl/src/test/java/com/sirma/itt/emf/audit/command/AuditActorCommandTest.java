package com.sirma.itt.emf.audit.command;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.instance.AuditActorCommand;
import com.sirma.itt.emf.audit.command.instance.AuditContextCommand;
import com.sirma.itt.emf.audit.observer.AuditObserverHelper;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the label assignment in {@link AuditContextCommand}.
 *
 * @author Mihail Radkov
 */
public class AuditActorCommandTest {

	private static final String EXPECTED_USER_NAME = "someUser";
	private static final String EXPECTED_USER_ID = "someUserId";

	@InjectMocks
	private AuditActorCommand command;

	@Mock
	private AuditObserverHelper auditObserverHelper;

	@Mock
	private ResourceService resourceService;

	@Mock
	private HeadersService headersService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests the retrieval of the user executing the audited operation
	 */
	@Test
	public void testUsernameCommand() {
		AuditablePayload payload = AuditCommandTest.getTestPayload();
		AuditActivity activity = new AuditActivity();

		EmfUser user = new EmfUser(EXPECTED_USER_NAME);
		user.setId(EXPECTED_USER_ID);
		when(auditObserverHelper.getCurrentUser()).thenReturn(user);

		// Correct test
		command.execute(payload, activity);
		assertEquals(EXPECTED_USER_NAME, activity.getUserName());
		assertEquals(EXPECTED_USER_ID, activity.getUserId());

		// Null instance
		activity = new AuditActivity();
		payload = new AuditablePayload(null, null, null, true);
		command.execute(payload, activity);
		assertEquals(EXPECTED_USER_NAME, activity.getUserName());
	}

	/**
	 * Tests the logic in {@link AuditContextCommand#assignLabel(AuditActivity, AuditContext)}.
	 */
	@Test
	public void testAssignUsernameLabel() {
		AuditActivity activity = new AuditActivity();
		activity.setUserName("the_username");
		Resource resource = mock(Resource.class);

		when(resourceService.findResource("the_username")).thenReturn(resource);
		when(headersService.generateInstanceHeader(resource, DefaultProperties.HEADER_BREADCRUMB)).thenReturn("awesome_label");

		command.assignLabel(activity, null);

		Assert.assertEquals("awesome_label", activity.getUserDisplayName());
	}

}
