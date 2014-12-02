package com.sirma.cmf.web.navigation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * EmfNavigationHandler is custom implementation of the
 * javax.faces.application.ConfigurableNavigationHandler where navigation points are created for
 * chosen navigation cases to allow switching between navigation points. Register it in
 * faces-config.xml in order to work with it.
 * 
 * <pre>
 * <navigation-handler>com.sirma.cmf.web.navigation.EmfNavigationHandler</navigation-handler>
 * </pre>
 * 
 * @author svelikov
 */
public class EmfNavigationHandler extends ConfigurableNavigationHandler {

	private final Logger log;

	/** Wrapped handler. */
	private final NavigationHandler wrapped;

	/** Calculated current action outcome. */
	private String currentOutcome;

	/** navigation rules that leads to allowed for storing in navigation history pages. */
	private final Set<String> safePages = new HashSet<String>(Arrays.asList(
			// dashboards
			"dashboard", "project-dashboard",
			"case-dashboard",
			// case tabs
			"caseinstance", "case-details", "case-documents", "case-relations", "case-workflow",
			"case-objects",
			// project tabs
			"projectinstance", "manage-resources", "project-relations", "project-schedule",
			"project-resource-allocation",
			// document
			"documentinstance",
			// task
			"taskinstance", "standalonetaskinstance",
			//
			"object",
			// search pages
			"basic-search", "search", "case-list", "project-list", "document-list", "task-list",
			//
			/* "case-link", */"help-request-menu"));

	/**
	 * Navigation action types.
	 */
	enum Action {

		/** Page refresh. */
		REFRESH,
		/** Return to page after backward operation. */
		RETURN,
		/** Default action. */
		FORWARD,
		/** Back to previous page. */
		BACKWARD,
		/** Default action. */
		NOTHING,
		/** Skip updating history. */
		SKIP
	}

