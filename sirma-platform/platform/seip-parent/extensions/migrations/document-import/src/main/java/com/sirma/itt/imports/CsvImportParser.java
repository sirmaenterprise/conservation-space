package com.sirma.itt.imports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.constants.DocumentProperties;
import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.imports.configuration.DocumentImportConfiguration;

/**
 * Parser class for CSV file format for importing data in EMF repository.
 *
 * @author BBonev
 */
@ApplicationScoped
public class CsvImportParser {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CsvImportParser.class);
	/** The Constant LINE_SPLIT. */
	private static final Pattern LINE_SPLIT = Pattern.compile("\\s*(?:;|,)\\s*");

	/** The Constant NON_APPLICABLE. */
	private static final Pattern NON_APPLICABLE = Pattern.compile("\\s*(?:n/a|none|-*)\\s*",
			Pattern.CASE_INSENSITIVE);
	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The default user definition. */
	@Inject
	@Config(name = DocumentImportConfiguration.DEFAULT_USER_DEFINITION, defaultValue = "QVI2PERS")
	private String defaultUserDefinition;

	/**
	 * Parses the file.
	 * 
	 * @param properties
	 *            the properties
	 * @param pathToValueMapping
	 *            the path to value mapping
	 * @param relations
	 *            the relations
	 * @return the map
	 * @throws Exception
	 *             the exception
	 */
	public Map<String, Instance> parseFile(Map<String, Serializable> properties,
			Map<String, AnnotationEntry> pathToValueMapping, Map<String, Instance> relations)
			throws Exception {
		InputStream inputStream = getFileStream(properties);
		if (inputStream == null) {
			LOGGER.error("Failed to read source file for parsing from {}", properties);
			return null;
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

			Map<String, Instance> instances = new LinkedHashMap<>();

			List<String> block;
			while (!(block = readBlock(reader)).isEmpty()) {
				processBlock(instances, block, pathToValueMapping, relations);
			}

			if (instances.isEmpty()) {
				LOGGER.warn("No data was extracted form the input file. Eghter");
				return null;
			}

			return instances;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Process block.
	 * 
	 * @param instances
	 *            the instances
	 * @param block
	 *            the block
	 * @param pathToValueMapping
	 *            the path to value mapping
	 * @param relations
	 *            the relations
	 */
	private void processBlock(Map<String, Instance> instances, List<String> block,
			Map<String, AnnotationEntry> pathToValueMapping, Map<String, Instance> relations) {
		for (String line : block) {
			// we will have only 4 columns in the file
			// the last column will be treated as one
			String[] split = LINE_SPLIT.split(line, 5);

			if (split.length < 5) {
				LOGGER.warn("Read line with not sufficient columns {} but extepted 5 from {} ",
						line.length(), line);
				continue;
			}

			// no need to trim the rest of the parts because the pattern will do the job.
			String options = split[0].trim();
			String path = split[1];
			String subject = split[2];
			String predicate = split[3];
			String value = split[4].trim();
			if (!value.isEmpty()
					&& LINE_SPLIT.matcher(String.valueOf(value.charAt(value.length() - 1)))
							.matches()) {
				value = value.substring(0, value.length() - 1);
			}

			// do something with the options

			Instance instance = getOrCreateInstance(subject, instances);
			Serializable serializable = convertValue(value, options, instances);

			// check for relations
			if (isComplexRelation(options)) {
				// create relation object for the given link
				Instance relation = getOrCreateInstance(predicate, instances);
				relations.put((String) relation.getId(), relation);
				relation.getProperties().put("from", subject);
				relation.getProperties().put("to", value);

				// does not add the complex relations to the instance as property
				continue;
			}

			// create mapping for the path and the current value and options for HTML
			// annotations
			if ((serializable != null) && !path.isEmpty()) {
				if (StringUtils.isBlank(options)) {
					if ("dcterms:identifier".equals(predicate)) {
						options = "LINK";
					}
				}
				pathToValueMapping.put(path, new AnnotationEntry(options, value, serializable,
						instance, path));
			}

			addToInstance(instance, predicate, serializable);
		}
	}

	/**
	 * Adds the given value to the instance properties. If a property with the given key exists a
	 * collection will be created and both properties will be added to it.
	 *
	 * @param instance
	 *            the instance
	 * @param propertyName
	 *            the predicate
	 * @param value
	 *            the value to add
	 */
	@SuppressWarnings("unchecked")
	private void addToInstance(Instance instance, String propertyName, Serializable value) {
		if (value == null) {
			// no need to add null value
			return;
		}
		Serializable currentValue = instance.getProperties().get(propertyName);
		if (currentValue instanceof Collection) {
			((Collection<Serializable>) currentValue).add(value);
		} else if (currentValue != null) {
			LinkedList<Serializable> data = new LinkedList<>();
			data.add(currentValue);
			data.add(value);
			instance.getProperties().put(propertyName, data);
		} else {
			instance.getProperties().put(propertyName, value);
		}
	}

	/**
	 * Convert value.
	 *
	 * @param value
	 *            the value
	 * @param options
	 *            the options
	 * @param instances
	 *            the instances
	 * @return the serializable
	 */
	private Serializable convertValue(String value, String options,
			Map<String, Instance> instances) {
		Serializable result = null;
		// remove any leading/trailing white spaces
		value = value.trim();
		if (value.isEmpty()) {
			return null;
		}

		if (StringUtils.isBlank(options)) {
			// plain string value
			result = value;
		} else if ("USER".equals(options)) {
			EmfUser user = new EmfUser();
			if (value.contains(":")) {
				LOGGER.debug("Detected user URI: {}", value);
				user.setId(value);
			} else {
				if (value.contains(" ")) {
					user.getProperties().put(ResourceProperties.NAME, value);
					// check if we have reverse order names
					// well we could have also a user names list but how to determine this?
					if (value.contains(",")) {
						LOGGER.debug("Detected user names with possible reverse order: {}", value);
						String[] split = value.split("\\s*,?\\s+", 2);
						user.getProperties().put(ResourceProperties.FIRST_NAME, split[1]);
						user.getProperties().put(ResourceProperties.LAST_NAME, split[0]);
					} else {
						LOGGER.debug("Detected user names with normal order: {}", value);
						String[] split = value.split("\\s+", 2);
						user.getProperties().put(ResourceProperties.FIRST_NAME, split[0]);
						user.getProperties().put(ResourceProperties.LAST_NAME, split[1]);
					}
					user.setIdentifier(user.getDisplayName().toLowerCase().replaceAll("\\s+", "-"));
					user.setId("emf:" + user.getIdentifier());
					user.getProperties().put(ResourceProperties.USER_ID, user.getIdentifier());
				} else {
					LOGGER.debug("Detected possible user name : {}", value);
					user.setId("emf:" + value.toLowerCase());
					user.setIdentifier(value);
					user.getProperties().put(ResourceProperties.INITIALS, value);
					user.getProperties().put(ResourceProperties.USER_ID, value);
				}
			}
			result = convertUserToEmfInstance(user, instances);
		} else if ("DATE".equals(options)) {
			// if the date format is less then this it's definitely not a date
			if (value.length() < 5) {
				LOGGER.warn("Invalid date value {}", value);
				return null;
			}
			result = typeConverter.convert(Date.class, value);
		} else if ("INTEGER".equals(options)) {
			try {
				BigDecimal bigDecimal = new BigDecimal(value.replace(',', '.'));
				// check if we have an integer value or decimal value
				if (bigDecimal.intValue() == bigDecimal.doubleValue()) {
					result = typeConverter.convert(Integer.class, value.trim());
				} else {
					result = typeConverter.convert(Double.class, value.trim());
				}
			} catch (NumberFormatException e) {
				LOGGER.warn("Invalid number value {}", value);
				return null;
			}
		} else if ("BOOLEAN".equals(options)) {
			if (EqualsHelper.nullSafeEquals(value, "x", true)
					|| EqualsHelper.nullSafeEquals(value, "*")) {
				result = Boolean.TRUE;
			} else {
				result =  Boolean.valueOf(value);
			}
		} else if ("RELATION".equals(options)) {
			if (!value.contains(":")) {
				LOGGER.debug("Invalid uri format for identifier or missing "
						+ "for field with options={} and value=", options, value);
				return value;
			}
			Instance instance = getOrCreateInstance(value, instances);
			// instance.getProperties().put("$sourceType$", options);
			result =  instance;
		} else if ("INSTANCE".equals(options) || "URI".equals(options)) {
			result =  typeConverter.convert(Uri.class, value);
		} else if (isComplexRelation(options)) {
			if (!value.contains(":")) {
				LOGGER.debug("Invalid uri format for identifier or missing "
						+ "for field with options={} and value=", options, value);
				return value;
			}

			// LinkInstance linkInstance = new LinkInstance();
			// Instance to = getOrCreateInstance(value, instances);
			// linkInstance.setTo(to);
			// result = linkInstance;
			Instance instance = getOrCreateInstance(value, instances);
			// instance.getProperties().put("$sourceType$", options);
			result = instance;
		}
		if ((result instanceof String) && result.toString().isEmpty()) {
			return null;
		}
		// if not recognized then return the value as is
		return result;
	}

	/**
	 * Checks if is complex relation.
	 * 
	 * @param options
	 *            the options
	 * @return true, if is complex relation
	 */
	private boolean isComplexRelation(String options) {
		return "RELATION TO PART".equals(options) || "RELATION TO ECN".equals(options);
	}

	/**
	 * Convert user to emf instance.
	 *
	 * @param user
	 *            the user
	 * @param instances
	 *            the instances
	 * @return the emf instance
	 */
	private Instance convertUserToEmfInstance(EmfUser user, Map<String, Instance> instances) {
		Instance instance = getOrCreateInstance((String) user.getId(), instances);
		instance.getProperties().putAll(PropertiesUtil.cloneProperties(user.getProperties()));
		instance.getProperties()
				.put("rdf:type", typeConverter.convert(Uri.class, "emf:DomainUser"));
		// set default user definition
		instance.setIdentifier(defaultUserDefinition);
		return instance;
	}

	/**
	 * Gets the or create instance.
	 *
	 * @param instanceId
	 *            the value
	 * @param instances
	 *            the instances
	 * @return the or create instance
	 */
	private Instance getOrCreateInstance(String instanceId, Map<String, Instance> instances) {
		Instance instance = instances.get(instanceId);
		if (instance == null) {
			Uri uri = typeConverter.convert(Uri.class, instanceId);
			if ((uri.getLocalName() == null)
					|| NON_APPLICABLE.matcher(uri.getLocalName()).matches()) {
				return null;
			}
			instance = new EmfInstance();
			instance.setId(instanceId);
			instance.setProperties(new LinkedHashMap<String, Serializable>());
			instances.put(instanceId, instance);
		}
		return instance;
	}

	/**
	 * Read block.
	 *
	 * @param reader
	 *            the reader
	 * @return the list
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private List<String> readBlock(BufferedReader reader) throws IOException {
		List<String> list = new LinkedList<>();
		String line = null;
		while (((line = reader.readLine()) != null) && !isBreakLine(line)) {
			list.add(line);
		}

		return list;
	}

	/**
	 * Checks if is break line.
	 *
	 * @param line
	 *            the line
	 * @return true, if is break line
	 */
	private boolean isBreakLine(String line) {
		return line.isEmpty() || LINE_SPLIT.matcher(line).replaceAll("").trim().isEmpty();
	}

	/**
	 * Gets the file stream.
	 *
	 * @param properties
	 *            the properties
	 * @return the file stream
	 */
	private InputStream getFileStream(Map<String, Serializable> properties) {
		Serializable serializable = properties.get(DocumentProperties.FILE_LOCATOR);
		if (serializable instanceof FileDescriptor) {
			return ((FileDescriptor) serializable).getInputStream();
		}
		return null;
	}
}
