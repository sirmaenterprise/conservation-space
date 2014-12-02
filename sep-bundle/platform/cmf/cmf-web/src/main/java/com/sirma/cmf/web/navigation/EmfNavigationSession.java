package com.sirma.cmf.web.navigation;

import java.io.Serializable;
import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowScoped;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.web.config.EmfWebConfigurationProperties;

/**
 * This component keeps track of the navigation performed by EMF logged in user.
 * 
 * @author svelikov
 */
@Named
@WindowScoped
public class EmfNavigationSession implements Serializable {

	private static final long serialVersionUID = 8193432550343814099L;

	@Inject
	@Config(name = EmfWebConfigurationProperties.NAVIGATION_HISTORY_MAX_COUNT, defaultValue = "20")
	protected int maxHistoryNavigationPoints = 0;

	/** The return point stack. */
	private Stack<NavigationPoint> returnPointStack;

	/** The trail point stack. */
	private Stack<NavigationPoint> trailPointStack;

	/**
	 * Instantiates a new session handler.
	 */
	@PostConstruct
	public void init() {
		returnPointStack = new Stack<>();
		trailPointStack = new Stack<>();

		// create and push home page point
		pushHomePagePoint();
	}

	/**
	 * Push return point.
	 * 
	 * @param point
	 *            the point
	 */
	public void pushReturnPoint(NavigationPoint point) {
		returnPointStack.push(point);
	}

	/**
	 * Pop return point.
	 * 
	 * @return the navigation point
	 */
	public NavigationPoint popReturnPoint() {
		return returnPointStack.pop();
	}

	/**
	 * Checks if is return point stack empty.
	 * 
	 * @return true, if is return point stack empty
	 */
	public boolean isReturnPointStackEmpty() {
		return returnPointStack.empty();
	}

	/**
	 * Push home page point.
	 */
	public void pushHomePagePoint() {
		NavigationPoint navigationPoint = new NavigationPoint();
		navigationPoint.setViewId("/userDashboard/dashboard.xhtml");
		navigationPoint.setOutcome("dashboard");
		pushTrailPoint(navigationPoint);
	}

	/**
	 * Push dummy point.
	 */
	public void pushDummyPoint() {
		NavigationPoint navigationPoint = new NavigationPoint();
		navigationPoint.setOutcome(NavigationHandlerConstants.UNSAFE);
		pushTrailPoint(navigationPoint);
	}

	/**
	 * Push trail point.
	 * 
	 * @param point
	 *            the point
	 */
	public void pushTrailPoint(NavigationPoint point) {
		if (point == null) {
			return;
		}
		// if max size is reached, then remove from the bottom
		if (trailPointStack.size() >= maxHistoryNavigationPoints) {
			trailPointStack.remove(trailPointStack.lastElement());
		}
		//
		if (trailPointStack.isEmpty()) {
			trailPointStack.push(point);
		} else {
			NavigationPoint top = trailPointStack.peek();
			if (!isAlreadyAdded(point, top.getOutcome(), top.getInstanceId())) {
				if (NavigationHandlerConstants.UNSAFE.equals(top.getOutcome())) {
					trailPointStack.pop();
				}
				trailPointStack.push(point);
			}
		}
	}

	/**
	 * Checks if is alreadt added.
	 * 
	 * @param trailPoint
	 *            the trail point
	 * @param outcome
	 *            the outcome
	 * @param id
	 *            the id
	 * @return true, if is alreadt added
	 */
	public boolean isAlreadyAdded(NavigationPoint trailPoint, String outcome, Serializable id) {
		return EqualsHelper.nullSafeEquals(trailPoint.getOutcome(), outcome, true)
				&& EqualsHelper.nullSafeEquals(trailPoint.getInstanceId(), id);
	}

	/**
	 * Get last navigation point from the navigation history stack without removing it.
	 * 
	 * @return the trail point
	 */
	public NavigationPoint getTrailPoint() {
		if (!trailPointStack.isEmpty()) {
			return trailPointStack.peek();
		}
		return null;
	}

	/**
	 * Get a navigation point before the last one.
	 * 
	 * @return the over trail point
	 */
	public NavigationPoint getOverTrailPoint() {
		if (!trailPointStack.isEmpty() && (trailPointStack.size() > 1)) {
			return trailPointStack.get(trailPointStack.size() - 2);
		}
		return null;
	}

	/**
	 * Pop trail point from navigation history stack. If the stack is empty, this method returns
	 * null. We don't allow initial point (home page) to be removed from the stack, so we check if
	 * there is only one point left, we peek and return that point. Otherwise perform pop (remove).
	 * 
	 * @return the navigation point
	 */
	public NavigationPoint popTrailPoint() {
		if (!trailPointStack.isEmpty()) {
			if (trailPointStack.size() > 1) {
				NavigationPoint point = trailPointStack.pop();
				if (NavigationHandlerConstants.UNSAFE.equals(point.getOutcome())) {
					// if the current is unsafe then get the last save element
					point = trailPointStack.peek();
				}
				return point;
			}
			return trailPointStack.peek();
		}
		return null;
	}

	/**
	 * Checks if is trail point stack empty.
	 * 
	 * @return true, if is trail point stack empty
	 */
	public boolean isTrailPointStackEmpty() {
		return trailPointStack.empty();
	}

	/**
	 * Getter method for trailPointStack.
	 * 
	 * @return the trailPointStack
	 */
	public Stack<NavigationPoint> getTrailPointStack() {
		return trailPointStack;
	}

}