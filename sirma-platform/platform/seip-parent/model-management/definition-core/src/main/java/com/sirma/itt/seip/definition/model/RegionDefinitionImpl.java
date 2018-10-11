package com.sirma.itt.seip.definition.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.compile.EmfMergeableFactory;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Implementation class for region definition.
 *
 * @author BBonev
 */
public class RegionDefinitionImpl extends BaseDefinition<RegionDefinitionImpl>implements RegionDefinition, Copyable<RegionDefinitionImpl> {

	private static final Logger LOGGER = Logger.getLogger(RegionDefinitionImpl.class);

	private static final long serialVersionUID = -4897000305871045218L;

	/** The region id. */
	@Tag(1)
	protected String identifier;

	/** The preview empty. */
	@Tag(2)
	protected DisplayType displayType;

	/** The label. */
	@Tag(3)
	protected String labelId;

	/** The label. */
	@Tag(4)
	protected String tooltipId;

	/** The order. */
	@Tag(5)
	protected Integer order;

	/** The control definition. */
	@Tag(6)
	protected ControlDefinition controlDefinition;

	/** The task definition ref. */
	protected transient BaseDefinition<?> baseRegionDefinition;

	/** The conditions. */
	@Tag(7)
	protected List<Condition> conditions;

	/** The label provider. */
	protected transient LabelProvider labelProvider;

	/** The template. */
	@Tag(8)
	protected Boolean template = Boolean.FALSE;

	@Override
	@SuppressWarnings("unchecked")
	public RegionDefinitionImpl mergeFrom(RegionDefinitionImpl source) {
		mergeFields(source);

		MergeHelper.copyOrMergeLists(MergeHelper.convertToMergable(getFields()),
				MergeHelper.convertToMergable(source.getFields()));
		setExpression(MergeHelper.replaceIfNull(getExpression(), source.getExpression()));

		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());

		RegionDefinitionImpl src = source;
		labelId = MergeHelper.replaceIfNull(labelId, src.getLabelId());
		tooltipId = MergeHelper.replaceIfNull(tooltipId, src.getTooltipId());
		displayType = MergeHelper.replaceIfNull(displayType, src.getDisplayType());
		order = MergeHelper.replaceIfNull(order, src.getOrder());

		conditions = MergeHelper.mergeLists(MergeHelper.convertToMergable(conditions),
				MergeHelper.convertToMergable(src.getConditions()), EmfMergeableFactory.CONDITION_DEFINITION);

