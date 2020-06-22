package com.sirma.itt.seip.testutil.fakes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbIdGenerator;

/**
 * Dummy implementation of {@link DatabaseIdManager} to be used for tests purposes only.
 *
 * @author BBonev
 */
public class DatabaseIdManagerFake implements DatabaseIdManager {

	private static final long serialVersionUID = 6681212451438994815L;
	private final Set<Serializable> generatedIds = new LinkedHashSet<>();
	private final Set<Serializable> toPersistIds = new LinkedHashSet<>();
	private final Map<Serializable, IdState> restorePoints = new HashMap<>();

	private DbIdGenerator generator;

	/**
	 * Instantiates a new database id manager fake.
	 */
	public DatabaseIdManagerFake() {
		this(new DbIdGeneratorFake());
	}

	/**
	 * Instantiates a new database id manager fake with custom id generator
	 *
	 * @param generator
	 *            the generator
	 */
	public DatabaseIdManagerFake(DbIdGenerator generator) {
		this.generator = generator;
	}

	/**
	 * Generate an id that is optionally tracked if persisted or not.
	 *
	 * @param track
	 *            if the id should be tracked or not.
	 * @return the generated db id.
	 */
	@Override
	public Serializable generate(boolean track) {
		return generateIdInternal(track);
	}

	/**
	 * Generate an id that is tracked or not if persisted or not.
	 *
	 * @param track
	 *            if the id should be tracked or not.
	 * @return the generated db id.
	 */
	public Serializable generateId(boolean track) {
		return generateIdInternal(track);
	}

	private Serializable generateIdInternal(boolean toKeepTrack) {
		Serializable id = generator.generateId().toString();
		if (toKeepTrack) {
			generatedIds.add(id);
		}
		return id;
	}

	@Override
	public boolean isIdPersisted(Serializable id) {
		if (id == null) {
			return false;
		}
		// all entities that have a long ID and that id is present are considered for persisted
		if (id instanceof Long) {
			return true;
		}
		boolean inPersisted = toPersistIds.contains(id);
		boolean notInGenerated = !generatedIds.contains(id);
		return inPersisted || notInGenerated;
	}

	@Override
	public boolean isIdRegistered(Serializable id) {
		if (id == null) {
			return false;
		}
		return generatedIds.contains(id) || toPersistIds.contains(id);
	}

	@Override
	public boolean isPersisted(Entity<? extends Serializable> entity) {
		if (entity == null || entity.getId() == null) {
			return false;
		}
		return isIdPersisted(entity.getId());
	}

	@Override
	public boolean persisted(Entity<? extends Serializable> entity) {
		if (entity == null || entity.getId() == null) {
			return false;
		}
		if (generatedIds.remove(entity.getId())) {
			toPersistIds.add(entity.getId());
			return true;
		}
		return false;
	}

	@Override
	public void register(Entity<? extends Serializable> entity) {
		if (entity != null && entity.getId() != null) {
			generatedIds.add(entity.getId());
		}
	}

	@Override
	public <S extends Serializable> boolean unregisterId(S id) {
		if (id != null) {
			boolean present = generatedIds.remove(id);
			present |= toPersistIds.remove(id);
			return present;
		}
		return false;
	}

	@Override
	public boolean registerId(Serializable id) {
		if (id != null) {
			return generatedIds.add(id);
		}
		return false;
	}

	@Override
	public void createRestorePoint(Serializable id) {
		if (id == null) {
			return;
		}
		IdState state;
		state = new IdState(id);
		restorePoints.put(id, state);
	}

	@Override
	public boolean hasRestorePoint(Serializable id) {
		if (id == null) {
			return false;
		}
		return restorePoints.containsKey(id);
	}

	@Override
	public boolean restoreSavedPoint(Serializable id) {
		if (!hasRestorePoint(id)) {
			return false;
		}
		return restorePoints.remove(id).restore();
	}

	@Override
	public DbIdGenerator getGenerator() {
		return generator;
	}

	/**
	 * Class that represents a state of the a single id in the internal model. This should be used only when there are
	 * needed modifications to the model and need to be restored after that
	 *
	 * @author BBonev
	 */
	private class IdState {
		private final Serializable id;
		private final boolean generated;
		private final boolean toPersist;

		/**
		 * Instantiates a new id state.
		 *
		 * @param id
		 *            the id
		 */
		private IdState(Serializable id) {
			this.id = id;
			generated = generatedIds.contains(id);
			toPersist = toPersistIds.contains(id);
		}

		/**
		 * Restores the state for the current id
		 *
		 * @return true, if any modifications to the internal model has been done
		 */
		boolean restore() {
			boolean result = false;
			if (generated) {
				result |= generatedIds.add(id);
			}
			if (toPersist) {
				result |= toPersistIds.add(id);
			}
			return result;
		}
	}

}
