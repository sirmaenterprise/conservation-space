package com.sirma.itt.emf.solr.schema.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.solr.exception.SolrSchemaException;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * The SchemaModel is java representation of schema.xml. Make use of /schema api represented in Solr 5
 *
 * @author bbanchev
 */
public class SolrSchemaModel {
	private boolean readOnly = true;
	/** The version. */
	private String version;
	/** The name. */
	private String name;
	/** The model. */
	private Map<String, SchemaEntry> entries = new LinkedHashMap<>();

	/**
	 * Instantiates a new schema model.
	 *
	 * @param name
	 *            the name
	 * @param version
	 *            the version
	 */
	public SolrSchemaModel(String name, String version) {
		super();
		this.name = name;
		this.version = version;
	}

	/**
	 * Adds the entry to the model.
	 *
	 * @param entry
	 *            the entry to add
	 * @return the schema model itself
	 * @throws SolrSchemaException
	 *             on duplicate addition of entry
	 */
	public SolrSchemaModel addEntry(SchemaEntry entry) throws SolrSchemaException {
		if (entries.containsKey(entry.getId())) {
			throw new SolrSchemaException("Already contained!: " + entry.getId());
		}
		entries.put(entry.getId(), entry);
		return this;

	}

	/**
	 * Makes diff of current model to some other model and return a new model. <br>
	 * If entry exist in old and current model it is set for replacement in the new model. <br>
	 * If entry is not contained in the fromModel it is set for add in the new model. <br>
	 * If entry is not contained in the current model but in the fromModel it is set for remove in the new model.
	 *
	 * @param fromModel
	 *            the other model to diff from
	 * @return a new solr schema model ready for build
	 * @throws SolrSchemaException
	 *             on solr model populate error
	 */
	public SolrSchemaModel diff(SolrSchemaModel fromModel) throws SolrSchemaException {
		Map<String, SchemaEntry> fromEntries = new LinkedHashMap<>(fromModel.entries);
		SolrSchemaModel newModel = new SolrSchemaModel(name, version);
		newModel.readOnly = false;
		// first add the new fields to be discoverable in POST
		for (Entry<String, SchemaEntry> schemaEntry : entries.entrySet()) {
			SchemaEntry value = schemaEntry.getValue();
			String key = schemaEntry.getKey();
			if (!fromEntries.containsKey(key)) {
				SchemaEntry copy = value.createCopy();
				((AbstractSchemaEntry)copy).operation = SolrSchemaMode.ADD;
				newModel.addEntry(copy);
			}
		}
		// add the replaced
		for (Entry<String, SchemaEntry> schemaEntry : entries.entrySet()) {
			SchemaEntry value = schemaEntry.getValue();
			String key = schemaEntry.getKey();
			if (fromEntries.containsKey(key)) {
				SchemaEntry oldEntry = fromEntries.remove(key);
				if (!sameEntry(value, oldEntry)) {
					SchemaEntry copy = value.createCopy();
					((AbstractSchemaEntry)copy).operation = SolrSchemaMode.REPLACE;
					newModel.addEntry(copy);
				}
			}
		}
		// now add the removed
		for (SchemaEntry schemaEntry : fromEntries.values()) {
			SchemaEntry copy = schemaEntry.createCopy();
			((AbstractSchemaEntry)copy).operation = SolrSchemaMode.DELETE;
			newModel.addEntry(copy);
		}
		return newModel;
	}

	private boolean sameEntry(SchemaEntry value, SchemaEntry oldEntry) {
		return value.hashCode() == oldEntry.hashCode();
	}

	/**
	 * Builds the.
	 *
	 * @return the string
	 */
	public String build() {
		if (readOnly) {
			throw new EmfRuntimeException("Model is readOnly!");
		}
		StringBuilder builder = new StringBuilder("{");
		Iterator<Entry<String, SchemaEntry>> iterator = entries.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, SchemaEntry> schemaEntry = iterator.next();
			String nextEntry = schemaEntry.getValue().build();
			if (StringUtils.isNotBlank(nextEntry)) {
				if (builder.length() > 1) {
					builder.append(",");
				}
				builder.append(nextEntry);

			}
		}
		builder.append("}");
		return builder.toString();

	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
}
