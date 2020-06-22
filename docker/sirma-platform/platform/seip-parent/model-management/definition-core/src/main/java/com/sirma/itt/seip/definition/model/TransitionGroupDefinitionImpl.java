package com.sirma.itt.seip.definition.model;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;

/**
 * Implementation class for transition definition.
 *
 * @author Adrian Mitev
 */
public class TransitionGroupDefinitionImpl extends BaseDefinition<TransitionGroupDefinitionImpl>
		implements Serializable, TransitionGroupDefinition, Copyable<TransitionGroupDefinitionImpl> {

	private static final long serialVersionUID = 5227732384306471050L;

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Tag(1)
	protected String labelId;

	@Tag(3)
	protected Integer order;

	@Tag(4)
	protected String type;

	@Tag(5)
	protected String parent;

	protected transient LabelProvider labelProvider;

	@Override
	public String getLabelId() {
		return labelId;
	}

	/**
	 * Sets the value of the label property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setLabelId(String value) {
		if (!isSealed()) {
			labelId = value;
		}
	}

	@Override
	public TransitionGroupDefinitionImpl mergeFrom(TransitionGroupDefinitionImpl source) {
		setExpression(MergeHelper.replaceIfNull(getExpression(), source.getExpression()));

		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		labelId = MergeHelper.replaceIfNull(labelId, source.getLabelId());
		type = MergeHelper.replaceIfNull(type, source.getType());
		order = MergeHelper.replaceIfNull(order, source.getOrder());
		parent = MergeHelper.replaceIfNull(parent, source.getParent());

		return this;
	}

	@Override
	public void setIdentifier(String identifier) {
		if (!isSealed()) {
			this.identifier = identifier;
			super.setIdentifier(identifier);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TransitionGroupDefinitionImpl [");
		builder.append("labelId=");
		builder.append(labelId);
		builder.append(", parent=");
		builder.append(parent);
		builder.append(", order=");
		builder.append(order);
		builder.append(", type=");
		builder.append(type);
		builder.append(", super=");
		builder.append(super.toString());
		return builder.toString();
	}

	@Override
	public TransitionGroupDefinitionImpl createCopy() {
		TransitionGroupDefinitionImpl copy = new TransitionGroupDefinitionImpl();
		copy.identifier = getIdentifier();
		copy.parent = parent;
		copy.labelId = labelId;
		copy.order = order;
		copy.type = type;
		return copy;
	}

	@Override
	public void setLabelProvider(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	@Override
	public String getLabel() {
		if (labelProvider != null) {
			if (StringUtils.isBlank(labelId)) {
				LOGGER.warn("Requesting a label from a region that does not have a label: {}", this);
				return labelId;
			}
			return labelProvider.getLabel(labelId);
		}
		return labelId;
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
		if (!isSealed()) {
			this.order = order;
		}
	}

	@Override
	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof Identity)) {
			return false;
		}
		Identity other = (Identity) object;
		return nullSafeEquals(identifier, other.getIdentifier());
	}

	@Override
	public int hashCode() {
		return ((getIdentifier() == null ? 0 : getIdentifier().hashCode()) + 31) * 31;
	}

	@Override
	public DisplayType getDisplayType() {
		return null;
	}

	@Override
	public String getTooltip() {
		return null;
	}

	@Override
	public String getTooltipId() {
		return null;
	}

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
