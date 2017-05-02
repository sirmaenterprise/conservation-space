package com.sirma.itt.emf.sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.sequence.entity.SequenceEntity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.db.DbDao;

/**
 * Default implementation of {@link SequenceGeneratorService} that uses an internal cache of {@link AtomicLong}s and
 * only executes updates to database.
 * <p>
 * <b>NOTE: </b> The current implementation is not cluster enabled!
 *
 * @author BBonev
 */
@Singleton
public class SequenceGeneratorServiceImpl implements SequenceGeneratorService {

	private static final String SEQUENCE_ID = "sequenceId";

	/** The Constant DEFAULT_SEQUENCE. */
	public static final String DEFAULT_SEQUENCE = "default";

	private static final Pattern TEMPLATE_PATTERN = Pattern.compile("^\\{?(\\+)?([\\w\\W\\s]+?)(\\+)?\\}?$");

	@Inject
	private DbDao dbDao;

	/** The sequence cache. */
	@Inject
	protected ContextualMap<String, AtomicLong> sequenceCache;

	@PostConstruct
	void init() {
		sequenceCache.initializeWith(() -> Collections.synchronizedMap(new LinkedHashMap<String, AtomicLong>(250)));
	}

	/**
	 * Initialize the internal cache.
	 */
	@Override
	public void initialize() {
		Collection<SequenceEntity> list = listAllInternal();

		for (SequenceEntity sequenceEntity : list) {
			sequenceCache.put(sequenceEntity.getSequenceId(), new AtomicLong(sequenceEntity.getSequence()));
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Lock(LockType.WRITE)
	public Long getNextId(String sequenceId) {
		String sequence = getNonNullSequenceId(sequenceId);
		AtomicLong atomicLong = getSequenceInternal(sequenceId);
		long result = atomicLong.incrementAndGet();

		persistSequence(sequence, result);
		return result;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Lock(LockType.WRITE)
	public Sequence incrementSequence(String sequenceId) {
		Long nextId = getNextId(sequenceId);
		SequenceEntity entity = new SequenceEntity();
		entity.setSequenceId(sequenceId);
		entity.setSequence(nextId);
		return entity;
	}

	/**
	 * Persist sequence.
	 *
	 * @param sequence
	 *            the sequence
	 * @param result
	 *            the result
	 */
	private void persistSequence(String sequence, long result) {
		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<String, Object>(SEQUENCE_ID, sequence));
		args.add(new Pair<String, Object>("sequence", result));

		dbDao.executeUpdate(EmfQueries.UPDATE_SEQUENCES_ENTRY_KEY, args);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Lock(LockType.READ)
	public Long getCurrentId(String sequenceId) {
		return getSequenceInternal(getNonNullSequenceId(sequenceId)).get();
	}

	/**
	 * Gets the non null sequence id.
	 *
	 * @param sequenceId
	 *            the sequence id
	 * @return the non null sequence id
	 */
	protected String getNonNullSequenceId(String sequenceId) {
		String sequence = sequenceId;
		if (StringUtils.isNullOrEmpty(sequence)) {
			sequence = DEFAULT_SEQUENCE;
		}
		return sequence;
	}

	/**
	 * Gets the sequence.
	 *
	 * @param sequenceId
	 *            the sequence id
	 * @return the sequence
	 */
	protected AtomicLong getSequenceInternal(String sequenceId) {
		AtomicLong atomicLong = sequenceCache.get(sequenceId);
		if (atomicLong == null) {
			atomicLong = new AtomicLong(0);
			sequenceCache.put(sequenceId, atomicLong);
			SequenceEntity entity = new SequenceEntity();
			entity.setSequence(0L);
			entity.setSequenceId(sequenceId);
			dbDao.saveOrUpdateInNewTx(entity);
		}
		return atomicLong;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Lock(LockType.WRITE)
	public String getNextSequenceByTemplate(String template) {
		if (StringUtils.isNullOrEmpty(template) || template.contains("null")) {
			return null;
		}

		StringBuilder result = new StringBuilder(template);
		Matcher matcher = TEMPLATE_PATTERN.matcher(template);
		int index = 0;
		while (matcher.find(index)) {
			String preIncrement = matcher.group(1);
			String sequenceId = matcher.group(2);
			String postIncrement = matcher.group(3);
			String sequenceValue;
			if (preIncrement == null && postIncrement == null) {
				Long currentId = getCurrentId(sequenceId);
				sequenceValue = currentId.toString();
			} else if (preIncrement != null) {
				Long nextId = getNextId(sequenceId);
				sequenceValue = nextId.toString();
			} else {
				Long currentId = getCurrentId(sequenceId);
				getNextId(sequenceId);
				sequenceValue = currentId.toString();
			}
			result.replace(matcher.start(), matcher.end(), sequenceValue);
			index = matcher.end();
		}
		return result.toString();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Lock(LockType.WRITE)
	public void resetSequenceTo(String sequenceId, Long value) {
		if (value == null || value.longValue() < 0) {
			return;
		}
		// creates the sequence if not exists
		AtomicLong atomicLong = getSequenceInternal(sequenceId);
		atomicLong.set(value);

		// persist the given value
		persistSequence(sequenceId, value);
	}

	@Override
	@Lock(LockType.READ)
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Sequence> Collection<S> listAll() {
		return listAllInternal();
	}

	/**
	 * List all internal.
	 *
	 * @param <S>
	 *            the generic type
	 * @return the collection
	 */
	private <S extends Sequence> Collection<S> listAllInternal() {
		return dbDao.fetchWithNamed(EmfQueries.QUERY_SEQUENCES_KEY, Collections.emptyList());
	}

	@Override
	@Lock(LockType.READ)
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Sequence> S getSequence(String name) {
		List<S> list = dbDao.fetchWithNamed(EmfQueries.QUERY_SEQUENCE_BY_NAME_KEY,
				Collections.singletonList(new Pair<>(SEQUENCE_ID, name)));
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

}
