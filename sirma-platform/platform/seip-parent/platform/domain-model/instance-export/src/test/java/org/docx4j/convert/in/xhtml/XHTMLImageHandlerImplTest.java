package org.docx4j.convert.in.xhtml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.Base64;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.docx4j.convert.in.xhtml.TableHelper;
import org.docx4j.convert.in.xhtml.XHTMLImageHandlerImpl;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.org.xhtmlrenderer.docx.Docx4jUserAgent;
import org.docx4j.wml.CTTblCellMar;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Style;
import org.docx4j.wml.Style.BasedOn;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Text;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/**
 * Test class for SepXHTMLImageHandler.
 *
 * @author Hristo Lungov
 */
public class XHTMLImageHandlerImplTest {

	private static final String TEST_STYLE = "testStyle";
	private static final String TEST_MESSAGE = "Test Message";
	private static final String IMAGE_PREFIX = "data:image/jpg;base64,";

	/**
	 * Adds the failure message test.
	 */
	@Test
	public static void addFailureMessageTest() {
		P paragraph = Context.getWmlObjectFactory().createP();
		XHTMLImageHandlerImpl.addFailureMessage(paragraph, TEST_MESSAGE);
		Assert.assertEquals(paragraph.getContent().size(), 1);
		R r = (R) paragraph.getContent().get(0);
		Assert.assertEquals(r.getContent().size(), 1);
		Text text = (Text) r.getContent().get(0);
		Assert.assertEquals(text.getValue(), TEST_MESSAGE);
	}

	/**
	 * Load image by url from hdd test.
	 */
	@Test
	public static void loadImageByURLFromHDDTest() {
		Docx4jUserAgent docx4jUserAgent = Mockito.mock(Docx4jUserAgent.class);
		byte[] loadImageByURL = XHTMLImageHandlerImpl.loadImageByURL(docx4jUserAgent, "D:\test.jpg");
		Assert.assertTrue(loadImageByURL.length == 0);
	}

	/**
	 * Load image by url test.
	 */
	@Test
	public void loadImageByURLTest() {
		URL testJpgURL = getClass().getClassLoader().getResource("test.jpg");
		Docx4jUserAgent docx4jUserAgent = new Docx4jUserAgent();
		byte[] imageBytes = XHTMLImageHandlerImpl.loadImageByURL(docx4jUserAgent, testJpgURL.toString());
		Assert.assertTrue(imageBytes.length > 0);
	}

	/**
	 * Creates the image part test.
	 *
	 * @throws InvalidFormatException
	 *             the invalid format exception
	 * @throws JAXBException
	 *             the JAXB exception
	 * @throws PartUnrecognisedException
	 *             the part unrecognised exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void createImagePartTest() throws InvalidFormatException, JAXBException, PartUnrecognisedException, IOException {
		URL testJpgURL = getClass().getClassLoader().getResource("test.jpg");
		Docx4jUserAgent docx4jUserAgent = new Docx4jUserAgent();
		byte[] imageBytes = XHTMLImageHandlerImpl.loadImageByURL(docx4jUserAgent, testJpgURL.toString());
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();
		BinaryPartAbstractImage createImagePart = XHTMLImageHandlerImpl.createImagePart(imageBytes, wordMLPackage);
		Assert.assertNotNull(createImagePart);
	}

	/**
	 * Creates the image part empty image content test.
	 *
	 * @throws InvalidFormatException
	 *             the invalid format exception
	 * @throws JAXBException
	 *             the JAXB exception
	 * @throws PartUnrecognisedException
	 *             the part unrecognised exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public static void createImagePartEmptyImageContentTest() throws InvalidFormatException, JAXBException, PartUnrecognisedException, IOException {
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();
		BinaryPartAbstractImage createImagePart = XHTMLImageHandlerImpl.createImagePart(new byte[0], wordMLPackage);
		Assert.assertNull(createImagePart);
	}

	/**
	 * Adds the image test with none scale.
	 *
	 * @throws InvalidFormatException
	 *             the invalid format exception
	 * @throws JAXBException
	 *             the JAXB exception
	 */
	@Test
	public void addImageWithNoneScaleTest() throws InvalidFormatException, JAXBException {
		P paragraph = Context.getWmlObjectFactory().createP();
		Element e = Mockito.mock(Element.class);
		URL testJpgURL = getClass().getClassLoader().getResource("test.jpg");
		Mockito.when(e.getAttribute(XHTMLImageHandlerImpl.SRC)).thenReturn(testJpgURL.toString());
		Docx4jUserAgent docx4jUserAgent = new Docx4jUserAgent();
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(null, content -> new byte[0]);
		sepXHTMLImageHandler.addImage(docx4jUserAgent, wordMLPackage, paragraph, e, null, null);
		Assert.assertEquals(paragraph.getContent().size(), 1);
		R r = (R) paragraph.getContent().get(0);
		Assert.assertEquals(r.getContent().size(), 1);
		Drawing drawing = (Drawing) r.getContent().get(0);
		List<Object> anchorOrInline = drawing.getAnchorOrInline();
		Assert.assertEquals(anchorOrInline.size(), 1);
		// pass second time for cache check
		sepXHTMLImageHandler.addImage(docx4jUserAgent, wordMLPackage, paragraph, e, null, null);
		Assert.assertEquals(paragraph.getContent().size(), 2);
	}

