package com.sirma.cmf.web.navigation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;

/**
 * Test for EmfNavigationSession class.
 * 
 * @author svelikov
 */
@Test
public class EmfNavigationSessionTest {

	private static final String USER_DASHBOARD_DASHBOARD_XHTML = "/userDashboard/dashboard.xhtml";
	private static final String TEST = "test";
	private static final String DASHBOARD = "dashboard";
	private final EmfNavigationSession emfNavigationSession;

	/**
	 * Instantiates a new emf navigation session test.
	 */
	public EmfNavigationSessionTest() {
		emfNavigationSession = new EmfNavigationSession();

		ReflectionUtils.setField(emfNavigationSession, "maxHistoryNavigationPoints", 20);
	}

	/**
	 * Reset test.
	 */
	@BeforeMethod
	public void resetTest() {
		emfNavigationSession.init();
	}

	/**
	 * Test for init method.
	 */
	public void initTest() {
		emfNavigationSession.init();
		assertTrue(emfNavigationSession.getTrailPointStack() != null);
		assertTrue(emfNavigationSession.getTrailPointStack().size() == 1);
		NavigationPoint trailPoint = emfNavigationSession.getTrailPoint();
		assertEquals(trailPoint.getOutcome(), DASHBOARD);
		assertEquals(trailPoint.getViewId(), USER_DASHBOARD_DASHBOARD_XHTML);
	}

	/**
	 * Test for pushHomePagePoint method.
	 */
	public void pushHomePagePointTest() {
		emfNavigationSession.pushHomePagePoint();

		// don't duplicate navigation points
		assertTrue(emfNavigationSession.getTrailPointStack().size() == 1);
		NavigationPoint trailPoint = emfNavigationSession.getTrailPoint();
		assertEquals(trailPoint.getOutcome(), DASHBOARD);
		assertEquals(trailPoint.getViewId(), USER_DASHBOARD_DASHBOARD_XHTML);

		//
		NavigationPoint navigationPoint = new NavigationPoint();
		navigationPoint.setOutcome(TEST);
		emfNavigationSession.pushTrailPoint(navigationPoint);
		emfNavigationSession.pushHomePagePoint();
		assertTrue(emfNavigationSession.getTrailPointStack().size() == 3);
		trailPoint = emfNavigationSession.getTrailPoint();
		assertEquals(trailPoint.getOutcome(), DASHBOARD);
		assertEquals(trailPoint.getViewId(), USER_DASHBOARD_DASHBOARD_XHTML);
	}

	/**
	 * Test for pushDummyPoint method.
	 */
	public void pushDummyPointTest() {
		emfNavigationSession.pushDummyPoint();

		assertTrue(emfNavigationSession.getTrailPointStack().size() == 2);
		NavigationPoint trailPoint = emfNavigationSession.getTrailPoint();
		assertEquals(trailPoint.getOutcome(), NavigationHandlerConstants.UNSAFE);
	}

	/**
	 * Test for pushTrailPoint method.
	 */
	public void pushTrailPointTest() {
		// don't add nulls
		emfNavigationSession.pushTrailPoint(null);
		assertTrue(emfNavigationSession.getTrailPointStack().size() == 1);

		// new navigation point that is different from one on top of the trailing point stack should
		// be added
		NavigationPoint navigationPoint = new NavigationPoint();
		navigationPoint.setOutcome(TEST);
		emfNavigationSession.pushTrailPoint(navigationPoint);
		assertTrue(emfNavigationSession.getTrailPointStack().size() == 2);
		assertEquals(emfNavigationSession.getTrailPointStack().get(1), navigationPoint);

		// don't add same point twice on top
		emfNavigationSession.pushTrailPoint(navigationPoint);
		assertTrue(emfNavigationSession.getTrailPointStack().size() == 2);

		// if there is UNSAFE point on top, it should be removed before add new SAFE point
		NavigationPoint unsafePoint = new NavigationPoint();
		navigationPoint.setOutcome(NavigationHandlerConstants.UNSAFE);
		emfNavigationSession.pushTrailPoint(unsafePoint);
		emfNavigationSession.pushTrailPoint(navigationPoint);
		assertTrue(emfNavigationSession.getTrailPointStack().size() == 3);
		assertEquals(emfNavigationSession.getTrailPointStack().get(2), navigationPoint);
	}

	/**
	 * Test for getTrailPoint method.
	 */
	public void getTrailPointTest() {
		//
		NavigationPoint trailPoint = emfNavigationSession.getTrailPoint();
		assertEquals(trailPoint.getOutcome(), DASHBOARD);

		//
		NavigationPoint navigationPoint = new NavigationPoint();
		navigationPoint.setOutcome(TEST);
		emfNavigationSession.pushTrailPoint(navigationPoint);
		trailPoint = emfNavigationSession.getTrailPoint();
		assertEquals(trailPoint, navigationPoint);

		//
		emfNavigationSession.getTrailPointStack().clear();
		trailPoint = emfNavigationSession.getTrailPoint();
		assertNull(trailPoint);
	}

	/**
	 * Test for getOverTrailPoint method.
	 */
	public void getOverTrailPointTest() {
		// initially we have only dashboard and this method should return null because there is only
		// one point in the stack
		NavigationPoint overTrailPoint = emfNavigationSession.getOverTrailPoint();
		assertNull(overTrailPoint);

		//
		NavigationPoint navigationPoint = new NavigationPoint();
		navigationPoint.setOutcome(TEST);
		emfNavigationSession.pushTrailPoint(navigationPoint);
		overTrailPoint = emfNavigationSession.getOverTrailPoint();
		assertEquals(overTrailPoint.getOutcome(), DASHBOARD);
	}

	/**
	 * Test for popTrailPoint method.
	 */
	public void popTrailPointTest() {
		// if stack is empty then null should be returned
		emfNavigationSession.getTrailPointStack().clear();
		NavigationPoint popTrailPoint = emfNavigationSession.popTrailPoint();
		assertNull(popTrailPoint);

		// if there is a single point in the stack we should get that instance without removing from
		// the stack
		emfNavigationSession.init();
		popTrailPoint = emfNavigationSession.popTrailPoint();
		assertTrue(emfNavigationSession.getTrailPointStack().size() == 1);
		assertEquals(popTrailPoint.getOutcome(), DASHBOARD);

		// if there is more than one point we should get the last point
		NavigationPoint navigationPoint = new NavigationPoint();
		navigationPoint.setOutcome(TEST);
		emfNavigationSession.pushTrailPoint(navigationPoint);
		popTrailPoint = emfNavigationSession.popTrailPoint();
		assertEquals(popTrailPoint, navigationPoint);
		assertTrue(emfNavigationSession.getTrailPointStack().size() == 1);
	}

	/**
	 * Test for isTrailPointStackEmpty method.
	 */
	public void isTrailPointStackEmptyTest() {
		boolean isEmpty = emfNavigationSession.isTrailPointStackEmpty();
		assertFalse(isEmpty);

		emfNavigationSession.getTrailPointStack().clear();
		isEmpty = emfNavigationSession.isTrailPointStackEmpty();
		assertTrue(isEmpty);
	}
}
