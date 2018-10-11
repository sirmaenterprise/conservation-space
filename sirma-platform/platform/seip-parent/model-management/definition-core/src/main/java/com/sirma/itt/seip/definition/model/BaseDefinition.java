package com.sirma.itt.seip.definition.model;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.MergeableBase;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Base definition with fields. Provides common access to fields definitions list.
 *
 * @author BBonev
 * @param <E>
 *            the element type
 */
public class BaseDefinition<E extends BaseDefinition<?>> extends MergeableBase<E>
		implements DefinitionModel, BidirectionalMapping, Condition, JsonRepresentable, Sealable {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final long serialVersionUID = 672798411875226686L;

	@Tag(101)
	protected String identifier;

	@Tag(102)
	protected String expression;

	@Tag(103)
	protected Integer hash;

	@Tag(104)
	protected List<PropertyDefinition> fields;

	/** internal cache of the mapped properties. This is save for use only because the fields are not modifiable. */
	private transient Map<String, PropertyDefinition> fieldsMapping;

	private boolean sealed;

	@Override
	public List<PropertyDefinition> getFields() {
		if (fields == null) {
			fields = new LinkedList<>();
		}
		return fields;
	}

	@Override
	public Optional<PropertyDefinition> getField(String name) {
		return Optional.ofNullable(getCachedFieldsAsMap().get(name));
	}

	private Map<String, PropertyDefinition> getCachedFieldsAsMap() {
		if (fieldsMapping == null) {
			// create mapping for fields by name and uri
			fieldsMapping = DefinitionModel.super.getFieldsAsMap();
			fieldsMapping.putAll(fieldsStream()
					.filter(PropertyDefinition.hasUri())
					.collect(Collectors.toMap(PropertyDefinition.resolveUri(), Function.identity(),
							(v1, v2) -> {
								// this should not happen as there is a validation during compilation but just in case
								LOGGER.trace("Found fiends {} and {} with duplicate URI {} in {}", v1.getName(), v2.getName(),
										PropertyDefinition.resolveUri().apply(v2), getIdentifier());
								return v2;
							})));
		}
		return fieldsMapping;
	}

	/**
	 * Setter method for fields.
	 *
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(List<PropertyDefinition> fields) {
		if (!isSealed()) {
			this.fields = fields;
			fieldsMapping = null;
		}
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		if (!isSealed()) {
			this.identifier = identifier;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public E mergeFrom(E source) {
		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		expression = MergeHelper.replaceIfNull(expression, source.getExpression());

		// we do not assign the list to fields back because we are sure we will send a non null list
		// and the result will be into the given list
		MergeHelper.copyOrMergeLists(MergeHelper.convertToMergable(getFields()),
				MergeHelper.convertToMergable(source.getFields()));

		DefinitionUtil.sort(getFields());
		return (E) this;
	}

	@Override
	public void initBidirection() {
		if (fields != null) {
			for (PropertyDefinition definition : fields) {
				WritablePropertyDefinition definitionImpl = (WritablePropertyDefinition) definition;
				if (definitionImpl instanceof PropertyDefinitionProxy) {
					((PropertyDefinitionProxy) definitionImpl).setBaseDefinition(this);
				}
				definitionImpl.initBidirection();
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n BaseDefinition [");
		builder.append("getIdentifier()=");
		builder.append(getIdentifier());
		builder.append(", getHash()=");
		builder.append(getHash());
		builder.append(", expression=");
		builder.append(expression);
		builder.append(", getFields()=");
		builder.append(getFields());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String getRenderAs() {
		return "DISABLED";
	}

	@Override
	public String getExpression() {
		return expression;
	}

	/**
	 * Setter method for expression.
	 *
	 * @param expression
	 *            the expression to set
	 */
	public void setExpression(String expression) {
		if (!isSealed()) {
			this.expression = expression;
		}
	}

	@Override
	public Integer getHash() {
		return hash;
	}

	@Override
	public void setHash(Integer hash) {
		if (!isSealed()) {
			this.hash = hash;
		}
	}

	@Override
	public boolean hasChildren() {
		return !getFields().isEmpty();
	}

	@Override
	public Node getChild(String name) {
		return PathHelper.find(getFields(), name);
	}

	@Override
	public Long getRevision() {
		return 0L;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "identifier", getIdentifier());
		Collection<JSONObject> fieldsData = TypeConverterUtil.getConverter().convert(JSONObject.class, getFields());
		JsonUtil.addToJson(object, "fields", new JSONArray(fieldsData));
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// Method not in use
	}

	@Override
	public String getType() {
		return null;
	}

	@Override
	public boolean isSealed() {
		return sealed;
	}

	@Override
	public void seal() {
		// if already sealed nothing to do
		if (isSealed()) {
			return;
		}

		DefinitionUtil.sort(getFields());
		fields = Collections.unmodifiableList(Sealable.seal(getFields()));

		sealed = true;
	}

}
