package com.sirma.itt.seip.template.script;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.script.BaseInstanceScriptTest;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * The Class ContentScriptsProviderTest.
 *
 * @author BBonev
 */
@Test
public class ContentScriptsProviderTest extends BaseInstanceScriptTest {

	/** The template service. */
	@Mock
	private TemplateService templateService;

	@Spy
	private InstanceProxyMock<TemplateService> templateServiceProxy = new InstanceProxyMock<>(null);

	/** The scripts provider. */
	@InjectMocks
	private ContentScriptsProvider scriptsProvider;

	/** The template. */
	TemplateInstance template = new TemplateInstance();

	/**
	 * Before method.
	 */
	@BeforeMethod
	@Override
	public void beforeMethod() {
		super.beforeMethod();
		templateServiceProxy.add(templateService);
		template.getProperties().put(DefaultProperties.CONTENT, "template content");
		when(templateService.getTemplateWithContent(anyString())).thenReturn(template);
		scriptsProvider.initialize();
	}

	/**
	 * Provide bindings.
	 *
	 * @param bindingsExtensions
	 *            the bindings extensions
	 */
	@Override
	protected void provideBindings(List<GlobalBindingsExtension> bindingsExtensions) {
		super.provideBindings(bindingsExtensions);
		bindingsExtensions.add(scriptsProvider);
	}

	/**
	 * Sets the content from template_ js.
	 */
	public void setContentFromTemplate_JS() {
		EmfInstance instance = new EmfInstance();
		instance.setId("instance");
		instance.setProperties(new HashMap<String, Serializable>());
		eval("setContentFromTemplate(root, 'templateId')", instance);
		verify(templateService).getTemplateWithContent("templateId");
	}

}
