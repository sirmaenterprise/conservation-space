package com.sirma.itt.emf.sequence;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.emf.sequence.entity.SequenceEntity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;

/**
 * Test the sequence generator service.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class SequenceGeneratorServiceImplTest {

	@Mock
	private DbDao dbDao;

	@Mock
	private EntityManager entityManager;

	@InjectMocks
	private SequenceGeneratorService sequenceGenerator = new SequenceGeneratorServiceImpl();

	@Test
	public void should_getNextId() {
		SequenceEntity entity = new SequenceEntity();
		entity.setSequence(3L);
		mockEntityManager(Arrays.asList(entity));

		// Generate it with no sequenceId so the default one will be used.
		Long id = sequenceGenerator.getNextId("");
		verifySequencePersisted("default", 4L);
		assertEquals(4L, id.longValue());
	}

	@Test
	public void should_createAndIncrementSequence_when_Missing() {
		mockEntityManager(CollectionUtils.emptyList());

		Long id = sequenceGenerator.getNextId("case");
		verifySequencePersisted("case", 1L);
		assertEquals(1L, id.longValue());
	}

	@Test
	public void should_incrementSequence() {
		SequenceEntity entity = new SequenceEntity();
		entity.setSequence(1L);
		mockEntityManager(Arrays.asList(entity));

		// Increment it with no sequenceId so the default one will be used.
		Sequence sequence = sequenceGenerator.incrementSequence("");
		verifySequencePersisted("default", 2L);
		assertEquals(2L, sequence.getValue().longValue());
	}

	@Test
	public void should_getCurrentId() {
		SequenceEntity entity = new SequenceEntity();
		entity.setSequence(15L);
		mockEntityManager(Arrays.asList(entity));

		// Increment it with no sequenceId so the default one will be used.
		Long id = sequenceGenerator.getCurrentId("");
		assertEquals(15L, id.longValue());
	}

	@Test
	public void should_getCurrentId_byTemplate() {
		SequenceEntity entity = new SequenceEntity();
		entity.setSequence(3L);
		mockEntityManager(Arrays.asList(entity));

		String id = sequenceGenerator.getNextSequenceByTemplate("case");
		// We just want to retrieve the current id, no increment/persist should be happening here.
		verifyZeroInteractions(dbDao);
		assertEquals("3", id);
	}

	@Test
	public void should_getCurrentId_withIncrementAfter_byTemplate() {
		SequenceEntity entity = new SequenceEntity();
		entity.setSequence(3L);
		mockEntityManager(Arrays.asList(entity));

		String id = sequenceGenerator.getNextSequenceByTemplate("case+");
		// This should return the current sequence but persist the incremented one.
		verifySequencePersisted("case", 4L);
		assertEquals("3", id);
	}

	@Test
	public void should_getCurrentId_withIncrementBefore_byTemplate() {
		SequenceEntity entity = new SequenceEntity();
		entity.setSequence(3L);
		mockEntityManager(Arrays.asList(entity));

		String id = sequenceGenerator.getNextSequenceByTemplate("+case");
		// This should increment and then persist
		verifySequencePersisted("case", 4L);
		assertEquals("4", id);
	}

	@Test
	public void should_resetSequence() {
		SequenceEntity entity = new SequenceEntity();
		entity.setSequence(3L);
		mockEntityManager(Arrays.asList(entity));

		sequenceGenerator.resetSequenceTo("case", 0L);
		verifySequencePersisted("case", 0L);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_listAllSequences() {
		SequenceEntity entity = new SequenceEntity();
		entity.setSequence(3L);
		when(dbDao.fetchWithNamed(anyString(), anyList())).thenReturn(Arrays.asList(entity));

		Collection<SequenceEntity> entities = sequenceGenerator.listAll();
		SequenceEntity entityResult = entities.iterator().next();
		assertEquals(3L, entityResult.getSequence().longValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_getSequence() {
		SequenceEntity entity = new SequenceEntity();
		entity.setSequence(3L);
		when(dbDao.fetchWithNamed(anyString(), anyList())).thenReturn(Arrays.asList(entity));

		SequenceEntity entityResult = sequenceGenerator.getSequence("case");
		assertEquals(3L, entityResult.getSequence().longValue());
	}

	private void mockEntityManager(List<SequenceEntity> entities) {
		TypedQuery<SequenceEntity> sequenceEntityQuery = mock(TypedQuery.class);
		when(sequenceEntityQuery.getResultList()).thenReturn(entities);
		when(entityManager.createNamedQuery(anyString(), eq(SequenceEntity.class))).thenReturn(sequenceEntityQuery);
	}

	@SuppressWarnings("unchecked")
	private void verifySequencePersisted(String sequenceId, long sequence) {
		ArgumentCaptor<List<Pair<String, Object>>> updateArgsCaptor = ArgumentCaptor.forClass(List.class);
		// Verify that the new sequence is persisted.
		verify(dbDao).executeUpdate(eq(SequenceEntity.UPDATE_SEQUENCES_ENTRY_KEY), updateArgsCaptor.capture());
		// This is the default sequenceId
		assertEquals(sequenceId, updateArgsCaptor.getValue().get(0).getSecond());
		// The sequence should've been incremented
		assertEquals(sequence, updateArgsCaptor.getValue().get(1).getSecond());
	}

}
