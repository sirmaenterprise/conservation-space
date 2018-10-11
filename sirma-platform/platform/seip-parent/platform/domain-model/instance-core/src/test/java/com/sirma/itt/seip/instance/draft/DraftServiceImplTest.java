package com.sirma.itt.seip.instance.draft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Tests for {@link DraftServiceImpl}.
 *
 * @author A. Kunchev
 */
public class DraftServiceImplTest {

	@InjectMocks
	private DraftServiceImpl service;

	@Mock
	private DbDao dao;

	@Mock
	private ResourceService resourceService;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private SecurityContext securityContext;

	@Before
	public void setUp() {
		service = new DraftServiceImpl();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getDraft_nullInstance_null() {
		assertNull(service.getDraft(null, "user-id"));
	}

	@Test
	public void getDraft_noDraftFound_null() {
		when(dao.fetchWithNamed(anyString(), anyListOf(Pair.class))).thenReturn(Collections.emptyList());
		assertNull(service.getDraft("instance-id", "user-id"));
	}

	@Test
	public void getDraft_draftFound_DraftInstance() {
		when(dao.fetchWithNamed(anyString(), anyListOf(Pair.class)))
				.thenReturn(Arrays.asList(prepareDraftEntity("id-1", "content")));

		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		when(instanceContentService.getContent(anyString(), any())).thenReturn(contentInfo);

		DraftInstance draft = service.getDraft("id-1", "user-id");
		assertNotNull(draft);
		assertEquals("content", draft.getDraftContentId());
		assertEquals("id-1", draft.getInstanceId());
	}

	@Test
	public void create_nullInstanceId_null() {
		when(securityContext.getAuthenticated()).thenReturn(new EmfUser("user-id"));
		assertNull(service.create(null, "user-id", ""));
	}

	@Test
	public void create_emptyInstanceId_null() {
		assertNull(service.create("", "user-id", ""));
	}

	@Test
	public void create_nullUserId_null() {
		when(securityContext.getAuthenticated()).thenReturn(new EmfUser());
		assertNull(service.create("instance-id", null, ""));
	}

	@Test
	public void create_emptyUserId_null() {
		when(securityContext.getAuthenticated()).thenReturn(new EmfUser(""));
		assertNull(service.create("instance-id", "", ""));
	}

	@Test
	public void create_bothParametersNull_null() {
		when(securityContext.getAuthenticated()).thenReturn(new EmfUser());
		assertNull(service.create(null, null, ""));
	}

	@Test
	public void create_withCorrectData_createdDraftInstance() {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.getContentId()).thenReturn("content-id");
		when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		when(instanceContentService.saveContent(any(Instance.class), any(Content.class))).thenReturn(contentInfo);
		service.create("instance-id", "user-id", "");
		verify(dao).saveOrUpdate(any(DraftEntity.class));
	}

	@Test(expected = EmfRuntimeException.class)
	public void deleteDraft_nullInstance() {
		service.delete(null, "user-id");
	}

	@Test(expected = EmfRuntimeException.class)
	public void deleteDraft_nullUser() {
		when(securityContext.getAuthenticated()).thenReturn(new EmfUser());
		service.delete("instance-id", null);
	}

	@Test(expected = EmfRuntimeException.class)
	public void deleteDraft_bothParametersNull() {
		when(securityContext.getAuthenticated()).thenReturn(new EmfUser());
		service.delete(null, null);
	}

	@Test
	public void deleteDraft_noDraftFound() {
		when(dao.fetchWithNamed(anyString(), anyListOf(Pair.class))).thenReturn(Collections.emptyList());
		assertNull(service.delete("instance-id", "user-id"));
		verify(dao, never()).delete(eq(DraftEntity.class), any(DraftEntityId.class));
	}

	@Test
	public void deleteDraft_oneDeleted() {
		when(dao.fetchWithNamed(anyString(), anyListOf(Pair.class)))
				.thenReturn(Arrays.asList(prepareDraftEntity("deleted-draft-id", "deleted-draft-content")));
		when(dao.delete(eq(DraftEntity.class), any(DraftEntityId.class))).thenReturn(1);
		assertNotNull(service.delete("instance-id", "user-id"));
		verify(dao).delete(eq(DraftEntity.class), any(DraftEntityId.class));
		verify(instanceContentService).deleteContent(eq("deleted-draft-id"), eq("draft-user-id"), eq(1),
				eq(TimeUnit.SECONDS));
	}

	@Test
	public void deleteDrafts_nullInstance_null() {
		assertEquals(Collections.emptyList(), service.delete(null));
	}

	@Test
	public void deleteDrafts_deletedInstance() {
		when(dao.fetchWithNamed(anyString(), anyListOf(Pair.class)))
				.thenReturn(Arrays.asList(prepareDraftEntity("deleted-draft-id", "deleted-draft-content")));
		assertNotNull(service.delete("instance-id"));
		verify(dao, times(1)).delete(eq(DraftEntity.class), any(DraftEntityId.class));
	}

	private static DraftEntity prepareDraftEntity(String id, String contentId) {
		DraftEntityId entityId = new DraftEntityId();
		entityId.setUserId("user-id");
		entityId.setInstanceId(id);

		DraftEntity entity = new DraftEntity();
		entity.setContentId(contentId);
		entity.setId(entityId);
		return entity;
	}

}
