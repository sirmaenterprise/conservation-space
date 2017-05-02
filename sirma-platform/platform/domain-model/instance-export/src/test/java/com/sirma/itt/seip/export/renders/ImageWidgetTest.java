package com.sirma.itt.seip.export.renders;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.google.gson.JsonParser;
import com.sirma.itt.seip.adapters.iiif.ImageServerConfigurations;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.annotations.AnnotationSearchRequest;
import com.sirma.itt.seip.annotations.AnnotationService;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.ContentMetadata;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.export.renders.utils.JsoupUtil;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.search.converters.JsonToDateRangeConverter;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;
import com.sirmaenterprise.sep.content.idoc.nodes.WidgetNode;
import com.sirmaenterprise.sep.export.services.HtmlTableAnnotationService;

/**
 * Tests for ImageWidget.
 *
 * @author Hristo Lungov
 */
@SuppressWarnings("static-method")
public class ImageWidgetTest {

	private static final String TEST_JPG = "test.jpg";
	private static final String TEST_FILENAME = "testFilename";
	private static final String INSTANCE_TEST_ID = "instanceTestId";
	private static final String WARN_MESSAGE_NO_IMAGES_FOUND = "No images selected.";

	private static final String IMAGE_WIDGET_GALLERY_MODE = "image-widget-gallery-mode.json";
	private static final String IMAGE_WIDGET_BOOK_MODE = "image-widget-book-mode.json";
	private static final String IMAGE_WIDGET_BOOK_MODE_WITH_SELECTED_IMAGE = "image-widget-book-mode-with-selected-image.json";
	private static final String IMAGE_WIDGET_BOOK_MODE_WITH_SELECTED_IMAGE_WITHOUT_CONTENT = "image-widget-book-mode-selected-image-without-content.json";
	private static final String IMAGE_WIDGET_SCROLL_MODE = "image-widget-scroll-mode.json";
	private static final String IMAGE_WIDGET_IMAGE_MODE = "image-widget-image-mode.json";
	private static final String IMAGE_WIDGET_IMAGE_MODE_WITHOUT_FILTER = "image-widget-image-mode-without-filter.json";
	private static final String IMAGE_WIDGET_NO_IMAGES_FOUND = "image-widget-no-images-found.json";
	private static final String ANNOTATION_CONTENT = "{\"on\":{\"selector\":{\"@type\":\"oa:SvgSelector\",\"value\":\"<svg xmlns=\\\"http://www.w3.org/2000/svg\\\"><path xmlns=\\\"http://www.w3.org/2000/svg\\\" d=\\\"M56.57257,58.32903c4.00819,-2.65204 9.66238,-4.62692 15.34398,-4.62456l0,0l0,0c5.68159,0.00236 10.63122,1.84983 15.34398,4.62456c4.71276,2.77473 6.54455,7.00288 6.35568,11.16468c-0.18886,4.16179 -2.39838,8.25723 -6.35568,11.16468c-3.9573,2.90745 -9.66238,4.62692 -15.34398,4.62456c-5.68159,-0.00236 -11.3397,-1.72655 -15.34398,-4.62456c-4.00427,-2.89801 -6.3547,-6.96985 -6.35568,-11.16468c-0.00098,-4.19483 2.3475,-8.51264 6.35568,-11.16468z\\\" data-paper-data=\\\"{&quot;defaultStrokeValue&quot;:1,&quot;editStrokeValue&quot;:5,&quot;currentStrokeValue&quot;:1,&quot;rotation&quot;:0,&quot;deleteIcon&quot;:null,&quot;rotationIcon&quot;:null,&quot;group&quot;:null,&quot;editable&quot;:true,&quot;annotation&quot;:null}\\\" id=\\\"ellipse_cd316c3e-0797-45fe-8d1c-1b09930c4255\\\" fill-opacity=\\\"0\\\" fill=\\\"#00bfff\\\" fill-rule=\\\"nonzero\\\" stroke=\\\"#00bfff\\\" stroke-width=\\\"0.59351\\\" stroke-linecap=\\\"butt\\\" stroke-linejoin=\\\"miter\\\" stroke-miterlimit=\\\"10\\\" stroke-dasharray=\\\"\\\" stroke-dashoffset=\\\"0\\\" font-family=\\\"sans-serif\\\" font-weight=\\\"normal\\\" font-size=\\\"12\\\" text-anchor=\\\"start\\\" style=\\\"mix-blend-mode: normal\\\"/></svg>\"}}}";
	private static final String BUGGED_ANNOTATION_CONTENT = "{\"on\":{\"selector\":{\"@type\":\"oa:SvgSelector\",\"value\":\"<svg xmlns=\\\"http://www.w3.org/2000/svg\\\"><path xmlns=\\\"http://www.w3.org/2000/svg\\\" d=\\\"M56.57257,58.32903c4.00819,-2.65204 9.66238,-4.62692 15.34398,-4.62456l0,0l0,0c5.68159,0.00236 10.63122,1.84983 15.34398,4.62456c4.71276,2.77473 6.54455,7.00288 6.35568,11.16468c-0.18886,4.16179 -2.39838,8.25723 -6.35568,11.16468c-3.9573,2.90745 -9.66238,4.62692 -15.34398,4.62456c-5.68159,-0.00236 -11.3397,-1.72655 -15.34398,-4.62456c-4.00427,-2.89801 -6.3547,-6.96985 -6.35568,-11.16468c-0.00098,-4.19483 2.3475,-8.51264 6.35568,-11.16468z\\\" data-paper-data=\\\"{&quot;defaultStrokeValue&quot;:1,&quot;editStrokeValue&quot;:5,&quot;currentStrokeValue&quot;:1,&quot;rotation&quot;:0,&quot;deleteIcon&quot;:null,&quot;rotationIcon&quot;:null,&quot;group&quot;:null,&quot;editable&quot;:true,&quot;annotation&quot;:null}\\\" id=\\\"ellipse_cd316c3e-0797-45fe-8d1c-1b09930c4255\\\" fill-opacity=\\\"0\\\" fill=\\\"#00bfff\\\" fill-rule=\\\"nonzero\\\" stroke=\\\"#00bfff\\\" stroke-width=\\\"0.59351\\\" stroke-linecap=\\\"butt\\\" stroke-linejoin=\\\"miter\\\" stroke-miterlimit=\\\"10\\\" stroke-dasharray=\\\"\\\" stroke-dashoffset=\\\"0\\\" font-family=\\\"sans-serif\\\" font-weight=\\\"normal\\\" font-size=\\\"12\\\" text-anchor=\\\"start\\\" style=\\\"mix-blend-mode: normal\\\"/></svg>\"}}}";

