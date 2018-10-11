package com.sirma.sep.content.jms;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.jms.Message;

import com.sirma.itt.seip.security.context.ContextualExecutor;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.sep.content.ContentEntity;
import com.sirma.sep.content.ContentEntityDao;
import com.sirma.sep.content.ContentStore;
import com.sirma.sep.content.ContentStoreProvider;
import com.sirma.sep.content.DeleteContentData;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link DeleteContentQueueHandler}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 02/01/2018
 */
public class DeleteContentQueueHandlerTest {
	private static final String STORE_NAME = "correctStoreName";

	@InjectMocks
	private DeleteContentQueueHandler handler;

	@Mock
	private ContentStoreProvider storeProvider;
	@Mock
	private ContentStore contentStore;
	@Mock
	private ContentEntityDao contentEntityDao;
	@Mock
	private Message message;

	@Mock
	private SecurityContextManager securityContextManager;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(storeProvider.findStore(anyString())).thenReturn(Optional.of(contentStore));
		when(contentStore.getName()).thenReturn(STORE_NAME);

		ContextualExecutor contextualExecutor = new ContextualExecutor.NoContextualExecutor();
		when(securityContextManager.executeAsTenant(any(String.class))).thenReturn(contextualExecutor);
	}

	@Test
	public void onDeleteContent_should_notDeleteEntityOnDeleteError() throws Exception {
		when(contentEntityDao.getEntity(any(String.class), any(String.class))).thenThrow(new RuntimeException());

		DeleteContentData data = new DeleteContentData().setStoreName(STORE_NAME)
				.setContentId("contentId")
				.setTenantId("tenant.id")
				.addProperty("prop1", "value1");
		when(message.getBody(String.class)).thenReturn(data.asJsonString());
		handler.onDeleteContent(message);
		verify(contentEntityDao, never()).delete(any(ContentEntity.class));
	}

	@Test
	public void onDeleteContent_shouldNotDeleteEntityIfNotRequested() throws Exception {
		when(contentEntityDao.getEntity(any(String.class), any(String.class))).thenThrow(new RuntimeException());

		DeleteContentData data = new DeleteContentData().setStoreName(STORE_NAME)
				.setContentId("contentId")
				.setTenantId("tenant.id")
				.addProperty("prop1", "value1")
				.setContentOnly(true);
		when(message.getBody(String.class)).thenReturn(data.asJsonString());
		handler.onDeleteContent(message);
		verify(contentEntityDao, never()).delete(any(ContentEntity.class));
	}

	@Test
	public void onDeleteContent_shouldNotDeleteContentIfNotRequested() throws Exception {
		when(contentEntityDao.getEntity(any(String.class), any(String.class))).thenReturn(mock(ContentEntity.class));

		DeleteContentData data = new DeleteContentData().setStoreName(STORE_NAME)
				.setContentId("contentId")
				.setTenantId("tenant.id")
				.addProperty("prop1", "value1")
				.doNotDeleteContent();
		when(message.getBody(String.class)).thenReturn(data.asJsonString());

		handler.onDeleteContent(message);

		verify(contentStore, never()).delete(data);
		verify(contentEntityDao).delete(any(ContentEntity.class));
	}

	@Test
	public void onDeleteContent_shouldDeleteContentAndEntity() throws Exception {
		when(contentEntityDao.getEntity(any(String.class), any(String.class))).thenReturn(mock(ContentEntity.class));

		DeleteContentData data = new DeleteContentData().setStoreName(STORE_NAME)
				.setContentId("contentId")
				.setTenantId("tenant.id")
				.addProperty("prop1", "value1");
		when(message.getBody(String.class)).thenReturn(data.asJsonString());

		handler.onDeleteContent(message);

		verify(contentStore).delete(data);
		verify(contentEntityDao).delete(any(ContentEntity.class));
	}

	@Test(expected = IllegalStateException.class)
	public void onDeleteContent_shouldFailIfTheLoadedStoreIsNotTheSameAsRequested() throws Exception {
		when(storeProvider.findStore("someOtherStoreName")).thenReturn(Optional.empty());
		DeleteContentData data = new DeleteContentData().setStoreName("someOtherStoreName")
				.setContentId("contentId")
				.setTenantId("tenant.id")
				.addProperty("prop1", "value1");
		when(message.getBody(String.class)).thenReturn(data.asJsonString());

		handler.onDeleteContent(message);
	}

}
