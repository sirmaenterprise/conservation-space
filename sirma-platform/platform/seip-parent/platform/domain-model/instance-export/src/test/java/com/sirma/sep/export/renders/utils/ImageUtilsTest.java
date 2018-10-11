package com.sirma.sep.export.renders.utils;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.sep.export.renders.utils.ImageUtils;

import java.awt.image.BufferedImage;

/**
 * @author Boyan Tonchev.
 */
public class ImageUtilsTest {

	@Mock
	private BufferedImage srcImage;

	/**
	 * Runs before each method and setup mockito.
	 */
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(srcImage.getWidth()).thenReturn(300);
		Mockito.when(srcImage.getHeight()).thenReturn(200);
	}

	/**
	 * Tests method getSubImageTest scenarios when wanted height and widt are bigger than image height and width.
	 */
	@Test
	public void getSubImageBiggerThanOriginalTest() {
		ImageUtils.getSubImage(srcImage, 0, 0 , 2000, 1000);
		Mockito.verify(srcImage).getSubimage(0, 0, 300, 200);
	}

	/**
	 * Tests method getSubImageTest scenarios y coordinate plus wanted height is bigger than image height.
	 */
	@Test
	public void getSubImageYandHeightBiggerImageSizeTest() {
		ImageUtils.getSubImage(srcImage, 3, 400 , 200, 100);
		Mockito.verify(srcImage).getSubimage(3, 0, 200, 100);
	}

	/**
	 * Tests method getSubImageTest scenarios x coordinate plus wanted width is bigger than image width.
	 */
	@Test
	public void getSubImageXandWidthBiggerImageSizeTest() {
		ImageUtils.getSubImage(srcImage, 400, -4 , 200, 100);
		Mockito.verify(srcImage).getSubimage(0, 0, 200, 100);
	}

	/**
	 * Tests method getSubImageTest scenarios with negative coordinates.
	 */
	@Test
	public void getSubImageNegativeCoordinatesTest() {
		ImageUtils.getSubImage(srcImage, -4, -4 , 200, 100);
		Mockito.verify(srcImage).getSubimage(0, 0, 200, 100);
	}

	/**
	 * Tests method getSubImageTest correct parameters.
	 */
	@Test
	public void getSubImageTest() {
		ImageUtils.getSubImage(srcImage, 4, 4 , 200, 100);
		Mockito.verify(srcImage).getSubimage(4, 4, 200, 100);
	}
}
