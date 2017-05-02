package com.sirma.itt.emf.solr.services.impl.facet;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Contains configuration properties related to the faceted search.
 *
 * @author Mihail Radkov
 */
@Singleton
public class FacetConfigurationProperties {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "faceting.date.gap", defaultValue = "+1DAY", label = "Gap size when faceting date fields.")
	private ConfigurationProperty<String> dateGap;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "faceting.date.start", defaultValue = "NOW-1YEAR/DAY", label = "Start date for faceting date fields.")
	private ConfigurationProperty<String> dateStart;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "faceting.date.end", defaultValue = "NOW+1YEAR/DAY+1DAY", label = "End date for faceting date fields.")
	private ConfigurationProperty<String> dateEnd;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "faceting.date.other", defaultValue = "all", label = "Enables/disabled facet calculation for dates outside the start & end range.")
	private ConfigurationProperty<String> other;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "faceting.objects.batchSize", defaultValue = "1000", label = "Defines the batch size when fetching the object facets' values from Solr.", type = Integer.class)
	private ConfigurationProperty<Integer> batchSize;

	/**
	 * Getter method for dateGap.
	 *
	 * @return the dateGap
	 */
	public ConfigurationProperty<String> getDateGap() {
		return dateGap;
	}

	/**
	 * Getter method for dateStart.
	 *
	 * @return the dateStart
	 */
	public ConfigurationProperty<String> getDateStart() {
		return dateStart;
	}

	/**
	 * Getter method for dateEnd.
	 *
	 * @return the dateEnd
	 */
	public ConfigurationProperty<String> getDateEnd() {
		return dateEnd;
	}

	/**
	 * Getter method for other.
	 *
	 * @return the other
	 */
	public ConfigurationProperty<String> getOther() {
		return other;
	}

	/**
	 * Getter method for batchSize.
	 *
	 * @return the batchSize
	 */
	public ConfigurationProperty<Integer> getBatchSize() {
		return batchSize;
	}

}
