package com.sirma.itt.seip.domain;

/**
 * Interface that can allow component to be disabled by adding disable reason messages. The implementation should handle
 * how to combine all messages and returned them via the method {@link #getDisabledReason()}.
 *
 * @author BBonev
 */
public interface BehaviorControl {

	/**
	 * Checks if this control should is disabled.
	 *
	 * @return <code>true</code>, if is disabled
	 */
	boolean isDisabled();

	/**
	 * Gets the combined disabled reason message or empty string if no reasons are added.
	 *
	 * @return the combined disabled reason
	 */
	String getDisabledReason();

	/**
	 * Adds the disabled reason. After calling this method it's advisable the method {@link #isDisabled()} to return
	 * <code>true</code>.
	 *
	 * @param reason
	 *            the reason to add
	 */
	void addDisabledReason(String reason);
}
