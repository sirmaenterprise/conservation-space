package com.sirma.cmf.web.navigation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Stack;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Test for EmfNavigationSession class.
 *
 * @author svelikov
 */
@Test
public class EmfNavigationSessionTest {

	private static final int MAX_NAVIGATION_POINTS_SIZE = 5;
	private static final String USER_DASHBOARD_DASHBOARD_XHTML = "/userDashboard/dashboard.xhtml";
	private static final String TEST = "test";
	private static final String DASHBOARD = "dashboard";
	private final EmfNavigationSession emfNavigationSession;

	/**
	 * Instantiates a new emf navigation session test.
	 */
	public EmfNavigationSessionTest() {
		emfNavigationSession = new EmfNavigationSession();

		ReflectionUtils.setField(emfNavigationSession, "maxHistoryNavigationPoints",
				new ConfigurationPropertyMock<>(MAX_NAVIGATION_POINTS_SIZE));
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

	public void pushNullToReturnPointAndCheckIfOmmited() {
		emfNavigationSession.pushReturnPoint(null);
		Stack<NavigationPoint> returnPointStack = emfNavigationSession.getReturnPointStack();
		assertTrue(returnPointStack.isEmpty());
	}

	public void pushPointToReturnPointAndCheckIfThere() {
		NavigationPoint navigationPoint = new NavigationPoint();
		navigationPoint.setOutcome(TEST);
		emfNavigationSession.pushReturnPoint(navigationPoint);
		Stack<NavigationPoint> returnPointStack = emfNavigationSession.getReturnPointStack();
		assertTrue(returnPointStack.size() == 1);
	}

	/**
	 * Test for pushHomePagePoint method.
	 */
	public void pushHomePagePointAndCheckIfItsThere() {
		emfNavigationSession.pushHomePagePoint();

		assertTrue(emfNavigationSession.getTrailPointStack().size() == 2);
		NavigationPoint trailPoint = emfNavigationSession.getTrailPoint();
		assertEquals(trailPoint.getOutcome(), DASHBOARD);
		assertEquals(trailPoint.getViewId(), USER_DASHBOARD_DASHBOARD_XHTML);
	}

	/**
	 * Pop return point.
	 */
	public void popReturnPointAndCheckIfReturnsProperPoint() {
		NavigationPoint navigationPoint = new NavigationPoint();
		navigationPoint.setOutcome(TEST);
		emfNavigationSession.pushReturnPoint(navigationPoint);
		NavigationPoint returnPoint = emfNavigationSession.popReturnPoint();
		assertNotNull(returnPoint);
		assertEquals(returnPoint.getOutcome(), TEST);
	}

	/**
	 * Checks if is return point stack empty returns false when empty.
	 */
	public void isReturnPointStackEmptyReturnsFalseWhenEmpty() {
		boolean isEmpty = emfNavigationSession.isReturnPointStackEmpty();
		assertTrue(isEmpty);
	}

	/**
	 * Checks if is return point stack empty returns true when not empty.
	 */
	public void isReturnPointStackEmptyReturnsTrueWhenNotEmpty() {
		NavigationPoint navigationPoint = new NavigationPoint();
		navigationPoint.setOutcome(TEST);
		emfNavigationSession.pushReturnPoint(navigationPoint);
		boolean isEmpty = emfNavigationSession.isReturnPointStackEmpty();
		assertFalse(isEmpty);
	}

	/**
	 * Push home page point above another and check if there.
	 */
	public void pushHomePagePointAboveAnotherAndCheckIfThere() {
		NavigationPoint navigationPoint = new NavigationPoint();
		navigationPoint.setOutcome(TEST);
		emfNavigationSession.pushTrailPoint(navigationPoint);
		emfNavigationSession.pushHomePagePoint();
		assertTrue(emfNavigationSession.getTrailPointStack().size() == 3);
		NavigationPoint trailPoint = emfNavigationSession.getTrailPoint();
		assertEquals(trailPoint.getOutcome(), DASHBOARD);
		assertEquals(trailPoint.getViewId(), USER_DASHBOARD_DASHBOARD_XHTML);
	}

	/**
	 * Push null trail point should be ommited.
	 */
	public void pushNullTrailPointShouldBeOmmited() {
		emfNavigationSession.pushTrailPoint(null);
		assertTrue(emfNavigationSession.getTrailPointStack().size() == 1);
	}

	/**
	 * Push valid trail point and check if there.
	 */
	public void pushValidTrailPointAndCheckIfThere() {
		// new navigation point that is different from one on top of the trailing point stack should
		// be added
		NavigationPoint navigationPoint = new NavigationPoint();
		navigationPoint.setOutcome(TEST);
		emfNavigationSession.pushTrailPoint(navigationPoint);
		assertTrue(emfNavigationSession.getTrailPointStack().size() == 2);
		assertEquals(emfNavigationSession.getTrailPointStack().get(1), navigationPoint);
	}

	/**
	 * Dont push same trail point twice.
	 */
	public void dontPushSameTrailPointTwice() {
		// don't add same point twice on top
		// NavigationPoint navigationPoint = new NavigationPoint();
		// navigationPoint.setOutcome(TEST);
		// emfNavigationSession.pushTrailPoint(navigationPoint);
		// emfNavigationSession.pushTrailPoint(navigationPoint);
		// assertTrue(emfNavigationSession.getTrailPointStack().size() == 2);
	}

	/**
	 * When push in full trail point stack bottom points are removed.
	 */
	public void whenPushInFullTrailPointStackBottomPointsAreRemoved() {
		// push to max and then check if the first added are removed from the bottom
		for (int i = 0; i < 4; i++) {
			NavigationPoint point = new NavigationPoint();
			point.setOutcome(TEST + i);
			emfNavigationSession.pushTrailPoint(point);
		}
		NavigationPoint newPoint = new NavigationPoint();
		newPoint.setOutcome("newOutcome");
		emfNavigationSession.pushTrailPoint(newPoint);
		assertTrue(emfNavigationSession.getTrailPointStack().size() == MAX_NAVIGATION_POINTS_SIZE);
		assertEquals(emfNavigationSession.getTrailPointStack().get(4), newPoint);
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
