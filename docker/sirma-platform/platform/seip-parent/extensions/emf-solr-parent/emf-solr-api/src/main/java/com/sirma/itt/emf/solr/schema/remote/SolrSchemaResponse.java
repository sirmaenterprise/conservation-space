package com.sirma.itt.emf.solr.schema.remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.SolrResponseBase;
import org.apache.solr.common.util.NamedList;
import org.json.JSONObject;

import com.sirma.itt.emf.solr.exception.SolrSchemaException;
import com.sirma.itt.emf.solr.schema.model.SchemaEntryCopyField;
import com.sirma.itt.emf.solr.schema.model.SchemaEntryDynamicField;
import com.sirma.itt.emf.solr.schema.model.SchemaEntryField;
import com.sirma.itt.emf.solr.schema.model.SchemaEntryFieldType;
import com.sirma.itt.emf.solr.schema.model.SolrSchemaModel;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * The Class SolrSchemaResponse.
 */
public class SolrSchemaResponse extends SolrResponseBase {

	/** serialVersionUID. */
	private static final long serialVersionUID = -17058268746433722L;
	/** The solr schema. */
	private NamedList<Object> rawSolrSchema = null;
	/** The model. */
	private SolrSchemaModel model;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setResponse(NamedList<Object> response) {
		super.setResponse(response);
		Object errors = response.get("errors");
		if (errors != null) {
			throw new EmfRuntimeException("Error recieved during schema request! " + errors);
		}
		rawSolrSchema = (NamedList<Object>) response.get("schema");
		if (rawSolrSchema == null) {
			return;
		}
		try {
			model = new SolrSchemaModel(getName(), getVersion());
			addFieldTypes();
			addFields();
			addDynamicFields();
			addCopyFields();
		} catch (SolrSchemaException e) {
			model = new SolrSchemaModel(getName(), getVersion());
			throw new EmfRuntimeException("Solr schema parse error!", e);
		}
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return rawSolrSchema.get("name").toString();
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return rawSolrSchema.get("version").toString();
	}

	/**
	 * Gets the parsed solr model schema.
	 *
	 * @return the parsed model or null on error
	 */
	public SolrSchemaModel getSolrModel() {
		return model;
	}

	/**
	 * Gets the field types.
	 *
	 * @throws SolrSchemaException
	 *             on solr parse error
	 */
	@SuppressWarnings("unchecked")
	private void addFieldTypes() throws SolrSchemaException {

		List<NamedList<Object>> object = (List<NamedList<Object>>) rawSolrSchema.get("fieldTypes");

		for (NamedList<Object> fieldTypes : object) {
			JSONObject asMap = new JSONObject(asMap(fieldTypes, 10));
			SchemaEntryFieldType createdEntry = new SchemaEntryFieldType(asMap.remove("name").toString(),
					asMap.remove("class").toString(), JsonUtil.toMap(asMap));
			model.addEntry(createdEntry);
		}
	}

	/**
	 * Gets the fields.
	 *
	 * @throws SolrSchemaException
	 *             on solr parse error
	 */
	@SuppressWarnings("unchecked")
	private void addFields() throws SolrSchemaException {
		List<NamedList<Object>> object = (List<NamedList<Object>>) rawSolrSchema.get("fields");
		for (NamedList<Object> fieldTypes : object) {
			Map<Object, Object> asMap = fieldTypes.asMap(10);
			SchemaEntryField createdEntry = new SchemaEntryField(asMap.remove("name").toString(),
					asMap.remove("type").toString(), asMap);
			model.addEntry(createdEntry);
		}
	}

	/**
	 * Gets the dynamic fields.
	 *
	 * @throws SolrSchemaException
	 *             on solr parse error
	 */
	@SuppressWarnings("unchecked")
	private void addDynamicFields() throws SolrSchemaException {
		List<NamedList<Object>> object = (List<NamedList<Object>>) rawSolrSchema.get("dynamicFields");
		for (NamedList<Object> fieldTypes : object) {
			Map<Object, Object> asMap = fieldTypes.asMap(10);
			SchemaEntryDynamicField createdEntry = new SchemaEntryDynamicField(asMap.remove("name").toString(),
					asMap.remove("type").toString(), asMap);
			model.addEntry(createdEntry);
		}
	}

	/**
	 * Gets the copy fields.
	 *
	 * @throws SolrSchemaException
	 *             on solr parse error
	 */
	@SuppressWarnings("unchecked")
	private void addCopyFields() throws SolrSchemaException {
		List<NamedList<Object>> object = (List<NamedList<Object>>) rawSolrSchema.get("copyFields");
		for (NamedList<Object> fieldTypes : object) {
			Map<Object, Object> asMap = fieldTypes.asMap(10);
			SchemaEntryCopyField createdEntry = new SchemaEntryCopyField(asMap.remove("source").toString(),
					asMap.remove("dest").toString());
			model.addEntry(createdEntry);
		}
	}

	/**
	 * Converts to map a solr response.
	 *
	 * @param fieldTypes
	 *            the current level model
	 * @param maxDepth
	 *            the max depth
	 * @return the result as map model
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<Object, Object> asMap(NamedList<Object> fieldTypes, int maxDepth) {
		Map<Object, Object> result = new LinkedHashMap<>(fieldTypes.size());
		for (int i = 0; i < fieldTypes.size(); i++) {
			Object val = fieldTypes.getVal(i);
			if (maxDepth < 0) {
				return result;
			}
			String key = fieldTypes.getName(i);
			if (val instanceof NamedList) {
				val = asMap((NamedList<Object>) val, maxDepth - 1);
			} else if (val instanceof Collection) {
				List<Object> newValue = new ArrayList<Object>(((Collection) val).size());
				for (Object collectionEntry : (Collection) val) {
					Object next = null;
					if (collectionEntry instanceof NamedList) {
						next = asMap((NamedList<Object>) collectionEntry, maxDepth - 1);
					}
					newValue.add(next);
				}
				val = newValue;
			}
			Object old = result.put(key, val);
			if (old != null) {
				if (old instanceof List) {
					List list = (List) old;
					list.add(val);
					result.put(key, old);
				} else {
					List l = new ArrayList();
					l.add(old);
					l.add(val);
					result.put(key, l);
				}
			}
		}
		return result;
	}

}
