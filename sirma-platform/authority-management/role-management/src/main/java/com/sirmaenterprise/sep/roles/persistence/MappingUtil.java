package com.sirmaenterprise.sep.roles.persistence;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.Filterable;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.model.RoleId;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirmaenterprise.sep.roles.ActionDefinition;
import com.sirmaenterprise.sep.roles.RoleActionModel.RoleActionMapping;
import com.sirmaenterprise.sep.roles.RoleDefinition;

/**
 * Defines local DTO mapping methods for role and action definitions
 *
 * @since 2017-03-27
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public class MappingUtil {

	private MappingUtil() {
		// utility class
	}

	/**
	 * Returns mapper from {@link ActionDefinition} to {@link Action}
	 *
	 * @param labelProvider
	 *            the label provider to use when resolving labels
	 * @return a mapper that builds {@link Action}s from {@link ActionDefinition}
	 */
	public static Function<ActionDefinition, Action> defToAction(LabelProvider labelProvider) {
		return def -> {
			EmfAction action = new EmfAction(def.getId(), labelProvider);
			action.setImmediate(def.isImmediate());
			action.setLabel(def.getId() + ".label");
			action.setTooltip(def.getId() + ".tooltip");
			action.setConfirmationMessage(def.getId() + ".confirm");
			action.setDisabledReason(def.getId() + ".disabled.reason");
			action.setVisible(def.isVisible());
			action.setPurpose(def.getActionType());
			action.setDisabled(!def.isEnabled());
			action.setIconImagePath(def.getImagePath());
			return action;
		};
	}

	/**
	 * Returns a mapper that produces {@link Role} from a given {@link RoleDefinition}. Depending on the provided
	 * {@code roleActionProvider} the role could have it's actions initialized or not.
	 *
	 * @param labelProvider
	 *            the label provider to use when resolving labels
	 * @param roleActionProvider
	 *            a provider that should return a proper stream of {@link RoleActionMapping} based on a
	 *            role identifier. If no actions are needed just use
	 *            <p>
	 *            {@code roleId -> Stream.empty()}
	 *            </p>
	 * @return a mapping function that transforms {@link RoleDefinition}s to {@link Role} instances
	 */
	public static Function<RoleDefinition, Role> defToRole(LabelProvider labelProvider,
			Function<String, Stream<RoleActionMapping>> roleActionProvider) {
		return def -> {
			Role role = new Role(defToRoleId().apply(def));
			role.addActions(getRoleActions(def, labelProvider, roleActionProvider));
			role.seal();
			return role;
		};
	}

	/**
	 * Returns a mapper that builds {@link RoleIdentifier}s based on a given {@link RoleDefinition}
	 *
	 * @return a mapping function that builds a {@link RoleIdentifier} from a {@link RoleDefinition}
	 */
	public static Function<RoleDefinition, RoleIdentifier> defToRoleId() {
		return def -> new RoleId(def.getId(), def.getOrder())
				.setCanRead(def.isCanRead())
					.setCanWrite(def.isCanWrite())
					.setInternal(def.isInternal())
					.setUserDefined(def.isUserDefined());
	}

	private static List<Action> getRoleActions(RoleDefinition def, LabelProvider labelProvider,
			Function<String, Stream<RoleActionMapping>> roleActionProvider) {
		return roleActionProvider.apply(def.getId()).map(entry -> {
			Action action = defToAction(labelProvider).apply(entry.getAction());
			Collection<String> filters = getOrDefault(entry.getFilters(), new LinkedList<String>());
			Filterable.setFilters(action, Collections.unmodifiableList(new ArrayList<>(filters)));
			action.seal();
			return action;
		}).collect(Collectors.toList());
	}
}
