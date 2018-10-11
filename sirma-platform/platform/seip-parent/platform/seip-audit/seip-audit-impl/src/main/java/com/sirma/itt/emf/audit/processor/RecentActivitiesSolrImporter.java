package com.sirma.itt.emf.audit.processor;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrDataService;
import com.sirma.itt.seip.exception.RollbackedException;

/**
 * Imports the given audit activities into recent activities solr core
 *
 * @author BBonev
 */
@ApplicationScoped
public class RecentActivitiesSolrImporter {
	private static final String TIMESTAMP = "timestamp";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private AuditConfiguration auditConfiguration;
	@Inject
	private SolrDataService importService;
	@Inject
	private AuditActivityReducer activityReducer;

	/**
	 * Import activities to recent activities solr core after performing a reduce functionality using the
	 * {@link AuditActivityReducer}.
	 *
	 * @param activities
	 *            the activities to import
	 * @throws RollbackedException
	 *             the rollbacked exception if the import fails
	 */
	@SuppressWarnings("boxing")
	public void importActivities(Collection<AuditActivity> activities) throws RollbackedException {
		if (isEmpty(activities)) {
			return;
		}
		List<Map<String, Object>> dataToImport = activityReducer
				.reduce(activities)
					.map(StoredAuditActivity::toMap)
					.collect(toList());

		try {
			SolrResponse response = importService.addData(getRecentActivitiesClient(), dataToImport);
			LOGGER.trace("Imported {} for {} ms", dataToImport.size(), response.getElapsedTime());
		} catch (SolrClientException e) {
			throw new RollbackedException(e);
		}
	}

	/**
	 * Gets the last known activity date from the recent activities core.
	 *
	 * @return the last known activity date or <code>null</code> if the core is empty.
	 * @throws RollbackedException
	 *             the rollbacked exception if the communication with solr fails
	 */
	public Date getLastKnownActivityDate() throws RollbackedException {
		try {
			return queryLastKnownActivityDate();
		} catch (SolrServerException | IOException e) {
			throw new RollbackedException(e);
		} catch (RemoteSolrException exc) {
			LOGGER.error("Error while accessing " + ((HttpSolrClient) getRecentActivitiesClient()).getBaseURL()
					+ " (Perhaps the tenant hasn't been updated yet?).");
			throw new RollbackedException(exc);
		}
	}

	private Date queryLastKnownActivityDate() throws SolrServerException, IOException, RollbackedException {
		SolrQuery query = buildLastKnownDateQuery();
		QueryResponse response = new QueryRequest(query, METHOD.GET).process(getRecentActivitiesClient());
		SolrDocumentList results = response.getResults();
		if (isEmpty(results)) {
			// no data in the core trigger full data import
			return null;
		}
		SolrDocument document = results.get(0);
		Object object = document.get(TIMESTAMP);
		if (!(object instanceof Date)) {
			throw new RollbackedException("Expected java.util.Date but found " + object
					+ " last known recent activity date. Check your schema.xml timestamp field configuration.");
		}
		return (Date) object;
	}

	private static SolrQuery buildLastKnownDateQuery() {
		SolrQuery query = new SolrQuery("*:*")
				.addSort(TIMESTAMP, ORDER.desc)
					.setRows(Integer.valueOf(1))
					.setFields(TIMESTAMP);
		query.set(CommonParams.OMIT_HEADER, true);
		return query;
	}

	private SolrClient getRecentActivitiesClient() throws RollbackedException {
		if (auditConfiguration.getRecentActivitiesSolrClient().isNotSet()) {
			// we cannot return null in this case as we do not know the state of the core or the connection
			throw new RollbackedException("Recent activities solr core is not configured!");
		}
		return auditConfiguration.getRecentActivitiesSolrClient().get();
	}
}
