package com.sirma.sep.content.idoc.nodes.image;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.nodes.AbstractNode;

/**
 * Image content node that represents a &lt;img/&gt; tags. The implementation could handle external and embedded images
 *
 * @author BBonev
 */
public class ImageNode extends AbstractNode {
	/**
	 * Content identifier of embedded images. These are images that are were base64 encoded in the node's src tag but
	 * are extracted in a separate content, stored in the content store. <br>
	 * The value of this attribute is the content id of that content.
	 */
	public static final String EMBEDDED_ID = "data-embedded-id";
	/**
	 * Attribute {@code src} that locates the image location(source)
	 */
	public static final String ATTR_SRC = "src";
	/**
	 * Attribute {@code height} that represents the image height.
	 */
	public static final String ATTR_HEIGHT = "height";
	/**
	 * Attribute {@code width} that represents the image width.
	 */
	public static final String ATTR_WIDTH = "width";
	/**
	 * Default mimetype used when setting embedded image data
	 */
	public static final String DEFAULT_MIMETYPE = "image/jpg";

	/**
	 * Instantiate new image node using the given element
	 *
	 * @param node
	 *            the source element
	 */
	public ImageNode(Element node) {
		super(node);
	}

	@Override
	public boolean isImage() {
		return true;
	}

	/**
	 * Check if the image dimensions are specified in the img tag.
	 *
	 * @return <code>true</code> if image dimensions are present
	 */
	public boolean hasImageDimensions() {
		return getElement().hasAttr(ATTR_WIDTH) && getElement().hasAttr(ATTR_HEIGHT);
	}

	/**
	 * Gets the image dimensions if specified in the node.
	 *
	 * @return the image dimensions
	 */
	public Optional<Dimension> getImageDimensions() {
		if (hasImageDimensions()) {
			int width = Integer.parseInt(getElement().attr(ATTR_WIDTH));
			int height = Integer.parseInt(getElement().attr(ATTR_HEIGHT));
			return Optional.of(new Dimension(width, height));
		}
		return Optional.empty();
	}

	/**
	 * Sets new image dimensions to the current image node
	 *
	 * @param width
	 *            the new image width
	 * @param height
	 *            the new image height
	 */
	public void setImageDimensions(int width, int height) {
		addProperty(ATTR_WIDTH, Integer.toString(width));
		addProperty(ATTR_HEIGHT, Integer.toString(height));
	}

	/**
	 * Checks if this node has embedded image content.
	 *
	 * @return <code>true</code> if the value of the {@code src} attribute has base64 encoded image
	 */
	public boolean isEmbedded() {
		String src = getSource();
		if (StringUtils.isBlank(src)) {
			return false;
		}
		return src.startsWith("data:");
	}

	/**
	 * Gets the embedded image mimetype
	 *
	 * @return the image mimetype if the source represents an embedded image
	 */
	public Optional<String> getEmbeddedImageMimetype() {
		if (!isEmbedded()) {
			return Optional.empty();
		}
		String source = getSource();
		int mimetypeStart = source.indexOf(':');
		int mimetypeEnd = source.indexOf(';');
		return Optional.of(source.substring(mimetypeStart + 1, mimetypeEnd));
	}

	/**
	 * Get the value of the {@code src} attribute in the image tag
	 *
	 * @return the attribute value as is
	 */
	public String getSource() {
		return getElement().attr(ATTR_SRC);
	}

	/**
	 * Gets the embedded image data bytes after base64 decoding.
	 *
	 * @return the decoded embedded image
	 */
	public Optional<byte[]> getEmbeddedData() {
		if (!isEmbedded()) {
			return Optional.empty();
		}
		String source = getSource();
		int dataIndex = source.indexOf(',');
		return Optional.of(Base64.getDecoder().decode(source.substring(dataIndex + 1)));
	}

	/**
	 * Set new value of the src attribute
	 *
	 * @param value
	 *            the new value to set
	 */
	public void setSource(String value) {
		addProperty(ATTR_SRC, Objects.requireNonNull(value, "The new src attribute value cannot be null"));
	}

	/**
	 * Sets new embedded image in this node. The image bytes will be encoded to base64 before setting.
	 *
	 * @param mimetype
	 *            of the provided image. If not specified the default mimetype
	 *            {@link #DEFAULT_MIMETYPE}={@value #DEFAULT_MIMETYPE} will be used
	 * @param bytes
	 *            the not encoded image bytes to set
	 */
	public void setSource(String mimetype, byte[] bytes) {
		Objects.requireNonNull(bytes, "Cannot set null byte[] for image source");
		setSource(mimetype, Base64.getEncoder().encodeToString(bytes));
	}

	/**
	 * Remove the source attribute of the current image node
	 */
	public void revemoSource() {
		removeProperty(ATTR_SRC);
	}

	/**
	 * Get attribute value that represent an internal identifier that can be used for loading the image. This is filled
	 * when content (base64 or other addresses are read and stored internally)
	 *
	 * @return embedded content identifier or {@code null} if not extracted
	 */
	public String getEmbeddedId() {
		return getProperty(EMBEDDED_ID);
	}

	/**
	 * Sets embedded identifier.
	 *
	 * @param id
	 *            the new identifier to set. If {@code null} it will remove the previous value if any
	 * @see #getEmbeddedId()
	 */
	public void setEmbeddedId(String id) {
		addProperty(EMBEDDED_ID, id);
	}

	/**
	 * Clear any embedded id set.
	 */
	public void removeEmbeddedId() {
		removeProperty(EMBEDDED_ID);
	}

	/**
	 * Sets new embedded image in this node. The image data is read via the given {@link InputStream}. The stream data
	 * will be encoded to base64 before setting.
	 *
	 * @param mimetype
	 *            of the provided image. If not specified the default mimetype
	 *            {@link #DEFAULT_MIMETYPE}={@value #DEFAULT_MIMETYPE} will be used
	 * @param stream
	 *            the stream to read and set
	 * @throws IOException
	 *             in case of problem while reading the input stream
	 */
	public void setSource(String mimetype, InputStream stream) throws IOException {
		Objects.requireNonNull(stream, "Cannot set null InputStream for image source");
		try (InputStream input = stream) {
			setSource(mimetype, Base64.getEncoder().encodeToString(IOUtils.toByteArray(input)));
		}
	}

	/**
	 * Sets new embedded image in this node. The second argument should be base64 encoded text of the image.
	 *
	 * @param mimetype
	 *            of the provided image. If not specified the default mimetype
	 *            {@link #DEFAULT_MIMETYPE}={@value #DEFAULT_MIMETYPE} will be used
	 * @param encoded
	 *            image content to set
	 */
	public void setSource(String mimetype, String encoded) {
		Objects.requireNonNull(encoded, "Cannot set null Base64 encoded data for image source");
		setSource("data:" + getMimeType(mimetype) + ";base64," + encoded);
	}

	private static String getMimeType(String mime) {
		if (StringUtils.isBlank(mime)) {
			return DEFAULT_MIMETYPE;
		}
		return mime.trim();
	}
}