	@InjectMocks
	private ImageWidget imageWidget;

	@Mock
	private InstanceLoadDecorator instanceDecorator;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private ImageServerConfigurations imageServerConfigurations;

	@Mock
	private javax.enterprise.inject.Instance<RESTClient> restClient;

	@Mock
	private AnnotationService annotationService;

	@Mock
	private HtmlTableAnnotationService htmlTableAnnotationService;

	@Mock
	private InstanceTypeResolver instanceResolver;

	@Mock
	private JsonToDateRangeConverter jsonToDateRangeConverter;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Mock
	private LabelProvider labelProvider;

	/**
	 * Runs before each method and setup mockito.
	 *
	 * @throws URISyntaxException
	 */
	@BeforeMethod
	public void setup() throws URISyntaxException {
		MockitoAnnotations.initMocks(this);
		Mockito.when(imageServerConfigurations.getNoContentImageName()).thenReturn(new ConfigurationPropertyMock<>("nocontent"));
		Mockito.when(imageServerConfigurations.getIiifServerAddress()).thenReturn(new ConfigurationPropertyMock<>(new java.net.URI("test")));
		Mockito.when(imageServerConfigurations.getIiifServerAddressSuffix()).thenReturn(new ConfigurationPropertyMock<>("test"));
	}
	
