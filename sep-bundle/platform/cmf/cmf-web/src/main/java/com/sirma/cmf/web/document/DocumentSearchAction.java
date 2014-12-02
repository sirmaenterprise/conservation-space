package com.sirma.cmf.web.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.caseinstance.CaseDocumentsTableAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.search.SearchAction;
import com.sirma.cmf.web.search.SearchConstants;
import com.sirma.cmf.web.search.SearchPageType;
import com.sirma.cmf.web.search.SearchTypeSelectedEvent;
import com.sirma.cmf.web.search.facet.FacetSearchAction;
import com.sirma.cmf.web.search.facet.SelectedFilternameHolder;
import com.sirma.cmf.web.search.facet.event.FacetEvent;
import com.sirma.cmf.web.search.facet.event.FacetEventType;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArgumentsMap;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * DocumentSearchAction backing bean responsible for search operations for documents.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class DocumentSearchAction extends
		FacetSearchAction<CaseInstance, SearchArgumentsMap<CaseInstance, List<DocumentInstance>>>
		implements SearchAction, Serializable {

	private static final long serialVersionUID = -1962648569577964984L;

	// bugfix - temporary title
	private static final String DOCUMENT_TITLE_INPUT = "documentTitleInput";

	private Map<CaseInstance, List<DocumentInstance>> resultMap;

	@Inject
	private CaseDocumentsTableAction caseDocumentsTableAction;

	private Map<String, String> descriptionToMapMapping = null;

	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_LANGUAGE, defaultValue = "bg")
	private String language;

	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_DOCUMENT_TITLE, defaultValue = "210")
	private Integer documentTitleCL;

	@Inject
	private SelectedFilternameHolder selectedFilternameHolder;

	/**
	 * On search page selected.
	 * 
	 * @param event
	 *            the event
	 */
	public void onSearchPageSelected(
			@Observes @SearchPageType(SearchConstants.DOCUMENT_SEARCH) SearchTypeSelectedEvent event) {
		// do the cache for oposite mapping
		Map<String, CodeValue> codeValues = codelistService.getCodeValues(documentTitleCL);
		descriptionToMapMapping = new HashMap<String, String>(codeValues.size());
		for (Entry<String, CodeValue> cl : codeValues.entrySet()) {
			CodeValue value = cl.getValue();
			String description = (String) value.getProperties().get(language);
			if (StringUtils.isNotNull(description)) {
				if (descriptionToMapMapping.containsKey(description)) {
					throw new RuntimeException("Duplicate description for: " + description);
				}
				descriptionToMapMapping.put(description, cl.getKey());
			}
		}
		onCreate();
	}

	/**
	 * Sets a currently selected case filter.
	 * 
	 * @param facetEvent
	 *            Event object.
	 */
	public void selectActiveCaseFilter(
			@Observes @FacetEventType("DocumentInstance") FacetEvent facetEvent) {

		log.debug("CMFWeb: Executing observer DocumentSearchAction.selectActiveCaseFilter: ["
				+ facetEvent.getActiveFilterName() + "]");

		selectedFilternameHolder.setSelectedFilterName(facetEvent.getActiveFilterName());
	}

	@Override
	public String applySearchFilter(String filterType) {
		// Not used
		return null;
	}

	@Override
	protected String fetchResults() {
		try {

			SearchArgumentsMap<CaseInstance, List<DocumentInstance>> searchData = getSearchData();

			String titleFromInput = null;
			try {
				String title = null;
				if (searchData.getArguments().containsKey(DOCUMENT_TITLE_INPUT)) {
					titleFromInput = (String) searchData.getArguments().get(DOCUMENT_TITLE_INPUT);
					title = titleFromInput.trim();
				}

				if ((titleFromInput != null) && StringUtils.isNotNullOrEmpty(title)) {
					ArrayList<String> codeValuesForTitle = new ArrayList<String>();
					// iterate and on found description add to requested list.
					for (Entry<String, String> mapEntry : descriptionToMapMapping.entrySet()) {
						if (mapEntry.getKey().contains(title)) {
							// add the key for this description
							codeValuesForTitle.add(mapEntry.getValue());
						}
					}
					// if actually something is added otherwise use the original
					if (codeValuesForTitle.size() > 0) {
						searchData.getArguments().put(DocumentProperties.TITLE, codeValuesForTitle);
					}
				}
				searchData.getArguments().remove(DOCUMENT_TITLE_INPUT);
				searchService.search(DocumentInstance.class, searchData);
			} finally {
				if ((titleFromInput != null) && (searchData != null)) {
					searchData.getArguments().put(DOCUMENT_TITLE_INPUT, titleFromInput);
				}
			}
			resultMap = getSearchData().getResultMap();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		log.debug("After search: " + resultMap);
		return NavigationConstants.NAVIGATE_DOCUMENT_LIST_PAGE;
	}

	@Override
	protected Class<CaseInstance> getEntityClass() {
		return CaseInstance.class;
	}

	@Override
	protected void updateDataModel() {
		getDataModel()
				.setData(new ArrayList<CaseInstance>(getSearchData().getResultMap().keySet()));
	}

	@Override
	protected SearchArgumentsMap<CaseInstance, List<DocumentInstance>> initSearchData() {

		SearchArgumentsMap<CaseInstance, List<DocumentInstance>> searchArguments = new SearchArgumentsMap<CaseInstance, List<DocumentInstance>>();

		Map<String, Serializable> argumentsMap = new HashMap<String, Serializable>();

		TreeSet<String> aspects = new TreeSet<String>();
		aspects.add(DocumentProperties.TYPE_DOCUMENT_ATTACHMENT);
		aspects.add(DocumentProperties.TYPE_DOCUMENT_STRUCTURED);

		argumentsMap.put(CommonProperties.KEY_SEARCHED_ASPECT, aspects);

		argumentsMap.put(DocumentProperties.CREATED_ON, new DateRange(null, null));
		argumentsMap.put(DocumentProperties.MODIFIED_ON, new DateRange(null, null));

		searchArguments.setArguments(argumentsMap);

		return searchArguments;
	}

	/**
	 * Open document.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @param caseInstance
	 *            the case instance
	 * @return the string
	 */
	// TODO: deprecated - should be removed
	public String openDocument(DocumentInstance documentInstance, CaseInstance caseInstance) {

		CaseInstance instance = caseInstanceService.loadByDbId(caseInstance.getId());
		// we should find document instance that is connected to the given case
		// instance
		DocumentInstance localDocument = documentInstance;
		boolean found = false;
		for (SectionInstance sectionInstance : instance.getSections()) {
			for (Instance doc : sectionInstance.getContent()) {
				if ((doc instanceof DocumentInstance)
						&& EqualsHelper.entityEquals(doc, documentInstance)) {
					localDocument = (DocumentInstance) doc;
					found = true;
					break;
				}
			}
			if (found) {
				break;
			}
		}

		if (!found) {
			log.warn("Case and document instances does ot have anything in common!!");
		}

		getDocumentContext().addInstance(instance);
		return caseDocumentsTableAction.open(localDocument);
	}

	/**
	 * Getter method for resultMap.
	 * 
	 * @return the resultMap
	 */
	public Map<CaseInstance, List<DocumentInstance>> getResultMap() {
		return resultMap;
	}

	/**
	 * Setter method for resultMap.
	 * 
	 * @param resultMap
	 *            the resultMap to set
	 */
	public void setResultMap(Map<CaseInstance, List<DocumentInstance>> resultMap) {
		this.resultMap = resultMap;
	}

	@Override
	public boolean canHandle(com.sirma.itt.emf.security.model.Action action) {
		return false;
	}

	@Override
	public String getSearchDataFormPath() {
		return "/search/document-search-form.xhtml";
	}

	/**
	 * Getter method for documentTitleCL.
	 * 
	 * @return the documentTitleCL
	 */
	public Integer getDocumentTitleCL() {
		return documentTitleCL;
	}

}
