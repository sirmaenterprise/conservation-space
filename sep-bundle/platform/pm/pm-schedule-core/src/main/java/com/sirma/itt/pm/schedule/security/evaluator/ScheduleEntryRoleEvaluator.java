package com.sirma.itt.pm.schedule.security.evaluator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.services.adapter.CMFUserService;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.TenantAware;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.EvaluatorScope;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorType;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.schedule.constants.ScheduleConfigProperties;
import com.sirma.itt.pm.schedule.domain.ObjectTypesPms;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleEntryProperties;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.pm.schedule.security.ScheduleActions;
import com.sirma.itt.pm.schedule.service.ScheduleResourceService;
import com.sirma.itt.pm.schedule.service.ScheduleService;

/**
 * Actions evaluator for {@link ScheduleEntry}s. In order to work property the evaluated entry
 * should have filled the current children entries. <br>
 * NOTE: for optimization if used for batch evaluation consider following:
 * <ul>
 * <li>pre-load the actions tree at least 2 levels above and 1 bellow (his direct children) for each
 * child.
 * <li>if all are from the same tree then better build the tree before evaluation - this will save
 * memory and loading time if many entries.
 * <li>pre-load the actual root instance and the schedule instance and set them the the all
 * evaluated entries.
 * </ul>
 *
 * @author BBonev
 */
@ApplicationScoped
@RoleEvaluatorType(value = ObjectTypesPms.SCHEDULE_ENTRY, scope = EvaluatorScope.INTERNAL)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 80)
public class ScheduleEntryRoleEvaluator extends BaseRoleEvaluator<ScheduleEntry> {

