package com.sirma.itt.seip.db;

/**
 * Defines the persistence units available for database access
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/10/2017
 */
public final class PersistenceUnits {

	/**
	 * Core persistence unit name used for accessing the common database outside tenant context
	 */
	public static final String CORE = "CoreDatabase";

	/**
	 * Default persistence unit name for accessing per tenant databases
	 */
	public static final String PRIMARY = "SEIP-Primary";

	private PersistenceUnits() {
		// nothing to add
	}
}
