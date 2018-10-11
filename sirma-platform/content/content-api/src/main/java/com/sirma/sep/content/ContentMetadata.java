package com.sirma.sep.content;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.seip.PropertiesReader;

/**
 * Defines metadata representation of a content
 * 
 * @author BBonev
 */
public interface ContentMetadata extends PropertiesReader {

	/** Defines empty metadata to represent absence of metadata information. */
	static ContentMetadata NO_METADATA = new SimpleContentMetadata(null);

	/**
	 * Returns simple metadata implementation that is initialized from the given properties map
	 *
	 * @param metadata
	 *            the metadata
	 * @return the content metadata
	 */
	static ContentMetadata from(Map<String, Serializable> metadata) {
		return new SimpleContentMetadata(metadata);
	}

	/**
	 * Simple metadata implementation that works from unmodifiable map
	 */
	class SimpleContentMetadata implements ContentMetadata {

		private final Map<String, Serializable> metadata;

		/**
		 * Instantiates a new simple content metadata.
		 *
		 * @param metadata
		 *            the metadata to initialize from. The source map is cloned shallow and set as unmodifiable.
		 */
		public SimpleContentMetadata(Map<String, Serializable> metadata) {
			this.metadata = isEmpty(metadata) ? Collections.emptyMap()
					: Collections.unmodifiableMap(new HashMap<>(metadata));
		}

		@Override
		public Map<String, Serializable> getProperties() {
			return metadata;
		}
	}

}
