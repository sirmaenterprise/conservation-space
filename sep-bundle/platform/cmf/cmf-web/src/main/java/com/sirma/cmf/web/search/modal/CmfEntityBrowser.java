package com.sirma.cmf.web.search.modal;

import java.io.Serializable;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;
import org.richfaces.event.DataScrollEvent;

import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.search.SearchAction;
import com.sirma.cmf.web.search.SearchActionBase;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.allowed_action.AllowedActionType;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * EntityBrowser backing bean.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class CmfEntityBrowser extends EntityAction implements Serializable {

	private static final long serialVersionUID = -5129296823487415432L;

	@Inject
	private Instance<SearchAction> searchActions;

	@Inject
	private Instance<EntityBrowserHandler> entityBrowserHandlers;

	private SearchActionBase<CaseInstance, SearchArguments<CaseInstance>> searchAction;

	protected AbstractBrowserHandler entityBrowserHandler;

	private String searchDataFormPath;

	private String resultListFormPath;

	private AllowedActionType actionType;

	/**
	 * Inits the browser.
	 * 
	 * @param action
	 *            the action
	 * @param entity
	 *            the entity
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void initBrowser(com.sirma.itt.emf.security.model.Action action, Entity entity) {
		String actionId = action.getActionId();
		actionType = AllowedActionType.getActionType(actionId);

		entityBrowserHandler = (AbstractBrowserHandler) findEntityBrowserHandler(action);
		entityBrowserHandler.initialize(entity);

		switch (actionType) {
			case DOCUMENT_MOVE_OTHER_CASE:
				findSearchService(action);
				setSearchDataFormPath(((SearchAction) searchAction).getSearchDataFormPath());
				setResultListFormPath("/search/case-sections.xhtml");
				searchAction.onCreate();
				// searchAction.setSearchData(searchService.listActiveCaseInstances());
				break;

			case COPY_CONTENT:
				findSearchService(action);
				setSearchDataFormPath("/search/document-search-form.xhtml");
				setResultListFormPath("/search/entity-list.xhtml");
				searchAction.onCreate();
				break;

			case LINK:
				if (entity instanceof DocumentInstance) {
					findSearchService(action);
					setSearchDataFormPath("/search/document-search-form.xhtml");
					setResultListFormPath("/search/entity-list.xhtml");
					searchAction.onCreate();
				}

				if (entity instanceof CaseInstance) {
					findSearchService(action);
					setSearchDataFormPath(((SearchAction) searchAction).getSearchDataFormPath());
					setResultListFormPath("/case/includes/case-list.xhtml");
					searchAction.onCreate();
				}
				break;

			default:
				break;
		}

	}

	/**
	 * A proxy method for the datascroller onscroll event. We need to filter the result after
	 * scrolling so we get the result list after the scroll and pass it to be filtered. If search
	 * should not be in context, then remove context and call the search. Restore context after
	 * that.
	 * 
	 * @param event
	 *            the event
	 */
	@SuppressWarnings("unchecked")
	public void onScroll(DataScrollEvent event) {
		searchAction.onScroll(event);
		SearchArguments<CaseInstance> searchData = searchAction.getSearchData();
		entityBrowserHandler.afterSearch(searchData);
	}

	/**
	 * Find entity browser handler.
	 * 
	 * @param action
	 *            the action
	 * @return the entity browser handler
	 */
	protected EntityBrowserHandler findEntityBrowserHandler(
			com.sirma.itt.emf.security.model.Action action) {
		EntityBrowserHandler entityBrowserHandler = null;
		for (EntityBrowserHandler handler : entityBrowserHandlers) {
			if (handler.canHandle(action)) {
				entityBrowserHandler = handler;
				break;
			}
		}
		return entityBrowserHandler;
	}

	/**
	 * Find search service.
	 * 
	 * @param action
	 *            the action
	 */
	@SuppressWarnings("unchecked")
	private void findSearchService(com.sirma.itt.emf.security.model.Action action) {
		for (SearchAction searchActionHandler : searchActions) {
			if (searchActionHandler.canHandle(action)) {
				searchAction = (SearchActionBase<CaseInstance, SearchArguments<CaseInstance>>) searchActionHandler;
				break;
			}
		}
	}

	/**
	 * Delegate search method to the appropriate search service.
	 */
	public void search() {
		searchAction.search();
		entityBrowserHandler.afterSearch(searchAction.getSearchData());
	}

	/**
	 * Perform search in context. Check if root context is available. If context exists, then remove
	 * it before search invocation. After search restore context.
	 * 
	 * @param inContext
	 *            the in context
	 */
	public void searchInContext(boolean inContext) {
		searchAction.setSearchInContext(inContext);
		searchAction.search();
		entityBrowserHandler.afterSearch(searchAction.getSearchData());
	}

	/**
	 * Getter method for searchDataFormPath.
	 * 
	 * @return the searchDataFormPath
	 */
	public String getSearchDataFormPath() {
		return searchDataFormPath;
	}

	/**
	 * Setter method for searchDataFormPath.
	 * 
	 * @param searchDataFormPath
	 *            the searchDataFormPath to set
	 */
	public void setSearchDataFormPath(String searchDataFormPath) {
		this.searchDataFormPath = searchDataFormPath;
	}

	/**
	 * Getter method for resultListFormPath.
	 * 
	 * @return the resultListFormPath
	 */
	public String getResultListFormPath() {
		return resultListFormPath;
	}

	/**
	 * Setter method for resultListFormPath.
	 * 
	 * @param resultListFormPath
	 *            the resultListFormPath to set
	 */
	public void setResultListFormPath(String resultListFormPath) {
		this.resultListFormPath = resultListFormPath;
	}

	/**
	 * Getter method for actionType.
	 * 
	 * @return the actionType
	 */
	public AllowedActionType getActionType() {
		return actionType;
	}

	/**
	 * Setter method for actionType.
	 * 
	 * @param actionType
	 *            the actionType to set
	 */
	public void setActionType(AllowedActionType actionType) {
		this.actionType = actionType;
	}

	/**
	 * Getter method for searchAction.
	 * 
	 * @return the searchAction
	 */
	public SearchActionBase<CaseInstance, SearchArguments<CaseInstance>> getSearchAction() {
		return searchAction;
	}

	/**
	 * Setter method for searchAction.
	 * 
	 * @param searchAction
	 *            the searchAction to set
	 */
	public void setSearchAction(
			SearchActionBase<CaseInstance, SearchArguments<CaseInstance>> searchAction) {
		this.searchAction = searchAction;
	}

}
