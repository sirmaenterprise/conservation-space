/**
 *
 */
package com.sirma.itt.seip.testutil.mocks;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbIdGenerator;

/**
 * @author BBonev
 *
 */
public class DatabaseIdManagerMock implements DatabaseIdManager {
	/** The list of all generated IDs that are tracked for persistence. */
	private final Set<Serializable> generatedIds = new LinkedHashSet<>(1024);

	/** A place for ids that has been persisted before transaction completion. */
	private final Set<Serializable> toPersistIds = new LinkedHashSet<>(1024);

	/** The id generator. */
	private DbIdGenerator generator;

	@Override
	public Serializable generate(boolean track) {
		// TODO implement DatabaseIdManager.generate!
		return null;
	}

	@Override
	public DbIdGenerator getGenerator() {
		// TODO implement DatabaseIdManager.getGenerator!
		return null;
	}

	@Override
	public boolean isIdPersisted(Serializable id) {
		// TODO implement DatabaseIdManager.isIdPersisted!
		return false;
	}

	@Override
	public boolean isIdRegistered(Serializable id) {
		// TODO implement DatabaseIdManager.isIdRegistered!
		return false;
	}

	@Override
	public boolean isPersisted(Entity<? extends Serializable> entity) {
		// TODO implement DatabaseIdManager.isPersisted!
		return false;
	}

	@Override
	public boolean persisted(Entity<? extends Serializable> entity) {
		// TODO implement DatabaseIdManager.persisted!
		return false;
	}

	@Override
	public void register(Entity<? extends Serializable> entity) {
		// TODO implement DatabaseIdManager.register!

	}

	@Override
	public <S extends Serializable> boolean unregisterId(S id) {
		// TODO implement DatabaseIdManager.unregisterId!
		return false;
	}

	@Override
	public boolean registerId(Serializable id) {
		// TODO implement DatabaseIdManager.registerId!
		return false;
	}

	@Override
	public void createRestorePoint(Serializable id) {
		// TODO implement DatabaseIdManager.createRestorePoint!

	}

	@Override
	public boolean hasRestorePoint(Serializable id) {
		// TODO implement DatabaseIdManager.hasRestorePoint!
		return false;
	}

	@Override
	public boolean restoreSavedPoint(Serializable id) {
		// TODO implement DatabaseIdManager.restoreSavedPoint!
		return false;
	}

}
