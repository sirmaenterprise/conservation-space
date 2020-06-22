package org.docx4j.convert.in.xhtml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.imageio.ImageIOUtil;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.org.xhtmlrenderer.docx.Docx4JFSImage;
import org.docx4j.org.xhtmlrenderer.docx.Docx4jUserAgent;
import org.docx4j.org.xhtmlrenderer.extend.UserAgentCallback;
import org.docx4j.wml.CTTblCellMar;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Style;
import org.docx4j.wml.Style.BasedOn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Custom implementation of XHTMLImageHandler because the default {@link XHTMLImageHandlerDefault} fetch images, then
 * write to temp files and read them back. To skip this slow behaviour this class use directly images bytes and convert
 * stream in-memory.
 *
 * @author Hristo Lungov
 */
public class XHTMLImageHandlerImpl implements XHTMLImageHandler {

	private static final String ALT = "alt";

	protected static final String SRC = "src";

	protected static final String EMBEDDED_ID = "data-embedded-id";

	private static final String DATA_IMAGE = "data:image";

	private static final String INVALID_DATA_URI = "[INVALID DATA URI: ";

	private static final Logger LOGGER = LoggerFactory.getLogger(XHTMLImageHandlerImpl.class);

	private int maxWidth = -1;
	private String tableStyle;
	private HashMap<String, BinaryPartAbstractImage> imagePartCache = new HashMap<>();
	private XHTMLImporterImpl importer;

	private Function<String, byte[]> embeddedImageLoader;

	/**
	 * Instantiates a new SEPXHTML image handler.
	 *
	 * @param importer
	 *            the importer of xhtml
	 * @param embeddedImageLoader
	 *            loader function for internal embedded images. These are images that are extracted from base64 and
	 *            stored in the content server. The loader should fetch the image content by content identifier
	 */
	public XHTMLImageHandlerImpl(XHTMLImporterImpl importer, Function<String, byte[]> embeddedImageLoader) {
		this.importer = importer;
		this.embeddedImageLoader = embeddedImageLoader;
		ImageIO.scanForPlugins();
	}

	/**
	 * Adds image to WordprocessingMLPackage when meet image tag during parsing html content.
	 *
	 * @param docx4jUserAgent
	 *            is a simple implementation of {@link UserAgentCallback} which places no restrictions on what XML, CSS
	 *            or images are loaded, and reports visited links without any filtering.
	 * @param wordMLPackage
	 *            logical entity which holds a collection of parts
	 * @param p
	 *            the paragraph
	 * @param e
	 *            the image element from DOM
	 * @param cx
	 *            width of image itself (ie excluding CSS margin, padding) in EMU
	 * @param cy
	 *            height of image itself (ie excluding CSS margin, padding) in EMU
	 */
	@Override
	public void addImage(Docx4jUserAgent docx4jUserAgent, WordprocessingMLPackage wordMLPackage, P p, Element e,
			Long cx, Long cy) {
		String errorMessage = "[MISSING IMAGE: " + e.getAttribute(ALT) + ", " + e.getAttribute(ALT) + " ]";
		try {
			String srcAttribute = e.getAttribute(SRC);
			byte[] imageBytes;
			BinaryPartAbstractImage imagePart;
			if (StringUtils.isNotBlank(srcAttribute)) {
				imagePart = imagePartCache.get(srcAttribute);
				if (imagePart != null) {
					addImageToDocument(p, e, cx, cy, imagePart);
					return;
				}
			}
			if (e.hasAttribute(EMBEDDED_ID)) {
				// when we do not have a source but we have embedded id
				String embeddedId = e.getAttribute(EMBEDDED_ID);
				imageBytes = embeddedImageLoader.apply(embeddedId);
				srcAttribute = embeddedId;
			} else {
				imageBytes = getImageBytes(docx4jUserAgent, srcAttribute, p);
			}
			if (imageBytes != null && imageBytes.length > 0) {
				imagePart = createImagePart(imageBytes, wordMLPackage);
				if (imagePart != null) {
					addImageToDocument(p, e, cx, cy, imagePart);
					imagePartCache.put(srcAttribute, imagePart);
					return;
				}
			} else {
				// something goes wrong or can't load image, so add failure message
				addFailureMessage(p, errorMessage);
			}
		} catch (Exception ex) {
			LOGGER.error(MessageFormat.format("Error during image processing: ''{0}'', insert default text.",
					new Object[] { e.getAttribute(ALT) }), ex);
			// something goes wrong or can't load image, so add failure message
			addFailureMessage(p, errorMessage);
		}

	}

	/**
	 * Gets the image bytes.
	 *
	 * @param docx4jUserAgent
	 *            the docx4j user agent
	 * @param srcAttribute
	 *            the src attribute
	 * @param p
	 *            the paragraph
	 * @return the image bytes
	 */
	protected static byte[] getImageBytes(Docx4jUserAgent docx4jUserAgent, String srcAttribute, P p) {
		if (srcAttribute.startsWith(DATA_IMAGE)) {
			String base64String = srcAttribute;
			int commaPos = base64String.indexOf(",");
			if (commaPos < 6) {
				// wrong comma position add fail message and return
				addFailureMessage(p, INVALID_DATA_URI + srcAttribute);
				return new byte[0];
			}
			base64String = base64String.substring(commaPos + 1);
			return Base64.decodeBase64(base64String.getBytes(StandardCharsets.UTF_8));
		}
		return loadImageByURL(docx4jUserAgent, srcAttribute);
	}

