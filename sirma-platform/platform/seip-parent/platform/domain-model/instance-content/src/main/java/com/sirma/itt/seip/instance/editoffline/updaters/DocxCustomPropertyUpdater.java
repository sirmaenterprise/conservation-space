package com.sirma.itt.seip.instance.editoffline.updaters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import com.sirma.itt.seip.instance.content.MimetypeConstants;
import com.sirma.itt.seip.instance.editoffline.exception.FileCustomPropertiesUpdateException;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Custom property updater of MSOffice files with mime types as follows:
 *
 * <pre>
 * 		application/vnd.openxmlformats-officedocument.wordprocessingml.document - DOCX
 * 		application/vnd.ms-word.document.macroEnabled.12 - DOCM
 * 		application/vnd.ms-word.template.macroEnabled.12 - DOTM
 * 		application/vnd.openxmlformats-officedocument.wordprocessingml.template - DOTX
 * </pre>
 *
 * Sets custom property:
 *
 * <pre>
 * 		rest_url = <protocol>://<host>:<port>/emf/api/instances/<instanceId>/actions/edit-offline-check-in?version=<version>
 * </pre>
 *
 * @author T. Dossev
 */
@Extension(target = AbstractMSOfficeCustomPropertyUpdater.PLUGIN_NAME, order = 1)
public class DocxCustomPropertyUpdater extends AbstractMSOfficeCustomPropertyUpdater {
	private static final List<String> SUPPORTED_MIME_TYPES = Arrays.asList(MimetypeConstants.DOCX,
			MimetypeConstants.DOCM, MimetypeConstants.DOTM, MimetypeConstants.DOTX);

	@Override
	protected File updateFile(File fileIn, File fileOut, Serializable instanceId) {
		try (FileInputStream inputDocument = new FileInputStream(fileIn);
				XWPFDocument document = new XWPFDocument(inputDocument);
				FileOutputStream out = new FileOutputStream(fileOut)) {

			addCustomPropertiesMSOffice2007(document.getProperties(), instanceId);

			document.write(out);
		} catch (Exception e) {
			throw new FileCustomPropertiesUpdateException(fileOut.getName(),
					"Error during creation of edit offline MSWord file: " + fileOut.getName(), e);
		}
		return fileOut;
	}

	@Override
	protected List<String> getSupportedMimeTypes() {
		return SUPPORTED_MIME_TYPES;
	}
}
