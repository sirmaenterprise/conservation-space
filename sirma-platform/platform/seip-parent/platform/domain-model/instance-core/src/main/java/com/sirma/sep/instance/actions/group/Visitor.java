package com.sirma.sep.instance.actions.group;

import java.util.List;

/**
 * Represents visitor of visitor pattern.
 *
 * @author T. Dossev
 */
public interface Visitor {

	/**
	 * Visit the menu and register itself as visitor.
	 *
	 * @param menu
	 *            to visit
	 */
	void visitMenu(VisitableMenu menu);

	/**
	 * Gets menu members.
	 * 
	 * @param menu
	 *            the menu
	 * @return members if any or Collections.emtyList() if none or not registered visitor
	 */
	List<ActionMenu> visitMenuMembers(VisitableMenu menu);

	/**
	 * Gets menu member.
	 * 
	 * @param menu
	 *            the menu
	 * @return menu member if any or null if none or not registered visitor
	 */
	ActionMenuMember visitMenuMember(VisitableMenu menu);

}