	/**
	 * Adds the image test with none scale with width.
	 *
	 * @throws InvalidFormatException
	 *             the invalid format exception
	 * @throws JAXBException
	 *             the JAXB exception
	 */
	@Test
	public void addImageWithNoneScaleWithWidthTest() throws InvalidFormatException, JAXBException {
		P paragraph = Context.getWmlObjectFactory().createP();
		Element e = Mockito.mock(Element.class);
		URL testJpgURL = getClass().getClassLoader().getResource("test.jpg");
		Mockito.when(e.getAttribute(XHTMLImageHandlerImpl.SRC)).thenReturn(testJpgURL.toString());
		Docx4jUserAgent docx4jUserAgent = new Docx4jUserAgent();
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(null, content -> new byte[0]);
		sepXHTMLImageHandler.setMaxWidth(200, null);
		sepXHTMLImageHandler.addImage(docx4jUserAgent, wordMLPackage, paragraph, e, null, null);
		Assert.assertEquals(paragraph.getContent().size(), 1);
		R r = (R) paragraph.getContent().get(0);
		Assert.assertEquals(r.getContent().size(), 1);
		Drawing drawing = (Drawing) r.getContent().get(0);
		List<Object> anchorOrInline = drawing.getAnchorOrInline();
		Assert.assertEquals(anchorOrInline.size(), 1);
	}

	@Test
	public void addImageWithBase64Test() throws InvalidFormatException, JAXBException, IOException {
		P paragraph = Context.getWmlObjectFactory().createP();
		Element e = Mockito.mock(Element.class);
		try (InputStream testJpgURLInputStream = getClass().getClassLoader().getResourceAsStream("test.jpg")) {
			byte[] byteArray = IOUtils.toByteArray(testJpgURLInputStream);
			String thumbnail = Base64.getEncoder().encodeToString(byteArray);
			String base64 = new StringBuilder(IMAGE_PREFIX.length() + thumbnail.length()).append(IMAGE_PREFIX).append(thumbnail).toString();
			Mockito.when(e.getAttribute(XHTMLImageHandlerImpl.SRC)).thenReturn(base64);
			Docx4jUserAgent docx4jUserAgent = new Docx4jUserAgent();
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
			NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
			wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
			ndp.unmarshalDefaultNumbering();
			XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(null, content -> new byte[0]);
			sepXHTMLImageHandler.setMaxWidth(200, null);
			sepXHTMLImageHandler.addImage(docx4jUserAgent, wordMLPackage, paragraph, e, null, null);
			Assert.assertEquals(paragraph.getContent().size(), 1);
			R r = (R) paragraph.getContent().get(0);
			Assert.assertEquals(r.getContent().size(), 1);
			Drawing drawing = (Drawing) r.getContent().get(0);
			List<Object> anchorOrInline = drawing.getAnchorOrInline();
			Assert.assertEquals(anchorOrInline.size(), 1);
		}
	}

	@Test
	public void addImageWithScaleTest() throws InvalidFormatException, JAXBException {
		P paragraph = Context.getWmlObjectFactory().createP();
		Element e = Mockito.mock(Element.class);
		URL testJpgURL = getClass().getClassLoader().getResource("test.jpg");
		Mockito.when(e.getAttribute(XHTMLImageHandlerImpl.SRC)).thenReturn(testJpgURL.toString());
		Docx4jUserAgent docx4jUserAgent = new Docx4jUserAgent();
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(null, content -> new byte[0]);
		sepXHTMLImageHandler.addImage(docx4jUserAgent, wordMLPackage, paragraph, e, 100L, 100L);
		Assert.assertEquals(paragraph.getContent().size(), 1);
		R r = (R) paragraph.getContent().get(0);
		Assert.assertEquals(r.getContent().size(), 1);
		Drawing drawing = (Drawing) r.getContent().get(0);
		List<Object> anchorOrInline = drawing.getAnchorOrInline();
		Assert.assertEquals(anchorOrInline.size(), 1);
	}

