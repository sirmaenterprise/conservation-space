package com.sirma.itt.emf.definition.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.MergeableBase;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Base definition with fields. Provides common access to fields definitions list.
 *
 * @author BBonev
 * @param <E>
 *            the element type
 */
public class BaseDefinition<E extends BaseDefinition<?>> extends MergeableBase<E> implements
		Serializable, DefinitionModel, BidirectionalMapping, Condition {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 672798411875226686L;

	/** The identifier. */
	@Tag(101)
	protected String identifier;

	/** The expression. */
	@Tag(102)
	protected String expression;

	/** The hash. */
	@Tag(103)
	protected Integer hash;

	/** The fields. */
	@Tag(104)
	protected List<PropertyDefinition> fields;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PropertyDefinition> getFields() {
		if (fields == null) {
			fields = new LinkedList<PropertyDefinition>();
		}
		return fields;
	}

	/**
	 * Setter method for fields.
	 *
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(List<PropertyDefinition> fields) {
		this.fields = fields;
	}

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

	@Override
	@SuppressWarnings("unchecked")
	public E mergeFrom(E source) {
		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		expression = MergeHelper.replaceIfNull(expression, source.getExpression());

		// we do not assign the list to fields back because we are sure we will send a non null list
		// and the result will be into the given list
		MergeHelper.copyOrMergeLists(MergeHelper.convertToMergable(getFields()),
				MergeHelper.convertToMergable(source.getFields()));

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

	/**
	 * {@inheritDoc}
	 */
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
		this.expression = expression;
	}

	/**
	 * Getter method for hash.
	 *
	 * @return the hash
	 */
	@Override
	public Integer getHash() {
		return hash;
	}

	/**
	 * Setter method for hash.
	 *
	 * @param hash
	 *            the hash to set
	 */
	@Override
	public void setHash(Integer hash) {
		this.hash = hash;
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

}