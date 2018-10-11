package com.sirma.itt.emf.solr.connector;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import com.sirma.itt.emf.solr.configuration.SolrSearchConfiguration;
import com.sirma.itt.emf.solr.configuration.TokenProcessorConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.domain.instance.DefaultProperties;

/**
 * Search configuration object
 *
 * @author BBonev
 * @see com.sirma.itt.emf.solr.config.SolrSearchConfigurationsImpl
 *
 * @deprecated (Deprecated use SolrSearchConfigurationsImpl instead.)
 */
@Deprecated
@Singleton
public class SearchConfigurations implements SolrSearchConfiguration {
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "solr.runtime.dashlet.all.fl", defaultValue = DefaultProperties.URI + ","
			+ DefaultProperties.INSTANCE_TYPE, label = "Solr FL query param for dashlets. Split fields by ','")
	private ConfigurationProperty<String> dashletRequestFields;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "solr.runtime.links.fl", defaultValue = DefaultProperties.URI + ","
			+ DefaultProperties.INSTANCE_TYPE + "," + DefaultProperties.MIMETYPE + "," + DefaultProperties.PURPOSE + ","
			+ DefaultProperties.TITLE + ","
			+ DefaultProperties.STATUS, label = "Solr FL query param for links. Split fields by ','.")
	private ConfigurationProperty<String> relationsRequestFields;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "solr.runtime.fq.status", defaultValue = "isDeleted:false", label = "Solr filter query for status")
	private ConfigurationProperty<String> statusFilter;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "solr.search.ftsearch.query", defaultValue = "title:({0}) OR content:({0})", label = "Holds information about the fts query used in search.")
	private ConfigurationProperty<String> fullTextSearchTemplate;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "solr.search.ftsearch.regex.escape", defaultValue = "([:\\[\\]])", type = Pattern.class, label = "Holds information about what to be auto escaped during request parse.")
	private ConfigurationProperty<Pattern> fullTextSearchEscapeRegex;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "solr.search.ftsearch.basic.fq", defaultValue = "NOT rdfType:*CaseSection", label = "Solr filter query used in basic search on the result: <b>default_header:*</b>")
	private ConfigurationProperty<String> fullTextSearchFQ;

	private static final String SOLR_CONFIG_TOKEN_PREPROCESSING_MODEL = "solr.search.ftsearch.token.preprocess.model";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = SOLR_CONFIG_TOKEN_PREPROCESSING_MODEL, defaultValue = "[]", type = List.class, label = "Holds information how tokens to be updated prior of query generation. Sample model is <code>[{\"pattern\":\"^\\\"|\\\"$\",\"replacement\":\"\"},{\"pattern\":\"\\\\s{1}\",\"replacement\":\"\\\\\\\\ \"}]</code> ")
	private ConfigurationProperty<List<TokenProcessorConfiguration>> fullTextSearchTokenPreprocessingModel;

	@ConfigurationConverter(SOLR_CONFIG_TOKEN_PREPROCESSING_MODEL)
	static List<TokenProcessorConfiguration> buildPreprocessingModel(ConverterContext context) {// NOSONAR
		try {
			if (context.getRawValue() == null) {
				return Collections.emptyList();
			}
			String rawValue = context.getRawValue();
			try (Reader reader = new StringReader(rawValue); JsonReader jsonReader = Json.createReader(reader)) {
				JsonArray configs = jsonReader.readArray();
				List<TokenProcessorConfiguration> result = new ArrayList<>(configs.size());
				for (JsonValue jsonValue : configs) {
					JsonObject nextConfig = (JsonObject) jsonValue;
					result.add(new TokenProcessorConfiguration(
							Pattern.compile(nextConfig.getString("pattern")), nextConfig.getString("replacement")));
				}
				return result;
			}
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}
	}

	@Override
	public ConfigurationProperty<String> getDashletsRequestFields() {
		return dashletRequestFields;
	}

	@Override
	public ConfigurationProperty<String> getRelationsRequestFields() {
		return relationsRequestFields;
	}

	@Override
	public ConfigurationProperty<String> getStatusFilterQuery() {
		return statusFilter;
	}

	@Override
	public ConfigurationProperty<String> getFullTextSearchTemplate() {
		return fullTextSearchTemplate;
	}

	@Override
	public ConfigurationProperty<Pattern> getFullTextSearchEscapePattern() {
		return fullTextSearchEscapeRegex;
	}

	@Override
	public ConfigurationProperty<String> getFullTextSearchFilterQuery() {
		return fullTextSearchFQ;
	}

	@Override
	public ConfigurationProperty<List<TokenProcessorConfiguration>> getFullTextTokenPreprocessModel() {
		return fullTextSearchTokenPreprocessingModel;
	}

}