	@Test
	public void addImageWithYScaleTest() throws InvalidFormatException, JAXBException {
		P paragraph = Context.getWmlObjectFactory().createP();
		Element e = Mockito.mock(Element.class);
		URL testJpgURL = getClass().getClassLoader().getResource("test.jpg");
		Mockito.when(e.getAttribute(XHTMLImageHandlerImpl.SRC)).thenReturn(testJpgURL.toString());
		Docx4jUserAgent docx4jUserAgent = new Docx4jUserAgent();
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(null, content -> new byte[0]);
		sepXHTMLImageHandler.addImage(docx4jUserAgent, wordMLPackage, paragraph, e, null, 100L);
		Assert.assertEquals(paragraph.getContent().size(), 1);
		R r = (R) paragraph.getContent().get(0);
		Assert.assertEquals(r.getContent().size(), 1);
		Drawing drawing = (Drawing) r.getContent().get(0);
		List<Object> anchorOrInline = drawing.getAnchorOrInline();
		Assert.assertEquals(anchorOrInline.size(), 1);
	}

	@Test
	public void addImageWithXScaleTest() throws InvalidFormatException, JAXBException {
		P paragraph = Context.getWmlObjectFactory().createP();
		Element e = Mockito.mock(Element.class);
		URL testJpgURL = getClass().getClassLoader().getResource("test.jpg");
		Mockito.when(e.getAttribute(XHTMLImageHandlerImpl.SRC)).thenReturn(testJpgURL.toString());
		Docx4jUserAgent docx4jUserAgent = new Docx4jUserAgent();
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(null, content -> new byte[0]);
		sepXHTMLImageHandler.addImage(docx4jUserAgent, wordMLPackage, paragraph, e, 100L, null);
		Assert.assertEquals(paragraph.getContent().size(), 1);
		R r = (R) paragraph.getContent().get(0);
		Assert.assertEquals(r.getContent().size(), 1);
		Drawing drawing = (Drawing) r.getContent().get(0);
		List<Object> anchorOrInline = drawing.getAnchorOrInline();
		Assert.assertEquals(anchorOrInline.size(), 1);
	}

	@Test
	public void addEmbeddedImage() throws InvalidFormatException, JAXBException {
		P paragraph = Context.getWmlObjectFactory().createP();
		Element e = Mockito.mock(Element.class);
		URL testJpgURL = getClass().getClassLoader().getResource("test.jpg");
		Mockito.when(e.hasAttribute(XHTMLImageHandlerImpl.EMBEDDED_ID)).thenReturn(Boolean.TRUE);
		Mockito.when(e.getAttribute(XHTMLImageHandlerImpl.EMBEDDED_ID)).thenReturn("emf:embeddedImageId");
		Mockito.when(e.getAttribute(XHTMLImageHandlerImpl.SRC)).thenReturn("");
		Docx4jUserAgent docx4jUserAgent = new Docx4jUserAgent();
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(null, content -> {
			assertEquals("emf:embeddedImageId", content);
			try {
				return IOUtils.toByteArray(testJpgURL.openStream());
			} catch (IOException e1) {
				fail(e1.getMessage());
				return new byte[0];
			}
		});
		sepXHTMLImageHandler.addImage(docx4jUserAgent, wordMLPackage, paragraph, e, 100L, null);
		Assert.assertEquals(paragraph.getContent().size(), 1);
		R r = (R) paragraph.getContent().get(0);
		Assert.assertEquals(r.getContent().size(), 1);
		Drawing drawing = (Drawing) r.getContent().get(0);
		List<Object> anchorOrInline = drawing.getAnchorOrInline();
		Assert.assertEquals(anchorOrInline.size(), 1);
	}

