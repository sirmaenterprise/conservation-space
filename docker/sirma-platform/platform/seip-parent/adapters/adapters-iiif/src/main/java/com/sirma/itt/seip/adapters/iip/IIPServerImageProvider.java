package com.sirma.itt.seip.adapters.iip;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.adapters.iiif.Dimension;
import com.sirma.itt.seip.adapters.iiif.ImageServerConfigurations;
import com.sirma.itt.seip.rest.exceptions.HTTPClientRuntimeException;
import com.sirma.itt.seip.rest.utils.HttpClientUtil;

/**
 * Service to access and extract images from an image server
 * that supports <a href="http://iiif.io/api/image">IIIF Image API</a>
 *
 * @author radoslav
 */
public class IIPServerImageProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ImageServerConfigurations imageServerConfigurations;

	/**
	 * Gets the full image.
	 *
	 * @param imageId
	 * 		The id of the image.
	 * @return Stream containing the image or null if cannot extract the image.
	 */
	public InputStream getImage(String imageId) {
		String urlToImage = getImageUrl(imageId);
		try {
			return HttpClientUtil.callRemoteService(new HttpGet(urlToImage)).getInputStream();
		} catch (HTTPClientRuntimeException e) {
			LOGGER.debug("Could not fetch image {} from the iip server, due to: {}", urlToImage, e.getMessage());
			LOGGER.trace("Failed to fetch image from the iip server due to", e);
		}
		return null;
	}

	/**
	 * Gets the image scaled to the provided dimensions.
	 * If either width or height is null then it automatically calculates the null dimension
	 * using the width/height ratio of the full image size.
	 * <ol>
	 * <li> If <code>bestFit</code> is true then the image will be scaled for the best fit such that the
	 * resulting width and height are less than or equal to the provided dimensions. </li>
	 * <li>If <code>bestFit</code> is false then image size will equal to the provided dimensions.
	 * </ol>
	 * <a href="https://iiif.io/api/image/2.1/#size">More information about image size request parameters</a>
	 *
	 * @param imageId The id of the image.
	 * @param size    Width and height of the requested image. At least one of the dimensions should be different from 0 or null.
	 * @param bestFit - switch on/off best fit of requested image.
	 * @return Stream containing the image or null if it cannot get the image.
	 */
	public InputStream getImage(String imageId, Dimension<Integer> size, boolean bestFit) {
		String urlToImage = getImageUrl(imageId, size, bestFit);
		try {
			return HttpClientUtil.callRemoteService(new HttpGet(urlToImage)).getInputStream();
		} catch (HTTPClientRuntimeException e) {
			LOGGER.debug("Could not fetch image {} from the iip server, due to: {}", urlToImage, e.getMessage());
			LOGGER.trace("Failed to fetch image from the iip server due to", e);
		}

		return null;
	}

	/**
	 * Gets the url of the image.
	 *
	 * @param imageId
	 * 		The image id.
	 * @return The image url.
	 */
	public String getImageUrl(String imageId) {
		return getBaseUri(imageId) + "/full/full/0/default.jpg";
	}

	/**
	 * Gets the url of the image with size:
	 * <ol>
	 * <li> If <code>bestFit</code> is true then the image will be scaled for the best fit such that the
	 * resulting width and height are less than or equal to the provided dimensions. </li>
	 * <li>If <code>bestFit</code> is false then image size will equal to the provided dimensions.
	 * </ol>
	 * <a href="https://iiif.io/api/image/2.1/#size">More information about image size request parameters</a>
	 *
	 * @param imageId The image id.
	 * @param size    The dimensions of the requested image
	 * @param bestFit - switch on/off best fit of requested image.
	 * @return The url of the image.
	 */
	public String getImageUrl(String imageId, Dimension<Integer> size, boolean bestFit) {
		return getBaseUri(imageId) + "/full/" + getSizeParam(size, bestFit) + "/0/default.jpg";
	}

	private static String getSizeParam(Dimension<Integer> size, boolean bestFit) {
		if (size.getWidth() != null) {
			if (size.getHeight() != null) {
				String sizeParam = bestFit ? "!" : "";
				return sizeParam + size.getWidth() + "," + size.getHeight();
			}
			return size.getWidth() + ",";
		} else {
			return "," + size.getHeight();
		}
	}

	private String getBaseUri(String imageId) {
		return new StringBuilder(256).append(imageServerConfigurations.getIiifServerAddress().requireConfigured().get())
				.append(imageId)
				.append(StringUtils.trimToEmpty(imageServerConfigurations.getIiifServerAddressSuffix().get()))
				.toString();
	}
}
