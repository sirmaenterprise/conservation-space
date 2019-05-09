package com.sirma.itt.emf.sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.sequence.entity.SequenceEntity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.PersistenceUnits;

/**
 * Default implementation of {@link SequenceGeneratorService} that uses an internal cache of
 * {@link AtomicLong}s and only executes updates to database.
 * <p>
 * <b>NOTE: </b> The current implementation is not cluster enabled!
 *
 * @author BBonev
 */
@ApplicationScoped
public class SequenceGeneratorServiceImpl implements SequenceGeneratorService {

	private static final String SEQUENCE_ID = "sequenceId";

	private static final String DEFAULT_SEQUENCE = "default";

	private static final Pattern TEMPLATE_PATTERN = Pattern.compile("^\\{?(\\+)?([\\w\\W\\s]+?)(\\+)?\\}?$");

	@Inject
	private DbDao dbDao;

	@PersistenceContext(unitName = PersistenceUnits.PRIMARY)
	private EntityManager entityManager;

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public Long getNextId(String sequenceId) {
		String sequence = getNonNullSequenceId(sequenceId);
		AtomicLong atomicLong = getSequenceInternal(sequenceId, LockModeType.PESSIMISTIC_WRITE);
		long result = atomicLong.incrementAndGet();

		persistSequence(sequence, result);
		return result;
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
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
		args.add(new Pair<>(SEQUENCE_ID, sequence));
		args.add(new Pair<>("sequence", result));

		dbDao.executeUpdate(SequenceEntity.UPDATE_SEQUENCES_ENTRY_KEY, args);
	}

	@Override
	public Long getCurrentId(String sequenceId) {
		return getSequenceInternal(getNonNullSequenceId(sequenceId), LockModeType.READ).get();
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
		if (StringUtils.isBlank(sequence)) {
			sequence = DEFAULT_SEQUENCE;
		}
		return sequence;
	}

	/**
	 * Gets the sequence.
	 *
	 * @param sequenceId
	 *            the sequence id
	 * @param lockMode
	 *            the lock mode
	 * @return the sequence
	 */
	protected AtomicLong getSequenceInternal(String sequenceId, LockModeType lockMode) {
		TypedQuery<SequenceEntity> query = entityManager.createNamedQuery(SequenceEntity.QUERY_SEQUENCE_BY_NAME_KEY,
				SequenceEntity.class);
		query.setParameter("sequenceId", sequenceId);
		query.setLockMode(lockMode);
		List<SequenceEntity> results = query.getResultList();
		if (CollectionUtils.isEmpty(results)) {
			SequenceEntity entity = new SequenceEntity();
			entity.setSequence(0L);
			entity.setSequenceId(sequenceId);
			dbDao.saveOrUpdateInNewTx(entity);
			return new AtomicLong(0);
		}
		return new AtomicLong(results.get(0).getSequence());
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public String getNextSequenceByTemplate(String template) {
		if (StringUtils.isBlank(template) || template.contains("null")) {
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
	@Transactional(TxType.REQUIRES_NEW)
	public void resetSequenceTo(String sequenceId, Long value) {
		if (value == null || value < 0) {
			return;
		}
		// creates the sequence if not exists
		AtomicLong atomicLong = getSequenceInternal(sequenceId, LockModeType.PESSIMISTIC_WRITE);
		atomicLong.set(value);

		// persist the given value
		persistSequence(sequenceId, value);
	}

	@Override
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
		return dbDao.fetchWithNamed(SequenceEntity.QUERY_SEQUENCES_KEY, Collections.emptyList());
	}

	@Override
	public <S extends Sequence> S getSequence(String name) {
		List<S> list = dbDao.fetchWithNamed(SequenceEntity.QUERY_SEQUENCE_BY_NAME_KEY,
				Collections.singletonList(new Pair<>(SEQUENCE_ID, name)));
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

}
