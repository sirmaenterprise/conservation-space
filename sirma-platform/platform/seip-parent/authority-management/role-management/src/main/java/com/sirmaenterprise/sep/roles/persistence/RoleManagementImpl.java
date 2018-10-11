package com.sirmaenterprise.sep.roles.persistence;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.collections.CollectionUtils.toIdentityMap;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.event.EventService;
import com.sirmaenterprise.sep.roles.ActionDefinition;
import com.sirmaenterprise.sep.roles.RoleActionChanges;
import com.sirmaenterprise.sep.roles.RoleActionChanges.RoleActionChange;
import com.sirmaenterprise.sep.roles.RoleActionModel;
import com.sirmaenterprise.sep.roles.RoleDefinition;
import com.sirmaenterprise.sep.roles.RoleManagement;
import com.sirmaenterprise.sep.roles.events.ActionDefinitionsChangedEvent;
import com.sirmaenterprise.sep.roles.events.RoleActionMappingsChangedEvent;
import com.sirmaenterprise.sep.roles.events.RoleDefinitionsChangedEvent;

/**
 * Default implementation of {@link RoleManagement}. The implementation uses a database to store the all role and action
 * definitions and their mapping.
 *
 * @author BBonev
 */
@ApplicationScoped
public class RoleManagementImpl implements RoleManagement {

	@Inject
	private RoleActionsDao dao;
	@Inject
	private EventService eventService;

	@Override
	public void updateRoleActionMappings(RoleActionChanges changes) {
		if (changes == null || isEmpty(changes.getChanges())) {
			return;
		}
		Map<RoleActionId, RoleActionEntity> mapping = toIdentityMap(dao.getRoleActions(), RoleActionEntity::getId);

		for (RoleActionChange change : changes.getChanges()) {
			RoleActionId id = new RoleActionId(change.getRole(), change.getAction());
			RoleActionEntity entity = mapping.computeIfAbsent(id, RoleActionEntity::new);
			entity.setEnabled(change.isActive());
			entity.setFilters(change.getFilters());
			dao.save(entity);
		}
		eventService.fire(new RoleActionMappingsChangedEvent());
	}

	@Override
	public void saveActions(Collection<ActionDefinition> definitions) {
		if (isEmpty(definitions)) {
			return;
		}
		Set<String> modifiedIds = definitions.stream().map(ActionDefinition::getId).collect(Collectors.toSet());
		Collection<ActionEntity> roleEntities = dao.getActions(modifiedIds);
		Map<String, ActionEntity> roleMapping = toIdentityMap(roleEntities, ActionEntity::getId);

		for (ActionDefinition definition : definitions) {
			roleMapping.compute(definition.getId(), mergeAndSave(definition, actionDefToEntity(), ActionEntity::new));
		}
		eventService.fire(new ActionDefinitionsChangedEvent());
	}

	@Override
	public void saveRoles(Collection<RoleDefinition> definitions) {
		if (isEmpty(definitions)) {
			return;
		}
		Set<String> modifiedIds = definitions.stream().map(RoleDefinition::getId).collect(Collectors.toSet());
		Collection<RoleEntity> roleEntities = dao.getRoles(modifiedIds);
		Map<String, RoleEntity> roleMapping = toIdentityMap(roleEntities, RoleEntity::getId);

		for (RoleDefinition definition : definitions) {
			roleMapping.compute(definition.getId(), mergeAndSave(definition, roleDefToEntity(), RoleEntity::new));
		}
		eventService.fire(new RoleDefinitionsChangedEvent());
	}

	/**
	 * @param <D>
	 *            source type
	 * @param <E>
	 *            result entity type
	 * @param definition
	 *            the source object that provide the input data for mapping
	 * @param dataMapper
	 *            the data mapper that should be called to convert the the source object type to the output type
	 * @param entityBulder
	 *            supplier that can be used to provide new instances of the result entity when needed.
	 * @return a new result entity or update existing one.
	 */
	private <E extends Entity<? extends Serializable>, D> BiFunction<String, E, E> mergeAndSave(D definition,
			BiFunction<D, Supplier<E>, E> dataMapper, Supplier<E> entityBulder) {
		return (id, currentEntity) -> {
			E entity;
			if (currentEntity == null) {
				entity = dataMapper.apply(definition, entityBulder);
				entity = dao.saveNew(entity);
			} else {
				entity = dataMapper.apply(definition, () -> currentEntity);
				entity = dao.save(entity);
			}
			return entity;
		};
	}

	private static BiFunction<ActionDefinition, Supplier<ActionEntity>, ActionEntity> actionDefToEntity() {
		return (definition, entityProvider) -> {
			ActionEntity entity = entityProvider.get();
			entity.setId(definition.getId());
			entity.setActionType(definition.getActionType());
			entity.setEnabled(definition.isEnabled());
			entity.setUserDefined(definition.isUserDefined());
			return entity;
		};
	}

	private static BiFunction<RoleDefinition, Supplier<RoleEntity>, RoleEntity> roleDefToEntity() {
		return (definition, entityProvider) -> {
			RoleEntity entity = entityProvider.get();
			entity.setId(definition.getId());
			entity.setCanRead(definition.isCanRead());
			entity.setCanWrite(definition.isCanWrite());
			entity.setEnabled(definition.isEnabled());
			entity.setInternal(definition.isInternal());
			entity.setUserDefined(definition.isUserDefined());
			entity.setOrder(definition.getOrder());
			return entity;
		};
	}

	@Override
	public Stream<RoleDefinition> getRoles() {
		return dao.getRoles().stream().map(roleEntityToDefinition());
	}

	@Override
	public Optional<RoleDefinition> getRole(String roleId) {
		if (StringUtils.isBlank(roleId)) {
			return Optional.empty();
		}
		return dao.getRoles(Collections.singleton(roleId)).stream().map(roleEntityToDefinition()).findFirst();
	}

	@Override
	public Stream<ActionDefinition> getActions() {
		return dao.getActions().stream().map(actionEntityToDefinition());
	}

	@Override
	public Optional<ActionDefinition> getAction(String actionid) {
		if (StringUtils.isBlank(actionid)) {
			return Optional.empty();
		}
		return dao.getActions(Collections.singleton(actionid)).stream().map(actionEntityToDefinition()).findFirst();
	}

	@Override
	public RoleActionModel getRoleActionModel() {
		RoleActionModel model = new RoleActionModel();
		getRoles().forEach(model::add);
		getActions().forEach(model::add);

		for (RoleActionEntity entity : dao.getRoleActions()) {
			model.add(entity.getId().getRole(), entity.getId().getAction(), entity.isEnabled(), entity.getFilters());
		}
		return model;
	}

	private static Function<RoleEntity, RoleDefinition> roleEntityToDefinition() {
		return entity -> new RoleDefinition()
				.setId(entity.getId())
					.setOrder(entity.getOrder())
					.setCanRead(entity.isCanRead())
					.setCanWrite(entity.isCanWrite())
					.setInternal(entity.isInternal())
					.setEnabled(entity.isEnabled());
	}

	private static Function<ActionEntity, ActionDefinition> actionEntityToDefinition() {
		return entity -> new ActionDefinition()
				.setId(entity.getId())
					.setEnabled(entity.isEnabled())
					.setActionType(entity.getActionType())
					.setUserDefined(entity.isUserDefined())
					.setImmediate(entity.isImmediate())
					.setVisible(entity.isVisible())
					.setImagePath(entity.getImagePath());
	}

	@Override
	public void deleteRoleActionMappings() {
		dao.deleteRoleActionMappings();
	}

}
