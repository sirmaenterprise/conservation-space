package com.sirma.itt.seip.instance.editoffline.updaters;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.sirma.itt.seip.instance.content.MimetypeConstants;
import com.sirma.itt.seip.instance.editoffline.exception.FileCustomPropertiesUpdateException;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Custom property updater of MSOffice files with mime types as follows:
 *
 * <pre>
 * 		application/vnd.ms-powerpoint - both PPT and POT extensions
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
@Extension(target = AbstractMSOfficeCustomPropertyUpdater.PLUGIN_NAME, order = 6)
public class PptCustomPropertyUpdater extends AbstractMSOfficeCustomPropertyUpdater {
	private static final List<String> SUPPORTED_MIME_TYPES = Collections.singletonList(MimetypeConstants.PPT_POT);

	@Override
	protected File updateFile(File fileIn, File fileOut, Serializable instanceId) {
		try (BufferedInputStream bufIStream = new BufferedInputStream(new FileInputStream(fileIn));
				POIFSFileSystem fileSystem = new POIFSFileSystem(bufIStream);
				HSLFSlideShowImpl slideShow = new HSLFSlideShowImpl(fileSystem);
				BufferedOutputStream bufOStream = new BufferedOutputStream(new FileOutputStream(fileOut))) {

			addCustomPropertiesMSOffice2003(slideShow.getDocumentSummaryInformation(), instanceId);

			slideShow.write(bufOStream);
		} catch (Exception e) {
			throw new FileCustomPropertiesUpdateException(fileOut.getName(),
					"Error during creation of edit offline MSPowerPoint 97-2003 file: " + fileOut.getName(), e);
		}
		return fileOut;
	}

	@Override
	protected List<String> getSupportedMimeTypes() {
		return SUPPORTED_MIME_TYPES;
	}
}
