package com.sirma.sep.export.renders.html.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import com.sirma.sep.export.renders.utils.JsoupUtil;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;

/**
 * Tests for HtmlValueImageBuilder.
 *
 * @author Boyan Tonchev
 */
public class HtmlValueImageBuilderTest {

    private static String PORTRAIT_75_PERCENTAGE = "portrait-75-percentage.jpg";
    private static String PORTRAIT_75_PERCENTAGE_LESS = "portrait-75-percentage-less.jpg";
    private static String LANDSCAPE = "landscape.jpg";

    @Test
    public void should_MaxWidthNotBeSet() throws IOException {
        try (InputStream inputStream = loadImage(LANDSCAPE)) {
            HtmlValueImageBuilder htmlValueImageBuilder = new HtmlValueImageBuilder(ImageIO.read(inputStream), "title");
            Element td = new Element(Tag.valueOf(JsoupUtil.TAG_TD), "");
            htmlValueImageBuilder.build(td);
            Element img = td.select("img").get(0);
            assertFalse(hasStyle(img, "max-width"));
        }
    }

    @Test
    public void should_MaxHeightNotBeSet() throws IOException {
        try (InputStream inputStream = loadImage(LANDSCAPE)) {
            HtmlValueImageBuilder htmlValueImageBuilder = new HtmlValueImageBuilder(ImageIO.read(inputStream), "title");
            Element td = new Element(Tag.valueOf(JsoupUtil.TAG_TD), "");
            htmlValueImageBuilder.build(td);
            Element img = td.select("img").get(0);
            assertFalse(hasStyle(img, "max-height"));
        }
    }

    @Test
    public void should_MaxHeightBe29Percentage() throws IOException {
        try (InputStream inputStream = loadImage(PORTRAIT_75_PERCENTAGE_LESS)) {
            HtmlValueImageBuilder htmlValueImageBuilder = new HtmlValueImageBuilder(ImageIO.read(inputStream), "title");
            Element td = new Element(Tag.valueOf(JsoupUtil.TAG_TD), "");
            htmlValueImageBuilder.build(td);
            Element img = td.select("img").get(0);
            assertTrue(hasStyle(img, "max-height:29% !important"));
        }
    }

    @Test
    public void should_MaxWidthBe29Percentage() throws IOException {
        try (InputStream inputStream = loadImage(PORTRAIT_75_PERCENTAGE_LESS)) {
            HtmlValueImageBuilder htmlValueImageBuilder = new HtmlValueImageBuilder(ImageIO.read(inputStream), "title");
            Element td = new Element(Tag.valueOf(JsoupUtil.TAG_TD), "");
            htmlValueImageBuilder.build(td);
            Element img = td.select("img").get(0);
            assertTrue(hasStyle(img, "max-width:29% !important"));
        }
    }

    @Test
    public void should_MaxWidthBe75Percentage() throws IOException {
        try (InputStream inputStream = loadImage(PORTRAIT_75_PERCENTAGE)) {
            HtmlValueImageBuilder htmlValueImageBuilder = new HtmlValueImageBuilder(ImageIO.read(inputStream), "title");
            Element td = new Element(Tag.valueOf(JsoupUtil.TAG_TD), "");
            htmlValueImageBuilder.build(td);
            Element img = td.select("img").get(0);
            assertTrue(hasStyle(img, "max-width:75% !important"));
        }
    }

    @Test
    public void should_MaxHeightBe75Percentage() throws IOException {
        try (InputStream inputStream = loadImage(PORTRAIT_75_PERCENTAGE)) {
            HtmlValueImageBuilder htmlValueImageBuilder = new HtmlValueImageBuilder(ImageIO.read(inputStream), "title");
            Element td = new Element(Tag.valueOf(JsoupUtil.TAG_TD), "");
            htmlValueImageBuilder.build(td);
            Element img = td.select("img").get(0);
            assertTrue(hasStyle(img, "max-height:75% !important"));
        }
    }

    /**
     * Test method build scenarios with exception.
     */
    @Test
    public void buildTest() {
        HtmlValueImageBuilder htmlValueImageBuilder = new HtmlValueImageBuilder(null, "tile");
        Element td = new Element(Tag.valueOf(JsoupUtil.TAG_TD), "");
        htmlValueImageBuilder.build(td);

        assertEquals("", td.html());
    }

    private boolean hasStyle(Element element, String styleToBeCheck) {
        String style = element.attr("style");
        return style != null && style.contains(styleToBeCheck);
    }

    private InputStream loadImage(String fileName) {
        return getClass().getResourceAsStream(fileName);
    }
}
