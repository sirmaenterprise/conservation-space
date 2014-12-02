package com.sirma.cmf.web.workflow;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.inject.Named;

/**
 * The Class DrawBean.
 * 
 * @author y.yordanov
 */
@Named
@RequestScoped
public class DrawBean implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7860839404084292396L;

	/**
	 * Missing image property.
	 */
	private boolean missingImage;

	/**
	 * Renders process diagram on client.
	 * 
	 * @param out
	 *            jsf output stream
	 * @param data
	 *            key to fetch rendered resource
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void render(OutputStream out, Object data) throws IOException {

		if (data instanceof String) {
			String picKey = (String) data;
			BufferedImage bufferedImage = (BufferedImage) FacesContext
					.getCurrentInstance().getExternalContext().getSessionMap()
					.get(picKey);
			if (bufferedImage != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(bufferedImage, "png", baos);
				baos.flush();
				byte[] byteArray = baos.toByteArray();
				out.write(byteArray);
			}
		}
	}

	/**
	 * Getter method for missingImage.
	 * 
	 * @return the missingImage
	 */
	public boolean isMissingImage() {
		return missingImage;
	}

	/**
	 * Setter method for missingImage.
	 * 
	 * @param missingImage
	 *            the missingImage to set
	 */
	public void setMissingImage(boolean missingImage) {
		this.missingImage = missingImage;
	}

}