	/**
	 * Adds fail message to current paragraph.
	 *
	 * @param p
	 *            the current paragraph
	 * @param message
	 *            the failure message to add
	 */
	protected static void addFailureMessage(P p, String message) {
		org.docx4j.wml.R run = Context.getWmlObjectFactory().createR();
		p.getContent().add(run);
		org.docx4j.wml.Text text = Context.getWmlObjectFactory().createText();
		text.setValue(message);
		run.getContent().add(text);
	}

	/**
	 * Load image by url.
	 *
	 * @param docx4jUserAgent
	 *            the docx4j user agent
	 * @param srcAttribute
	 *            the src attribute
	 * @return the byte[]
	 */
	protected static byte[] loadImageByURL(Docx4jUserAgent docx4jUserAgent, String srcAttribute) {
		String url = srcAttribute;
		if (":".equals(url.substring(1, 2))) {
			url = "file:/" + url;
		}
		Docx4JFSImage docx4JFSImage = docx4jUserAgent.getDocx4JImageResource(url);
		// in case of wrong URL - docx4JFSImage will be null
		if (docx4JFSImage != null) {
			return docx4JFSImage.getBytes();
		}
		return new byte[0];
	}

	/**
	 * Adds the image to document.
	 *
	 * @param p
	 *            the paragraph
	 * @param e
	 *            the image element from DOM
	 * @param cx
	 *            width of image itself (ie excluding CSS margin, padding) in EMU
	 * @param cy
	 *            height of image itself (ie excluding CSS margin, padding) in EMU
	 * @param imagePart
	 *            the image part
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings({ "boxing", "deprecation" })
	protected void addImageToDocument(P p, Element e, Long cx, Long cy, BinaryPartAbstractImage imagePart)
			throws Exception {
		Long x = cx;
		Long y = cy;
		R run = Context.getWmlObjectFactory().createR();
		p.getContent().add(run);
		Drawing drawing = Context.getWmlObjectFactory().createDrawing();
		run.getContent().add(drawing);
		if (x == null && y == null) {
			if (maxWidth > 0) {
				long excessWidth = getTblCellMargins();
				if (excessWidth > 0) {
					LOGGER.debug("table style margins subtracted (twips): " + excessWidth);
				}
				Inline inline = imagePart.createImageInline(null, e.getAttribute(ALT), 0, 1, false,
						maxWidth - (int) excessWidth);
				drawing.getAnchorOrInline().add(inline);
			} else {
				Inline inline = imagePart.createImageInline(null, e.getAttribute(ALT), 0, 1, false);
				drawing.getAnchorOrInline().add(inline);
			}
		} else {
			if (x == null) {
				x = imagePart.getImageInfo().getSize().getWidthPx()
						* (y / imagePart.getImageInfo().getSize().getHeightPx());
			}
			if (y == null) {
				y = imagePart.getImageInfo().getSize().getHeightPx()
						* (x / imagePart.getImageInfo().getSize().getWidthPx());
			}
			Inline inline = imagePart.createImageInline(null, e.getAttribute(ALT), 0, 1, x, y, false);
			drawing.getAnchorOrInline().add(inline);
		}
	}

	/**
	 * Create an image part from the provided byte array, attach it to the source part (eg the main document part, a
	 * header part etc), and return it. Works for both docx and pptx. Note: this method creates a temp file (and
	 * attempts to delete it). That's because it uses org.apache.xmlgraphics
	 *
	 * @param imageBytes
	 *            byte input stream of image
	 * @param wordMLPackage
	 *            the opc package
	 * @return the binary part representing image with image info
	 * @throws IOException
	 *             cause by some input/output exception
	 * @throws PartUnrecognisedException
	 *             docx4j exception , possible when creating image part from provided byte array
	 * @throws InvalidFormatException
	 *             raised when not recognized image type
	 */
	@SuppressWarnings("deprecation")
	protected static BinaryPartAbstractImage createImagePart(byte[] imageBytes, WordprocessingMLPackage wordMLPackage)
			throws IOException, InvalidFormatException, PartUnrecognisedException {
		try (InputStream bis = new ByteArrayInputStream(imageBytes);
				ImageInputStream iis = ImageIO.createImageInputStream(bis);) {
			Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
			if (imageReaders.hasNext()) {
				ImageReader imageReader = imageReaders.next();

				ContentTypeManager ctm = wordMLPackage.getContentTypeManager();
				MainDocumentPart sourcePart = wordMLPackage.getMainDocumentPart();
				// Ensure the relationships part exists
				if (sourcePart.getRelationshipsPart() == null) {
					RelationshipsPart.createRelationshipsPartForPart(sourcePart);
				}
				String proposedRelId = sourcePart.getRelationshipsPart().getNextId();
				ImageInfo imageInfo = getImageInfo(imageReader, iis);
				if (imageInfo == null) {
					return null;
				}
				String mimeType = imageInfo.getMimeType();
				String ext = mimeType.substring(imageInfo.getMimeType().indexOf("/") + 1);
				BinaryPartAbstractImage imagePart = (BinaryPartAbstractImage) ctm.newPartForContentType(mimeType,
						BinaryPartAbstractImage.createImageName(wordMLPackage, sourcePart, proposedRelId, ext), null);
				imagePart.setBinaryData(imageBytes);
				imagePart.getRels().add(sourcePart.addTargetPart(imagePart, proposedRelId));
				imagePart.setImageInfo(imageInfo);
				return imagePart;

			}
		}
		return null;
	}

