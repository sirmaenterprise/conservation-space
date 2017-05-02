package com.sirma.itt.emf.audit.solr.service;

import static com.sirma.itt.emf.audit.solr.service.RecentActivitiesFields.ACTION;
import static com.sirma.itt.emf.audit.solr.service.RecentActivitiesFields.ADDED_TARGET_PROPERTIES;
import static com.sirma.itt.emf.audit.solr.service.RecentActivitiesFields.IDS;
import static com.sirma.itt.emf.audit.solr.service.RecentActivitiesFields.INSTANCE_ID;
import static com.sirma.itt.emf.audit.solr.service.RecentActivitiesFields.INSTANCE_TYPE;
import static com.sirma.itt.emf.audit.solr.service.RecentActivitiesFields.OPERATION;
import static com.sirma.itt.emf.audit.solr.service.RecentActivitiesFields.RELATION;
import static com.sirma.itt.emf.audit.solr.service.RecentActivitiesFields.REMOVED_TARGET_PROPERTIES;
import static com.sirma.itt.emf.audit.solr.service.RecentActivitiesFields.REQUEST_ID;
import static com.sirma.itt.emf.audit.solr.service.RecentActivitiesFields.STATE;
import static com.sirma.itt.emf.audit.solr.service.RecentActivitiesFields.TIMESTAMP;
import static com.sirma.itt.emf.audit.solr.service.RecentActivitiesFields.USER_ID;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.emf.audit.processor.StoredAuditActivitiesWrapper;
import com.sirma.itt.emf.audit.processor.StoredAuditActivity;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.solr.services.SolrQueryHelper;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.time.DateRange;

/**
 * Default implementation for the recent activities retriever.
 *
 * @author nvelkov
 */
public class RecentActivitiesRetrieverImpl implements RecentActivitiesRetriever {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private AuditConfiguration configuration;

	@Override
	@SuppressWarnings("resource")
	public StoredAuditActivitiesWrapper getActivities(Collection<Serializable> ids, int offset, int limit,
			Optional<DateRange> range) {
		if (configuration.getRecentActivitiesSolrClient().isNotSet()) {
			LOGGER.warn("The recent activities solr client couldn't be retrieved.");
			return new StoredAuditActivitiesWrapper().setActivities(Collections.emptyList());
		}

		try {
			SolrClient client = configuration.getRecentActivitiesSolrClient().get();
			String idQuery = SolrQueryHelper.createUriQuery(ids, INSTANCE_ID).toString();
			SolrQuery query = constructSolrQuery(offset, limit, idQuery, range);
			QueryResponse queryResponse = client.query(query, METHOD.POST);
			SolrDocumentList solrDocuments = queryResponse.getResults();

			List<StoredAuditActivity> activities = new LinkedList<>();
			for (SolrDocument doc : solrDocuments) {
				activities.add(solrDocumentToActivity(doc));
			}

			return new StoredAuditActivitiesWrapper().setActivities(activities).setTotal(solrDocuments.getNumFound());
		} catch (SolrServerException | IOException e) {
			throw new EmfApplicationException("Error while retrieving activities", e);
		}
	}

	private static SolrQuery constructSolrQuery(int offset, int limit, String idQuery, Optional<DateRange> range) {
		SolrQuery query = new SolrQuery()
				.setQuery(SolrQueryConstants.QUERY_DEFAULT_ALL)
					.setRows(limit)
					.setStart(offset)
					.setSort(TIMESTAMP, ORDER.desc)
					.addFilterQuery(idQuery);

		if (range.isPresent()) {
			StringBuilder dateRangeFilter = SolrQueryHelper.buildDateRangeFilterQuery(TIMESTAMP, range.get());
			query.addFilterQuery(dateRangeFilter.toString());
		}

		return query;
	}

	private static StoredAuditActivity solrDocumentToActivity(SolrDocument document) {
		StoredAuditActivity activity = new StoredAuditActivity();
		setCollectionIfNotNull(document, ADDED_TARGET_PROPERTIES, activity::setAddedTargetProperties);
		setCollectionIfNotNull(document, REMOVED_TARGET_PROPERTIES, activity::setRemovedTargetProperties);
		setCollectionIfNotNull(document, IDS, activity::setIds);
		setIfNotNull(document, INSTANCE_ID, activity::setInstanceId);
		setIfNotNull(document, INSTANCE_TYPE, activity::setInstanceType);
		setIfNotNull(document, RELATION, activity::setRelation);
		setIfNotNull(document, STATE, activity::setState);
		setIfNotNull(document, USER_ID, activity::setUserId);
		setIfNotNull(document, TIMESTAMP, activity::setTimestamp);
		setIfNotNull(document, OPERATION, activity::setOperation);
		setIfNotNull(document, REQUEST_ID, activity::setRequestId);
		setIfNotNull(document, ACTION, activity::setAction);
		return activity;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T> void setCollectionIfNotNull(SolrDocument document, String key, Consumer<Set<T>> consumer) {
		Collection<Object> values = document.getFieldValues(key);
		if (CollectionUtils.isNotEmpty(values)) {
			consumer.accept(new HashSet(values));
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> void setIfNotNull(SolrDocument document, String key, Consumer<T> consumer) {
		Object value = document.getFieldValue(key);
		if (value != null) {
			consumer.accept((T) value);
		}
	}

}
