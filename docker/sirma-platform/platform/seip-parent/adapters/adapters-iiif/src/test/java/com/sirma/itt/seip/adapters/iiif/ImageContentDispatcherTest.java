package com.sirma.itt.seip.adapters.iiif;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.sep.content.Content;
import com.srima.itt.seip.adapters.mock.ImageServerConfigurationsMock;

/**
 * Tests for {@link ImageContentDispatcher}.
 *
 * @author BBonev
 */
public class ImageContentDispatcherTest {

	@InjectMocks
	private ImageContentDispatcher dispatcher = new ImageContentDispatcher();

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Spy
	private ImageServerConfigurationsMock configMock = new ImageServerConfigurationsMock();

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		configMock.setEnabled(true);

		when(semanticDefinitionService.getHierarchy(anyString()))
				.then(a -> Arrays.asList(a.getArgumentAt(0, String.class)));
	}

	@Test
	public void notEnabled() throws Exception {
		configMock.setEnabled(false);
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.add(DefaultProperties.MIMETYPE, "image/jpep");

		Assert.assertNull(dispatcher.getContentManagementSystem(instance, Content.createEmpty()));
	}

	@Test
	public void imageDispatching() throws Exception {
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.add(DefaultProperties.MIMETYPE, "image/jpep");

		Assert.assertEquals(dispatcher.getContentManagementSystem(instance, Content.createEmpty()),
				IiifImageContentStore.STORE_NAME);

		instance.add(DefaultProperties.MIMETYPE, "text/html");

		Assert.assertNull(dispatcher.getContentManagementSystem(instance, Content.createEmpty()));
	}

	@Test
	public void imageDispatching_ByClass() throws Exception {
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.add(DefaultProperties.MIMETYPE, "application/pdf");
		instance.add(DefaultProperties.SEMANTIC_TYPE, "emf:Image");

		Assert.assertEquals(dispatcher.getContentManagementSystem(instance, Content.createEmpty()),
				IiifImageContentStore.STORE_NAME);
	}

	@Test(dataProvider = "mimeTypes")
	public void mimeTypeCompatibility(String mimetype) {
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.add(DefaultProperties.MIMETYPE, mimetype);

		Assert.assertEquals(dispatcher.getContentManagementSystem(instance, Content.createEmpty()),
				IiifImageContentStore.STORE_NAME);
	}

	@DataProvider(name = "mimeTypes")
	public Object[][] mimeTypeProvider() {
		return new Object[][] { { "image/vnd.dxf" }, { "image/bmp" }, { "image/prs.btif" },
				{ "image/vnd.dvb.subtitle" }, { "image/x-cmu-raster" }, { "image/cgm" }, { "image/x-cmx" },
				{ "image/vnd.dece.graphic" }, { "image/vnd.djvu" }, { "image/vnd.dwg" },
				{ "image/vnd.fujixerox.edmics-mmr" }, { "image/vnd.fujixerox.edmics-rlc" }, { "image/vnd.xiff" },
				{ "image/vnd.fst" }, { "image/vnd.fastbidsheet" }, { "image/vnd.fpx" }, { "image/vnd.net-fpx" },
				{ "image/x-freehand" }, { "image/g3fax" }, { "image/gif" }, { "image/x-icon" }, { "image/ief" },
				{ "image/jpeg" }, { "image/vnd.ms-modi" }, { "image/ktx" }, { "image/x-pcx" },
				{ "image/vnd.adobe.photoshop" }, { "image/x-pict" }, { "image/x-portable-anymap" },
				{ "image/x-portable-bitmap" }, { "image/x-portable-graymap" }, { "image/png" },
				{ "image/x-portable-pixmap" }, { "image/svg" }, { "image/x-rgb" }, { "image/tiff" },
				{ "image/vnd.wap.wbmp" }, { "image/webp" }, { "image/x-xbitmap" }, { "image/x-xpixmap" },
				{ "image/x-xwindowdump" } };
	}

}
