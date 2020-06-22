package com.sirma.itt.emf.rule;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Context instance that has helper methods to provide convenient access to stored values when running rules.
 *
 * @author BBonev
 */
public class RuleContext extends Context<String, Object> {
	private static final long serialVersionUID = -3108749870938461862L;
	/**
	 * A key that can be used to access the currently processed instance
	 */
	public static final String PROCESSING_INSTANCE = "processingInstance";
	/**
	 * A key that can be used to access the previous version of the processed instance.
	 */
	public static final String PREVIOUS_VERSION = "previousVersion";
	/**
	 * A key that can be used to access the operation id that triggered the rule execution
	 */
	public static final String OPERATION = "operation";

	/**
	 * Key that can be used to access the instance rule state. The state is optional and could be used to provide means
	 * of accessing the rule internal state during processing.
	 */
	public static final String STATE = "state";

	/**
	 * Instantiates a new rule context.
	 */
	public RuleContext() {
		super();
	}

	/**
	 * Instantiates a new rule context.
	 *
	 * @param <M>
	 *            the generic type
	 * @param source
	 *            the source
	 */
	public <M extends Map<String, Object>> RuleContext(M source) {
		super(source);
	}

	/**
	 * Instantiates a new rule context.
	 *
	 * @param <M>
	 *            the generic type
	 * @param preferredSize
	 *            the preferred size
	 * @param source
	 *            the source
	 */
	public <M extends Map<String, Object>> RuleContext(int preferredSize, M source) {
		super(preferredSize, source);
	}

	/**
	 * Instantiates a new rule context.
	 *
	 * @param preferredSize
	 *            the preferred size
	 */
	public RuleContext(int preferredSize) {
		super(preferredSize);
	}

	/**
	 * Gets the operation that triggered the rule invocation.
	 *
	 * @return the operation
	 */
	public String getOperation() {
		return getIfSameType(RuleContext.OPERATION, String.class);
	}

	/**
	 * Gets the instance that triggered the rule activation
	 *
	 * @return the trigger instance
	 */
	public Instance getTriggerInstance() {
		return getIfSameType(RuleContext.PROCESSING_INSTANCE, Instance.class);
	}

	/**
	 * Gets the previous version of the trigger instance. If triggered on instance creation this may be
	 * <code>null</code>.
	 *
	 * @return the previous instance version or <code>null</code> if non existent
	 */
	public Instance getPreviousInstanceVersion() {
		return getIfSameType(RuleContext.PREVIOUS_VERSION, Instance.class);
	}

	/**
	 * Gets the rule state if any.
	 *
	 * @param <R>
	 *            the generic type
	 * @return the state or <code>null</code>
	 * @see #STATE
	 */
	@SuppressWarnings("unchecked")
	public <R extends RuleState> R getState() {
		return (R) getIfSameType(STATE, RuleState.class);
	}

	/**
	 * Sets the current state of the rule.
	 *
	 * @param state
	 *            the new state
	 * @see #STATE
	 */
	public void setState(RuleState state) {
		put(STATE, state);
	}

	/**
	 * Gets the trigger instance id from the given context
	 *
	 * @param context
	 *            the context
	 * @return the trigger instance id
	 */
	public static Serializable getTriggerInstanceId(Context<String, Object> context) {
		if (context instanceof RuleContext) {
			Instance triggerInstance = ((RuleContext) context).getTriggerInstance();
			if (triggerInstance != null) {
				return triggerInstance.getId();
			}
		}
		Instance triggerInstance = context.getIfSameType(RuleContext.PROCESSING_INSTANCE, Instance.class);
		if (triggerInstance != null) {
			return triggerInstance.getId();
		}
		// add other ways of loading trigger instance if needed
		return null;
	}

	/**
	 * Creates rule context from the given instance data and operation
	 *
	 * @param currentInstance
	 *            the current instance
	 * @param oldVersionInstance
	 *            the old version instance
	 * @param operation
	 *            the operation
	 * @return the rule context
	 */
	public static RuleContext create(Instance currentInstance, Instance oldVersionInstance, String operation) {
		RuleContext context = new RuleContext(10);
		context.put(RuleContext.PROCESSING_INSTANCE, currentInstance);
		context.put(RuleContext.PREVIOUS_VERSION, oldVersionInstance);
		context.put(RuleContext.OPERATION, operation);
		return context;
	}

	/**
	 * Create a copy of the given context as {@link RuleContext}.
	 *
	 * @param context
	 *            the context
	 * @return the rule context
	 */
	public static RuleContext copy(Context<String, Object> context) {
		return new RuleContext(context);
	}

}
