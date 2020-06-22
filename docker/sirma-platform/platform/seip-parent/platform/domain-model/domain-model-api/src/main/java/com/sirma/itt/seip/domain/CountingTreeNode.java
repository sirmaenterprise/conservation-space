package com.sirma.itt.seip.domain;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.Entity;

/**
 * Tree node that counts and keeps track for the number of his child nodes and the level where it's placed.
 *
 * @param <E>
 *            the data type
 * @author BBonev
 */
public class CountingTreeNode<E extends Serializable> implements Entity<Long>, Serializable, PathElement {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 454024966023973459L;

	/** The id. */
	private Long id;

	/** The current level. */
	private short level = 0;

	/** A custom identifier used for path construction. */
	private String identifier;

	/** The parent element. */
	private CountingTreeNode<E> parentElement;

	// the default value is 1 that represents the current node
	/** The child count. */
	private int childCount = 1;

	/** The data stored into the node. */
	private E data;

	/** The children. */
	private List<CountingTreeNode<E>> children = new ArrayList<>();

	/**
	 * The children snapshot returned from the getter method for children property. This is used to force the user to
	 * use the custom add and remove methods instead of the collection methods
	 */
	private transient List<CountingTreeNode<E>> childrenSnapshot;

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
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		return parentElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return getIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren() {
		return childCount > 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getChild(String name) {
		return children.stream().filter(node -> nullSafeEquals(node.getIdentifier(), name)).findFirst().orElse(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Setter method for parentElement.
	 *
	 * @param parentElement
	 *            the parentElement to set
	 */
	public void setParentElement(CountingTreeNode<E> parentElement) {
		this.parentElement = parentElement;
	}

	/**
	 * Getter method for childCount.
	 *
	 * @return the childCount
	 */
	public int getChildCount() {
		return childCount;
	}

	/**
	 * Returns a snapshot of the children. The list cannot be modified!!!. To add or remove elements call the methods
	 * {@link #addChild(CountingTreeNode)} and {@link #removeChild(CountingTreeNode)}.
	 *
	 * @return the children
	 */
	public List<CountingTreeNode<E>> getChildren() {
		if (childrenSnapshot == null) {
			childrenSnapshot = Collections.unmodifiableList(children);
		}
		return childrenSnapshot;
	}

	/**
	 * Adds the child.
	 *
	 * @param countingTreeNode
	 *            the counting tree node
	 * @return the integer
	 */
	public int addChild(CountingTreeNode<E> countingTreeNode) {
		countingTreeNode.levelUpdated((short) (level + 1));
		countingTreeNode.setParentElement(this);
		children.add(countingTreeNode);
		updateChildrenCount(true, countingTreeNode.getChildCount());
		// count and update the children counter
		return childCount;
	}

	/**
	 * Removes the child.
	 *
	 * @param countingTreeNode
	 *            the counting tree node
	 * @return the integer
	 */
	public int removeChild(CountingTreeNode<E> countingTreeNode) {
		int index = children.indexOf(countingTreeNode);
		if (index >= 0) {
			CountingTreeNode<E> node = children.get(index);
			children.remove(index);
			updateChildrenCount(false, node.getChildCount());
		}
		return childCount;
	}

	/**
	 * Notifies the parent chain for added/removed child.
	 *
	 * @param isAdd
	 *            if child was added or removed is add
	 * @param diff
	 *            the diff
	 */
	protected void updateChildrenCount(boolean isAdd, int diff) {
		if (isAdd) {
			childCount += diff;
		} else if (childCount > 1) {
			childCount -= diff;
		}
		if (parentElement != null) {
			parentElement.updateChildrenCount(isAdd, diff);
		}
	}

	/**
	 * Notifies the children that the level has been updated.
	 *
	 * @param parentLevel
	 *            the parent level
	 */
	protected void levelUpdated(short parentLevel) {
		level = parentLevel;
		short newLevel = (short) (parentLevel + 1);
		for (CountingTreeNode<E> node : children) {
			node.levelUpdated(newLevel);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CountingTreeNode)) {
			return false;
		}
		CountingTreeNode<?> other = (CountingTreeNode<?>) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TreeNode(");
		builder.append(id);
		builder.append(", ");
		builder.append(identifier);
		builder.append(", W");
		builder.append(childCount);
		builder.append(", L");
		builder.append(level);
		if (children.isEmpty()) {
			builder.append(", ");
			builder.append(children);
			builder.append(")");
		} else {
			builder.append(",");
			StringBuilder indent = new StringBuilder((level + 1) * 4);
			for (int i = 0; i < level + 1; i++) {
				indent.append("    ");
			}
			for (int i = 0; i < children.size(); i++) {
				CountingTreeNode<E> node = children.get(i);
				builder.append('\n').append(indent).append(node);
			}
			builder.append(")");
		}
		return builder.toString();
	}

	/**
	 * Getter method for level.
	 *
	 * @return the level
	 */
	public short getLevel() {
		return level;
	}

	/**
	 * Getter method for data.
	 *
	 * @return the data
	 */
	public E getData() {
		return data;
	}

	/**
	 * Setter method for data.
	 *
	 * @param data
	 *            the data to set
	 */
	public void setData(E data) {
		this.data = data;
	}

}