	/**
	 * Gets the image info.
	 *
	 * @param reader
	 *            image reader used to fetch image metadata
	 * @param iis
	 *            image input stream
	 * @return the image info
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected static ImageInfo getImageInfo(ImageReader reader, ImageInputStream iis) throws IOException {
		reader.setInput(ImageUtil.ignoreFlushing(iis), true, false);
		final int imageIndex = 0;
		IIOMetadata iiometa = reader.getImageMetadata(imageIndex);
		ImageSize size = new ImageSize();
		size.setSizeInPixels(reader.getWidth(imageIndex), reader.getHeight(imageIndex));
		String mime = reader.getOriginatingProvider().getMIMETypes()[0];
		// Resolution (first a default, then try to read the metadata)
		size.setResolution(72.0f);
		ImageIOUtil.extractResolution(iiometa, size);
		if (size.getWidthPx() <= 0 || size.getHeightPx() <= 0) {
			// Watch out for a special case: a TGA image was erroneously identified
			// as a WBMP image by a Sun ImageIO codec.
			return null;
		}
		if (size.getWidthMpt() == 0) {
			size.calcSizeFromPixels();
		}

		ImageInfo info = new ImageInfo("", mime);
		info.getCustomObjects().put(ImageIOUtil.IMAGEIO_METADATA, iiometa);
		info.setSize(size);

		return info;
	}

	/**
	 * Get table cell margins from table style. <br>
	 * Parameter tableStyle can be null - 0 will be returned.
	 *
	 * @return left margin plus right margin (twips)
	 */
	protected long getTblCellMargins() {
		Style style = null;
		if (tableStyle != null && !tableStyle.isEmpty()) {
			style = importer.getStyleByIdOrName(tableStyle);
		}
		if (style != null && importer.getTableHelper().isTableStyle(style)) {
			CTTblCellMar cellMar = getTblCellMar(style);
			if (cellMar == null) {
				// try "based on" style
				CTTblCellMar bsCellMar = getBasedOnTblCellMar(style);
				if (bsCellMar != null) {
					return getLeftPlusRightMarginsValue(bsCellMar);
				}
			} else {
				return getLeftPlusRightMarginsValue(cellMar);
			}
		}
		return 0;
	}

	/**
	 * Gets sum of the left width plus right width margins.
	 *
	 * @param cellMar
	 *            table cell margin
	 * @return the sum of margins
	 */
	private static long getLeftPlusRightMarginsValue(CTTblCellMar cellMar) {
		return cellMar.getLeft().getW().longValue() + cellMar.getRight().getW().longValue();
	}

	/**
	 * Get cell margins from "based on" style. <br>
	 * Search recursively while possible.
	 *
	 * @param style
	 *            the style
	 * @return style based on tbl cell mar
	 */
	private CTTblCellMar getBasedOnTblCellMar(Style style) {
		BasedOn bo = style.getBasedOn();
		if (bo != null) {
			String basedOn = bo.getVal();
			if (StringUtils.isNotBlank(basedOn)) {
				return getInnerBasedOn(importer.getStyleByIdOrName(basedOn));
			}
		}
		return null;
	}

	/**
	 * Gets the inner based on.
	 *
	 * @param style
	 *            the style
	 * @return the inner based on
	 */
	private CTTblCellMar getInnerBasedOn(Style style) {
		if (style != null) {
			CTTblCellMar bsCellMar = getTblCellMar(style);
			if (bsCellMar != null) {
				return bsCellMar;
			}
			return getBasedOnTblCellMar(style);
		}
		return null;
	}

	/**
	 * Gets the table cell margin object.
	 *
	 * @param style
	 *            table cell style
	 * @return the table cell margin
	 */
	private static CTTblCellMar getTblCellMar(Style style) {
		CTTblPrBase tpb = style.getTblPr();
		if (tpb != null) {
			return tpb.getTblCellMar();
		}
		return null;
	}

	/**
	 * Gets the max width.
	 *
	 * @return the max width
	 */
	public int getMaxWidth() {
		return maxWidth;
	}

	@Override
	public void setMaxWidth(int maxWidth, String tableStyle) {
		this.maxWidth = maxWidth;
		this.tableStyle = tableStyle;
	}

}
