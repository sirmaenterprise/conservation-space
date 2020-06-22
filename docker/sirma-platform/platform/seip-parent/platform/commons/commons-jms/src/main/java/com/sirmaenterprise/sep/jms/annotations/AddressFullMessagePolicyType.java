package com.sirmaenterprise.sep.jms.annotations;

/**
 * The possible policy types for full message address.
 *
 * @author BBonev
 */
public enum AddressFullMessagePolicyType {
	/**
	 * If max size bytes is configured then the address will write the extra messages to the page files
	 */
	PAGE,
	/**
	 * Extra messages will be dropped silently
	 */
	DROP,
	/**
	 * The client will be blocked until the message could be accepted to the destination
	 */
	BLOCK;
}
