package com.sirma.sep.content.jms;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.json.Json;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.ContentCorruptedException;
import com.sirma.sep.content.ContentStoreManagementService;

/**
 * Test for {@link ContentMigrationHandler}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 02/05/2018
 */
public class ContentMigrationHandlerTest {
	@InjectMocks
	private ContentMigrationHandler migrationHandler;
	@Mock
	private ContentStoreManagementService storeManagementService;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onMigrateContentEven_shouldCallActualContentMove() throws Exception {
		Message message = createMessage("emf:content-id", "localStore");
		migrationHandler.onMigrateContentEven(message);
		verify(storeManagementService).moveContent("emf:content-id", "localStore");
	}

	@Test
	public void onMigrateContentOdd_shouldCallActualContentMove() throws Exception {
		Message message = createMessage("emf:content-id", "localStore");
		migrationHandler.onMigrateContentOdd(message);
		verify(storeManagementService).moveContent("emf:content-id", "localStore");
	}

	@Test
	public void onMigrateContent_ShouldNotFailIfContentCorruptionDetected() throws Exception {
		doThrow(ContentCorruptedException.class).when(storeManagementService).moveContent("emf:content-id", "localStore");
		Message message = createMessage("emf:content-id", "localStore");
		migrationHandler.onMigrateContentEven(message);
		verify(storeManagementService).moveContent("emf:content-id", "localStore");
	}

	private Message createMessage(String contentId, String targetStore) throws JMSException {
		String messageBody = Json.createObjectBuilder()
				.add("contentId", contentId)
				.add("targetStore", targetStore)
				.build()
				.toString();
		Message message = mock(Message.class);
		when(message.getBody(String.class)).thenReturn(messageBody);
		return message;
	}
}