	/**
	 * Instantiates a new emf navigation handler.
	 * 
	 * @param wrapped
	 *            the wrapped navigation handler.
	 */
	public EmfNavigationHandler(NavigationHandler wrapped) {
		this.wrapped = wrapped;
		log = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleNavigation(FacesContext context, String method, String outcome) {
		Action action;
		NavigationPoint point;
		String actionMethod = method;
		currentOutcome = outcome;
		EmfNavigationSession sessionHandler = SessionHandlerProviderFactory
				.getSessionHandlerProvider().getEmfSessionHandler();
		DocumentContext documentContext = SessionHandlerProviderFactory.getSessionHandlerProvider()
				.getDocumentContext();
		String currentViewId = context.getViewRoot().getViewId();
		action = defineNavigationAction(sessionHandler, actionMethod, outcome, currentViewId);
		switch (action) {
			case RETURN:
				point = sessionHandler.popReturnPoint();
				currentOutcome = point.getOutcome();
				actionMethod = point.getActionMethod();
				context.getViewRoot().setViewId(point.getViewId());
				break;
			case REFRESH:
				point = sessionHandler.getTrailPoint();
				if (point != null) {
					documentContext.deserialize(point.getSerializedDocumentContext());
					log.trace("refreshing page[" + currentViewId + "]");
				}
				currentOutcome = null;
				break;
			case FORWARD:
				point = createNavigationPoint(outcome, actionMethod, currentViewId,
						documentContext, sessionHandler);
				sessionHandler.pushTrailPoint(point);
				break;
			case BACKWARD:
				point = sessionHandler.popTrailPoint();
				point = sessionHandler.getTrailPoint();
				if (point != null) {
					currentOutcome = point.getOutcome();
					actionMethod = point.getActionMethod();
					context.getViewRoot().setViewId(point.getViewId());
					documentContext.deserialize(point.getSerializedDocumentContext());
					SessionHandlerProviderFactory.getSessionHandlerProvider()
							.fireNavigationHistoryEvent(currentOutcome);
					log.trace("backward to[" + point.getViewId() + "]");
				}
				break;
			case SKIP:
				log("Skip", action, actionMethod, currentViewId);
				point = createNavigationPoint(outcome, actionMethod, currentViewId,
						documentContext, sessionHandler);
				sessionHandler.pushTrailPoint(point);
				break;
			default:
				point = createNavigationPoint(outcome, actionMethod, currentViewId,
						documentContext, sessionHandler);
				sessionHandler.pushTrailPoint(point);
		}
		log("Handle navigation", action, actionMethod, currentViewId);

		wrapped.handleNavigation(context, actionMethod, currentOutcome);
	}

	/**
	 * Define navigation action.
	 * 
	 * @param sessionHandler
	 *            the session handler
	 * @param actionMethod
	 *            the action method
	 * @param outcome
	 *            the action outcome
	 * @param currentViewId
	 *            The current view id
	 * @return the action
	 */
	private Action defineNavigationAction(EmfNavigationSession sessionHandler, String actionMethod,
			String outcome, String currentViewId) {
		if (skipPoint(actionMethod, outcome, currentViewId)) {
			return Action.SKIP;
		}

		try {
			return Action.valueOf(currentOutcome);
		} catch (Exception ex) {
			//
		}
		return Action.NOTHING;
	}

	/**
	 * Creates the navigation point.
	 * 
	 * @param outcome
	 *            the action
	 * @param actionMethod
	 *            the action method
	 * @param viewId
	 *            the view id
	 * @param documentContext
	 *            the document context
	 * @param sessionHandler
	 *            Session handler instance.
	 * @return the navigation point
	 */
	private NavigationPoint createNavigationPoint(String outcome, String actionMethod,
			String viewId, DocumentContext documentContext, EmfNavigationSession sessionHandler) {
		NavigationPoint navigationPoint = null;
		// if outcome is null (ajax probably) we skip the point
		if (outcome == null) {
			return null;
		}
		// for non safe pages we store dummy navigation points
		else if (!safePages.contains(outcome)) {
			navigationPoint = createDummyNavigationPoint();
		} else {
			NavigationPoint trailPoint = sessionHandler.getTrailPoint();
			boolean skip = false;
			if (trailPoint != null) {
				Instance currentInstance = documentContext.getCurrentInstance();
				Serializable id = null;
				if (currentInstance != null) {
					id = currentInstance.getId();
				}
				boolean alreadyAdded = sessionHandler.isAlreadyAdded(trailPoint, outcome, id);
				if (NavigationHandlerConstants.UNSAFE.equals(trailPoint.getOutcome())
						&& "/entity/open.xhtml".equals(viewId)) {
					NavigationPoint overTrailPoint = sessionHandler.getOverTrailPoint();
					if (overTrailPoint != null) {
						alreadyAdded = sessionHandler.isAlreadyAdded(overTrailPoint, outcome, id);
					}
				}
				if (alreadyAdded) {
					skip = true;
				} else {
					skip = false;
				}
			} else {
				skip = false;
			}
			if (!skip) {
				navigationPoint = new NavigationPoint();
				navigationPoint.setOutcome(outcome);
				navigationPoint.setActionMethod(actionMethod);
				navigationPoint.setViewId(viewId);
				navigationPoint.setSerializedDocumentContext(documentContext.serialize());
				Instance instance = documentContext.getCurrentInstance();
				if (instance != null) {
					navigationPoint.setInstanceId(instance.getId());
				}
			}
		}
		return navigationPoint;
	}

	/**
	 * Creates the dummy navigation point.
	 * 
	 * @return the navigation point
	 */
	private NavigationPoint createDummyNavigationPoint() {
		return new NavigationPoint(NavigationHandlerConstants.UNSAFE);
	}

	/**
	 * If current navigation should be skipped and should not add navigation point for it.
	 * 
	 * @param actionMethod
	 *            the action method
	 * @param outcome
	 *            the action outcome
	 * @param currentViewId
	 *            the current view id
	 * @return true, if successful
	 */
	private boolean skipPoint(String actionMethod, String outcome, String currentViewId) {
		boolean skip = true;
		if (safePages.contains(outcome)) {
			skip = false;
		}
		if (NavigationHandlerConstants.BACKWARD.equals(outcome)
				|| NavigationHandlerConstants.REFRESH.equals(outcome)) {
			skip = false;
		}
		return skip;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome) {
		NavigationCase navigationCase = (wrapped instanceof ConfigurableNavigationHandler) ? ((ConfigurableNavigationHandler) wrapped)
				.getNavigationCase(context, fromAction, outcome) : null;
		log.trace("navigationCase[" + navigationCase + "]");
		return navigationCase;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Set<NavigationCase>> getNavigationCases() {
		return (wrapped instanceof ConfigurableNavigationHandler) ? ((ConfigurableNavigationHandler) wrapped)
				.getNavigationCases() : null;
	}

	/**
	 * Log.
	 * 
	 * @param prefix
	 *            the prefix
	 * @param action
	 *            the action
	 * @param actionMethod
	 *            the action method
	 * @param currentViewId
	 *            the current view id
	 */
	private void log(String prefix, Action action, String actionMethod, String currentViewId) {
		log.debug(
				"{}: currentOutcome[{}] actionMethod[{}] calculated action[{}] currentViewId[{}]",
				prefix, currentOutcome, actionMethod, action, currentViewId);
	}

}