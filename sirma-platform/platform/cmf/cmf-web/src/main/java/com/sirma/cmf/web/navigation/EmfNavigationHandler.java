package com.sirma.cmf.web.navigation;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * EmfNavigationHandler is custom implementation of the javax.faces.application.ConfigurableNavigationHandler where
 * navigation points are created for chosen navigation cases to allow switching between navigation points. Register it
 * in faces-config.xml in order to work with it.
 *
 * <pre>
 * <navigation-handler>com.sirma.cmf.web.navigation.EmfNavigationHandler</navigation-handler>
 * </pre>
 *
 * @author svelikov
 */
public class EmfNavigationHandler extends ConfigurableNavigationHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmfNavigationHandler.class);

	private final boolean trace;

	/** Wrapped handler. */
	private final NavigationHandler wrapped;

	/** Calculated current action outcome. */
	private String currentOutcome;

	@Inject
	private DocumentContext documentContext;

	/**
	 * Instantiates a new emf navigation handler.
	 *
	 * @param wrapped
	 *            the wrapped navigation handler.
	 */
	public EmfNavigationHandler(NavigationHandler wrapped) {
		this.wrapped = wrapped;
		trace = LOGGER.isTraceEnabled();
	}

	@Override
	public void handleNavigation(FacesContext context, String method, String outcome) {
		NavigationType action;
		NavigationPoint point;
		String actionMethod = method;
		currentOutcome = outcome;
		EmfNavigationSession sessionHandler = SessionHandlerProviderFactory
				.getSessionHandlerProvider()
					.getEmfSessionHandler();
		String currentViewId = context.getViewRoot().getViewId();
		action = defineNavigationAction(actionMethod, currentViewId);
		// the PROCEED action is handled in default action of the switch
		switch (action) {
			case RETURN:
				actionMethod = executeReturn(context, sessionHandler);
				break;
			case REFRESH:
				executeRefresh(sessionHandler, currentViewId);
				break;
			case BACKWARD:
				actionMethod = executeBackward(context, actionMethod, sessionHandler);
				break;
			default:
				point = createNavigationPoint(outcome, actionMethod, currentViewId, sessionHandler);
				sessionHandler.pushTrailPoint(point);
		}

		if (trace) {
			Stack<NavigationPoint> trailPointStack = sessionHandler.getTrailPointStack();
			for (NavigationPoint navigationPoint : trailPointStack) {
				LOGGER.trace(navigationPoint.toString());
			}
		}
		// handle navigation only for not simple links
		// pages accessed trough a simple links are added as navigation points but navigation is not
		// handled by the NavigationHandler
		if (!NavigationHandlerConstants.SIMPLE_LINK_OUTCOME.equals(actionMethod)) {
			log("Handle navigation", action, actionMethod, currentViewId);
			wrapped.handleNavigation(context, actionMethod, currentOutcome);
		}
	}

	/**
	 * Execute backward navigation.
	 *
	 * @param context
	 *            the context
	 * @param actionMethod
	 *            the action method
	 * @param sessionHandler
	 *            the session handler
	 * @return the string
	 */
	private String executeBackward(FacesContext context, String actionMethod, EmfNavigationSession sessionHandler) {
		String localActionMethod = actionMethod;
		NavigationPoint point = sessionHandler.popTrailPoint();
		point = sessionHandler.getTrailPoint();
		if (point != null) {
			currentOutcome = point.getOutcome();
			localActionMethod = point.getActionMethod();
			// reset action method with 'simpleLink' value on BACKWARD operations
			if (NavigationHandlerConstants.SIMPLE_LINK_OUTCOME.equals(localActionMethod)) {
				localActionMethod = null;
			}
			context.getViewRoot().setViewId(point.getViewId());
			documentContext.deserialize(point.getSerializedDocumentContext());
			setPreviewMode(documentContext);
			SessionHandlerProviderFactory.getSessionHandlerProvider().fireNavigationHistoryEvent(currentOutcome);
			LOGGER.trace("backward to[" + point.getViewId() + "]");
		}
		return localActionMethod;
	}

	/**
	 * Execute refresh.
	 *
	 * @param sessionHandler
	 *            the session handler
	 * @param currentViewId
	 *            the current view id
	 */
	private void executeRefresh(EmfNavigationSession sessionHandler, String currentViewId) {
		NavigationPoint point = sessionHandler.getTrailPoint();
		if (point != null) {
			documentContext.deserialize(point.getSerializedDocumentContext());
			LOGGER.trace("refreshing page[" + currentViewId + "]");
		}
		currentOutcome = null;
	}

	/**
	 * Execute return.
	 *
	 * @param context
	 *            the context
	 * @param sessionHandler
	 *            the session handler
	 * @return the string
	 */
	private String executeReturn(FacesContext context, EmfNavigationSession sessionHandler) {
		NavigationPoint point = sessionHandler.popReturnPoint();
		context.getViewRoot().setViewId(point.getViewId());
		currentOutcome = point.getOutcome();
		return point.getActionMethod();
	}

	/**
	 * Sets the preview mode when navigate backward or to other page.
	 *
	 * @param documentContext
	 *            the new preview mode
	 */
	private void setPreviewMode(DocumentContext documentContext) {
		documentContext.setFormMode(FormViewMode.PREVIEW);
	}

	/**
	 * Define navigation action.
	 *
	 * @param actionMethod
	 *            the action method
	 * @param currentViewId
	 *            The current view id
	 * @return the action
	 */
	private NavigationType defineNavigationAction(String actionMethod, String currentViewId) {
		if (NavigationHandlerConstants.SIMPLE_LINK_OUTCOME.equals(actionMethod)) {
			return NavigationType.PROCEED;
		}

		NavigationType navigationType = NavigationType.getNavigationType(currentOutcome);
		if (navigationType == null) {
			navigationType = NavigationType.PROCEED;
		}
		return navigationType;
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
	 * @param sessionHandler
	 *            Session handler instance.
	 * @return the navigation point
	 */
	private NavigationPoint createNavigationPoint(String outcome, String actionMethod, String viewId,
			EmfNavigationSession sessionHandler) {
		NavigationPoint navigationPoint = null;
		// if outcome is null (ajax probably) or entity open invocation we skip the point
		if (outcome == null || "open".equals(outcome)) {
			return null;
		}

		NavigationPoint trailPoint = sessionHandler.getTrailPoint();
		boolean skip = false;
		if (trailPoint != null) {
			Instance currentInstance = documentContext.getCurrentInstance();
			Serializable id = null;
			if (currentInstance != null) {
				id = currentInstance.getId();
			}
			boolean alreadyAdded = sessionHandler.isAlreadyAdded(trailPoint, outcome, id, actionMethod);
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
		return navigationPoint;
	}

	@Override
	public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome) {
		NavigationCase navigationCase = wrapped instanceof ConfigurableNavigationHandler
				? ((ConfigurableNavigationHandler) wrapped).getNavigationCase(context, fromAction, outcome) : null;
		LOGGER.trace("navigationCase[" + navigationCase + "]");
		return navigationCase;
	}

	@Override
	public Map<String, Set<NavigationCase>> getNavigationCases() {
		return wrapped instanceof ConfigurableNavigationHandler
				? ((ConfigurableNavigationHandler) wrapped).getNavigationCases() : null;
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
	private void log(String prefix, NavigationType action, String actionMethod, String currentViewId) {
		LOGGER.debug("{}: currentOutcome[{}] actionMethod[{}] calculated action[{}] currentViewId[{}]", prefix,
				currentOutcome, actionMethod, action, currentViewId);
	}

}