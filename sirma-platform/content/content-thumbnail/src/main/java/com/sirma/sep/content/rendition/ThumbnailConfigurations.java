package com.sirma.sep.content.rendition;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Common place for configurations
 *
 * @author BBonev
 */
@Singleton
public class ThumbnailConfigurations {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "thumbnail.maxDownloadRerties", defaultValue = "5", sensitive = true, type = Integer.class, label = "Property to define max retries for thumbnail download")
	private ConfigurationProperty<Integer> maxThumbnailRetryCount;

	/**
	 * Gets the max thumbnail retry count.
	 *
	 * @return the max thumbnail retry count
	 */
	public Integer getMaxThumbnailRetryCount() {
		return maxThumbnailRetryCount.get();
	}
}