		if (src.getControlDefinition() != null) {
			controlDefinition = MergeHelper.replaceIfNull(controlDefinition, new ControlDefinitionImpl());
			((ControlDefinitionImpl) controlDefinition).mergeFrom((ControlDefinitionImpl) src.getControlDefinition());
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	private void mergeFields(RegionDefinitionImpl source) {
		// If local is not a template and the source is template then should copy the fields as new
		// otherwise we need just to merge them
		List<PropertyDefinition> srcFields = source.getFields();
		if (!getTemplate().booleanValue() && source.getTemplate().booleanValue()) {
			// copy all properties to new objects
			List<PropertyDefinition> temp = new LinkedList<>();
			MergeHelper.mergeLists(MergeHelper.convertToMergable(temp),
					MergeHelper.convertToMergable(source.getFields()), EmfMergeableFactory.FIELD_DEFINITION);

			for (PropertyDefinition propertyDefinition : temp) {
				propertyDefinition.setId(null);
			}
			srcFields = temp;
		}
		MergeHelper.copyOrMergeLists(MergeHelper.convertToMergable(getFields()),
				MergeHelper.convertToMergable(srcFields));

		DefinitionUtil.sort(getFields());
	}

	@Override
	public void initBidirection() {
		super.initBidirection();
		if (controlDefinition != null) {
			ControlDefinitionImpl impl = (ControlDefinitionImpl) controlDefinition;
			impl.setParentEntity(this);
			impl.initBidirection();
		}
		if (getConditions() != null) {
			for (Condition definition : getConditions()) {
				ConditionDefinitionImpl definitionImpl = (ConditionDefinitionImpl) definition;
				definitionImpl.setRegionDefinition(this);
			}
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
			super.setIdentifier(identifier);
		}
	}

	@Override
	public PathElement getParentElement() {
		BaseDefinition<?> definition = getBaseRegionDefinition();
		if (definition instanceof PathElement) {
			return (PathElement) definition;
		}
		return null;
	}

	@Override
	public String getPath() {
		return null;
	}

	/**
	 * Getter method for taskDefinition.
	 *
	 * @return the taskDefinition
	 */
	public BaseDefinition<?> getBaseRegionDefinition() {
		return baseRegionDefinition;
	}

	/**
	 * Setter method for taskDefinition.
	 *
	 * @param taskDefinition
	 *            the taskDefinition to set
	 */
	public void setBaseRegionDefinition(BaseDefinition<?> taskDefinition) {
		if (!isSealed()) {
			baseRegionDefinition = taskDefinition;
		}
	}

	@Override
	public String getLabel() {
		String labelid = getLabelId();
		if (labelProvider != null) {
			if (labelid == null) {
				LOGGER.warn("Requesting a label from a region '" + getIdentifier() + "' that does not have a label");
				return labelid;
			}
			return labelProvider.getLabel(labelid);
		}
		return labelid;
	}

	@Override
	public String getTooltip() {
		String tooltip = getTooltipId();
		if (labelProvider != null) {
			if (tooltip == null) {
				LOGGER.warn(
						"Requesting a tooltip from a region '" + getIdentifier() + "' that does not have a tooltip");
				return tooltip;
			}
			return labelProvider.getLabel(tooltip);
		}
		return tooltip;
	}

	@Override
	public void setLabelProvider(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	@Override
	public String getLabelId() {
		return labelId;
	}

	/**
	 * Setter method for label.
	 *
	 * @param label
	 *            the label to set
	 */
	public void setLabelId(String label) {
		if (!isSealed()) {
			labelId = label;
		}
	}

	@Override
	public ControlDefinition getControlDefinition() {
		return controlDefinition;
	}

	/**
	 * Setter method for controlDefinition.
	 *
	 * @param controlDefinition
	 *            the controlDefinition to set
	 */
	public void setControlDefinition(ControlDefinition controlDefinition) {
		if (!isSealed()) {
			this.controlDefinition = controlDefinition;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RegionDefinitionImpl [identifier=");
		builder.append(identifier);
		builder.append(", order=");
		builder.append(order);
		builder.append(", labelId=");
		builder.append(labelId);
		builder.append(", displayType=");
		builder.append(getDisplayType());
		builder.append(", controlDefinition=");
		builder.append(getControlDefinition());
		builder.append(", super=");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public Integer getOrder() {
		return order;
	}

	/**
	 * Setter method for order.
	 *
	 * @param order
	 *            the order to set
	 */
	public void setOrder(Integer order) {
		if (!isSealed()) {
			this.order = order;
		}
	}

	@Override
	public DisplayType getDisplayType() {
		return displayType;
	}

	/**
	 * Setter method for displayType.
	 *
	 * @param displayType
	 *            the displayType to set
	 */
	public void setDisplayType(DisplayType displayType) {
		if (!isSealed()) {
			this.displayType = displayType;
		}
	}

	@Override
	public RegionDefinitionImpl createCopy() {
		RegionDefinitionImpl copy = new RegionDefinitionImpl();
		copy.identifier = identifier;
		copy.labelId = labelId;
		copy.order = order;
		copy.displayType = displayType;
		copy.controlDefinition = controlDefinition;
		if (conditions != null) {
			copy.setConditions(new ArrayList<>(conditions.size()));
			for (Condition condition : conditions) {
				copy.getConditions().add(((ConditionDefinitionImpl) condition).createCopy());
			}
		}
		copy.labelProvider = labelProvider;
		for (PropertyDefinition propertyDefinition : getFields()) {
			WritablePropertyDefinition clone = ((PropertyDefinitionProxy) propertyDefinition).cloneProxy();
			copy.getFields().add(clone);
		}
		DefinitionUtil.sort(copy.getFields());
		return copy;
	}

	/**
	 * Sets the default properties.
	 */
	public void setDefaultProperties() {
		if (displayType == null) {
			displayType = DisplayType.EDITABLE;
		} else if (displayType == DisplayType.SYSTEM) {
			// changes the display type of all fields to system if the region is
			// system
			for (PropertyDefinition definition : getFields()) {
				WritablePropertyDefinition impl = (WritablePropertyDefinition) definition;
				impl.setDisplayType(DisplayType.SYSTEM);
			}
		}
	}

	@Override
	public String getTooltipId() {
		return tooltipId;
	}

	/**
	 * Setter method for tooltipId.
	 *
	 * @param tooltipId
	 *            the tooltipId to set
	 */
	public void setTooltipId(String tooltipId) {
		if (!isSealed()) {
			this.tooltipId = tooltipId;
		}
	}

	@Override
	public List<Condition> getConditions() {
		return conditions;
	}

	/**
	 * Setter method for conditions.
	 *
	 * @param conditions
	 *            the conditions to set
	 */
	public void setConditions(List<Condition> conditions) {
		if (!isSealed()) {
			this.conditions = conditions;
		}
	}

	/**
	 * Getter method for template.
	 *
	 * @return the template
	 */
	public Boolean getTemplate() {
		return template;
	}

	/**
	 * Setter method for template.
	 *
	 * @param template
	 *            the template to set
	 */
	public void setTemplate(Boolean template) {
		if (!isSealed()) {
			this.template = template;
		}
	}

	@Override
	public boolean hasChildren() {
		return super.hasChildren() || getControlDefinition() != null && getControlDefinition().hasChildren();
	}

	@Override
	public Node getChild(String name) {
		Node child = super.getChild(name);
		if (child == null && getControlDefinition() != null) {
			child = getControlDefinition().getChild(name);
		}
		return child;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject object = super.toJSONObject();
		JsonUtil.addToJson(object, "label", getLabel());
		JSONObject controlDefinitionData = TypeConverterUtil.getConverter().convert(JSONObject.class,
				getControlDefinition());
		JsonUtil.addToJson(object, "controlDefinition", controlDefinitionData);
		JsonUtil.addToJson(object, "displayType", getDisplayType());
		Collection<JSONObject> condition = TypeConverterUtil.getConverter().convert(JSONObject.class, getConditions());
		JsonUtil.addToJson(object, "conditions", new JSONArray(condition));
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// Method not in use
	}

	@Override
	public void seal() {
		// nothing to do more
		if (isSealed()) {
			return;
		}

		controlDefinition = Sealable.seal(controlDefinition);
		if (conditions != null) {
			conditions = Collections.unmodifiableList(getConditions());
		}

		super.seal();
	}
}
