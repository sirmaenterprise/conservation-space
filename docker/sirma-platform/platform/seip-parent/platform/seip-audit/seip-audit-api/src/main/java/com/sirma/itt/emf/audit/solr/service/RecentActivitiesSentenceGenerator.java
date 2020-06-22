package com.sirma.itt.emf.audit.solr.service;

import java.util.List;

import com.sirma.itt.emf.audit.processor.RecentActivity;
import com.sirma.itt.emf.audit.processor.StoredAuditActivity;

/**
 * Generates sentences from stored audit activities.
 *
 * @author nvelkov
 */
public interface RecentActivitiesSentenceGenerator {

	/**
	 * Generate a sentence from the given audit activity. The sentence will be generated based on the given activity
	 * action.
	 *
	 * @param activity
	 *            the activity from which to generate the sentence
	 * @return the generated sentences
	 */
	List<RecentActivity> generateSentences(List<StoredAuditActivity> activity);
}
