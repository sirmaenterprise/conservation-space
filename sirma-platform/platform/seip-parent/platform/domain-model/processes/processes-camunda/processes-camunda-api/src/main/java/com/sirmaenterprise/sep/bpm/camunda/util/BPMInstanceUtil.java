package com.sirmaenterprise.sep.bpm.camunda.util;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * Instance util used by Camunda integration for various utility methods related to instance operations.
 * 
 * @author bbanchev
 */
public class BPMInstanceUtil {

	private BPMInstanceUtil() {
		// utility class
	}

	/**
	 * Helper method to resolve and validate instance resolving.
	 * 
	 * @param instanceDbId
	 *            is the instance id - as emf:....
	 * @param instanceResolver
	 *            is the resolver to use
	 * @return the resolved instance or throws exception if !=1 instances are found or provided id is invalid
	 */
	public static Instance resolveInstance(String instanceDbId, InstanceTypeResolver instanceResolver) {
		return resolveReference(instanceDbId, instanceResolver).toInstance();
	}

	/**
	 * Helper method to resolve and validate instance reference resolving.
	 *
	 * @param instanceDbId
	 *            is the instance id - as emf:....
	 * @param instanceResolver
	 *            is the resolver to use
	 * @return the resolved instance reference or throws exception if !=1 instances are found or provided id is invalid
	 */
	public static InstanceReference resolveReference(String instanceDbId, InstanceTypeResolver instanceResolver) {
		if (StringUtils.isBlank(instanceDbId)) {
			throw new CamundaIntegrationRuntimeException("Instance id is a required argument to resolve instance!");
		}
		Optional<InstanceReference> instanceReference = instanceResolver.resolveReference(instanceDbId);
		if (!instanceReference.isPresent()) {
			throw new CamundaIntegrationRuntimeException(
					"Unexpected result after search for bpm instance in the system by id: " + instanceDbId);
		}
		return instanceReference.get();
	}
}
