package com.sirma.itt.emf.audit.observer;

import javax.inject.Inject;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.cmf.help.HelpRequestEvent;
import com.sirma.itt.emf.audit.processor.AuditProcessor;
import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.event.UserPasswordChangeEvent;
import com.sirma.itt.seip.security.UserStore;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Test the {@link AuditObserverHelper}.
 *
 * @author nvelkov
 */
public class AuditActionObserverTest {

	private static final String USER_NAME = "some user";
	/** the needle to mock access. */
	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@Inject
	private AuditProcessor auditProcessor;
	@Inject
	private UserStore userStore;

	@ObjectUnderTest(id = "aes", implementation = AuditActionObserver.class)
	private AuditActionObserver actionObserver;
	@ObjectUnderTest(id = "aes", implementation = SecurityAuditObserver.class)
	private SecurityAuditObserver securityAuditObserver;

	/**
	 * Test the login observing.
	 */
	@Test
	public void testLogin() {
		Capture<EmfUser> capturedUser = Capture.newInstance();
		Capture<String> capturedId = Capture.newInstance();
		Capture<EmfEvent> event = Capture.newInstance();
		auditProcessor.auditUserOperation(EasyMock.capture(capturedUser), EasyMock.capture(capturedId),
				EasyMock.capture(event));
		EmfUser authenticatedUser = new EmfUser(USER_NAME);
		EasyMock.expect(userStore.wrap(authenticatedUser)).andReturn(authenticatedUser);
		EasyMock.replay(userStore);
		EasyMock.replay(auditProcessor);

		securityAuditObserver.onLogin(new UserAuthenticatedEvent(authenticatedUser));

		Assert.assertTrue(capturedUser.hasCaptured());
		Assert.assertEquals(USER_NAME, capturedUser.getValue().getName());
		Assert.assertTrue(capturedId.hasCaptured());
		Assert.assertEquals("login", capturedId.getValue());
		Assert.assertTrue(event.hasCaptured());
	}

	/**
	 * Test the logout observing.
	 */
	@Test
	public void testLogout() {
		Capture<EmfUser> capturedUser = Capture.newInstance();
		Capture<String> capturedId = Capture.newInstance();
		Capture<EmfEvent> event = Capture.newInstance();
		auditProcessor.auditUserOperation(EasyMock.capture(capturedUser), EasyMock.capture(capturedId),
				EasyMock.capture(event));
		
		EmfUser authenticatedUser = new EmfUser(USER_NAME);
		EasyMock.expect(userStore.wrap(authenticatedUser)).andReturn(authenticatedUser);
		EasyMock.replay(userStore);
		EasyMock.replay(auditProcessor);
		
		securityAuditObserver.onLogout(new UserLogoutEvent(authenticatedUser));

		Assert.assertTrue(capturedUser.hasCaptured());
		Assert.assertEquals(USER_NAME, capturedUser.getValue().getName());
		Assert.assertTrue(capturedId.hasCaptured());
		Assert.assertEquals("logout", capturedId.getValue());
		Assert.assertTrue(event.hasCaptured());
	}

	/**
	 * Test the help request observing.
	 */
	@Test
	public void testHelpRequest() {
		Capture<String> capturedId = Capture.newInstance();
		Capture<EmfEvent> event = Capture.newInstance();
		auditProcessor.process(EasyMock.anyObject(Instance.class), EasyMock.capture(capturedId),
				EasyMock.capture(event));
		EasyMock.replay(auditProcessor);

		actionObserver.onHelpRequest(new HelpRequestEvent());

		Assert.assertTrue(capturedId.hasCaptured());
		Assert.assertEquals("helpRequest", capturedId.getValue());
		Assert.assertTrue(event.hasCaptured());
	}

	/**
	 * Test the password change observing.
	 */
	@Test
	public void testPasswordChange() {
		Capture<String> capturedId = Capture.newInstance();
		Capture<EmfEvent> event = Capture.newInstance();
		auditProcessor.process(EasyMock.anyObject(Instance.class), EasyMock.capture(capturedId),
				EasyMock.capture(event));
		EasyMock.replay(auditProcessor);

		securityAuditObserver.onPasswordChange(new UserPasswordChangeEvent());

		Assert.assertTrue(capturedId.hasCaptured());
		Assert.assertEquals("changePassword", capturedId.getValue());
		Assert.assertTrue(event.hasCaptured());
	}

}
