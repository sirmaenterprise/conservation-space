package com.sirma.itt.seip.eai.service.communication;

import java.io.Serializable;

/**
 * Represent an unique identifier for a service to external system. In most cases should be used as enum interface
 * 
 * @author bbanchev
 */
@FunctionalInterface
public interface EAIServiceIdentifier extends Serializable {

	/**
	 * Gets the service unique id.
	 *
	 * @return the service id
	 */
	String getServiceId();

}