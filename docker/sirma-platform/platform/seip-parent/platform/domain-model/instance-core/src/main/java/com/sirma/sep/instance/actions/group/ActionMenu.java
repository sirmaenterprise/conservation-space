package com.sirma.sep.instance.actions.group;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;

import java.util.Collections;
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
 * @author A. Kunchev
 */
public class ActionMenu implements VisitableMenu, Comparable<ActionMenu> {

	private ActionMenuMember member;
	private List<ActionMenu> menuMembers;
	private Set<Visitor> visitors;

	public ActionMenu() {
		menuMembers = new LinkedList<>();
		visitors = new HashSet<>();
	}

	private ActionMenu(ActionMenuMember item) {
		this();
		member = item;
	}

	/**
	 * Adds new menu member if not present.
	 *
	 * @param newMember new member to add
	 * @param parentMenu current menu
	 * @return added member
	 */
	public ActionMenu addMenuMember(ActionMenuMember newMember) {
		return menuMembers
				.stream()
					.filter(menu -> menu.member.getIdentifier().equals(newMember.getIdentifier()))
					.findFirst()
					.orElse(addMenuMember(new ActionMenu(newMember)));
	}

	private ActionMenu addMenuMember(ActionMenu newMember) {
		menuMembers.add(newMember);
		Collections.sort(menuMembers);
		return newMember;
	}

	@Override
	public void acceptVisitor(Visitor visitor) {
		visitors.add(visitor);
	}

	@Override
	public List<ActionMenu> getMenuMembers(Visitor visitor) {
		return visitors.contains(visitor) ? menuMembers : emptyList();
	}

	@Override
	public ActionMenuMember getMenuMember(Visitor visitor) {
		return visitors.contains(visitor) ? member : null;
	}

	@Override
	public int compareTo(ActionMenu other) {
		return EqualsHelper.nullSafeCompare(member.getOrder(), other.member.getOrder());
	}
}