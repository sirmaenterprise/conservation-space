package com.sirmaenterprise.sep.jms.impl.receiver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;

import javax.jms.Message;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.plugin.Plugins;
import com.sirmaenterprise.sep.jms.api.MessageConsumerListener;

/**
 * Test for {@link ChainingMessageConsumerListener}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/06/2017
 */
public class ChainingMessageConsumerListenerTest {

	@InjectMocks
	private ChainingMessageConsumerListener chainingListener;

	@Mock
	private MessageConsumerListener plugin;

	private Collection<MessageConsumerListener> plugins = new ArrayList<>();
	@Spy
	private Plugins<MessageConsumerListener> listeners = new Plugins<>("", plugins);

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		plugins.clear();
		plugins.add(plugin);
	}

	@Test
	public void beforeMessage() throws Exception {
		chainingListener.beforeMessage(mock(Message.class));

		verify(plugin).beforeMessage(any());
	}

	@Test
	public void onSuccess() throws Exception {
		chainingListener.onSuccess();
		verify(plugin).onSuccess();
	}

	@Test
	public void onError() throws Exception {
		chainingListener.onError(new Exception());
		verify(plugin).onError(any());
	}

}
