package com.sirma.sep.instance.actions.group;

import java.util.List;

/**
 * Represents visitable menu.
 * 
 * @author T. Dossev
 */
public interface VisitableMenu {

	/**
	 * Register visitor.
	 * 
	 * @param visitor
	 *            to be registered
	 */
	void acceptVisitor(Visitor visitor);

	/**
	 * Provides menu members to registered visitors.
	 * 
	 * @param visitor
	 *            visitor to check
	 * @return members if any or Collections.emptyList() if none or not registered visitor
	 */
	List<ActionMenu> getMenuMembers(Visitor visitor);

	/**
	 * Provides menu member to registered visitors.
	 * 
	 * @param visitor
	 *            visitor to check
	 * @return menu member if any or null if none or not registered visitor
	 */
	ActionMenuMember getMenuMember(Visitor visitor);

}
