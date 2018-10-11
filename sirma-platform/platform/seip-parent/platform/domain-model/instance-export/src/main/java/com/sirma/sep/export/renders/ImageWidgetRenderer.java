package com.sirma.sep.export.renders;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.docx4j.org.xhtmlrenderer.util.XMLUtil;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.jsonldjava.utils.JsonUtils;
import com.sirma.itt.seip.Quad;
import com.sirma.itt.seip.adapters.iiif.ImageServerConfigurations;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.annotations.AnnotationSearchRequest;
import com.sirma.itt.seip.annotations.AnnotationService;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentMetadata;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueImageBuilder;
import com.sirma.sep.export.renders.utils.ImageUtils;
import com.sirma.sep.export.services.HtmlTableAnnotationService;

/**
 * Represents the iDoc Image Widget.
 *
 * @author Hristo Lungov
 */
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 4)
public class ImageWidgetRenderer extends BaseRenderer {

	private static final String FULL_IMAGE_URL = "/full/full/0/default.jpg";

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageWidgetRenderer.class);

	private static final String ATTRIBUTE_VALUE_NONE = "none";
	private static final String ATTRIBUTE_NAME_FILL = "fill";
	public static final String CONFIGURATION_PROPERTY_WIDTH = "width";
	public static final String CONFIGURATION_PROPERTY_HEIGHT = "height";
	private static final String CONFIGURATION_PROPERTY_KEYWORD = "keyword";
	private static final String CONFIGURATION_PROPERTY_AUTHOR = "author";
	private static final String CONFIGURATION_PROPERTY_COMMENT_STATUS = "commentStatus";
	private static final String CONFIGURATION_PROPERTY_FROM_DATE = "fromDate";
	private static final String CONFIGURATION_PROPERTY_TO_DATE = "toDate";
	private static final String FILTERS = "filters";

	protected static final String IMAGE_WIDGET_NAME = "image-widget";

	/**
	 * This list contains which attributes can contain none value.
	 */
	private static final Set<String> VALID_ATTRIBUTES_WITH_NONE_VALUE = Collections.singleton(ATTRIBUTE_NAME_FILL);

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private ImageServerConfigurations imageServerConfigurations;

	@Inject
	private javax.enterprise.inject.Instance<RESTClient> restClient;

	@Inject
	private AnnotationService annotationService;

	@Inject
	private HtmlTableAnnotationService htmlTableAnnotationService;

	@Inject
	private DateConverter dateConverter;

	@Override
	public boolean accept(ContentNode node) {
		// verify that node is correct widget
		return imageServerConfigurations.isImageServerEnabled().get().booleanValue() && node.isWidget()
				&& IMAGE_WIDGET_NAME.equals(((Widget) node).getName());
	}

	@Override
	public Element render(String currentInstanceId, ContentNode node) {
		JsonObject jsonConfiguration = getWidgetConfiguration(node);
		HtmlTableBuilder tableBuilder = renderWidgetFrame(jsonConfiguration);

		String widgetTitle = IdocRenderer.getWidgetTitle(jsonConfiguration);
		String selectionMode = jsonConfiguration.getString(SELECT_OBJECT_MODE);
		setWidth(tableBuilder, node.getElement(), NIGHTY_NINE_PRECENT);
		if (IdocRenderer.AUTOMATICALLY.equalsIgnoreCase(selectionMode) && !hasSearchCriteria(jsonConfiguration)) {
			return buildOneRowMessageTable(tableBuilder, KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);
		}
		List<String> selectedInstances = getSelectedInstances(currentInstanceId, jsonConfiguration);
		if (selectedInstances.isEmpty()) {
			return buildOneRowMessageTable(tableBuilder, KEY_LABEL_NO_IMAGES_SELECTED);
		}
		JsonObject miradorCurrentConfig = jsonConfiguration.getJsonObject("miradorCurrentConfig");
		if (miradorCurrentConfig != null) {
			processesWithMiradorConfig(tableBuilder, jsonConfiguration, miradorCurrentConfig, selectedInstances,
					widgetTitle);
		} else {
			processesWithoutMiradorConfig(tableBuilder, jsonConfiguration, selectedInstances, widgetTitle);
		}
		return tableBuilder.build();
	}

	/**
	 * Processes with mirador configuration.
	 *
	 * @param tableBuilder
	 *            the table builder.
	 * @param jsonConfiguration
	 *            widget configuration.
	 * @param miradorCurrentConfig
	 *            the mirador configuration.
	 * @param selectedInstances
	 *            the selected instances
	 * @param widgetTitle
	 *            the widget title
	 */
	private void processesWithMiradorConfig(HtmlTableBuilder tableBuilder, JsonObject jsonConfiguration,
			JsonObject miradorCurrentConfig, List<String> selectedInstances, String widgetTitle) {
		// windowObjects are slots in image-widget
		JsonArray windowObjects = miradorCurrentConfig.getJsonArray("windowObjects");
		for (int i = 0; i < windowObjects.size(); i++) {
			JsonObject windowObject = windowObjects.getJsonObject(i);
			String canvasId = windowObject.getString("canvasID", "");
			if (selectedInstances.contains(canvasId) && StringUtils.isNotBlank(canvasId)
					&& !canvasId.equalsIgnoreCase(imageServerConfigurations.getNoContentImageName().get())) {
				drawToTable(tableBuilder, jsonConfiguration, widgetTitle, selectedInstances, windowObject, canvasId);
			} else {
				drawToTable(tableBuilder, jsonConfiguration, widgetTitle, selectedInstances, windowObject, null);
			}
		}
	}

	/**
	 * Processes widget without mirador configuration. If <code>selectedInstances</code>size is one image view will be
	 * used otherwise gallery.
	 *
	 * @param tableBuilder
	 *            the table builder
	 * @param jsonConfiguration
	 *            widget configuration
	 * @param selectedInstances
	 *            the selected instances
	 * @param widgetTitle
	 *            title of widget
	 */
	private void processesWithoutMiradorConfig(HtmlTableBuilder tableBuilder, JsonObject jsonConfiguration,
			List<String> selectedInstances, String widgetTitle) {
		if (selectedInstances.size() > 1) {
			createGalleryView(tableBuilder, split(selectedInstances, 4));
		} else {
			createImageView(tableBuilder, jsonConfiguration, null, selectedInstances.get(0), widgetTitle);
		}
	}

	/**
	 * Create rows in html table.
	 *
	 * @param tableBuilder
	 *            the table builder
	 * @param jsonConfiguration
	 *            the json configuration
	 * @param widgetTitle
	 *            the widget title
	 * @param foundInstancesIds
	 *            the found instances ids
	 * @param windowObject
	 *            the window object
	 * @param selectedInstanceId
	 *            the selected instance id
	 */
	private void drawToTable(HtmlTableBuilder tableBuilder, JsonObject jsonConfiguration, String widgetTitle,
			List<String> foundInstancesIds, JsonObject windowObject, String selectedInstanceId) {
		JsonObject windowOptions = windowObject.getJsonObject("windowOptions");
		JsonObject bounds = windowOptions != null ? windowOptions.getJsonObject("osdBounds") : null;
		switch (windowObject.getString("viewType", "")) {
			case "ImageView":
				createImageView(tableBuilder, jsonConfiguration, bounds,
						selectedInstanceId == null ? foundInstancesIds.get(0) : selectedInstanceId, widgetTitle);
				break;
			case "ThumbnailsView":
				List<List<String>> splittedIds = split(foundInstancesIds, 4);
				createGalleryView(tableBuilder, splittedIds);
				break;
			case "ScrollView":
				createScrollView(tableBuilder, foundInstancesIds);
				break;
			case "BookView":
				createBookView(tableBuilder, selectedInstanceId, foundInstancesIds);
				break;
			default:
				break;
		}
	}

	/**
	 * Split the list to sublists with pre-defined size.
	 *
	 * @param <T>
	 *            any type
	 * @param list
	 *            to be splitted
	 * @param size
	 *            of sublists
	 * @return result collection with sublists
	 */
	private static <T> List<List<T>> split(List<T> list, int size) {
		List<List<T>> result = new ArrayList<>(list.size() / size);
		for (int i = 0; i < list.size(); i += size) {
			result.add(list.subList(i, Math.min(list.size(), i + size)));
		}
		return result;
	}

	/**
	 * Populate <code>tableBuilder</code> with images. Every sublist of <code>imageInstanceIds</code> will be populate
	 * as row into table.
	 *
	 * @param tableBuilder
	 *            the table builder.
	 * @param imageInstanceIds
	 *            list with sublist contains ids of image instances.
	 */
	private void createGalleryView(HtmlTableBuilder tableBuilder, List<List<String>> imageInstanceIds) {
		for (int row = 0; row < imageInstanceIds.size(); row++) {
			List<String> instanceIds = imageInstanceIds.get(row);
			int rowCount = tableBuilder.getRowCount();
			for (int column = 0; column < instanceIds.size(); column++) {
				ContentInfo contentInfo = instanceContentService.getContent(instanceIds.get(column),
						Content.PRIMARY_CONTENT);
				if (contentInfo.exists()) {
					String constructImageAddress = createImageAddress(getImageId(contentInfo), FULL_IMAGE_URL);
					BufferedImage srcImage = loadImage(constructImageAddress);
					tableBuilder.addTdValue(rowCount, column,
							new HtmlValueImageBuilder(srcImage, contentInfo.getName()));
				}
			}
		}
	}

	/**
	 * Populate <code>tableBuilder</code> with images. All <codea>imageInstanceIds</codea> will be populated in one row.
	 *
	 * @param tableBuilder
	 *            the table builder.
	 * @param imageInstanceIds
	 *            the instance ids.
	 */
	private void createScrollView(HtmlTableBuilder tableBuilder, List<String> imageInstanceIds) {
		for (int i = 0; i < imageInstanceIds.size(); i++) {
			ContentInfo contentInfo = instanceContentService.getContent(imageInstanceIds.get(i),
					Content.PRIMARY_CONTENT);
			if (contentInfo.exists()) {
				String constructImageAddress = createImageAddress(getImageId(contentInfo), FULL_IMAGE_URL);
				BufferedImage srcImage = loadImage(constructImageAddress);
				tableBuilder.addTdValue(i, 0, new HtmlValueImageBuilder(srcImage, contentInfo.getName()));
			}
		}
	}

	/**
	 * Creates the book view by selected instance id. One image at whole image table row in single cell.
	 *
	 * @param tableBuilder
	 *            the table builder.
	 * @param selectedInstanceId
	 *            the instance id marked as selected in widget configuration
	 */
	private void createBookView(HtmlTableBuilder tableBuilder, String selectedInstanceId,
			List<String> foundInstancesIds) {
		if (selectedInstanceId != null) {
			ContentInfo contentInfo = instanceContentService.getContent(selectedInstanceId, Content.PRIMARY_CONTENT);
			if (contentInfo.exists()) {
				String constructImageAddress = createImageAddress(getImageId(contentInfo), FULL_IMAGE_URL);
				BufferedImage srcImage = loadImage(constructImageAddress);
				tableBuilder.addTdValue(0, 0, 2, new HtmlValueImageBuilder(srcImage, contentInfo.getName()));
			}
			foundInstancesIds.remove(selectedInstanceId);
		}
		createGalleryView(tableBuilder, split(foundInstancesIds, 2));
	}

	/**
	 * Creates the image view by selected instance id. One image at whole image table row in single cell. Also search
	 * and adds image annotations in word document.
	 *
	 * @param tableBuilder
	 *            the table builder.
	 * @param jsonConfiguration
	 *            the json configuration
	 * @param bounds
	 *            the bounds
	 * @param selectedInstanceId
	 *            the selected instance id
	 * @param widgetTitle
	 *            the widget title
	 */
	@SuppressWarnings("boxing")
	private void createImageView(HtmlTableBuilder tableBuilder, JsonObject jsonConfiguration, JsonObject bounds,
			String selectedInstanceId, String widgetTitle) {
		ContentInfo contentInfo = instanceContentService.getContent(selectedInstanceId, Content.PRIMARY_CONTENT);
		if (contentInfo.exists()) {
			ContentMetadata metadata = contentInfo.getMetadata();
			int imageHeight = metadata.getInt(CONFIGURATION_PROPERTY_HEIGHT);
			int imageWidth = metadata.getInt(CONFIGURATION_PROPERTY_WIDTH);
			Quad<Integer, Integer, Integer, Integer> calculatedRange = calculateRange(bounds, imageWidth, imageHeight);
			String constructImageAddress;
			// if calculated width and height are same as original we will load full image
			if (calculatedRange.getThird() == imageWidth && calculatedRange.getForth() == imageHeight) {
				constructImageAddress = createImageAddress(getImageId(contentInfo), FULL_IMAGE_URL);
			} else {
				constructImageAddress = createImageAddress(getImageId(contentInfo), calculatedRange);
			}
			try {
				BufferedImage srcImage = loadImage(constructImageAddress);
				BufferedImage scaledInstance = ImageUtils.scaleImage(srcImage, calculatedRange.getThird(),
						calculatedRange.getForth());
				int rowIndex = tableBuilder.getRowCount();

				if (shouldSkipAnnotations(jsonConfiguration)) {
					addImageToWidgetTable(tableBuilder, scaledInstance, contentInfo.getName(), rowIndex);
					return;
				}

				Collection<Annotation> foundAnnotations = annotationService.searchAnnotations(
						createSearchAnnotationRequest(jsonConfiguration, Arrays.asList(selectedInstanceId)));
				List<Annotation> imageAnnotations = foundAnnotations
						.stream()
							.filter(annotation -> !getSVGsFromAnnotation(annotation).isEmpty())
							.collect(Collectors.toList());
				if (imageAnnotations.isEmpty()) {
					addImageToWidgetTable(tableBuilder, scaledInstance, contentInfo.getName(), rowIndex);
				} else {
					if (jsonConfiguration.getBoolean("lockWidget", false)) {
						scaledInstance = createAnnotatedImage(scaledInstance, imageWidth, imageHeight,
							calculatedRange, imageAnnotations);
					}
					addImageToWidgetTable(tableBuilder, scaledInstance, contentInfo.getName(), rowIndex);
					// if only annotations should be rendered (lockWidget = true, hideAnnotationLayer = true)
					if(!jsonConfiguration.getBoolean("hideAnnotationLayer",false )) {
						htmlTableAnnotationService.createTableHeadings(tableBuilder);
						htmlTableAnnotationService.createAnnotationTable(tableBuilder, imageAnnotations);
					}
				}
			} catch (Exception e) {
				LOGGER.error("Skipping image widget: {} because of: {}", widgetTitle, e.getMessage(), e);
			}
		}
	}

	/**
	 * Adds image to the image widget table builder.
	 *
	 * @param tableBuilder image widget {@link HtmlTableBuilder}
	 * @param image {@link BufferedImage} instance of the image to be added
	 * @param name name for the {@link HtmlValueImageBuilder} element
	 * @param rowIndex which row index for the image to be added to.
	 */
	private void addImageToWidgetTable (HtmlTableBuilder tableBuilder, BufferedImage image, String name, int rowIndex){
		tableBuilder.addTdValue(rowIndex, 0, 6,
				new HtmlValueImageBuilder(image, name));
	}

	/**
	 * Creates the search annotation request.
	 *
	 * @param jsonConfiguration
	 *            the json configuration
	 * @param foundInstancesIds
	 *            the found instances ids
	 * @return the annotation search request
	 */
	@SuppressWarnings("boxing")
	private AnnotationSearchRequest createSearchAnnotationRequest(JsonObject jsonConfiguration,
			List<String> foundInstancesIds) {
		if (jsonConfiguration.containsKey(FILTERS)) {
			JsonObject filters = jsonConfiguration.getJsonObject(FILTERS);
			String author = filters.getString(CONFIGURATION_PROPERTY_AUTHOR, null);
			String status = filters.getString(CONFIGURATION_PROPERTY_COMMENT_STATUS, null);
			String keyword = filters.getString(CONFIGURATION_PROPERTY_KEYWORD, null);
			return new AnnotationSearchRequest()
					.setDateRange(getDateRange(filters))
						.setInstanceIds(foundInstancesIds)
						.setUserIds(StringUtils.isBlank(author) ? null : Arrays.asList(author))
						.setLimit(10)
						.setOffset(1)
						.setStatus(status)
						.setText(keyword);
		}
		return new AnnotationSearchRequest()
				.setDateRange(new DateRange(null, null))
					.setInstanceIds(foundInstancesIds)
					.setUserIds(Collections.emptyList())
					.setLimit(10)
					.setOffset(1)
					.setStatus(null)
					.setText(null);
	}

	/**
	 * Construct DateRange object from widget configuration.
	 *
	 * @param filters
	 *            configuration of widget.
	 * @return created object.
	 */
	private DateRange getDateRange(JsonObject filters) {
		String fromDate = filters.getString(CONFIGURATION_PROPERTY_FROM_DATE, "");
		String toDate = filters.getString(CONFIGURATION_PROPERTY_TO_DATE, "");
		DateRange dateRangeCreated = new DateRange(null, null);
		dateRangeCreated.setFirst(StringUtils.isBlank(fromDate) ? null : dateConverter.parseDate(fromDate));
		dateRangeCreated.setSecond(StringUtils.isBlank(toDate) ? null : dateConverter.parseDate(toDate));
		return dateRangeCreated;
	}

	/**
	 * Pass through all annotations transform them to PNG then join with main image.
	 *
	 * @param srcImage
	 *            the original image with annotations
	 * @param imageWidth
	 *            original image width
	 * @param imageHeight
	 *            original image height
	 * @param calculatedRange
	 *            the calculated range passed from widget configuration
	 * @param loadedAnnotations
	 *            found annotations for current image
	 * @return the annotated image
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TranscoderException
	 *             the transcoder exception
	 */
	@SuppressWarnings({ "boxing", "unchecked" })
	private static BufferedImage createAnnotatedImage(BufferedImage srcImage, int imageWidth, int imageHeight,
			Quad<Integer, Integer, Integer, Integer> calculatedRange, Collection<Annotation> loadedAnnotations)
			throws IOException, TranscoderException {
		BufferedImage newImage = srcImage;
		for (Annotation annotation : loadedAnnotations) {
			List<String> svgSelectors = getSVGsFromAnnotation(annotation);
			for (String svgSelector : svgSelectors) {
				if (svgSelector != null) {
					String cleanedSVGSelector = cleanSVGSelector(svgSelector);
					BufferedImage png = ImageUtils.svgToPNG(cleanedSVGSelector, imageWidth, imageHeight);
					BufferedImage cropped = ImageUtils.getSubImage(png, calculatedRange.getFirst(),
							calculatedRange.getSecond(), calculatedRange.getThird(), calculatedRange.getForth());
					newImage = ImageUtils.joinImages(newImage, cropped);
				}
			}
		}
		return newImage;
	}

	/**
	 * Clean svg selector from invalid attributes which cause slow drawing of annotation images.
	 *
	 * @param svgSelector
	 *            the xml svg selector as string
	 * @return cleaned svgSelector
	 */
	private static String cleanSVGSelector(String svgSelector) {
		try {
			Document svgXmlDoc = XMLUtil.documentFromString(svgSelector);
			NodeList pathElements = svgXmlDoc.getElementsByTagName("path");
			for (int i = 0; i < pathElements.getLength(); i++) {
				Node pathNode = pathElements.item(i);
				NamedNodeMap pathNodeAttributes = pathNode.getAttributes();
				// check svg attributes
				Set<String> invalidAttrNames = getInvalidSVGPathNodeAttrNames(pathNodeAttributes);
				// remove invalid attributes
				for (String attrName : invalidAttrNames) {
					pathNodeAttributes.removeNamedItem(attrName);
				}
			}
			return convertXMLDocumentToString(svgXmlDoc);
		} catch (Exception e) {
			LOGGER.error("Exception during cleaning SVG.", e);
		}
		return svgSelector;
	}

	/**
	 * Check for empty or none attribute values and return their names.
	 *
	 * @param pathNodeAttributes
	 *            the path node attributes
	 * @return the invalid set with attribute names
	 */
	protected static Set<String> getInvalidSVGPathNodeAttrNames(NamedNodeMap pathNodeAttributes) {
		Set<String> invalidAttrNames = new HashSet<>(1);
		for (int attrIndex = 0; attrIndex < pathNodeAttributes.getLength(); attrIndex++) {
			Node attribute = pathNodeAttributes.item(attrIndex);
			if (isAttributeInvalid(attribute)) {
				invalidAttrNames.add(attribute.getNodeName());
			}
		}
		return invalidAttrNames;
	}

	/**
	 * Check is attribute value empty or have value "none".
	 *
	 * @param attribute
	 *            to be checked.
	 * @return true if attribute value is empty or have value "none" if attribute is not included into list with valid
	 *         none value attribute.
	 */
	private static boolean isAttributeInvalid(Node attribute) {
		String nodeValue = attribute.getNodeValue();
		if (StringUtils.isBlank(nodeValue)) {
			return true;
		}
		return ATTRIBUTE_VALUE_NONE.equalsIgnoreCase(nodeValue)
				&& !VALID_ATTRIBUTES_WITH_NONE_VALUE.contains(attribute.getNodeName());
	}

	private static String convertXMLDocumentToString(Document doc) throws TransformerException, IOException {
		try (StringWriter writer = new StringWriter()) {
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			DOMSource domSource = new DOMSource(doc);
			transformer.transform(domSource, result);
			return writer.toString();
		}
	}

	/**
	 * Fetch all SVGs for <code>annotation</code>.
	 *
	 * @param annotation
	 *            the annotation
	 * @return fetched SVGs if any.
	 */
	@SuppressWarnings("unchecked")
	private static List<String> getSVGsFromAnnotation(Annotation annotation) {
		try {
			String annotationContent = annotation.getContent();
			Object object = JsonUtils.fromString(annotationContent);
			Map<Object, Object> contentMap = (Map<Object, Object>) object;
			Object on = contentMap.get("on");
			if (on instanceof Map) {
				return getSVGPaths((Map<Object, Object>) on);
			} else if (on instanceof List) {
				return getSVGPaths((List<Map<Object, Object>>) on);
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return Collections.emptyList();
	}

	private static List<String> getSVGPaths(Map<Object, Object> on) {
		Map<Object, Object> selector = (Map<Object, Object>) on.get("selector");
		String svgString = getSVGString(selector);
		return svgString != null ? Arrays.asList(svgString) : Collections.emptyList();
	}

	private static String getSVGString(Map<Object, Object> item) {
		if (item != null && item.containsKey("@type")) {
			String type = item.get("@type").toString();
			if ("oa:SvgSelector".equalsIgnoreCase(type)) {
				return item.get("value").toString();
			}
		}
		return null;
	}

	private static List<String> getSVGPaths(List<Map<Object, Object>> on) {
		return on.stream().map(objectMap -> {
			Map<Object, Object> selector = (Map<Object, Object>) objectMap.get("selector");
			Map<Object, Object> item = (Map<Object, Object>) selector.get("item");
			String svgString = getSVGString(item);
			return svgString;
		}).filter(StringUtils::isNotBlank).collect(Collectors.toList());
	}

	/**
	 * Load image from remote image address.
	 *
	 * @param imageAddress
	 *            the image address
	 * @return the image
	 */
	private BufferedImage loadImage(String imageAddress) {
		RESTClient client = restClient.get();
		try (InputStream inputStream = client
				.rawRequest(new GetMethod(), new URI(imageAddress, false))
					.getResponseBodyAsStream()) {
			return ImageIO.read(inputStream);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * Calculate range which is needed to calculate annotations and resize.
	 *
	 * @param bounds
	 *            fetched from image widget configuration
	 * @param tileWidth
	 *            the original image width
	 * @param tileHeight
	 *            the original image height
	 * @return the quad representing desired X & Y coordinates, Width & Height size
	 */
	@SuppressWarnings("boxing")
	private static Quad<Integer, Integer, Integer, Integer> calculateRange(JsonObject bounds, int tileWidth,
			int tileHeight) {
		if (bounds == null) {
			return new Quad<>(0, 0, tileWidth, tileHeight);
		}

		double configurationWidth = bounds.getJsonNumber(CONFIGURATION_PROPERTY_WIDTH).doubleValue();
		double configurationHeight = bounds.getJsonNumber(CONFIGURATION_PROPERTY_HEIGHT).doubleValue();
		double configurationX = bounds.getJsonNumber("x").doubleValue();
		double configurationY = bounds.getJsonNumber("y").doubleValue();

		double imageRatio = 1.0 * tileWidth / tileHeight;

		int x = (int) Math.round(tileWidth * configurationX);
		int y = (int) Math.round(tileHeight * configurationY * imageRatio);
		int factorWidth = (int) Math.round(tileWidth * configurationWidth);
		int factorHeight = (int) Math.round(tileHeight * configurationHeight * imageRatio);

		int xRange = x < 0 ? 0 : x;
		int yRange = y < 0 ? 0 : y;
		int widthRange = factorWidth > tileWidth ? tileWidth : factorWidth;
		int heightRange = factorHeight > tileHeight ? tileHeight : factorHeight;

		return new Quad<>(xRange, yRange, widthRange, heightRange);
	}

	/**
	 * Creates the image address by bounds.
	 *
	 * @param imageId
	 *            the image remote id
	 * @param bounds
	 *            the bounds with desired X & Y coordinates, Width & Height size
	 * @return the remote address
	 */
	private String createImageAddress(String imageId, Quad<Integer, Integer, Integer, Integer> bounds) {
		StringBuilder imageScale = new StringBuilder(FULL_IMAGE_URL);
		if (bounds != null) {
			imageScale = new StringBuilder("/");
			imageScale
					.append(bounds.getFirst())
						.append(",")
						.append(bounds.getSecond())
						.append(",")
						.append(bounds.getThird())
						.append(",")
						.append(bounds.getForth())
						.append("/")
						.append(bounds.getThird())
						.append(",")
						.append(bounds.getForth())
						.append("/0/default.jpg");
		}
		return createImageAddress(imageId, imageScale.toString());
	}

	/**
	 * Creates the image address.
	 *
	 * @param imageId
	 *            the image id
	 * @param imageScale
	 *            the scaling which remote server to return
	 * @return the remote address
	 */
	private String createImageAddress(String imageId, String imageScale) {
		String iiifServerAddress = imageServerConfigurations
				.getIiifServerAddress()
					.requireConfigured()
					.get()
					.toString();
		String addressSuffix = StringUtils.trimToEmpty(imageServerConfigurations.getIiifServerAddressSuffix().get());
		return iiifServerAddress + imageId + addressSuffix + imageScale;
	}

	/**
	 * Gets the image id.
	 *
	 * @param contentInfo
	 *            the content info
	 * @return the image id
	 */
	private static String getImageId(ContentInfo contentInfo) {
		return contentInfo.getMetadata().getString("id", () -> FileUtil.getName(contentInfo.getRemoteId()));
	}

	/**
	 * If image widget is not locked and annotation layer is hidden, do not render annotations.
	 * @param jsonConfiguration supplied json configuration.
	 * @return if annotations layer and comments section should be completely skipped
	 */
	private boolean shouldSkipAnnotations(JsonObject jsonConfiguration) {
		return !jsonConfiguration.getBoolean("lockWidget",false) && jsonConfiguration.getBoolean("hideAnnotationLayer", false);
	}

	/**
	 * Gets the selected instances from widget json configuration.
	 *
	 * @param currentInstanceId
	 *            the current instance id
	 * @param jsonConfiguration
	 *            the json configuration
	 * @return the selected instances
	 */
	private List<String> getSelectedInstances(String currentInstanceId, JsonObject jsonConfiguration) {
		if (jsonConfiguration.containsKey(SELECT_OBJECT_MODE)) {
			String selectObjectMode = jsonConfiguration.getString(SELECT_OBJECT_MODE);
			if (StringUtils.isNotBlank(selectObjectMode) && CURRENT.equals(selectObjectMode)) {
				// Arrays.asList return fixed size array and we cant add or remove objects
				// but we have scenario when this array will be modified.
				return new ArrayList<>(Arrays.asList(currentInstanceId));
			}
			if (StringUtils.isNotBlank(selectObjectMode) && MANUALLY.equals(selectObjectMode)
					&& jsonConfiguration.containsKey(SELECTED_OBJECTS)) {
				List<Serializable> selectedObjects = JSON.jsonToList(jsonConfiguration.getJsonArray(SELECTED_OBJECTS));
				// resolve references because when ids saved in widget static content, but use may delete some
				// instance and widget will not be updated automatically, so we need to verify that saved ids in
				// widget content are existing
				return instanceResolver
						.resolveReferences(selectedObjects)
							.stream()
							.map(InstanceReference::getId)
							.collect(Collectors.toList());
			}
		}
		return getSelectedInstancesFromCriteria(currentInstanceId, jsonConfiguration, false)
				.stream()
					.map(Instance::getId)
					.map(Serializable::toString)
					.collect(Collectors.toList());
	}
}
