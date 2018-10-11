/**
 *
 */
package com.sirma.itt.seip.testutil.mocks;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;

/**
 * @author BBonev
 */
public class TransitionDefinitionMock implements TransitionDefinition {

	private static final long serialVersionUID = 5807965649212528106L;

	@Tag(1)
	protected String identifier;

	@Tag(2)
	protected String labelId;

	@Tag(3)
	protected String tooltipId;

	@Tag(4)
	protected String eventId;

	@Tag(5)
	protected String nextPrimaryState;

	@Tag(6)
	protected String nextSecondaryState;

	private transient DefinitionModel owningDefinition;

	@Tag(7)
	protected List<Condition> conditions;

	@Tag(8)
	protected Boolean template = Boolean.FALSE;

	/** The default transition. */
	@Tag(9)
	private Boolean defaultTransition;

	@Tag(10)
	protected Boolean immediate;

	/** The purpose. */
	@Tag(11)
	protected String purpose;

	@Tag(12)
	private String ownerPrefix;

	@Tag(13)
	protected Integer order;

	@Tag(14)
	protected String actionPath;

	protected transient LabelProvider labelProvider;

	/*
	 * WHY this is transient!!!! Don't remember...? Do I need to make it persistent or to remove it at all. It's copied
	 * from XML definition, but why is transient??? FOUND IT: The transitions that are SYSTEM are removed compile time
	 */
	protected transient DisplayType displayType = DisplayType.EDITABLE;

	@Tag(14)
	protected String disabledReasonId;

	@Tag(15)
	protected String confirmationMessageId;

	/** The seal. */
	private transient boolean seal = false;

	private Integer hash;

	private List<PropertyDefinition> fields = new ArrayList<>();

