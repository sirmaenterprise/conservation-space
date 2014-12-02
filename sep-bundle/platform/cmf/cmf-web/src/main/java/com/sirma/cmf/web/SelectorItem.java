package com.sirma.cmf.web;

import java.util.Comparator;

import com.sirma.itt.emf.util.EqualsHelper;

/**
 * The Class SelectorItem.
 *
 * @author svelikov
 */
public class SelectorItem {

	/** The Constant DESCRIPTION_COMPARATOR. */
	public static final Comparator<SelectorItem> DESCRIPTION_COMPARATOR = new SelectorComparator(
			SelectorField.DESCRIPTION);
	/** The Constant ID_COMPARATOR. */
	public static final Comparator<SelectorItem> ID_COMPARATOR = new SelectorComparator(
			SelectorField.ID);
	/** The Constant TYPE_COMPARATOR. */
	public static final Comparator<SelectorItem> TYPE_COMPARATOR = new SelectorComparator(
			SelectorField.TYPE);

	/** The type. */
	private String type;

	/** The description. */
	private String description;

	/** The id. */
	private String id;

	/**
	 * Constructor.
	 *
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 * @param description
	 *            the description
	 */
	public SelectorItem(String id, String type, String description) {
		this.id = id;
		this.type = type;
		this.description = description;
	}

	/**
	 * Getter method for type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Setter method for type.
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Getter method for description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Setter method for description.
	 *
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Setter method for id.
	 *
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SelectorItem [id=");
		builder.append(id);
		builder.append(", type=");
		builder.append(type);
		builder.append(", description=");
		builder.append(description);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * The Class SelectorComparator.
	 */
	private static class SelectorComparator implements Comparator<SelectorItem> {

		/** The type. */
		private SelectorField type;

		/** The ascending. */
		private boolean ascending = true;

		/**
		 * Instantiates a new selector comparator.
		 *
		 * @param type
		 *            the type
		 */
		public SelectorComparator(SelectorField type) {
			this.type = type;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(SelectorItem arg0, SelectorItem arg1) {
			String value1 = null;
			String value2 = null;
			switch (type) {
				case DESCRIPTION:
					value1 = arg0.description;
					value2 = arg1.description;
					break;
				case ID:
					value1 = arg0.id;
					value2 = arg1.id;
					break;
				case TYPE:
					value1 = arg0.type;
					value2 = arg1.type;
					break;
				default:
					break;
			}

			int compare = EqualsHelper.nullCompare(value1, value2);
			if (compare != 2) {
				return ascending ? compare : -compare;
			}
			compare = value1.compareToIgnoreCase(value2);
			return ascending ? compare : -compare;
		}
	}

	/**
	 * The Enum SelectorField.
	 */
	private static enum SelectorField {

		/** The type. */
		TYPE,
		/** The id. */
		ID,
		/** The description. */
		DESCRIPTION;
	}

}
