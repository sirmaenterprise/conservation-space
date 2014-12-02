package com.sirma.itt.cmf.services.mock;

import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.sirma.codelist.service.CodelistManager;
import com.sirma.codelist.service.CodelistService;
import com.sirma.codelist.service.DefaultCodelistService;
import com.sirma.codelist.service.ws.WSCodelistManager;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.emf.configuration.Config;

/**
 */
@ApplicationScoped
public class CodelistServiceProviderMock {
	/** The wsdl location. */
	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_WSDL_LOCATION)
	private String wsdlLocation;

	/** The instance. */
	private CodelistService instance;

	/** The lock. */
	private ReentrantLock lock = new ReentrantLock(false);

	/**
	 * Gets the service instance.
	 *
	 * @return the service instance
	 */
	@Produces
	@Default
	public CodelistService getServiceInstance() {
		if (instance == null) {
			boolean loading = false;
			// check if someone else is creating the cache instance
			if (lock.isLocked()) {
				loading = true;
			}
			lock.lock();
			try {
				// check if the instance is created from someone else while we
				// are
				// waiting for the access, if so return the instance
				if (loading && (instance != null)) {
					return instance;
				}
				CodelistManager manager = new WSCodelistManager(wsdlLocation, true);
				// NOTE: the initialization of the codelist takes some time
				// (>15s (sometimes > 60s))
				instance = new DefaultCodelistService(manager);
			} finally {
				lock.unlock();
			}
		}
		return instance;
	}

	/**
	 * Reset codelist service instance.
	 */
	public void resetCodelistServiceInstance() {
		lock.lock();
		try {
			instance = null;
		} finally {
			lock.unlock();
		}
	}
}
