package com.sirma.itt.emf.mocks;

import java.util.Map;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.semantic.TransactionalRepositoryConnectionImpl;

/**
 * Mock class for {@link TransactionalRepositoryConnectionImpl}
 *
 * @author BBonev
 */
public class TransactionalRepositoryConnectionMock extends TransactionalRepositoryConnectionImpl {

	/**
	 * Instantiates a new transactional repository connection mock.
	 *
	 * @param context
	 *            the context
	 */
	public TransactionalRepositoryConnectionMock(Map<String, Object> context) {
		ReflectionUtils.setField(this, "connectionFactory", context.get("connectionFactory"));
		ReflectionUtils.setField(this, "securityContext", context.get("securityContext"));
		ReflectionUtils.setField(this, "monitor", context.get("monitor"));
	}

}
