package com.sirma.cmf.web.caseinstance.tab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.cmf.web.EntityAction;
import com.sirma.itt.emf.concurrent.TaskExecutor;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.search.model.SearchInstance;

/**
 * Base section management API. This class will track section content and will retrieve it parallel
 * from the solr.
 * 
 * @author cdimitrov
 */
public abstract class CaseSectionsActionBase extends EntityAction {

	/** The search criteria. */
	private SearchFilterConfig searchCriteria;

	/** The section counter label. */
	private String counterLabel;

	/** Container that will support mapping between current section and their content. */
	private Map<String, SectionContentLoader> sectionHolder;

	/** Service for executing solr queries. */
	@Inject
	protected SearchService searchService;

	/** Service for running parallel tasks. */
	@Inject
	private TaskExecutor taskExecutor;

	/**
	 * Initialize base section components.
	 */
	public void onOpen() {
		if (searchCriteria == null) {
			searchCriteria = searchService.getFilterConfiguration(getSectionIdentifier(),
					SearchInstance.class);
		}
		sectionHolder = new HashMap<String, SectionContentLoader>();
		String counter = getCounterBundle();
		setCounterLabel(labelProvider.getValue(counter));
	}

	/**
	 * Getter for section content based on section identifier.
	 * 
	 * @param sectionIdentifier
	 *            current section identifier
	 * @return instance represent section content
	 */
	public SectionContentLoader getSectionContentById(String sectionIdentifier) {
		// request for mapping between section and their content
		addNewSectionContent(sectionIdentifier);
		return sectionHolder.get(sectionIdentifier);
	}

	/**
	 * Execute available section content objects parallel. Every object will holds solr arguments
	 * and based on them will be retrieved the actual content.
	 */
	public void extractSectionContent() {
		if (sectionHolder != null) {
			taskExecutor.execute(new ArrayList<SectionContentLoader>(sectionHolder.values()));
		}
	}

	/**
	 * Add new section content in the {@link #sectionHolder}, if not available.
	 * 
	 * @param sectionIdentifier
	 *            current section identifier
	 */
	private void addNewSectionContent(String sectionIdentifier) {
		if (sectionHolder.get(sectionIdentifier) == null) {
			SectionContentLoader sectionContentLoader = new SectionContentLoader(searchCriteria, searchService,
					eventService, typeConverter);
			sectionContentLoader.setSectionIdentifier(sectionIdentifier);
			sectionHolder.put(sectionIdentifier, sectionContentLoader);
		}
	}

	/**
	 * Getter for section counter label.
	 * 
	 * @return section counter label
	 */
	public String getCounterLabel() {
		return counterLabel;
	}

	/**
	 * Setter for section counter label.
	 * 
	 * @param counterLabel
	 *            current counter label
	 */
	public void setCounterLabel(String counterLabel) {
		this.counterLabel = counterLabel;
	}

	/**
	 * Getter for section holder.
	 * 
	 * @return holder with mapping between section and section content
	 */
	public Map<String, SectionContentLoader> getSectionHolder() {
		return sectionHolder;
	}

	/**
	 * Setter for section holder.
	 * 
	 * @param sectionHolder
	 *            holder with mapping between section and section content
	 */
	public void setSectionHolder(Map<String, SectionContentLoader> sectionHolder) {
		this.sectionHolder = sectionHolder;
	}

	/**
	 * Getter for specific section identifier, represent definition identifier. The definition will
	 * holds filters, sorter, labels and base solr queries for retrieving section content.
	 * 
	 * @return section identifier
	 */
	public abstract String getSectionIdentifier();

	/**
	 * Getter for section counter bundle.
	 * 
	 * @return section counter bundle
	 */
	public abstract String getCounterBundle();

}