	@Test
	public void addEmbeddedImageFromCache() throws InvalidFormatException, JAXBException {
		P paragraph = Context.getWmlObjectFactory().createP();
		Element e = Mockito.mock(Element.class);
		URL testJpgURL = getClass().getClassLoader().getResource("test.jpg");
		Mockito.when(e.hasAttribute(XHTMLImageHandlerImpl.EMBEDDED_ID)).thenReturn(Boolean.TRUE);
		Mockito.when(e.getAttribute(XHTMLImageHandlerImpl.EMBEDDED_ID)).thenReturn("emf:embeddedImageId");
		Mockito.when(e.getAttribute(XHTMLImageHandlerImpl.SRC)).thenReturn("");
		Docx4jUserAgent docx4jUserAgent = new Docx4jUserAgent();
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(null, content -> {
			assertEquals("emf:embeddedImageId", content);
			try {
				return IOUtils.toByteArray(testJpgURL.openStream());
			} catch (IOException e1) {
				fail(e1.getMessage());
				return new byte[0];
			}
		});
		sepXHTMLImageHandler.addImage(docx4jUserAgent, wordMLPackage, paragraph, e, 100L, null);
		sepXHTMLImageHandler.addImage(docx4jUserAgent, wordMLPackage, paragraph, e, 100L, null);
		Assert.assertEquals(paragraph.getContent().size(), 2);
	}

	/**
	 * Width test.
	 */
	@Test
	public static void widthTest() {
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(null, content -> new byte[0]);
		sepXHTMLImageHandler.setMaxWidth(300, null);
		Assert.assertEquals(sepXHTMLImageHandler.getMaxWidth(), 300);
	}

	@Test
	public static void getTblCellMarginsNullStyleTest() {
		XHTMLImporterImpl importer = Mockito.mock(XHTMLImporterImpl.class);
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(importer, content -> new byte[0]);
		sepXHTMLImageHandler.setMaxWidth(300, null);
		Assert.assertEquals(sepXHTMLImageHandler.getTblCellMargins(), 0);
	}

	@Test
	public static void getTblCellMarginsEmptyStyleTest() {
		XHTMLImporterImpl importer = Mockito.mock(XHTMLImporterImpl.class);
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(importer, content -> new byte[0]);
		sepXHTMLImageHandler.setMaxWidth(300, "");
		Assert.assertEquals(sepXHTMLImageHandler.getTblCellMargins(), 0);
	}

	@Test
	public static void getTblCellMarginsNotFoundStyleTest() {
		XHTMLImporterImpl importer = Mockito.mock(XHTMLImporterImpl.class);
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(importer, content -> new byte[0]);
		sepXHTMLImageHandler.setMaxWidth(300, TEST_STYLE);
		Mockito.when(importer.getStyleByIdOrName(TEST_STYLE)).thenReturn(null);
		Assert.assertEquals(sepXHTMLImageHandler.getTblCellMargins(), 0);
	}

	@Test
	@SuppressWarnings("boxing")
	public static void getTblCellMarginsStyleFalseTest() {
		XHTMLImporterImpl importer = Mockito.mock(XHTMLImporterImpl.class);
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(importer, content -> new byte[0]);
		sepXHTMLImageHandler.setMaxWidth(300, TEST_STYLE);
		Style style = Mockito.mock(Style.class);
		Mockito.when(importer.getStyleByIdOrName(TEST_STYLE)).thenReturn(style);
		TableHelper tableHelper = Mockito.mock(TableHelper.class);
		Mockito.when(importer.getTableHelper()).thenReturn(tableHelper);
		Mockito.when(tableHelper.isTableStyle(style)).thenReturn(Boolean.FALSE).thenReturn(Boolean.TRUE);

		// false call
		Assert.assertEquals(sepXHTMLImageHandler.getTblCellMargins(), 0);
	}

	@Test
	@SuppressWarnings("boxing")
	public static void getTblCellMarginsStyleTrueWithNullCTTblPrBaseTest() {
		XHTMLImporterImpl importer = Mockito.mock(XHTMLImporterImpl.class);
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(importer, content -> new byte[0]);
		sepXHTMLImageHandler.setMaxWidth(300, TEST_STYLE);
		Style style = Mockito.mock(Style.class);
		Mockito.when(importer.getStyleByIdOrName(TEST_STYLE)).thenReturn(style);
		TableHelper tableHelper = Mockito.mock(TableHelper.class);
		Mockito.when(importer.getTableHelper()).thenReturn(tableHelper);
		Mockito.when(tableHelper.isTableStyle(style)).thenReturn(Boolean.TRUE);

		Mockito.when(style.getTblPr()).thenReturn(null);

		// true call with null ctTblPrBase
		Assert.assertEquals(sepXHTMLImageHandler.getTblCellMargins(), 0);
	}