	/**
	 * Test method getInvalidSVGPathNodeAttrNames.
	 *
	 * @param attributeName the name of attribute.
	 * @param attributeValue the value of attribute.
	 * @param isValid true if value have to be valid.
	 * @param errorMessage error message if test fail.
	 */
	@Test(dataProvider = "getInvalidSVGPathNodeAttrNamesDP")
	public void getInvalidSVGPathNodeAttrNamesTest(String attributeName, String attributeValue, boolean isValid, String errorMessage) {
		Node fillAttributeNotEmpty = Mockito.mock(Node.class);
		Mockito.when(fillAttributeNotEmpty.getNodeName()).thenReturn(attributeName);
		Mockito.when(fillAttributeNotEmpty.getNodeValue()).thenReturn(attributeValue);
		NamedNodeMap pathNodeAttributes = Mockito.mock(NamedNodeMap.class);
		Mockito.when(pathNodeAttributes.getLength()).thenReturn(1);
		Mockito.when(pathNodeAttributes.item(0)).thenReturn(fillAttributeNotEmpty);
		
		Set<String> invalidSVGPathNodeAttrNames = ImageWidget.getInvalidSVGPathNodeAttrNames(pathNodeAttributes);
		
		Assert.assertEquals(!invalidSVGPathNodeAttrNames.contains(attributeName), isValid, errorMessage);
	}
	
	/**
	 * Data provider for getInvalidSVGPathNodeAttrNamesTest.
	 *
	 * @return the data provider.
	 */
	@DataProvider
	public Object[][] getInvalidSVGPathNodeAttrNamesDP() {
		return new Object[][] {
			{"src", "non empty value", Boolean.TRUE, "Tests any attribute with non empty value."},
			{"src", "none", Boolean.FALSE, "Tests any attribute with \"none\" value."},
			{"src", "", Boolean.FALSE, "Tests any attribute with empty value."},
			{"fill", "non empty value", Boolean.TRUE, "Tests fill attribute with non empty value."},
			{"fill", "none", Boolean.TRUE, "Tests fill attribute with \"none\" value."},
			{"fill", "", Boolean.FALSE, "Tests fill attribute with empty value."}
			
		};
	}

