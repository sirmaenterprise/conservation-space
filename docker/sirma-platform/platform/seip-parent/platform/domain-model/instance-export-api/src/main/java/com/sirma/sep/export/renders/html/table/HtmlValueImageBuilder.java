package com.sirma.sep.export.renders.html.table;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Builder for <b>ing</b> tag.
 * 
 * @author Boyan Tonchev
 */
public class HtmlValueImageBuilder extends HtmlValueElementBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(HtmlValueImageBuilder.class);
	private static final String IMAGE_PREFIX = "data:image/jpg;base64,";

	private BufferedImage src;
	private String title;

	/**
	 * Initialize the builder.
	 * 
	 * @param src
	 *            - image src.
	 * @param title
	 *            - title of image it will be set to <b>title</b> and <b>alt</b> attribute of <b>img</b> tag.
	 */
	public HtmlValueImageBuilder(BufferedImage src, String title) {
		super(new Element(Tag.valueOf(JsoupUtil.TAG_IMG), ""));
		this.src = src;
		this.title = title;
		element.addClass("htmlValueImage");
	}

	@Override
	public void build(Element td) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			ImageIO.write(src, "jpg", bos);
			String thumbnail = Base64.getEncoder().encodeToString(bos.toByteArray());
			String base64 = new StringBuilder(IMAGE_PREFIX.length() + thumbnail.length()).append(IMAGE_PREFIX).append(thumbnail).toString();
			addAttribute(JsoupUtil.ATTRIBUTE_SRC, base64);
			addAttribute(JsoupUtil.ATTRIBUTE_ALT, title);
			addAttribute(JsoupUtil.ATTRIBUTE_TITLE, title);
			float width = src.getWidth();
			float height = src.getHeight();
			if (height > width) {
				// calculate max-width and max-height if image is portrait
				int percentage = Math.round(100 * width / height);
				// max-width and max-height must be no more than 75%
				// otherwise  image will be cut after export. See CMF-26225
				percentage = percentage > 75 ? 75 : percentage;
				addStyle("max-width:" + percentage + "% !important;");
				addStyle("max-height:" + percentage + "% !important;");
			}
			td.appendChild(element);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

}
