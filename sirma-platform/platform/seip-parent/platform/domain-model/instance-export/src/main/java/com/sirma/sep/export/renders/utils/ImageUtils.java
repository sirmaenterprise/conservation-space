package com.sirma.sep.export.renders.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.StringReader;

import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.time.TimeTracker;

/**
 * Util class for work with BufferedImage/s.
 * 
 * @author Hristo Lungov
 */
public class ImageUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);

	/**
	 * Util Class no public constructor.
	 */
	private ImageUtils() {
		// utility class
	}

	/**
	 * Joins two BufferedImage where overlay must be a transparent png. After Join the PNG BufferedImage is converted to
	 * JPG BufferedImage. Note: overlay must be equal or smaller than source image.
	 *
	 * @param source
	 *            can be any type of image
	 * @param overlay
	 *            should be transparent png
	 * @return JPG image
	 */
	public static BufferedImage joinImages(BufferedImage source, BufferedImage overlay) {
		// create a new buffer and draw two image into the new image
		BufferedImage newImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = newImage.createGraphics();
		g2.drawImage(source, 0, 0, null);
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0F);
		g2.setComposite(ac);
		g2.drawImage(overlay, 0, 0, null);
		g2.dispose();
		return convertPNGToJPEG(newImage);
	}

	/**
	 * Transform PNG Image to JPEG Image. PNG format is bigger than JPEG also cannot convert to JPEG an image with an
	 * alpha channel.
	 *
	 * @param scrImage
	 *            the PNG image to convert
	 * @return coverted image
	 */
	public static BufferedImage convertPNGToJPEG(BufferedImage scrImage) {
		if (scrImage.getColorModel().hasAlpha()) {
			BufferedImage ret = new BufferedImage(scrImage.getWidth(), scrImage.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D retG = ret.createGraphics();
			retG.setColor(Color.white);
			retG.fillRect(0, 0, scrImage.getWidth(), scrImage.getHeight());
			retG.drawImage(scrImage, 0, 0, null);
			return ret;
		}
		return scrImage;
	}

	/**
	 * Scale passed image to desired width and height.
	 *
	 * @param srcImage
	 *            the image to scale
	 * @param width
	 *            the desired new width
	 * @param height
	 *            the desired new height
	 * @return the scaled image
	 */
	public static BufferedImage scaleImage(BufferedImage srcImage, int width, int height) {
		if (srcImage != null && (srcImage.getWidth() != width || srcImage.getHeight() != height)) {
			BufferedImage tmp = new BufferedImage(width, height, srcImage.getType());
			Graphics2D g2 = tmp.createGraphics();
			g2.drawImage(srcImage, 0, 0, width, height, null);
			g2.dispose();
			return tmp;
		}
		return srcImage;
	}

	/**
	 * Gets the sub image from original image by specified X,Y coordinates and Width and Height.
	 *
	 * @param srcImage
	 *            the image to get sub image from
	 * @param x
	 *            the X coordinate of the upper-left corner of the specified rectangular region
	 * @param y
	 *            the Y coordinate of the upper-left corner of the specified rectangular region
	 * @param factorWidth
	 *            the width of the specified rectangular region
	 * @param factorHeight
	 *            the height of the specified rectangular region
	 * @return sub-image from original image
	 */
	public static BufferedImage getSubImage(BufferedImage srcImage, int x, int y, int factorWidth, int factorHeight) {

		int clearX = x < 0 ? 0 : x;
		int clearY = y < 0 ? 0 : y;
		int imageWidth = srcImage.getWidth();
		int imageHeight = srcImage.getHeight();
		int clearWidth = factorWidth < imageWidth ? factorWidth : imageWidth;
		int clearHeight = factorHeight < imageHeight ? factorHeight : imageHeight;

		//check if x coordinate is bigger than width of image
		if (clearX + clearWidth > imageWidth) {
			clearX = 0;
		}
		//check if y coordinate is bigger than width of image
		if (clearY + clearHeight > imageHeight) {
			clearY = 0;
		}

		return srcImage.getSubimage(clearX, clearY, clearWidth, clearHeight);
	}

	/**
	 * Transforms SVG tag to PNG image.
	 *
	 * @param svgContent
	 *            the html representation of svg tag
	 * @param originalWidth
	 *            the original image width
	 * @param originalHeight
	 *            the original image height
	 * @return the image
	 * @throws TranscoderException
	 *             the transcoder exception if something went wrong
	 */
	public static BufferedImage svgToPNG(String svgContent, int originalWidth, int originalHeight) throws TranscoderException {
		TimeTracker tracker = TimeTracker.createAndStart();
		SVGToPNGTranscoder transcoder = new SVGToPNGTranscoder();
		transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, Float.valueOf(originalWidth));
		transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, Float.valueOf(originalHeight));
		transcoder.addTranscodingHint(XMLAbstractTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
		try (StringReader reader = new StringReader(svgContent)) {
			TranscoderInput transcoderInput = new TranscoderInput(reader);
			transcoder.transcode(transcoderInput, new TranscoderOutput());
			return transcoder.getBufferedImage();
		} finally {
			LOGGER.info("Transformation svgToPNG took: {} ms", Long.valueOf(tracker.stop()));
		}
	}

	/**
	 * The Class SVGToPNGTranscoder used to directly fetch the BufferedImage skipping write/read operations to save
	 * processor time.
	 */
	private static class SVGToPNGTranscoder extends ImageTranscoder {

		private BufferedImage bufferedImage = null;

		/**
		 * Constructs a new transcoder that produces png images.
		 */
		public SVGToPNGTranscoder() {
			hints.put(KEY_FORCE_TRANSPARENT_WHITE, Boolean.FALSE);
		}

		@Override
		public void writeImage(BufferedImage image, TranscoderOutput outputStream) {
			// removed writting to file, because we need only buffered image which saves write/read operations
			this.bufferedImage = image;
		}

		/**
		 * Gets the buffered image.
		 *
		 * @return the buffered image
		 */
		public BufferedImage getBufferedImage() {
			return bufferedImage;
		}

		@Override
		public BufferedImage createImage(int imageWidth, int imageHeight) {
			return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		}

	}

}
