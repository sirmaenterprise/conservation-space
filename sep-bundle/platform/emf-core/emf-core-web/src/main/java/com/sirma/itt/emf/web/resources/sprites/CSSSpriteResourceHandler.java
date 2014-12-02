package com.sirma.itt.emf.web.resources.sprites;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.google.common.hash.Hashing;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.plugin.PathDefinition;
import com.sirma.itt.emf.web.resources.WebResource;
import com.sirma.itt.emf.web.resources.WebResourceHandler;

/**
 * Builds a single sprite image from multiple images declared in the extension
 * points.
 *
 * @author Adrian Mitev
 */
@Extension(target = WebResourceHandler.TARGET_NAME, order = 0.1)
public class CSSSpriteResourceHandler implements WebResourceHandler {

	@Inject
	@ExtensionPoint(SpriteDefinition.SINGLE_FILE_TARGET_NAME)
	private Iterable<SpriteDefinition> files;

	@Inject
	@ExtensionPoint(SpriteDefinition.DIRECTORY_TARGET_NAME)
	private Iterable<SpriteDefinition> directories;

	@Override
	public boolean canHandle(String path, HttpServletRequest request, ServletContext servletContext) {
		return "sprites.png".equals(path) || "sprites.css".equals(path);
	}

	@Override
	public WebResource handle(String path, HttpServletRequest request, ServletContext servletContext) {
		Set<String> resources = new LinkedHashSet<String>();

		for (PathDefinition directory : directories) {
			Set<String> resourcePaths = servletContext.getResourcePaths(directory.getPath());
			if (resourcePaths != null) {
				resources.addAll(resourcePaths);
			}
		}

		for (PathDefinition file : files) {
			resources.add(file.getPath());
		}

		if ("sprites.png".equals(path)) {
			return buildSpriteImage(servletContext, resources);
		}

		if ("sprites.css".equals(path)) {
			return buildSpriteCSS(servletContext, resources);
		}

		return null;
	}

	/**
	 * Constructs a Resource representing a CSS file containing a CSS rules for
	 * applying a sprite image.
	 *
	 * @param servletContext
	 *            used for resource gathering.
	 * @param resources
	 *            list of the plugged resources.
	 * @return the constructed resource.
	 */
	private WebResource buildSpriteCSS(ServletContext servletContext, Set<String> resources) {
		int totalHeight = 0;
		StringBuilder spritesStyleBuilder = new StringBuilder();
		for (String resourcePath : resources) {
			try {
				if (!resourcePath.endsWith("/")) {
					BufferedImage bufferedImage = ImageIO.read(servletContext.getResourceAsStream(resourcePath));

					String imageName = resourcePath;
					int separatorIndex = imageName.lastIndexOf("/");
					if (separatorIndex != -1) {
						imageName = imageName.substring(separatorIndex);
					}

					// remove file extension
					int extensionIndex = imageName.lastIndexOf(".");
					if (extensionIndex != -1) {
						imageName = imageName.substring(0, extensionIndex);
					}

					if (imageName.startsWith("/")) {
						imageName = imageName.substring(1);
					}

					// append style for the current image
					spritesStyleBuilder.append(".sprite-" + imageName).append(" {").append(StringUtils.NEW_LINE);
					spritesStyleBuilder.append("\tbackground-position: 0px -").append(totalHeight).append("px;").append(StringUtils.NEW_LINE);
					spritesStyleBuilder.append("\twidth: ").append(bufferedImage.getWidth()).append("px;").append(StringUtils.NEW_LINE);
					spritesStyleBuilder.append("\theight: ").append(bufferedImage.getHeight()).append("px;").append(StringUtils.NEW_LINE);
					spritesStyleBuilder.append("}").append(StringUtils.NEW_LINE).append(StringUtils.NEW_LINE);

					// update total height and max width
					totalHeight = totalHeight + bufferedImage.getHeight();
				}
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}

		String content = spritesStyleBuilder.toString();
		String hash = Hashing.sha1().hashString(content).toString();

		return new WebResource("sprites.css", content.getBytes(), "text/css", hash, true);
	}

	/**
	 * Constructs a Resource representing a single sprite image combining all
	 * the images defined through the extension points.
	 *
	 * @param servletContext
	 *            used for resource gathering.
	 * @param resources
	 *            list of the plugged resources.
	 * @return the constructed resource.
	 */
	private WebResource buildSpriteImage(ServletContext servletContext, Set<String> resources) {
		// max width and total height are kept because the the images final
		// image will be positioned vertically
		int maxWidth = 0;
		int totalHeight = 0;
		List<BufferedImage> images = new ArrayList<BufferedImage>(resources.size());

		for (String resourcePath : resources) {
			try {
				if (!resourcePath.endsWith("/")) {
					BufferedImage bufferedImage = ImageIO.read(servletContext.getResourceAsStream(resourcePath));
					images.add(bufferedImage);

					// update total height and max width
					totalHeight = totalHeight + bufferedImage.getHeight();

					if (bufferedImage.getWidth() > maxWidth) {
						maxWidth = bufferedImage.getWidth();
					}
				}
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}

		// append all images in a single image
		BufferedImage sprite = new BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
		int currentY = 0;
		Graphics g = sprite.getGraphics();

		for (BufferedImage bufferedImage : images) {
			g.drawImage(bufferedImage, 0, currentY, null);
			currentY = currentY + bufferedImage.getHeight();
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			ImageIO.write(sprite, "png", outputStream);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		byte[] bytes = outputStream.toByteArray();
		String hash = Hashing.sha1().hashBytes(bytes).toString();

		return new WebResource("sprites.png", bytes, "image/png", hash, true);
	}

}
