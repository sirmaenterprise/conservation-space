package com.sirma.itt.emf.definition.model;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.domain.model.Mergeable;
import com.sirma.itt.emf.domain.model.MergeableBase;

/**
 * Implementation class for the control definition.
 *
 * @author BBonev
 */
public class ConditionDefinitionImpl extends MergeableBase<ConditionDefinitionImpl> implements
		Serializable, Condition, Cloneable, Mergeable<ConditionDefinitionImpl> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -530036154579530635L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ConditionDefinitionImpl.class);

	/** The identifier. */
	@Tag(1)
	protected String identifier;

	/** The render as. */
	@Tag(2)
	protected String renderAs;

	/** The expression. */
	@Tag(3)
	protected String expression;

	/** The conditional. */
	protected transient FieldDefinitionImpl fieldDefinition;

	/** The region definition. */
	protected transient RegionDefinitionImpl regionDefinition;

	/** The transition definition. */
	protected transient TransitionDefinitionImpl transitionDefinition;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConditionDefinitionImpl [");
		builder.append("identifier=");
		builder.append(identifier);
		builder.append(", renderAs=");
		builder.append(renderAs);
		builder.append(", expression=");
		builder.append(expression);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConditionDefinitionImpl clone() {
		try {
			return (ConditionDefinitionImpl) super.clone();
		} catch (CloneNotSupportedException e) {
			LOGGER.trace("Should not fail to clone instance but no luck", e);
		}
		return null;
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
	 * @param fieldDefinition the fieldDefinition to set
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
	 * @param regionDefinition the regionDefinition to set
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
	 * @param transitionDefinition the transitionDefinition to set
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

}
