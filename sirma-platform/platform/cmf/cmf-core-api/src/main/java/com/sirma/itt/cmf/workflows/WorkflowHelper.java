package com.sirma.itt.cmf.workflows;

import static com.sirma.itt.cmf.constants.TaskProperties.COMPLETED_BY;
import static com.sirma.itt.cmf.constants.TaskProperties.POOL_ACTORS;
import static com.sirma.itt.cmf.constants.TaskProperties.POOL_GROUP;
import static com.sirma.itt.cmf.constants.TaskProperties.TASK_ACTIVE_STATE;
import static com.sirma.itt.cmf.constants.TaskProperties.TASK_ASSIGNEE;
import static com.sirma.itt.cmf.constants.TaskProperties.TASK_ASSIGNEES;
import static com.sirma.itt.cmf.constants.TaskProperties.TASK_EXECUTORS;
import static com.sirma.itt.cmf.constants.TaskProperties.TASK_GROUP_ASSIGNEE;
import static com.sirma.itt.cmf.constants.TaskProperties.TASK_MULTI_ASSIGNEES;
import static com.sirma.itt.cmf.constants.TaskProperties.TASK_OWNER;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.DESCRIPTION;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_BREADCRUMB;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_COMPACT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_DEFAULT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_TOOLTIP;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.IS_ACTIVE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.IS_DELETED;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MODIFIED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MODIFIED_ON;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.NAME;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.STATUS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.UNIQUE_IDENTIFIER;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.sirma.itt.seip.domain.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.definition.Transitional;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Helper class for working with workflow definitions.
 *
 * @author BBonev
 */
public class WorkflowHelper {

	private static final Pattern ALPHA_ONLY = Pattern.compile("\\D+");

	/** Set of fields that should not be copied from task to workflow. */
	public static final Set<String> SYSTEM_FIELDS = new HashSet<>(
			Arrays.asList(TITLE, TYPE, CREATED_BY, CREATED_BY, MODIFIED_BY, MODIFIED_ON, NAME, DESCRIPTION, IS_DELETED,
					STATUS, UNIQUE_IDENTIFIER, HEADER_DEFAULT, HEADER_BREADCRUMB, HEADER_COMPACT, HEADER_TOOLTIP,
					SEMANTIC_TYPE, TASK_OWNER, TASK_ASSIGNEE, TASK_GROUP_ASSIGNEE, TASK_ASSIGNEES, TASK_MULTI_ASSIGNEES,
					TASK_EXECUTORS, COMPLETED_BY, POOL_ACTORS, POOL_GROUP, TASK_ACTIVE_STATE, IS_ACTIVE));

	/**
	 * Instantiates a new workflow helper.
	 */
	private WorkflowHelper() {
		// utility class
	}

	/**
	 * Strip workflow engine Id from the given key.
	 *
	 * @param key
	 *            the key
	 * @return the key value without the engine ID
	 */
	public static String stripEngineId(String key) {
		int index = key.indexOf('$');
		if (index == -1) {
			return key;
		}
		return key.substring(index + 1);
	}

	/**
	 * Gets the instance id by stripping and engine id and also removes any other prefixes from the id.
	 * <p>
	 * <b>NOTE</b>The returned string contains only digits
	 *
	 * @param key
	 *            the key
	 * @return the instance id
	 */
	public static String getInstanceId(String key) {
		String id = stripEngineId(key);
		return ALPHA_ONLY.matcher(id).replaceAll("");
	}

	/**
	 * Gets the transition by id if found.
	 *
	 * @param taskDefinition
	 *            the task definition ref
	 * @return the found transition or <code>null</code> if not found
	 */
	public static TransitionDefinition getDefaultTransition(Transitional taskDefinition) {
		if (taskDefinition == null) {
			return null;
		}
		List<TransitionDefinition> transitions = taskDefinition.getTransitions();
		for (TransitionDefinition transitionDefinition : transitions) {
			if (transitionDefinition.getDefaultTransition()) {
				return transitionDefinition;
			}
		}
		return null;
	}

	/**
	 * Gets the transition by id if found.
	 *
	 * @param taskDefinition
	 *            the task definition ref
	 * @param transitionId
	 *            the transition id
	 * @return the found transition or <code>null</code> if not found
	 */
	public static TransitionDefinition getTransitionById(Transitional taskDefinition, String transitionId) {
		if (taskDefinition == null) {
			return null;
		}
		List<TransitionDefinition> transitions = taskDefinition.getTransitions();
		for (TransitionDefinition transitionDefinition : transitions) {
			if (EqualsHelper.nullSafeEquals(transitionId, transitionDefinition.getIdentifier(), true)) {
				return transitionDefinition;
			}
		}
		return null;
	}

	/**
	 * Creates the operation from the given transition definition. If the definition defines case state change then they
	 * will be added to the operation
	 *
	 * @param operationType
	 *            the operation type
	 * @param transitionDefinition
	 *            the transition definition
	 * @return the operation
	 */
	public static Operation createOperation(String operationType, TransitionDefinition transitionDefinition) {
		return new Operation(operationType, transitionDefinition.getNextPrimaryState(),
				transitionDefinition.getNextSecondaryState());
	}

	/**
	 * Builds the tree path of an {@link AbstractTaskInstance}
	 *
	 * @param abstractTaskInstance
	 *            the task instance
	 * @return the built tree path
	 */
	public static String buildTreePath(Instance abstractTaskInstance) {
		return InstanceUtil.buildPath(abstractTaskInstance, null);
	}
}
