package com.sirma.itt.emf.cls.event;

import com.sirma.itt.emf.cls.entity.Tenant;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after tenant persisting in the DB.
 * 
 * @author Mihail Radkov
 */
// TODO: Is this event necessary?
@Documentation("Event fired after tenant persisting in the DB.")
public final class TenantPersistEvent implements EmfEvent {

	/** The persisted tenant. */
	private final Tenant tenant;

	/**
	 * Class constructor. Takes the persisted tenant.
	 * 
	 * @param tenant
	 *            the persisted tenant
	 */
	public TenantPersistEvent(Tenant tenant) {
		this.tenant = tenant;
	}

	/**
	 * Returns the persisted tenant.
	 * 
	 * @return the persisted tenant
	 */
	public Tenant getTenant() {
		return tenant;
	}

}
