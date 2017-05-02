package com.sirma.itt.emf.notification.script;

import javax.enterprise.inject.Instance;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.instance.notification.NotificationMessage;
import com.sirma.itt.seip.instance.notification.NotificationSupport;

/**
 * UT for {@link NotificationScriptProvider}
 *
 * @author yyordanov
 */
@Test
public class NotificationScriptProviderTest {

	@InjectMocks
	private NotificationScriptProvider notificationScriptProvider;

	private final String LEVEL = "info";

	private NotificationMessage message;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private Instance<NotificationSupport> instance;

	@Mock
	private NotificationSupport notificationSupport;

	/**
	 * Test init
	 */
	@BeforeTest
	public void init() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(labelProvider.getValue(Mockito.anyString())).thenReturn("A label");
		Mockito.when(instance.get()).thenReturn(notificationSupport);
	}

	/**
	 * UT for {@link NotificationScriptProvider#notifyUser(String, String, String)}
	 */
	public void testNotifyUserWithLocalizedMessage() {
		notificationScriptProvider.notifyUser(LEVEL, "some.label", "some message");
		Mockito.verify(instance.get()).addMessage(Mockito.any());
	}

}
