package com.sirma.itt.seip.context;

/**
 * Object that support passing an external configuration. The implementation of this object should NOT be annotated with
 * stateful scope and defined in any scope different than {@link javax.enterprise.inject.Default}. The factory used to
 * created instances that support configurations should ensure calling the {@link #configure(Context)} method before
 * calling any other method.
 *
 * @author BBonev
 */
public interface Configurable {

	/**
	 * Sets the configuration for the rule to use. This method will be called after rule instantiation before calling
	 * any other method.
	 *
	 * @param configuration
	 *            the configuration to pass
	 * @return <code>true</code> if the object was configured successfully. If the method returns <code>false</code>
	 *         this should mean that there passed configuration was incomplete or invalid and the implementer not ready
	 *         for work and should be ignored.
	 */
	boolean configure(Context<String, Object> configuration);

}
