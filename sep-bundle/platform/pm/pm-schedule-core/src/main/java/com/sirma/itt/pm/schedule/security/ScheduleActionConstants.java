package com.sirma.itt.pm.schedule.security;

import com.sirma.itt.pm.security.PmActionTypeConstants;

/**
 * List of action used in Schedule
 * 
 * @author BBonev
 */
public interface ScheduleActionConstants extends PmActionTypeConstants {

	/** The add child. */
	String ADD_CHILD = "addChild";

	/** The indent. */
	String INDENT = "indentTask";

	/** The outdent. */
	String OUTDENT = "outdentTask";

	/** The open. */
	String OPEN = "openTask";

	/** The change task color. */
	String CHANGE_TASK_COLOR = "changeTaskColor";

	/** The edit left label. */
	String EDIT_LEFT_LABEL = "editLeftLabel";

	/** The edit right label. */
	String EDIT_RIGHT_LABEL = "editRightLabel";

	/** The add task menu. */
	String ADD_TASK_MENU = "addTaskMenu";

	/** The add task above. */
	String ADD_TASK_ABOVE = "addTaskAbove";

	/** The add task below. */
	String ADD_TASK_BELOW = "addTaskBelow";

	/** The add milestone. */
	String ADD_MILESTONE = "addMilestone";

	/** The add successor. */
	String ADD_SUCCESSOR = "addSuccessor";

	/** The add predecessor. */
	String ADD_PREDECESSOR = "addPredecessor";

	/** The delete dependency menu. */
	String DELETE_DEPENDENCY_MENU = "deleteDependencyMenu";

}
