/*
 *
 */
package com.sirma.itt.seip.definition.model;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.compile.EmfMergeableFactory;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.rest.utils.JSON;

/**
 * Implementation class for transition definition.
 *
 * @author BBonev
 */
public class TransitionDefinitionImpl extends BaseDefinition<TransitionDefinitionImpl>
		implements Serializable, TransitionDefinition, Copyable<TransitionDefinitionImpl>, BidirectionalMapping {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final long serialVersionUID = 5807965649212528106L;

	private static final String CONFIGURATION = "configuration";

	private static final String DEFAULT_ACTION_PATH = "/actions";

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

	@Tag(9)
	protected Boolean defaultTransition;

	@Tag(10)
	protected Boolean immediate;

	@Tag(11)
	protected String purpose;

	@Tag(12)
	private String ownerPrefix;

	@Tag(13)
	protected Integer order;

	protected transient LabelProvider labelProvider;

	@Tag(16)
	protected DisplayType displayType = DisplayType.EDITABLE;

	@Tag(14)
	protected String disabledReasonId;

	@Tag(15)
	protected String confirmationMessageId;

	@Tag(17)
	protected String actionPath;

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
		if (!isSealed()) {
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
		if (!isSealed()) {
			eventId = value;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public TransitionDefinitionImpl mergeFrom(TransitionDefinitionImpl source) {
		mergeFields(source);
		setExpression(MergeHelper.replaceIfNull(getExpression(), source.getExpression()));

		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		displayType = MergeHelper.replaceIfNull(displayType, source.getDisplayType());
		labelId = MergeHelper.replaceIfNull(labelId, source.getLabelId());
		tooltipId = MergeHelper.replaceIfNull(tooltipId, source.getTooltipId());
		eventId = MergeHelper.replaceIfNull(eventId, source.getEventId());
		nextPrimaryState = MergeHelper.replaceIfNull(nextPrimaryState, source.getNextPrimaryState());
		nextSecondaryState = MergeHelper.replaceIfNull(nextSecondaryState, source.getNextSecondaryState());
		immediate = MergeHelper.replaceIfNull(immediate, source.getImmediate());
		purpose = MergeHelper.replaceIfNull(purpose, source.getPurpose());
		ownerPrefix = MergeHelper.replaceIfNull(ownerPrefix, source.getOwnerPrefix());
		order = MergeHelper.replaceIfNull(order, source.getOrder());
		disabledReasonId = MergeHelper.replaceIfNull(disabledReasonId, source.getDisabledReasonId());
		confirmationMessageId = MergeHelper.replaceIfNull(confirmationMessageId, source.getConfirmationMessageId());
		actionPath = MergeHelper.replaceIfNull(actionPath, source.getActionPath());
		group = MergeHelper.replaceIfNull(group, source.getGroup());

		conditions = MergeHelper.mergeLists(MergeHelper.convertToMergable(conditions),
				MergeHelper.convertToMergable(source.getConditions()), EmfMergeableFactory.CONDITION_DEFINITION);
		return this;
	}

	/**
	 * Merge fields.
	 *
	 * @param source
	 *            the source
	 */
	@SuppressWarnings("unchecked")
	private void mergeFields(TransitionDefinitionImpl source) {
		// If local is not a template and the source is template then should copy the fields as new
		// otherwise we need just to merge them
		List<PropertyDefinition> srcFields = source.getFields();
		if (!getTemplate() && source.getTemplate()) {
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
	}

	@Override
	public PathElement getParentElement() {
		if (getOwningDefinition() instanceof PathElement) {
			return (PathElement) getOwningDefinition();
		}
		return null;
	}

	@Override
	public String getActionPath() {
		return actionPath;
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
		if (!isSealed()) {
			this.nextPrimaryState = nextPrimaryState;
		}
	}

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
		if (!isSealed()) {
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
		builder.append(", actionPath=");
		builder.append(actionPath);
		builder.append(", group=");
		builder.append(group);
		builder.append(", super=");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public TransitionDefinitionImpl createCopy() {
		TransitionDefinitionImpl copy = new TransitionDefinitionImpl();
		copy.identifier = getIdentifier();
		copy.expression = expression;
		copy.labelId = labelId;
		copy.tooltipId = tooltipId;
		copy.eventId = eventId;
		copy.nextPrimaryState = nextPrimaryState;
		copy.nextSecondaryState = nextSecondaryState;
		copy.defaultTransition = defaultTransition;
		copy.immediate = immediate;
		copy.purpose = purpose;
		copy.ownerPrefix = ownerPrefix;
		copy.order = order;
		copy.disabledReasonId = disabledReasonId;
		copy.confirmationMessageId = confirmationMessageId;
		copy.actionPath = actionPath;
		copy.group = group;

		for (PropertyDefinition propertyDefinition : getFields()) {
			WritablePropertyDefinition clone = ((PropertyDefinitionProxy) propertyDefinition).cloneProxy();
			copy.getFields().add(clone);
		}
		DefinitionUtil.sort(copy.getFields());

		if (getConditions() != null) {
			copy.setConditions(new ArrayList<>(getConditions().size()));
			for (Condition condition : getConditions()) {
				Condition clone = ((ConditionDefinitionImpl) condition).createCopy();
				copy.getConditions().add(clone);
			}
		}
		return copy;
	}

	@Override
	public String getLabel() {
		String labelid = getLabelId();
		if (labelProvider != null) {
			if (StringUtils.isBlank(labelid)) {
				LOGGER.warn("Requesting a label from a region that does not have a label: {}", this);
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
		if (!isSealed()) {
			this.tooltipId = tooltipId;
		}
	}

	@Override
	public void initBidirection() {
		super.initBidirection();
		if (getConditions() != null) {
			for (Condition definition : getConditions()) {
				ConditionDefinitionImpl definitionImpl = (ConditionDefinitionImpl) definition;
				definitionImpl.setTransitionDefinition(this);
			}
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

	@Override
	public List<Condition> getConditions() {
		return conditions;
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
		if (!isSealed()) {
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
		if (!isSealed()) {
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
		if (!isSealed()) {
			this.immediate = immediate;
		}
	}

	@Override
	public String getPurpose() {
		return purpose;
	}

	@Override
	public void setPurpose(String purpose) {
		if (!isSealed()) {
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
	public void seal() {
		if (isSealed()) {
			return;
		}

		if (conditions != null) {
			conditions = Collections.unmodifiableList(getConditions());
		}

		super.seal();
	}

	/**
	 * Getter method for ownerPrefix.
	 *
	 * @return the ownerPrefix
	 */
	@Override
	public String getOwnerPrefix() {
		return ownerPrefix;
	}

	/**
	 * Setter method for ownerPrefix.
	 *
	 * @param ownerPrefix
	 *            the ownerPrefix to set
	 */
	public void setOwnerPrefix(String ownerPrefix) {
		if (!isSealed()) {
			this.ownerPrefix = ownerPrefix;
		}
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
		if (!isSealed()) {
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
		if (!isSealed()) {
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
		return nullSafeEquals(identifier, other.getIdentifier());
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public boolean isVisible() {
		// all transitions, except the one marked as hidden, will be visible
		return getDisplayType() != DisplayType.HIDDEN;
	}

	@Override
	public JsonObject getConfigurationAsJson() {
		return fieldsStream()
				.filter(PropertyDefinition.hasValue().and(PropertyDefinition.hasControl(CONFIGURATION)))
					.findFirst()
					.map(PropertyDefinition::getDefaultValue)
					.map(configuration -> JSON.readObject(configuration, json -> json))
					.orElse(null);
	}

	/**
	 * Setter method for actionPath.
	 *
	 * @param actionPath
	 *            the action path
	 */
	public void setActionPath(String actionPath) {
		if (!isSealed()) {
			this.actionPath = actionPath;
		}
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

	public void setDefaultProperties() {
		if (isSealed()) {
			return;
		}
		if (actionPath == null) {
			actionPath = DEFAULT_ACTION_PATH;
		}
	}

	@Override
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

}
