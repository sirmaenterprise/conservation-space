package com.sirma.itt.cmf.evaluators;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.TaskType;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.evaluation.BaseEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Task count evaluator to return active/inactive tasks count for current instance.
 * 
 * @author BBonev
 */
public class TaskCountEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4532849536272067602L;

	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{taskCount(?:\\((active|inactive)\\))?\\}");

	@Inject
	private TaskService taskService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {
		Instance instance = (Instance) getCurrentInstance(context, values);
		if (instance == null) {
			return 0;
		}
		String group = matcher.group(1);
		boolean active = StringUtils.isNullOrEmpty(group) || "active".equals(group);

		return taskService.getContextTasks(instance, active).size();
	}

	/**
	 * Gets the task type.
	 * 
	 * @param instance
	 *            the instance
	 * @return the task type
	 */
	protected TaskType getTaskType(Instance instance) {
		if (instance instanceof WorkflowInstanceContext) {
			return TaskType.WORKFLOW_TASK;
		}
		return TaskType.STANDALONE_TASK;
	}

}