	@Test
	@SuppressWarnings("boxing")
	public static void getTblCellMarginsStyleTrueWithCTTblPrBaseTest() {
		XHTMLImporterImpl importer = Mockito.mock(XHTMLImporterImpl.class);
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(importer, content -> new byte[0]);
		sepXHTMLImageHandler.setMaxWidth(300, TEST_STYLE);
		Style style = Mockito.mock(Style.class);
		Mockito.when(importer.getStyleByIdOrName(TEST_STYLE)).thenReturn(style);
		TableHelper tableHelper = Mockito.mock(TableHelper.class);
		Mockito.when(importer.getTableHelper()).thenReturn(tableHelper);
		Mockito.when(tableHelper.isTableStyle(style)).thenReturn(Boolean.TRUE);
		CTTblPrBase ctTblPrBase = Mockito.mock(CTTblPrBase.class);
		Mockito.when(style.getTblPr()).thenReturn(ctTblPrBase);
		CTTblCellMar ctTblCellMar = Mockito.mock(CTTblCellMar.class);
		Mockito.when(ctTblPrBase.getTblCellMar()).thenReturn(ctTblCellMar);
		TblWidth tblWidth = Mockito.mock(TblWidth.class);
		Mockito.when(ctTblCellMar.getLeft()).thenReturn(tblWidth);
		Mockito.when(ctTblCellMar.getRight()).thenReturn(tblWidth);
		Mockito.when(tblWidth.getW()).thenReturn(new BigInteger("100"));
		Assert.assertEquals(sepXHTMLImageHandler.getTblCellMargins(), 200);
	}

	@Test
	@SuppressWarnings("boxing")
	public static void getTblCellMarginsStyleTrueWithCTTblCellMarTest() {
		XHTMLImporterImpl importer = Mockito.mock(XHTMLImporterImpl.class);
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(importer, content -> new byte[0]);
		sepXHTMLImageHandler.setMaxWidth(300, TEST_STYLE);
		Style style = Mockito.mock(Style.class);
		Mockito.when(importer.getStyleByIdOrName(TEST_STYLE)).thenReturn(style);
		TableHelper tableHelper = Mockito.mock(TableHelper.class);
		Mockito.when(importer.getTableHelper()).thenReturn(tableHelper);
		Mockito.when(tableHelper.isTableStyle(style)).thenReturn(Boolean.TRUE);
		CTTblPrBase ctTblPrBase = Mockito.mock(CTTblPrBase.class);
		Mockito.when(style.getTblPr()).thenReturn(ctTblPrBase);
		Mockito.when(ctTblPrBase.getTblCellMar()).thenReturn(null);

		BasedOn basedOn = Mockito.mock(BasedOn.class);
		Mockito.when(style.getBasedOn()).thenReturn(basedOn);
		Mockito.when(basedOn.getVal()).thenReturn(null);
		Assert.assertEquals(sepXHTMLImageHandler.getTblCellMargins(), 0);
	}

	@Test
	@SuppressWarnings("boxing")
	public static void getTblCellMarginsStyleTrueWithNullCTTblCellMarNotEmptyBasedOnTest() {
		XHTMLImporterImpl importer = Mockito.mock(XHTMLImporterImpl.class);
		XHTMLImageHandlerImpl sepXHTMLImageHandler = new XHTMLImageHandlerImpl(importer, content -> new byte[0]);
		sepXHTMLImageHandler.setMaxWidth(300, TEST_STYLE);
		Style style = Mockito.mock(Style.class);
		Mockito.when(importer.getStyleByIdOrName(TEST_STYLE)).thenReturn(style);
		TableHelper tableHelper = Mockito.mock(TableHelper.class);
		Mockito.when(importer.getTableHelper()).thenReturn(tableHelper);
		Mockito.when(tableHelper.isTableStyle(style)).thenReturn(Boolean.TRUE);
		CTTblPrBase ctTblPrBase = Mockito.mock(CTTblPrBase.class);
		Mockito.when(style.getTblPr()).thenReturn(ctTblPrBase);
		CTTblCellMar ctTblCellMar = Mockito.mock(CTTblCellMar.class);
		Mockito.when(ctTblPrBase.getTblCellMar()).thenReturn(null).thenReturn(ctTblCellMar);

		BasedOn basedOn = Mockito.mock(BasedOn.class);
		Mockito.when(style.getBasedOn()).thenReturn(basedOn);
		Mockito.when(basedOn.getVal()).thenReturn(TEST_STYLE);
		TblWidth tblWidth = Mockito.mock(TblWidth.class);
		Mockito.when(ctTblCellMar.getLeft()).thenReturn(tblWidth);
		Mockito.when(ctTblCellMar.getRight()).thenReturn(tblWidth);
		Mockito.when(tblWidth.getW()).thenReturn(new BigInteger("100"));
		Assert.assertEquals(sepXHTMLImageHandler.getTblCellMargins(), 200);
	}

}
