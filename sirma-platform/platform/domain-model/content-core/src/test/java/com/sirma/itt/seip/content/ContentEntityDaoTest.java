package com.sirma.itt.seip.content;

import static com.sirma.itt.seip.collections.CollectionUtils.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;

/**
 * Test for {@link ContentEntityDao}.
 *
 * @author A. Kunchev
 */
public class ContentEntityDaoTest {

	@InjectMocks
	private ContentEntityDao dao;

	@Mock
	private DbDao dbDao;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		dao = new ContentEntityDao(null, null, dbDao, null, null);
	}

	@Test
	public void getContentsForInstance_emptyId_emptyList() {
		List<ContentEntity> results = dao.getContentsForInstance("", emptySet());
		assertTrue(results.isEmpty());
		verifyZeroInteractions(dbDao);
	}

	@Test
	public void getContentsForInstance_nullId_emptyList() {
		List<ContentEntity> results = dao.getContentsForInstance(null, emptySet());
		assertTrue(results.isEmpty());
		verifyZeroInteractions(dbDao);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getContentsForInstance_noContentsFilter_internalDaoCalled() {
		dao.getContentsForInstance("instnace-id", emptySet());
		verify(dbDao).fetchWithNamed(eq(ContentEntity.QUERY_LATEST_CONTENT_BY_INSTANCE_KEY), anyListOf(Pair.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getContentsForInstance_withContentsFilter_contentsFiltered() {
		ContentEntity entity = new ContentEntity();
		entity.setPurpose("content-purpose");

		ContentEntity entityFullPurposeMatch = new ContentEntity();
		entityFullPurposeMatch.setPurpose("purpose-to-skip");

		ContentEntity entityPartialPurposeMatch = new ContentEntity();
		entityPartialPurposeMatch.setPurpose("content-purpose-to-skip-partial-match");

		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_LATEST_CONTENT_BY_INSTANCE_KEY), anyListOf(Pair.class)))
				.thenReturn(Arrays.asList(entityFullPurposeMatch, entity));

		List<ContentEntity> results = dao.getContentsForInstance("instnace-id", Arrays.asList("purpose-to-skip"));
		assertEquals(1, results.size());
		assertEquals("content-purpose", results.get(0).getPurpose());
	}

}
