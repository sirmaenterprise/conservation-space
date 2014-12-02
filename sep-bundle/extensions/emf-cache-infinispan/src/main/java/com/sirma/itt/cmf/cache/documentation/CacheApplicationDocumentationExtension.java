package com.sirma.itt.cmf.cache.documentation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.cache.extension.CacheConfigurationExtension;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.documentation.ApplicationDocumentationExtension;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Cache documentation generation extension.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ApplicationDocumentationExtension.TARGET_NAME, order = 10)
public class CacheApplicationDocumentationExtension implements ApplicationDocumentationExtension {

	/** The container start. */
	static final String CONTAINER_START = "<cache-container name=\"{0}\" jndi-name=\"java:jboss/infinispan/{0}\">";

	/** The container end. */
	static final String CONTAINER_END = "</cache-container>";

	/** The local cache. */
	static final String LOCAL_CACHE = "\t<local-cache name=\"{0}_REGION\" start=\"EAGER\" jndi-name=\"java:jboss/infinispan/{1}/{0}_REGION\">"
			+ System.lineSeparator()
			+
	            "\t\t<transaction mode=\"{2}\"/>"+ System.lineSeparator() +
	            "\t\t<eviction strategy=\"{3}\" max-entries=\"{4}\"/>"+ System.lineSeparator() +
	            "\t\t<expiration max-idle=\"{5}\" interval=\"{6}\"/>"+ System.lineSeparator() +
	        "\t</local-cache>";

	/** The cache documentation. */
	static final String CACHE_DOCUMENTATION = "h3. {0}" + System.lineSeparator()
			+ " * cache identifier: {1}"
			+ System.lineSeparator() + " * expected cache container: {2}" + System.lineSeparator()
			+ " * transaction mode: {3}" + System.lineSeparator() + " * eviction configuration:"
			+ System.lineSeparator() + " ** eviction strategy: {4}" + System.lineSeparator()
			+ " ** maximum cached entries: {5}" + System.lineSeparator()
			+ " * expiration configuration:" + System.lineSeparator()
			+ " ** maximum idle time: {6}" + System.lineSeparator() + " ** interval: {7}"
			+ System.lineSeparator() + " ** lifespan: {8}" + System.lineSeparator()
			+ " * description: {9}" + System.lineSeparator() + System.lineSeparator();