	/**
	 * Accept method test.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void acceptTest() {
		WidgetNode widget = Mockito.mock(WidgetNode.class);
		Mockito.when(widget.isWidget()).thenReturn(false);
		Mockito.when(widget.getName()).thenReturn("");
		Mockito.when(imageServerConfigurations.isImageServerEnabled()).thenReturn(new ConfigurationPropertyMock<>(Boolean.FALSE));
		Assert.assertFalse(imageWidget.accept(widget));

		Mockito.when(widget.isWidget()).thenReturn(true);
		Mockito.when(widget.getName()).thenReturn("");
		Mockito.when(imageServerConfigurations.isImageServerEnabled()).thenReturn(new ConfigurationPropertyMock<>(Boolean.FALSE));
		Assert.assertFalse(imageWidget.accept(widget));

		Mockito.when(widget.isWidget()).thenReturn(false);
		Mockito.when(widget.getName()).thenReturn(ImageWidget.IMAGE_WIDGET_NAME);
		Mockito.when(imageServerConfigurations.isImageServerEnabled()).thenReturn(new ConfigurationPropertyMock<>(Boolean.FALSE));
		Assert.assertFalse(imageWidget.accept(widget));

		Mockito.when(widget.isWidget()).thenReturn(false);
		Mockito.when(widget.getName()).thenReturn("");
		Mockito.when(imageServerConfigurations.isImageServerEnabled()).thenReturn(new ConfigurationPropertyMock<>(Boolean.TRUE));
		Assert.assertFalse(imageWidget.accept(widget));

		Mockito.when(widget.isWidget()).thenReturn(true);
		Mockito.when(widget.getName()).thenReturn(ImageWidget.IMAGE_WIDGET_NAME);
		Mockito.when(imageServerConfigurations.isImageServerEnabled()).thenReturn(new ConfigurationPropertyMock<>(Boolean.FALSE));
		Assert.assertFalse(imageWidget.accept(widget));

		Mockito.when(widget.isWidget()).thenReturn(false);
		Mockito.when(widget.getName()).thenReturn(ImageWidget.IMAGE_WIDGET_NAME);
		Mockito.when(imageServerConfigurations.isImageServerEnabled()).thenReturn(new ConfigurationPropertyMock<>(Boolean.TRUE));
		Assert.assertFalse(imageWidget.accept(widget));

		Mockito.when(widget.isWidget()).thenReturn(true);
		Mockito.when(widget.getName()).thenReturn("");
		Mockito.when(imageServerConfigurations.isImageServerEnabled()).thenReturn(new ConfigurationPropertyMock<>(Boolean.TRUE));
		Assert.assertFalse(imageWidget.accept(widget));

		Mockito.when(widget.isWidget()).thenReturn(true);
		Mockito.when(widget.getName()).thenReturn(ImageWidget.IMAGE_WIDGET_NAME);
		Mockito.when(imageServerConfigurations.isImageServerEnabled()).thenReturn(new ConfigurationPropertyMock<>(Boolean.TRUE));
		Assert.assertTrue(imageWidget.accept(widget));
	}

	/**
	 * Load test resource.
	 *
	 * @param resource
	 *            the resource
	 * @return the com.google.gson. json object
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private com.google.gson.JsonObject loadTestResource(String resource) throws URISyntaxException, IOException {
		URL testJsonURL = getClass().getClassLoader().getResource(resource);
		File jsonConfiguration = new File(testJsonURL.toURI());
		try (FileReader fileReader = new FileReader(jsonConfiguration)) {
			return new JsonParser().parse(fileReader).getAsJsonObject();
		}
	}

	/**
	 * Render no images found test.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void renderNoImagesFoundTest() throws URISyntaxException, IOException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(IMAGE_WIDGET_NO_IMAGES_FOUND);
		WidgetNode widget = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		Mockito.when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));
		when(labelProvider.getLabel(IdocRenderer.KEY_LABEL_NO_IMAGES_SELECTED)).thenReturn(WARN_MESSAGE_NO_IMAGES_FOUND);
		Element table = imageWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements tableTitleRow = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(tableTitleRow.text(), WARN_MESSAGE_NO_IMAGES_FOUND);
	}

	/**
	 * Render gallery mode test.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws DMSClientException
	 *             the DMS client exception
	 */
	@Test
	public void renderGalleryModeTest() throws URISyntaxException, IOException, DMSClientException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(IMAGE_WIDGET_GALLERY_MODE);
		WidgetNode widget = testImageWidget(loadedTestResource);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		Element table = imageWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements imageDT = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(Integer.valueOf(imageDT.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 1);
		Elements imageTag = table.select("tr:eq(0) > td:eq(0) img");
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_ALT), TEST_FILENAME);
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_TITLE), TEST_FILENAME);
	}

	/**
	 * Render scroll mode test.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws DMSClientException
	 *             the DMS client exception
	 */
	@Test
	public void renderScrollModeTest() throws URISyntaxException, IOException, DMSClientException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(IMAGE_WIDGET_SCROLL_MODE);
		WidgetNode widget = testImageWidget(loadedTestResource);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		Element table = imageWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements imageDT = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(Integer.valueOf(imageDT.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 1);
		Elements imageTag = table.select("tr:eq(0) > td:eq(0) img");
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_ALT), TEST_FILENAME);
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_TITLE), TEST_FILENAME);
	}

	/**
	 * Render book mode test scenario without selected image.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws DMSClientException
	 *             the DMS client exception
	 */
	@Test
	public void renderBookModeWithoutSelectedImageTest() throws URISyntaxException, IOException, DMSClientException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(IMAGE_WIDGET_BOOK_MODE);
		WidgetNode widget = testImageWidget(loadedTestResource);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		Element table = imageWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements imageDT = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(Integer.valueOf(imageDT.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 1);
		Elements imageTag = table.select("tr:eq(0) > td:eq(0) img");
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_ALT), TEST_FILENAME);
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_TITLE), TEST_FILENAME);
	}

	/**
	 * Render book mode test scenario with selected image.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws DMSClientException
	 *             the DMS client exception
	 */
	@Test
	public void renderBookModeWithSelectedImageTest() throws URISyntaxException, IOException, DMSClientException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(IMAGE_WIDGET_BOOK_MODE_WITH_SELECTED_IMAGE);
		WidgetNode widget = testImageWidget(loadedTestResource);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);

		Element table = imageWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements imageDT = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(Integer.valueOf(imageDT.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 2);
		Elements imageTag = table.select("tr:eq(0) > td:eq(0) img");
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_ALT), TEST_FILENAME);
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_TITLE), TEST_FILENAME);
	}

	/**
	 * Render book mode test scenario with selected image which have not content.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws DMSClientException
	 *             the DMS client exception
	 */
	@Test
	public void renderBookModeWithSelectedImageWithoutContentTest() throws URISyntaxException, IOException, DMSClientException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(IMAGE_WIDGET_BOOK_MODE_WITH_SELECTED_IMAGE_WITHOUT_CONTENT);
		WidgetNode widget = testImageWidget(loadedTestResource);

		InstanceReference instance = Mockito.mock(InstanceReference.class);
		Mockito.when(instance.getIdentifier()).thenReturn("emf:64c201d2-dfd1-49e2-930a-87be0288a33a");
		Mockito.when(instanceResolver.resolveReferences(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.exists()).thenReturn(false);
		Mockito.when(instanceContentService.getContent("emf:64c201d2-dfd1-49e2-930a-87be0288a33a", Content.PRIMARY_CONTENT)).thenReturn(contentInfo);

		Element table = imageWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Assert.assertTrue(table.children().isEmpty());
	}

	/**
	 * Render image mode without annontations test.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws DMSClientException
	 *             the DMS client exception
	 */
	@Test
	public void renderImageModeWithoutAnnontationsTest() throws URISyntaxException, IOException, DMSClientException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(IMAGE_WIDGET_IMAGE_MODE);
		WidgetNode widget = testImageWidget(loadedTestResource);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		Element table = imageWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements imageDT = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(Integer.valueOf(imageDT.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 6);
		Elements imageTag = table.select("tr:eq(0) > td:eq(0) img");
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_ALT), TEST_FILENAME);
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_TITLE), TEST_FILENAME);
	}

	/**
	 * Render image mode with annontations test.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws DMSClientException
	 *             the DMS client exception
	 */
	@Test
	public void renderImageModeWithAnnontationsTest() throws URISyntaxException, IOException, DMSClientException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(IMAGE_WIDGET_IMAGE_MODE);
		Annotation annotation = Mockito.mock(Annotation.class);
		Mockito.when(annotation.getContent()).thenReturn(ANNOTATION_CONTENT);
		List<Annotation> imageAnnotations = Arrays.asList(annotation);
		Mockito.when(annotationService.searchAnnotations(Matchers.any(AnnotationSearchRequest.class))).thenReturn(imageAnnotations);
		WidgetNode widget = testImageWidget(loadedTestResource);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		Element table = imageWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements imageDT = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(Integer.valueOf(imageDT.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 6);
		Elements imageTag = table.select("tr:eq(0) > td:eq(0) img");
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_ALT), TEST_FILENAME);
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_TITLE), TEST_FILENAME);
	}

	/**
	 * Render image mode with annontations without filter test.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws DMSClientException
	 *             the DMS client exception
	 */
	@Test
	@SuppressWarnings({ "boxing", "unchecked" })
	public void renderImageModeWithAnnontationsWithoutFilterTest() throws URISyntaxException, IOException, DMSClientException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(IMAGE_WIDGET_IMAGE_MODE_WITHOUT_FILTER);
		Annotation annotation = Mockito.mock(Annotation.class);
		Mockito.when(annotation.getContent()).thenReturn(BUGGED_ANNOTATION_CONTENT);
		List<Annotation> imageAnnotations = Arrays.asList(annotation);
		Mockito.when(annotationService.searchAnnotations(Matchers.any(AnnotationSearchRequest.class))).thenReturn(imageAnnotations);
		WidgetNode widget = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		Mockito.when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getId()).thenReturn(INSTANCE_TEST_ID);
		Mockito.when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		ContentMetadata contentMetadata = Mockito.mock(ContentMetadata.class);
		Mockito.when(contentInfo.getMetadata()).thenReturn(contentMetadata);
		Mockito.when(contentInfo.getName()).thenReturn(TEST_FILENAME);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(contentMetadata.getInt(ImageWidget.CONFIGURATON_PROPERTY_WIDTH)).thenReturn(Integer.valueOf(200));
		Mockito.when(contentMetadata.getInt(ImageWidget.CONFIGURATON_PROPERTY_HEIGHT)).thenReturn(Integer.valueOf(200));
		Mockito.when(instanceContentService.getContent(Matchers.anyString(), Matchers.anyString())).thenReturn(contentInfo);

		RESTClient mockRestClient = Mockito.mock(RESTClient.class);
		Mockito.when(restClient.get()).thenReturn(mockRestClient);
		HttpMethod response = Mockito.mock(HttpMethod.class);
		Mockito.when(mockRestClient.rawRequest(Matchers.any(HttpMethod.class), Matchers.any(URI.class))).thenReturn(response);
		Mockito.when(response.getResponseBodyAsStream()).thenReturn(getClass().getClassLoader().getResourceAsStream(TEST_JPG));

		Element table = imageWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Elements imageDT = table.select("tr:eq(0) > td:eq(0)");
		Assert.assertEquals(Integer.valueOf(imageDT.attr(JsoupUtil.ATTRIBUTE_COLSPAN)).intValue(), 6);
		Elements imageTag = table.select("tr:eq(0) > td:eq(0) img");
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_ALT), TEST_FILENAME);
		Assert.assertEquals(imageTag.attr(JsoupUtil.ATTRIBUTE_TITLE), TEST_FILENAME);
	}

	/**
	 * Exception when loading image test.
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	@SuppressWarnings({ "boxing", "unchecked" })
	public void exceptionWhenLoadingImageTest() throws URISyntaxException, IOException {
		com.google.gson.JsonObject loadedTestResource = loadTestResource(IMAGE_WIDGET_IMAGE_MODE);
		WidgetNode widget = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		Mockito.when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getId()).thenReturn(INSTANCE_TEST_ID);
		Mockito.when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		ContentMetadata contentMetadata = Mockito.mock(ContentMetadata.class);
		Mockito.when(contentInfo.getMetadata()).thenReturn(contentMetadata);
		Mockito.when(contentInfo.getName()).thenReturn(TEST_FILENAME);
		Mockito.when(contentMetadata.getInt(ImageWidget.CONFIGURATON_PROPERTY_WIDTH)).thenReturn(Integer.valueOf(200));
		Mockito.when(contentMetadata.getInt(ImageWidget.CONFIGURATON_PROPERTY_HEIGHT)).thenReturn(Integer.valueOf(200));
		Mockito.when(instanceContentService.getContent(Matchers.anyString(), Matchers.anyString())).thenReturn(contentInfo);

		RESTClient mockRestClient = Mockito.mock(RESTClient.class);
		Mockito.when(restClient.get()).thenReturn(mockRestClient);

		Element table = imageWidget.render(INSTANCE_TEST_ID, widget);
		Assert.assertEquals(table.tagName(), JsoupUtil.TAG_TABLE);
		Assert.assertTrue(table.html().length() == 0);
	}

	/**
	 * Test image widget.
	 *
	 * @param loadedTestResource
	 *            the loaded test resource
	 * @return the widget node
	 * @throws DMSClientException
	 *             the DMS client exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings({ "boxing", "unchecked" })
	private WidgetNode testImageWidget(com.google.gson.JsonObject loadedTestResource) throws DMSClientException, IOException {
		WidgetNode widget = Mockito.mock(WidgetNode.class);

		Element parentOfNode = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
		
		Mockito.when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, loadedTestResource));
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getId()).thenReturn(INSTANCE_TEST_ID);
		Mockito.when(instanceResolver.resolveInstances(Matchers.anyCollection())).thenReturn(Arrays.asList(instance));
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		ContentMetadata contentMetadata = Mockito.mock(ContentMetadata.class);
		Mockito.when(contentInfo.getMetadata()).thenReturn(contentMetadata);
		Mockito.when(contentInfo.getName()).thenReturn(TEST_FILENAME);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(contentMetadata.getInt(ImageWidget.CONFIGURATON_PROPERTY_WIDTH)).thenReturn(Integer.valueOf(200));
		Mockito.when(contentMetadata.getInt(ImageWidget.CONFIGURATON_PROPERTY_HEIGHT)).thenReturn(Integer.valueOf(200));
		Mockito.when(instanceContentService.getContent(Matchers.anyString(), Matchers.anyString())).thenReturn(contentInfo);

		RESTClient mockRestClient = Mockito.mock(RESTClient.class);
		Mockito.when(restClient.get()).thenReturn(mockRestClient);
		HttpMethod response = Mockito.mock(HttpMethod.class);
		Mockito.when(mockRestClient.rawRequest(Matchers.any(HttpMethod.class), Matchers.any(URI.class))).thenReturn(response);
		Mockito.when(response.getResponseBodyAsStream()).thenReturn(getClass().getClassLoader().getResourceAsStream(TEST_JPG));
		return widget;
	}

}
