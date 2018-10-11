package com.sirma.itt.seip.definition.model;

import java.io.Serializable;

import org.json.JSONObject;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.definition.Mergeable;
import com.sirma.itt.seip.definition.MergeableBase;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Implementation class for the control definition.
 *
 * @author BBonev
 */
public class ConditionDefinitionImpl extends MergeableBase<ConditionDefinitionImpl>
implements Serializable, Condition,
		Copyable<ConditionDefinitionImpl>, Mergeable<ConditionDefinitionImpl>, JsonRepresentable, PathElement {

	private static final long serialVersionUID = -530036154579530635L;

	@Tag(1)
	protected String identifier;

	@Tag(2)
	protected String renderAs;

	@Tag(3)
	protected String expression;

	protected transient FieldDefinitionImpl fieldDefinition;

	protected transient RegionDefinitionImpl regionDefinition;

	protected transient TransitionDefinitionImpl transitionDefinition;

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;

	}

	/**
	 * Gets the render as.
	 *
	 * @return the render as
	 */
	@Override
	public String getRenderAs() {
		return renderAs;
	}

	/**
	 * Gets the expression.
	 *
	 * @return the expression
	 */
	@Override
	public String getExpression() {
		return expression;
	}

	/**
	 * Setter method for renderAs.
	 *
	 * @param renderAs
	 *            the renderAs to set
	 */
	public void setRenderAs(String renderAs) {
		this.renderAs = renderAs;
	}

	/**
	 * Setter method for expression.
	 *
	 * @param expression
	 *            the expression to set
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Condition [");
		builder.append("identifier=");
		builder.append(identifier);
		builder.append(", renderAs=");
		builder.append(renderAs);
		builder.append(", expression=");
		builder.append(expression);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public ConditionDefinitionImpl createCopy() {
		ConditionDefinitionImpl clone = new ConditionDefinitionImpl();
		clone.identifier = identifier;
		clone.renderAs = renderAs;
		clone.expression = expression;

		clone.fieldDefinition = null;
		clone.regionDefinition = null;
		clone.transitionDefinition = null;

		return clone;
	}

	/**
	 * Getter method for fieldDefinition.
	 *
	 * @return the fieldDefinition
	 */
	public FieldDefinitionImpl getFieldDefinition() {
		return fieldDefinition;
	}

	/**
	 * Setter method for fieldDefinition.
	 *
	 * @param fieldDefinition
	 *            the fieldDefinition to set
	 */
	public void setFieldDefinition(FieldDefinitionImpl fieldDefinition) {
		this.fieldDefinition = fieldDefinition;
	}

	/**
	 * Getter method for regionDefinition.
	 *
	 * @return the regionDefinition
	 */
	public RegionDefinitionImpl getRegionDefinition() {
		return regionDefinition;
	}

	/**
	 * Setter method for regionDefinition.
	 *
	 * @param regionDefinition
	 *            the regionDefinition to set
	 */
	public void setRegionDefinition(RegionDefinitionImpl regionDefinition) {
		this.regionDefinition = regionDefinition;
	}

	/**
	 * Getter method for transitionDefinition.
	 *
	 * @return the transitionDefinition
	 */
	public TransitionDefinitionImpl getTransitionDefinition() {
		return transitionDefinition;
	}

	/**
	 * Setter method for transitionDefinition.
	 *
	 * @param transitionDefinition
	 *            the transitionDefinition to set
	 */
	public void setTransitionDefinition(TransitionDefinitionImpl transitionDefinition) {
		this.transitionDefinition = transitionDefinition;
	}

	@Override
	public ConditionDefinitionImpl mergeFrom(ConditionDefinitionImpl source) {
		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		renderAs = MergeHelper.replaceIfNull(renderAs, source.getRenderAs());
		expression = MergeHelper.replaceIfNull(expression, source.getExpression());
		return this;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "identifier", getIdentifier());
		JsonUtil.addToJson(object, "expression", getExpression());
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// Method not in use
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
	public PathElement getParentElement() {
		if (fieldDefinition != null) {
			return fieldDefinition;
		} else if (regionDefinition != null) {
			return regionDefinition;
		}
		return transitionDefinition;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

}
