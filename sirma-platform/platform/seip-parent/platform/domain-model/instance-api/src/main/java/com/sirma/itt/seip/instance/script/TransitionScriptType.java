package com.sirma.itt.seip.instance.script;

/**
 * The supported script types for transition definitions
 *
 * @author BBonev
 */
public enum TransitionScriptType {

	/**
	 * The transition action script. This is the default value. This scripts are executed after user has clicked the
	 * transition operation and are executed before actual transition in Activiti server or after that. This is
	 * controlled by other configuration.
	 */
	TRANSITION_ACTION, /**
						 * The transition condition. These scripts are executed before user is allowed to perform the
						 * operation and can be used to restrict user actions. To do that a script must return a string
						 * message indicating why the action could not be executed. This message will be display to the
						 * user. If everything is OK then the script should not return anything.
						 */
	TRANSITION_CONDITION, /** The on transition. */
	SCRIPT_WITH_RESULT;
}