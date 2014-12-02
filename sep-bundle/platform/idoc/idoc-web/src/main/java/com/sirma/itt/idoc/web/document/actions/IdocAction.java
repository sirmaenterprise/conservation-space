package com.sirma.itt.idoc.web.document.actions;

import com.sirma.itt.emf.security.model.EmfAction;

/**
 * Ported action for idoc specific action. Class contains some extension as display order
 *
 * @author yasko
 */
public class IdocAction extends EmfAction {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7019321097079615429L;

	private int displayOrder;

	/**
	 * Constructs a new instance.
	 *
	 * @param id
	 *            the definition id
	 * @param label
	 *            the label
	 */
	public IdocAction(IdocActionDefinition id, String label) {
		super(id.getId());
		setOnclick(id.getOnClick());
		setIconImagePath(id.getIcon());
		setLabel(label);
		setDisplayOrder(id.ordinal());
	}

	/**
	 * Getter method for displayOrder.
	 *
	 * @return the displayOrder
	 */
	public int getDisplayOrder() {
		return displayOrder;
	}

	/**
	 * Setter method for displayOrder.
	 *
	 * @param displayOrder
	 *            the displayOrder to set
	 */
	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	@Override
	public String toString() {
		return getClass().getName() + " " + getActionId();
	}
}
