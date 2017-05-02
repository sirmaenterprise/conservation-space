package com.sirma.itt.emf.audit.observer;

import javax.inject.Inject;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.exception.ContextNotActiveException;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Test the {@link AuditObserverHelper}.
 *
 * @author nvelkov
 */
public class AuditObserverHelperTest {

	private static final String EXPECTED_USER = "kaloqn";

	/** The needle rule. */
	@Rule
	public NeedleRule needleRule = new NeedleRule();

	/** The export service. */
	@ObjectUnderTest(id = "aes", implementation = AuditObserverHelper.class)
	private AuditObserverHelper observerHelper;

	@Inject
	private SecurityContext securityContext;

	/**
	 * Test user fetching when there is <b>NOT</b> a logged user.
	 */
	@Test(expected = ContextNotActiveException.class)
	public void testGetWithoutLoggedUser() {
		mockAuthenticationService(false);
		observerHelper.getCurrentUser();
	}

	/**
	 * Test user fetching when there is a logged user.
	 */
	@Test
	public void testGetCurrentUser() {
		mockAuthenticationService(true);
		User user = observerHelper.getCurrentUser();
		Assert.assertEquals(EXPECTED_USER, user.getIdentityId());
	}

	/**
	 * Mocks {@link javax.enterprise.inject.Instance} & {@link SecurityContext}.
	 *
	 * @param loggedIn
	 *            - defines if the mocked service should return a user or not
	 */
	private void mockAuthenticationService(final boolean loggedIn) {
		EasyMock.expect(securityContext.getAuthenticated()).andAnswer(() -> {
			if (loggedIn) {
				return new EmfUser(EXPECTED_USER);
			}
			throw new ContextNotActiveException();
		});
		EasyMock.replay(securityContext);
	}

}
