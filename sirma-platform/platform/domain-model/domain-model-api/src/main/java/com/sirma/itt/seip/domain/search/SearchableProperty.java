package com.sirma.itt.seip.domain.search;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Represents a searchable field from the definitions, semantics &amp; solr.
 *
 * @author nvelkov
 * @author Mihail Radkov
 */
public class SearchableProperty implements JsonRepresentable {

	/** The id. */
	private String id;

	/** The codelist. */
	private Set<Integer> codelists;

	/** The uri. */
	private String uri;

	/** The range class. */
	private String rangeClass;

	/** The solr type. */
	private String solrType;

	/** The solr field name. */
	private String solrFieldName;

	/** The property type. */
	private String propertyType;

	/** The label id. */
	private Supplier<String> labelId;

	/** The label function. */
	private Function<String, String> labelProvider;

	/**
	 * Default constructor.
	 */
	public SearchableProperty() {
		// Default constructor.
	}

	/**
	 * A constructor used for deep copying a searchable property.
	 *
	 * @param searchableProperty
	 *            the searchable property that is going to be cloned
	 */
	public SearchableProperty(SearchableProperty searchableProperty) {
		id = searchableProperty.getId();
		if (CollectionUtils.isNotEmpty(searchableProperty.getCodelists())) {
			codelists = new HashSet<>(searchableProperty.getCodelists());
		}
		uri = searchableProperty.getUri();
		rangeClass = searchableProperty.getRangeClass();
		solrType = searchableProperty.getSolrType();
		solrFieldName = searchableProperty.getSolrFieldName();
		propertyType = searchableProperty.getPropertyType();
		labelId = searchableProperty.getLabelId();
		labelProvider = searchableProperty.getLabelProvider();
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the new id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets the text. The text is retrieved lazily based on the user language and the label id.
	 *
	 * @return the text
	 */
	public String getText() {
		return labelProvider.apply(getLabelId().get());
	}

	/**
	 * Gets the codelists.
	 *
	 * @return the codelists
	 */
	public Set<Integer> getCodelists() {
		return codelists;
	}

	/**
	 * Sets the codelists.
	 *
	 * @param codelists
	 *            the new codelists
	 */
	public void setCodelists(Set<Integer> codelists) {
		this.codelists = codelists;
	}

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the uri.
	 *
	 * @param uri
	 *            the new uri
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Gets the range class.
	 *
	 * @return the range class
	 */
	public String getRangeClass() {
		return rangeClass;
	}

	/**
	 * Sets the range class.
	 *
	 * @param rangeClass
	 *            the new range class
	 */
	public void setRangeClass(String rangeClass) {
		this.rangeClass = rangeClass;
	}

	/**
	 * Getter method for solrType.
	 *
	 * @return the solrType
	 */
	public String getSolrType() {
		return solrType;
	}

	/**
	 * Setter method for solrType.
	 *
	 * @param solrType
	 *            the solrType to set
	 */
	public void setSolrType(String solrType) {
		this.solrType = solrType;
	}

	/**
	 * Gets the solr field name.
	 *
	 * @return the solr field name
	 */
	public String getSolrFieldName() {
		return solrFieldName;
	}

	/**
	 * Sets the solr field name.
	 *
	 * @param solrFieldName
	 *            the new solr field name
	 */
	public void setSolrFieldName(String solrFieldName) {
		this.solrFieldName = solrFieldName;
	}

	/**
	 * Gets the property type.
	 *
	 * @return the property type
	 */
	public String getPropertyType() {
		return propertyType;
	}

	/**
	 * Sets the property type.
	 *
	 * @param propertyType
	 *            the new property type
	 */
	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	/**
	 * Gets the label id.
	 *
	 * @return the label id
	 */
	public Supplier<String> getLabelId() {
		return labelId;
	}

	/**
	 * Sets the label id.
	 *
	 * @param labelId
	 *            the new label id
	 */
	public void setLabelId(Supplier<String> labelId) {
		this.labelId = labelId;
	}

	/**
	 * Gets the label provider.
	 *
	 * @return the label provider
	 */
	public Function<String, String> getLabelProvider() {
		return labelProvider;
	}

	/**
	 * Sets the label provider.
	 *
	 * @param labelProvider
	 *            the new label provider
	 */
	public void setLabelProvider(Function<String, String> labelProvider) {
		this.labelProvider = labelProvider;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SearchableProperty)) {
			return false;
		}
		return EqualsHelper.nullSafeEquals(id, ((SearchableProperty) obj).id);
	}

	@Override
	public int hashCode() {
		final int primeNumber = 31;
		int hashCode = 1;
		hashCode = primeNumber * hashCode + (id == null ? 0 : id.hashCode());
		return hashCode;
	}

	@Override
	public String toString() {
		return "SearchableProperty [id=" + id + ", text=" + getText() + ", codelists=" + codelists + ", uri=" + uri
				+ ", rangeClass=" + rangeClass + ", solrType=" + solrType + ", solrFieldName=" + solrFieldName
				+ ", propertyType=" + propertyType + "]";
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject facetItem = new JSONObject();
		if (CollectionUtils.isNotEmpty(getCodelists())) {
			JSONArray codelistsJSON = new JSONArray();
			for (int codelist : getCodelists()) {
				codelistsJSON.put(codelist);
			}
			JsonUtil.addToJson(facetItem, SearchablePropertyProperties.CODELISTS, codelistsJSON);
		}
		JsonUtil.addToJson(facetItem, SearchablePropertyProperties.ID, getId());
		JsonUtil.addToJson(facetItem, SearchablePropertyProperties.RANGE_CLASS, getRangeClass());
		JsonUtil.addToJson(facetItem, SearchablePropertyProperties.SOLR_TYPE, getSolrType());
		JsonUtil.addToJson(facetItem, SearchablePropertyProperties.TEXT, getText());
		JsonUtil.addToJson(facetItem, SearchablePropertyProperties.URI, getUri());
		JsonUtil.addToJson(facetItem, SearchablePropertyProperties.SOLR_FIELD_NAME, getSolrFieldName());
		JsonUtil.addToJson(facetItem, SearchablePropertyProperties.PROPERTY_TYPE, getPropertyType());
		return facetItem;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		JSONArray codelistsJSON = JsonUtil.getJsonArray(jsonObject, SearchablePropertyProperties.CODELISTS);
		if (!JsonUtil.isNullOrEmpty(codelistsJSON)) {
			Set<Integer> codelistsLocal = new HashSet<>(codelistsJSON.length());
			for (int i = 0; i < codelistsJSON.length(); i++) {
				codelistsLocal.add(codelistsJSON.getInt(i));
			}
			setCodelists(codelistsLocal);
		}
		setRangeClass(JsonUtil.getStringValue(jsonObject, SearchablePropertyProperties.RANGE_CLASS));
		setSolrType(JsonUtil.getStringValue(jsonObject, SearchablePropertyProperties.SOLR_TYPE));
		setUri(JsonUtil.getStringValue(jsonObject, SearchablePropertyProperties.URI));
		setId(JsonUtil.getStringValue(jsonObject, SearchablePropertyProperties.ID));
		setRangeClass(JsonUtil.getStringValue(jsonObject, SearchablePropertyProperties.RANGE_CLASS));
		setSolrFieldName(JsonUtil.getStringValue(jsonObject, SearchablePropertyProperties.SOLR_FIELD_NAME));
		setPropertyType(JsonUtil.getStringValue(jsonObject, SearchablePropertyProperties.PROPERTY_TYPE));
	}

}