	/** The extension. */
	@Inject
	private CacheConfigurationExtension extension;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> generationOptions() {
		Set<String> set = CollectionUtils.createHashSet(5);
		set.add("cacheConfiguration");
		set.add("cacheDocumentation");
		return set;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String generate(String option) {
		if (EqualsHelper.nullSafeEquals(option, "cacheConfiguration", true)) {
			return wrap("cacheConfiguration", cleanXMLTags(getConfiguration()));
		} else if (EqualsHelper.nullSafeEquals(option, "cacheDocumentation", true)) {
			return wrap("cacheDocumentation", getDocumentation());
		}
		return "{\"error\": \"Option not supported: " + option + "\"}";
	}

	/**
	 * Clean xml tags.
	 * 
	 * @param xml
	 *            the xml
	 * @return the string
	 */
	private String cleanXMLTags(String xml) {
		return xml.replace("<", "&#60;").replace(">", "&#62;")
				.replace(System.lineSeparator(), "<br>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
	}

	/**
	 * Gets the documentation.
	 * 
	 * @return the documentation
	 */
	private String[] getDocumentation() {
		Map<String, CacheConfiguration> sortedConfig = new TreeMap<String, CacheConfiguration>(
				extension.getConfigurations());

		List<String> result = new ArrayList<>(sortedConfig.size() + 1);

		result.add(MessageFormat.format(CACHE_DOCUMENTATION, "Cache documentation description",
				getMethodDoc(CacheConfiguration.class, "name"),
				getMethodDoc(CacheConfiguration.class, "container"),
				getMethodDoc(CacheConfiguration.class, "transaction"),
				getMethodDoc(Eviction.class, "strategy"),
				getMethodDoc(Eviction.class, "maxEntries"),
				getMethodDoc(Expiration.class, "maxIdle"),
				getMethodDoc(Expiration.class, "interval"),
				getMethodDoc(Expiration.class, "lifespan"),
				getMethodDoc(CacheConfiguration.class, "doc")));
		for (Entry<String, CacheConfiguration> entry : sortedConfig.entrySet()) {
			String cacheId = entry.getKey();
			CacheConfiguration config = entry.getValue();
			String name = cacheId.toLowerCase().replace("_", " ");
			name = cacheId.charAt(0) + name.substring(1);
			result.add(MessageFormat.format(CACHE_DOCUMENTATION, name, cacheId, config.container(),
					config.transaction().toString(), config.eviction().strategy(),
					String.valueOf(config.eviction().maxEntries()),
					String.valueOf(config.expiration().maxIdle()),
					String.valueOf(config.expiration().interval()),
					String.valueOf(config.expiration().lifespan()), config.doc().value()));
		}
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Gets the method doc.
	 * 
	 * @param target
	 *            the target
	 * @param method
	 *            the method
	 * @return the method doc
	 */
	private String getMethodDoc(Class<?> target, String method) {
		try {
			Documentation documentation = target.getDeclaredMethod(method)
					.getAnnotation(Documentation.class);
			if (documentation != null) {
				return documentation.value();
			}
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Gets the configuration.
	 * 
	 * @return the configuration
	 */
	private String getConfiguration() {
		Map<String, CacheConfiguration> configurations = extension.getConfigurations();
		return generateConfiguration(configurations);
	}

	/**
	 * Wrap response.
	 * 
	 * @param base
	 *            the base
	 * @param contents
	 *            the content
	 * @return the string
	 */
	private String wrap(String base, String... contents) {
		try {
			List<JSONObject> annotations = new ArrayList<JSONObject>(contents.length);
			for (String content : contents) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("className", content);
				jsonObject.put("doc", "");
				annotations.add(jsonObject);
			}
			JSONObject result = new JSONObject();
			result.put(base, annotations);
			return result.toString();
		} catch (JSONException e) {
			return "{\"error\": \"" + e.getMessage() + "\"}";
		}
	}

	/**
	 * Generate cache configuration for the given configuration annotations.
	 * 
	 * @param configurations
	 *            the configurations
	 * @return the XML cache configuration
	 */
	public static String generateConfiguration(Map<String, CacheConfiguration> configurations) {
		Map<String, Map<String, CacheConfiguration>> containerMapping = new LinkedHashMap<>();
		for (Entry<String, CacheConfiguration> entry : configurations.entrySet()) {
			CacheConfiguration configuration = entry.getValue();
			String container = "default";
			if (StringUtils.isNotNullOrEmpty(configuration.container())) {
				container = configuration.container();
			}
			Map<String, CacheConfiguration> map = containerMapping.get(container);
			if (map == null) {
				map = new LinkedHashMap<>();
				containerMapping.put(container, map);
			}
			map.put(entry.getKey(), entry.getValue());
		}

		StringBuilder builder = new StringBuilder();
		for (Entry<String, Map<String, CacheConfiguration>> entry : containerMapping.entrySet()) {
			builder.append(MessageFormat.format(CONTAINER_START, entry.getKey())).append(
					System.lineSeparator());
			for (Entry<String, CacheConfiguration> configEntry : entry.getValue().entrySet()) {
				CacheConfiguration config = configEntry.getValue();
				builder.append(
						MessageFormat.format(LOCAL_CACHE, configEntry.getKey(), entry.getKey(),
								config.transaction().toString(), config.eviction().strategy(),
								String.valueOf(config.eviction().maxEntries()),
								String.valueOf(config.expiration().maxIdle()),
								String.valueOf(config.expiration().interval()))).append(
						System.lineSeparator());
			}
			builder.append(CONTAINER_END).append(System.lineSeparator());
		}
		return builder.toString();
	}

}