	@Tag(18)
	protected String group;

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
		if (!seal) {
			labelId = value;
		}
	}

	@Override
	public String getEventId() {
		return eventId;
	}

	/**
	 * Sets the value of the eventId property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setEventId(String value) {
		if (!seal) {
			eventId = value;
		}
	}

	@Override
	public PathElement getParentElement() {
		if (getOwningDefinition() instanceof PathElement) {
			return (PathElement) getOwningDefinition();
		}
		return null;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		if (!seal) {
			this.identifier = identifier;
		}
	}

	/**
	 * Getter method for nextPrimaryState.
	 *
	 * @return the nextPrimaryState
	 */
	@Override
	public String getNextPrimaryState() {
		return nextPrimaryState;
	}

	/**
	 * Setter method for nextPrimaryState.
	 *
	 * @param nextPrimaryState
	 *            the nextPrimaryState to set
	 */
	public void setNextPrimaryState(String nextPrimaryState) {
		if (!seal) {
			this.nextPrimaryState = nextPrimaryState;
		}
	}

	/**
	 * Getter method for nextSecondaryState.
	 *
	 * @return the nextSecondaryState
	 */
	@Override
	public String getNextSecondaryState() {
		return nextSecondaryState;
	}

	/**
	 * Setter method for nextSecondaryState.
	 *
	 * @param nextSecondaryState
	 *            the nextSecondaryState to set
	 */
	public void setNextSecondaryState(String nextSecondaryState) {
		if (!seal) {
			this.nextSecondaryState = nextSecondaryState;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TransitionDefinitionImpl [");
		builder.append("identifier=");
		builder.append(identifier);
		builder.append(", labelId=");
		builder.append(labelId);
		builder.append(", tooltipId=");
		builder.append(tooltipId);
		builder.append(", eventId=");
		builder.append(eventId);
		builder.append(", nextPrimaryState=");
		builder.append(nextPrimaryState);
		builder.append(", nextSecondaryState=");
		builder.append(nextSecondaryState);
		builder.append(", group=");
		builder.append(group);
		builder.append(", super=");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String getLabel() {
		String labelid = getLabelId();
		if (labelProvider != null) {
			if (StringUtils.isBlank(labelid)) {
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
			if (StringUtils.isBlank(tooltip)) {
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
		if (!seal) {
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
		if (!seal) {
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
		if (!seal) {
			this.template = template;
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
		if (!seal) {
			this.displayType = displayType;
		}
	}

	@Override
	public DefinitionModel getOwningDefinition() {
		return owningDefinition;
	}

	/**
	 * Setter method for owningDefinition.
	 *
	 * @param owningDefinition
	 *            the owningDefinition to set
	 */
	public void setOwningDefinition(DefinitionModel owningDefinition) {
		if (!seal) {
			this.owningDefinition = owningDefinition;
		}
	}

	@Override
	public Boolean getDefaultTransition() {
		Boolean transition = defaultTransition;
		if (transition == null) {
			transition = Boolean.FALSE;
		}
		return transition;
	}

	/**
	 * Setter method for defaultTransition.
	 *
	 * @param defaultTransition
	 *            the defaultTransition to set
	 */
	public void setDefaultTransition(Boolean defaultTransition) {
		if (!seal) {
			this.defaultTransition = defaultTransition;
		}
	}

	@Override
	public boolean isImmediateAction() {
		Boolean b = getImmediate();
		if (b == null) {
			return false;
		}
		return b.booleanValue();
	}

	/**
	 * Getter method for immediate.
	 *
	 * @return the immediate
	 */
	public Boolean getImmediate() {
		return immediate;
	}

	/**
	 * Setter method for immediate.
	 *
	 * @param immediate
	 *            the immediate to set
	 */
	public void setImmediate(Boolean immediate) {
		if (!seal) {
			this.immediate = immediate;
		}
	}

	@Override
	public String getPurpose() {
		return purpose;
	}

	/**
	 * Setter method for purpose.
	 *
	 * @param purpose
	 *            the purpose to set
	 */
	@Override
	public void setPurpose(String purpose) {
		if (!seal) {
			this.purpose = purpose;
		}
	}

	@Override
	public String getActionId() {
		return getIdentifier();
	}

	@Override
	public boolean isDisabled() {
		return getDisabledReason() != null;
	}

	@Override
	public String getDisabledReason() {
		String reason = getDisabledReasonId();
		if (StringUtils.isBlank(reason)) {
			reason = getActionId() + ".disabled.reason";
		}
		if (StringUtils.isNotBlank(reason) && labelProvider != null) {
			String label = labelProvider.getLabel(reason);
			if (reason.equals(label)) {
				return null;
			}
			return label;
		}
		return null;
	}

	@Override
	public String getConfirmationMessage() {
		String confirm = getConfirmationMessageId();
		if (StringUtils.isBlank(confirm)) {
			confirm = getActionId() + ".confirm";
		}
		if (StringUtils.isNotBlank(confirm) && labelProvider != null) {
			String label = labelProvider.getLabel(confirm);
			if (confirm.equals(label)) {
				return null;
			}
			return label;
		}
		return null;
	}

	@Override
	public String getIconImagePath() {
		return new StringBuilder(128)
				.append("images:icon_")
					.append(ownerPrefix == null ? "" : ownerPrefix + "_")
					.append(getActionId())
					.append(".png")
					.toString();
	}

	@Override
	public String getOnclick() {
		return null;
	}

	@Override
	public boolean isSealed() {
		return seal;
	}

	@Override
	public void seal() {
		seal = true;
	}

	@Override
	public String getOwnerPrefix() {
		return ownerPrefix;
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
		if (!seal) {
			this.order = order;
		}
	}

	@Override
	public String getDisabledReasonId() {
		return disabledReasonId;
	}

	@Override
	public String getConfirmationMessageId() {
		return confirmationMessageId;
	}

	/**
	 * Setter method for disabledReasonId.
	 *
	 * @param disabledReasonId
	 *            the disabledReasonId to set
	 */
	public void setDisabledReasonId(String disabledReasonId) {
		if (!seal) {
			this.disabledReasonId = disabledReasonId;
		}
	}

	/**
	 * Setter method for confirmationMessageId.
	 *
	 * @param confirmationMessageId
	 *            the confirmationMessageId to set
	 */
	public void setConfirmationMessageId(String confirmationMessageId) {
		if (!seal) {
			this.confirmationMessageId = confirmationMessageId;
		}
	}

	@Override
	public int hashCode() {
		return ((getIdentifier() == null ? 0 : getIdentifier().hashCode()) + 31) * 31;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Identity)) {
			return false;
		}
		Identity other = (Identity) obj;
		if (identifier == null) {
			if (other.getIdentifier() != null) {
				return false;
			}
		} else if (!identifier.equals(other.getIdentifier())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Node getChild(String name) {
		return null;
	}

	@Override
	public List<PropertyDefinition> getFields() {
		return fields;
	}

	/**
	 * Sets the fields.
	 *
	 * @param fields
	 *            the new fields
	 */
	public void setFields(List<PropertyDefinition> fields) {
		this.fields = fields;
	}

	@Override
	public Long getRevision() {
		return null;
	}

	@Override
	public Integer getHash() {
		return hash;
	}

	@Override
	public void setHash(Integer hash) {
		this.hash = hash;
	}

	@Override
	public String getType() {
		return null;
	}

	@Override
	public JsonObject getConfigurationAsJson() {
		return null;
	}

	@Override
	public String getActionPath() {
		return actionPath;
	}

	@Override
	public String getGroup() {
		return group;
	}
}
