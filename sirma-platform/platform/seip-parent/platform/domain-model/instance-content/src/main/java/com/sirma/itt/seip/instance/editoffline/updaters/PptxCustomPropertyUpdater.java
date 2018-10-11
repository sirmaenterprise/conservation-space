package com.sirma.itt.seip.instance.editoffline.updaters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.usermodel.XSLFSlideShow;

import com.sirma.itt.seip.instance.content.MimetypeConstants;
import com.sirma.itt.seip.instance.editoffline.exception.FileCustomPropertiesUpdateException;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Custom property updater of MSOffice files with mime types as follows:
 *
 * <pre>
 * 		application/vnd.openxmlformats-officedocument.presentationml.presentation - PPTX
 * 		application/vnd.openxmlformats-officedocument.presentationml.template - POTX
 * 		application/vnd.ms-powerpoint.presentation.macroEnabled.12 - PPTM
 * 		application/vnd.ms-powerpoint.template.macroEnabled.12 - POTM
 * </pre>
 *
 * Sets custom property:
 *
 * <pre>
 * 		rest_url = <protocol>://<host>:<port>/emf/api/instances/<instanceId>/actions/editOffline?version=<version>
 * </pre>
 *
 * @author T. Dossev
 */
@Extension(target = AbstractMSOfficeCustomPropertyUpdater.PLUGIN_NAME, order = 5)
public class PptxCustomPropertyUpdater extends AbstractMSOfficeCustomPropertyUpdater {
	private static final List<String> SUPPORTED_MIME_TYPES = Arrays.asList(MimetypeConstants.POTX,
			MimetypeConstants.POTM, MimetypeConstants.PPTM, MimetypeConstants.PPTX);

	@SuppressWarnings("resource")
	@Override
	protected File updateFile(File fileIn, File fileOut, Serializable instanceId) {
		try (OPCPackage pkg = OPCPackage.open(fileIn);
				FileOutputStream out = new FileOutputStream(fileOut)) {
			XSLFSlideShow slideshow = new XSLFSlideShow(pkg);

			addCustomPropertiesMSOffice2007(slideshow.getProperties(), instanceId);

			slideshow.write(out);
		} catch (Exception e) {
			throw new FileCustomPropertiesUpdateException(fileOut.getName(),
					"Error during creation of edit offline MSPowerPoint file: " + fileOut.getName(), e);
		}
		return fileOut;
	}

	@Override
	protected List<String> getSupportedMimeTypes() {
		return SUPPORTED_MIME_TYPES;
	}
}
