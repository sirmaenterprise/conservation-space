package com.sirma.sep.keycloak.events;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;

/**
 * {@link InitialContextFactory} implementation used for test purposes to mock jndi lookups.
 *
 * @author smustafov
 */
public class InitialContextFactoryFake implements InitialContextFactory {

	static Context context;

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) {
		return context;
	}

}
