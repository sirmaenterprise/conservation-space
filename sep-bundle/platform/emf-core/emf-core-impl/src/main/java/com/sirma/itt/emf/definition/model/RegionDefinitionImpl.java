package com.sirma.itt.emf.definition.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Mergeable;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Implementation class for region definition.
 *
 * @author BBonev
 */
public class RegionDefinitionImpl extends BaseDefinition<RegionDefinitionImpl> implements
		Serializable, BidirectionalMapping, Mergeable<RegionDefinitionImpl>, RegionDefinition,
		Cloneable {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(RegionDefinitionImpl.class);
	/**
	 * Comment for serialVersionUID.
	 */
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

	/**
	 * {@inheritDoc}
	 */
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
				MergeHelper.convertToMergable(src.getConditions()),
				EmfMergeableFactory.CONDITION_DEFINITION);

		if (src.getControlDefinition() != null) {
			controlDefinition = MergeHelper.replaceIfNull(controlDefinition,
					new ControlDefinitionImpl());
			((ControlDefinitionImpl) controlDefinition).mergeFrom(((ControlDefinitionImpl) src
					.getControlDefinition()));
		}
		return this;
	}

	/**
	 * Merge fields.
	 *
	 * @param source
	 *            the source
	 */
	@SuppressWarnings("unchecked")
	private void mergeFields(RegionDefinitionImpl source) {
		// If local is not a template and the source is template then should copy the fields as new
		// otherwise we need just to merge them
		List<PropertyDefinition> srcFields = source.getFields();
		if (!getTemplate()) {
			if (source.getTemplate()) {
				// copy all properties to new objects
				List<PropertyDefinition> temp = new LinkedList<PropertyDefinition>();
				MergeHelper.mergeLists(MergeHelper.convertToMergable(temp),
						MergeHelper.convertToMergable(source.getFields()),
						EmfMergeableFactory.FIELD_DEFINITION);

				for (PropertyDefinition propertyDefinition : temp) {
					propertyDefinition.setId(null);
				}
				srcFields = temp;
			}
		}
		MergeHelper.copyOrMergeLists(MergeHelper.convertToMergable(getFields()),
				MergeHelper.convertToMergable(srcFields));

	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
		super.setIdentifier(identifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		BaseDefinition<?> definition = getBaseRegionDefinition();
		if (definition instanceof PathElement) {
			return (PathElement) definition;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
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
	 * @param taskDefinition the taskDefinition to set
	 */
	public void setBaseRegionDefinition(BaseDefinition<?> taskDefinition) {
		baseRegionDefinition = taskDefinition;
	}

	/**
	 * Gets the value of the label property.
	 *
	 * @return the label possible object is {@link String }
	 */
	@Override
	public String getLabel() {
		String labelid = getLabelId();
		if (labelProvider != null) {
			if (labelid == null) {
				LOGGER.warn("Requesting a label from a region '" + getIdentifier()
						+ "' that does not have a label");
				return labelid;
			}
			return labelProvider.getLabel(labelid);
		}
		return labelid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTooltip() {
		String tooltip = getTooltipId();
		if (labelProvider != null) {
			if (tooltip == null) {
				LOGGER.warn("Requesting a tooltip from a region '" + getIdentifier()
						+ "' that does not have a tooltip");
				return tooltip;
			}
			return labelProvider.getLabel(tooltip);
		}
		return tooltip;
	}

	/**
	 * Setter method for labelProvider.
	 *
	 * @param labelProvider
	 *            the labelProvider to set
	 */
	@Override
	public void setLabelProvider(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	/**
	 * Gets the label id.
	 *
	 * @return the label id
	 */
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
		labelId = label;
	}

	/**
	 * Getter method for controlDefinition.
	 *
	 * @return the controlDefinition
	 */
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
		this.controlDefinition = controlDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * Getter method for order.
	 *
	 * @return the order
	 */
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
		this.order = order;
	}

	/**
	 * Getter method for displayType.
	 *
	 * @return the displayType
	 */
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
		this.displayType = displayType;
	}

	@Override
	public RegionDefinitionImpl clone() {
		RegionDefinitionImpl copy = new RegionDefinitionImpl();
		copy.identifier = identifier;
		copy.labelId = labelId;
		copy.order = order;
		copy.controlDefinition = controlDefinition;
		for (PropertyDefinition propertyDefinition : getFields()) {
			WritablePropertyDefinition clone = ((PropertyDefinitionProxy) propertyDefinition)
					.cloneProxy();
			copy.getFields().add(clone);
		}
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

	/**
	 * Getter method for tooltipId.
	 *
	 * @return the tooltipId
	 */
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
		this.tooltipId = tooltipId;
	}

	/**
	 * Getter method for conditions.
	 *
	 * @return the conditions
	 */
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
		this.conditions = conditions;
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
		this.template = template;
	}

	@Override
	public boolean hasChildren() {
		return super.hasChildren()
				|| ((getControlDefinition() != null) && getControlDefinition().hasChildren());
	}

	@Override
	public Node getChild(String name) {
		Node child = super.getChild(name);
		if ((child == null) && (getControlDefinition() != null)) {
			child = getControlDefinition().getChild(name);
		}
		return child;
	}

}
