package com.sirma.itt.emf.solr.services;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;

import com.sirma.itt.emf.solr.exception.SolrClientException;

/**
 * Default implementation of {@link SolrDataService}
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class SolrDataServiceImpl implements SolrDataService {

	@Override
	public SolrResponse addData(SolrClient client, Map<String, Object> data) throws SolrClientException {
		return importDataInternal(client, Stream.of(data));
	}

	@Override
	public SolrResponse addData(SolrClient client, Collection<? extends Map<String, Object>> data)
			throws SolrClientException {
		return importDataInternal(client, data != null ? data.stream() : Stream.empty());
	}

	private static SolrResponse importDataInternal(SolrClient client, Stream<? extends Map<String, Object>> dataStream)
			throws SolrClientException {
		Objects.requireNonNull(client, "SolrClient is required argument and cannot be null");

		UpdateRequest request = new UpdateRequest();
		request.setAction(UpdateRequest.ACTION.COMMIT, false, false);
		dataStream.filter(map -> !isEmpty(map)).map(buildDocument()).forEach(request::add);
		try {
			return request.process(client);
		} catch (SolrServerException | IOException e) {
			throw new SolrClientException("Failed to import data to solr", e);
		}
	}

	/**
	 * Builds {@link SolrInputDocument} from {@link Map}, where the map keys are the names of the fields and the map
	 * values represents the values of the fields.
	 *
	 * @return new {@link SolrInputDocument} with added fields
	 */
	private static Function<Map<String, Object>, SolrInputDocument> buildDocument() {
		return map -> {
			SolrInputDocument solrInputDocument = new SolrInputDocument();
			map.forEach(solrInputDocument::addField);
			return solrInputDocument;
		};
	}

}
