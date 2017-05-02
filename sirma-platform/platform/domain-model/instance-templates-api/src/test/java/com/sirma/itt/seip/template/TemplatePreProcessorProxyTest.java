package com.sirma.itt.seip.template;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.plugin.Plugins;

/**
 * Test for {@link TemplatePreProcessorProxy}
 *
 * @author BBonev
 */
public class TemplatePreProcessorProxyTest {

	@InjectMocks
	private TemplatePreProcessorProxy proxy;

	@Mock
	private TemplatePreProcessor preProcessor;

	private List<TemplatePreProcessor> list = new ArrayList<>();
	@Spy
	private Plugins<TemplatePreProcessor> plugins = new Plugins<>("", list);

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		list.clear();
		list.add(preProcessor);
	}

	@Test
	public void proxyShouldCallPlugins() throws Exception {
		proxy.process(new TemplateContext(null));

		verify(preProcessor).process(any(TemplateContext.class));
	}
}
