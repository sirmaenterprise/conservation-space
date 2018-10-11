package com.sirma.sep.instance.actions.group;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Tree of {@link ActionMenuMember} objects. Represents the logical structure of the actions menu. Implements visitor
 * pattern, act as visitable object.
 *
 * @author T. Dossev
 */
public class ActionMenu implements VisitableMenu {

	private ActionMenuMember member;
	private List<ActionMenu> menuMembers;
	private Set<Visitor> visitors;
	private Comparator<ActionMenu> comparator = (ActionMenu o1, ActionMenu o2) -> {
		Integer sortable1 = o1.member.getOrder();
		Integer sortable2 = o2.member.getOrder();
		return EqualsHelper.nullSafeCompare(sortable1, sortable2);
	};

	/**
	 * Tree root constructor.
	 */
	public ActionMenu() {
		this.menuMembers = new LinkedList<>();
		this.visitors = new HashSet<>();
	}

	private ActionMenu(ActionMenuMember item) {
		this();
		this.member = item;
	}

	/**
	 * Adds new menu member if not present.
	 *
	 * @param newMember
	 *            new member to add
	 * @param parentMenu
	 *            current menu
	 * @return added member
	 */
	public ActionMenu addMenuMember(ActionMenuMember newMember) {
		for (ActionMenu menu : menuMembers) {
			if (menu.member.getIdentifier().equals(newMember.getIdentifier())) {
				return menu;
			}
		}

		return addMenuMember(new ActionMenu(newMember));
	}

	private ActionMenu addMenuMember(ActionMenu newMember) {
		menuMembers.add(newMember);
		Collections.sort(menuMembers, comparator);
		return newMember;
	}

	@Override
	public void acceptVisitor(Visitor visitor) {
		visitors.add(visitor);
	}

	@Override
	public List<ActionMenu> getMenuMembers(Visitor visitor) {
		if (visitors.contains(visitor)) {
			return menuMembers;

		}
		return Collections.emptyList();
	}

	@Override
	public ActionMenuMember getMenuMember(Visitor visitor) {
		if (visitors.contains(visitor)) {
			return member;
		}
		return null;
	}
}