	private static final List<Class<?>> SUPPORTED = Arrays.asList(new Class<?>[] { ScheduleEntry.class });
	/** The user service. */
	@Inject
	protected CMFUserService userService;
	/** The authority service. */
	@Inject
	private AuthorityService authorityService;

	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesPms.SCHEDULE)
	private InstanceDao<ScheduleInstance> instanceDao;

	/** The entry dao. */
	@Inject
	@InstanceType(type = ObjectTypesPms.SCHEDULE_ENTRY)
	private InstanceDao<ScheduleEntry> entryDao;

	/** The schedule service. */
	@Inject
	private ScheduleService scheduleService;

	/** The state service. */
	@Inject
	private StateService stateService;

	/** The resource service. */
	@Inject
	private ScheduleResourceService resourceService;

	/** The evaluate actual instance actions. */
	@Inject
	@Config(name = ScheduleConfigProperties.SCHEDULE_ACTIONS_EVAL_ACTUAL_INSTANCE, defaultValue = "false")
	private Boolean evaluateActualInstanceActions;

	/** The evaluate actual instance role. */
	@Inject
	@Config(name = ScheduleConfigProperties.SCHEDULE_ACTIONS_EVAL_ACTUAL_INSTANCE_ROLE, defaultValue = "true")
	private Boolean evaluateActualInstanceRole;

	/** The properties service. */
	@Inject
	private PropertiesService propertiesService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pair<Role, RoleEvaluator<ScheduleEntry>> evaluateInternal(ScheduleEntry target,
			Resource user, final RoleEvaluatorRuntimeSettings settings) {
		if ((target == null)
				|| ((target.getSchedule() == null) && (target.getScheduleId() == null))) {
			return null;
		}
		// calculate role per entry if needed
		if ((target.getActualInstanceId() != null)
				&& Boolean.TRUE.equals(evaluateActualInstanceRole)) {
			com.sirma.itt.emf.instance.model.Instance actualInstance = target.getActualInstance();
			if ((actualInstance != null) && (actualInstance.getProperties() != null)) {
				propertiesService.loadProperties(actualInstance);
			}
			RoleEvaluator<com.sirma.itt.emf.instance.model.Instance> rootEvaluator = roleEvaluatorManagerService
					.get().getRootEvaluator(actualInstance);
			Pair<Role, RoleEvaluator<com.sirma.itt.emf.instance.model.Instance>> evaluated = rootEvaluator
					.evaluate(actualInstance, user, settings);
			return constructRoleModel(evaluated.getFirst().getRoleId());
		}

		if (target.getSchedule() == null) {
			Long id = target.getScheduleId();
			ScheduleInstance scheduleInstance = instanceDao.loadInstance(id, null, false);
			target.setSchedule(scheduleInstance);
		}
		ScheduleInstance schedule = target.getSchedule();
		com.sirma.itt.emf.instance.model.Instance owningInstance = schedule.getOwningInstance();
		Role userRole = authorityService.getUserRole(owningInstance, user);

		return new Pair<Role, RoleEvaluator<ScheduleEntry>>(userRole, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Action> filterActions(ScheduleEntry target, Resource resource, Role role) {

		if ((target == null) || (resource == null)) {
			return new HashSet<Action>(5);
		}
		// no need of chain
		Set<Action> actions = new HashSet<Action>(role.getAllowedActions(ScheduleEntry.class));
		filterInternal(target, resource, role, actions);
		return actions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Boolean filterInternal(ScheduleEntry target, Resource resource, Role role,
			Set<Action> actions) {

		String state = (String) target.getProperties().get(DefaultProperties.STATUS);

		// it non of the fields are set then we have root entry
		boolean noParent = (target.getParentId() == null) && (target.getParentPhantomId() == null);

		// outdent checks
		if (noParent) {
			actions.remove(ScheduleActions.OUTDENT);
		} else {
			if ((target.getParentPhantomId() == null) && (target.getParentInstance() == null)) {
				ScheduleEntry entry = entryDao.loadInstance(target.getParentId(), null, false);
				target.setParentInstance(entry);
				// we probably are on the second level and we cannot move to the level of the root
				if ((entry == null) || (entry.getParentId() == null)) {
					actions.remove(ScheduleActions.OUTDENT);
				}
			} else if ((target.getParentInstance() != null)
					&& (target.getParentInstance().getActualInstanceClass() != null)
					&& target.getParentInstance().getActualInstanceClass()
							.isAssignableFrom(ProjectInstance.class)) {
				actions.remove(ScheduleActions.OUTDENT);
			}
			// TODO: add additional checks if the parent task could accommodate the current node
		}

		// indent checks
		if (noParent) {
			actions.remove(ScheduleActions.INDENT);
		} else {
			ScheduleEntry parent = target.getParentInstance();
			if (parent != null) {
				// the parent children are not loaded - he has at least one child (the current node)
				if (parent.getChildren().isEmpty()) {
					List<ScheduleEntry> children = scheduleService.getChildren(parent.getId());
					parent.getChildren().addAll(children);
				}
				// only child nowhere to indent
				if (parent.getChildren().size() == 1) {
					actions.remove(ScheduleActions.INDENT);
				} else {
					// this should be done after proper sorting of the children so that they are in
					// the
					// same order as visible on the schedule
					int indexOf = parent.getChildren().indexOf(target);
					// we cannot indent if we are on the first position
					if (indexOf == 0) {
						actions.remove(ScheduleActions.INDENT);
						// TODO: add check if the new indent type is compatible to accommodate the
						// current node
					} else if (indexOf > 0) {
						ScheduleEntry previousSibling = parent.getChildren().get(indexOf-1);
						
						boolean targetIsActualInstance = (target.getActualInstanceId() != null)
								&& (target.getInstanceReference() != null)
								&& StringUtils.isNotNullOrEmpty(target.getInstanceReference().getIdentifier());
						
						boolean previousSiblingIsActualInstance = (previousSibling.getActualInstanceId() != null)
								&& (previousSibling.getInstanceReference() != null)
								&& StringUtils.isNotNullOrEmpty(previousSibling.getInstanceReference().getIdentifier());
						
						// Can't indent started instance into not started one
						if (targetIsActualInstance && !previousSiblingIsActualInstance) {
							actions.remove(ScheduleActions.INDENT);
						}
						
						// Can't indent tasks into workflows
						Class<? extends Instance> previousSiblingActualInstanceClass = previousSibling.getActualInstanceClass();
						if (previousSiblingActualInstanceClass != null
								&& previousSiblingActualInstanceClass.isAssignableFrom(WorkflowInstanceContext.class)) {
							actions.remove(ScheduleActions.INDENT);
						}
					}
				}
			}
		}

		// Can't indent/outdent cases, workflows and workflow tasks
		Class<? extends Instance> actualInstanceClass = target.getActualInstanceClass();
		if (actualInstanceClass != null
				&& (actualInstanceClass.isAssignableFrom(TaskInstance.class)
				|| actualInstanceClass.isAssignableFrom(WorkflowInstanceContext.class)
				|| actualInstanceClass.isAssignableFrom(CaseInstance.class))) {
			actions.remove(ScheduleActions.OUTDENT);
			actions.remove(ScheduleActions.INDENT);				
		}

		boolean hasActualInstance = (target.getActualInstanceId() != null)
				&& (target.getInstanceReference() != null)
				&& StringUtils.isNotNullOrEmpty(target.getInstanceReference().getIdentifier());
		// cannot open non existing instance
		if (!hasActualInstance) {
			actions.remove(ScheduleActions.OPEN);

			// no concrete type chosen to approve or no assignment is set
			if ((target.getActualInstanceClass() == null)
					|| StringUtils.isNullOrEmpty(target.getIdentifier())
					|| resourceService.getAssignments(target).isEmpty()) {
				actions.remove(ScheduleActions.APPROVE);
			}
		} else {
			// if we have actual instance then we cannot approve again and cannot delete it
			actions.remove(ScheduleActions.DELETE);
			actions.remove(ScheduleActions.APPROVE);
		}

		// actions are not valid for root entry
		if (noParent) {
			actions.remove(ScheduleActions.ADD_PREDECESSOR);
			actions.remove(ScheduleActions.ADD_SUCCESSOR);
			actions.remove(ScheduleActions.ADD_TASK_ABOVE);
			actions.remove(ScheduleActions.ADD_TASK_BELOW);
			actions.remove(ScheduleActions.STOP);
			actions.remove(ScheduleActions.EDIT_DETAILS);
		}
		if ((target.getActualInstanceClass() != null)
				&& TaskInstance.class.isAssignableFrom(target.getActualInstanceClass())) {
			actions.remove(ScheduleActions.ADD_PREDECESSOR);
			actions.remove(ScheduleActions.ADD_TASK_ABOVE);
			actions.remove(ScheduleActions.ADD_TASK_BELOW);
		}

		// check for possible children - this is not very accurate due to non persisted data but
		// only if the children are filled in at request time
		if ((target.getActualInstanceClass() != null)
				&& StringUtils.isNotNullOrEmpty(target.getIdentifier())) {
			Map<String, List<DefinitionModel>> allowedChildren = scheduleService
					.getAllowedChildrenForNode(target, target.getChildren());
			if (allowedChildren.isEmpty()) {
				actions.remove(ScheduleActions.ADD_CHILD);
			}
		}
		// handle stop/delete actions
		if (target.getActualInstanceId() == null) {
			actions.remove(ScheduleActions.STOP);
		} else {
			// remove delete operation if not in submitted state
			if (target.getChildren().isEmpty()
					&& !stateService.isState(PrimaryStates.SUBMITTED,
							target.getActualInstanceClass(), state)) {
				actions.remove(ScheduleActions.DELETE);
			}
			// CMF-2434: remove stop operation if not in active state
			boolean isInActiveState = stateService.isStateAs(target.getActualInstanceClass(),
					state, PrimaryStates.APPROVED, PrimaryStates.OPENED, PrimaryStates.ON_HOLD);
			if (!isInActiveState) {
				actions.remove(ScheduleActions.STOP);
			}

			// CMF-4253: remove add subtask operation if canceled or completed
			if (stateService.isStateAs(target.getActualInstanceClass(), state,
					PrimaryStates.CANCELED, PrimaryStates.COMPLETED)) {
				actions.remove(ScheduleActions.ADD_CHILD);
			}
		}

		// to show or not the remove dependency menu
		Serializable dependencies = target.getProperties().get(
				ScheduleEntryProperties.HAS_DEPENDENCIES);
		com.sirma.itt.emf.instance.model.Instance project = target.getSchedule()
				.getOwningInstance();
		boolean isProjectCompletedOrCanceled = stateService.isInStates(project,
				PrimaryStates.CANCELED, PrimaryStates.COMPLETED);
		if (!Boolean.TRUE.equals(dependencies) || isProjectCompletedOrCanceled) {
			actions.remove(ScheduleActions.DELETE_DEPENDENCY_MENU);
		}

		// CMF-2282: Edit could not be performed for projects in status "Completed" or "Canceled".
		// CMF-3103: remove all actions except open task
		if (isProjectCompletedOrCanceled) {
			Iterator<Action> iter = actions.iterator();
			while (iter.hasNext()) {
				Action action = iter.next();
				if (!ScheduleActions.OPEN.equals(action)) {
					iter.remove();
				}
			}
		}

		// if enabled evaluate the actual instance operations
		if (Boolean.TRUE.equals(evaluateActualInstanceActions)
				&& (target.getActualInstanceId() != null)) {
			com.sirma.itt.emf.instance.model.Instance actualInstance = target.getActualInstance();
			Set<Action> set = authorityService.getAllowedActions(resource.getIdentifier(),
					actualInstance, null);
			actions.addAll(set);
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getContainer(ScheduleEntry target) {
		if (target.getActualInstanceId() != null) {
			com.sirma.itt.emf.instance.model.Instance instance = target.getActualInstance();
			if (instance instanceof TenantAware) {
				return ((TenantAware) instance).getContainer();
			}
		}
		// fixed fetching tenant ID for ScheduleEntry
		// this will not work in multi tenant environment!!!
		User authentication = SecurityContextManager.getFullAuthentication();
		if (authentication != null) {
			return authentication.getTenantId();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED;
	}

}
