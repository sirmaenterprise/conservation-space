package com.sirma.sep.content;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.collections.CollectionUtils.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.type.MimeTypeResolver;

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

	@Mock
	private MimeTypeResolver mimeTypeResolver;

	@Mock
	private DatabaseIdManager databaseIdManager;

	@Mock
	private IdResolver idResolver;

	@Mock
	private ContentDigestProvider contentDigestProvider;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		when(idResolver.resolve(any())).then(a -> Optional.ofNullable(AdditionalAnswers.returnsFirstArg()));
		when(databaseIdManager.generateStringId(any(ContentEntity.class), anyBoolean())).then(a -> {
			ContentEntity entity = a.getArgumentAt(0, ContentEntity.class);
			entity.setId(UUID.randomUUID().toString());
			return entity;
		});

		dao = new ContentEntityDao(databaseIdManager, mimeTypeResolver, dbDao, idResolver, contentDigestProvider, transactionSupport);
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

		List<ContentEntity> results = dao.getContentsForInstance("instnace-id",
				Collections.singleton("purpose-to-skip"));
		assertEquals(1, results.size());
		assertEquals("content-purpose", results.get(0).getPurpose());
	}

	@Test
	public void assignContentToInstance_daoCalledWithCorrectArgs_unsuccessful() {
		assignContentToInstanceInternal(0);
	}

	@Test
	public void assignContentToInstance_daoCalledWithCorrectArgs_successful() {
		assignContentToInstanceInternal(1);
	}

	@SuppressWarnings("unchecked")
	private void assignContentToInstanceInternal(int successfulUpdate) {
		when(dbDao.executeUpdate(anyString(), anyList())).thenReturn(successfulUpdate);
		dao.assignContentToInstance("content-id", "instance-id", 4);
		verify(dbDao).executeUpdate(eq(ContentEntity.ASSIGN_CONTENT_TO_INSTANCE_KEY),
				argThat(CustomMatcher.of((List<Pair<String, Object>> list) -> {
					assertEquals(list.get(0).getSecond(), "instance-id");
					assertEquals(list.get(1).getSecond(), "content-id");
					assertEquals(list.get(2).getSecond(), 4);
				})));
	}

	@Test
	public void detectMimeTypeFromContent_nullMimeType_withoutContent_returnDefaultMimeType() {
		Content content = Content.createEmpty().setName("test-content.ext");
		when(mimeTypeResolver.resolveFromName("test-content.ext")).thenReturn(null);
		assertEquals(MediaType.APPLICATION_OCTET_STREAM, dao.detectMimeTypeFromContent(content));
	}

	@Test
	public void detectMimeTypeFromContent_emptyMimeType_withoutContent_returnDefaultMimeType() {
		Content content = Content.createEmpty().setName("test-content.ext");
		when(mimeTypeResolver.resolveFromName("test-content.ext")).thenReturn("");
		assertEquals(MediaType.APPLICATION_OCTET_STREAM, dao.detectMimeTypeFromContent(content));
	}

	@Test
	public void detectMimeTypeFromContent_invalidMimeType_withoutContent_returnDefaultMimeType() {
		Content content = Content.createEmpty().setName("test-content.ext");
		when(mimeTypeResolver.resolveFromName("test-content.ext")).thenReturn("application/unknown");
		assertEquals(MediaType.APPLICATION_OCTET_STREAM, dao.detectMimeTypeFromContent(content));
	}

	@Test
	public void detectMimeTypeFromContent_nullMimeType_withContent_mimeTypeDetectedFromContent() {
		String fileName = "test-content.ext";
		Content content = Content.createEmpty().setName(fileName).setContent("content".getBytes());
		when(mimeTypeResolver.resolveFromName(fileName)).thenReturn(null);
		when(mimeTypeResolver.getMimeType(any(InputStream.class), Matchers.eq(fileName)))
				.thenReturn(MediaType.TEXT_PLAIN);
		assertEquals(MediaType.TEXT_PLAIN, dao.detectMimeTypeFromContent(content));
	}

	@Test
	public void detectMimeTypeFromContent_withMimeType_withContent_shouldDetectFromContent() {
		String fileName = "test-content.ext";
		Content content = Content
				.createEmpty()
					.setName(fileName)
					.setContent("content".getBytes())
					.setDetectedMimeTypeFromContent(true);
		when(mimeTypeResolver.resolveFromName(fileName)).thenReturn(MediaType.TEXT_PLAIN);
		when(mimeTypeResolver.getMimeType(any(InputStream.class), Matchers.eq(fileName)))
				.thenReturn(MediaType.TEXT_PLAIN);
		assertEquals(MediaType.TEXT_PLAIN, dao.detectMimeTypeFromContent(content));
	}

	@Test
	public void detectMimeTypeFromContent_withMimeType() {
		Content content = Content.createEmpty().setName("test-content.ext").setContent("content".getBytes());
		when(mimeTypeResolver.resolveFromName("test-content.ext")).thenReturn(MediaType.TEXT_PLAIN);
		assertEquals(MediaType.TEXT_PLAIN, dao.detectMimeTypeFromContent(content));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getOrCreateEntity_newEntity_generatedContentId() {
		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE_KEY), anyList()))
				.thenReturn(emptyList());
		ContentEntity result = dao.getOrCreateEntity("instance-id", Content.createEmpty().setName("file-name"));
		assertNotNull(result);
		assertNotNull(result.getId());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getOrCreateEntity_versionalbleEntity_contentIdPreSet() {
		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE_KEY), anyList()))
				.thenReturn(Collections.singletonList(new ContentEntity()));
		Content content = Content.createEmpty().setContentId("content-id").setVersionable(true).setName("file-name");
		ContentEntity result = dao.getOrCreateEntity("instance-id", content);
		assertNotNull(result);
		assertEquals("content-id", result.getId());
		assertEquals(1, result.getVersion());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getOrCreateEntity_notVersionalbleEntity() {
		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE_KEY), anyList()))
				.thenReturn(Collections.singletonList(new ContentEntity()));
		Content content = Content.createEmpty().setContentId("content-id").setName("file-name");
		ContentEntity result = dao.getOrCreateEntity("instance-id", content);
		assertNotNull(result);
		assertEquals(0, result.getVersion());
	}

	@Test
	public void getUniqueContent_entityNotFound() {
		when(contentDigestProvider.digest(any(Content.class))).thenReturn(null);
		ContentEntity result = dao.getUniqueContent("instance-id", Content.createEmpty().setName("file-name"));
		assertNotNull(result);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getUniqueContent_entityFound() {
		when(contentDigestProvider.digest(any(Content.class))).thenReturn("digest-value");
		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_CONTENT_BY_CHECKSUM_KEY), anyList()))
				.thenReturn(Collections.singletonList(new ContentEntity()));
		ContentEntity result = dao.getUniqueContent("instance-id", Content.createEmpty().setName("file-name"));
		assertNotNull(result);
	}

	@Test
	public void importEntity_withMimeType() {
		ContentEntity result = dao
				.importEntity(ContentImport.createEmpty().setMimeType("mime-type").setName("file-name"));
		assertNotNull(result);
	}

}
