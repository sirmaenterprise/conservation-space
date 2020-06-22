package com.sirma.itt.seip.instance.editoffline.updaters;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.sirma.itt.seip.instance.content.MimetypeConstants;
import com.sirma.itt.seip.instance.editoffline.exception.FileCustomPropertiesUpdateException;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Custom property updater of MSOffice files with mime types as follows:
 *
 * <pre>
 * 		application/vnd.ms-excel - both XLS and XLT extensions
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
@Extension(target = AbstractMSOfficeCustomPropertyUpdater.PLUGIN_NAME, order = 4)
public class XlsCustomPropertyUpdater extends AbstractMSOfficeCustomPropertyUpdater {
	private static final List<String> SUPPORTED_MIME_TYPES = Collections.singletonList(MimetypeConstants.XLS_XLT);

	@Override
	protected File updateFile(File fileIn, File fileOut, Serializable instanceId) {
		try (FileInputStream inputDocument = new FileInputStream(fileIn);
				BufferedOutputStream bufOStream = new BufferedOutputStream(new FileOutputStream(fileOut));
				HSSFWorkbook workBook = new HSSFWorkbook(inputDocument)) {

			addCustomPropertiesMSOffice2003(workBook.getDocumentSummaryInformation(), instanceId);

			workBook.write(bufOStream);
		} catch (Exception e) {
			throw new FileCustomPropertiesUpdateException(fileOut.getName(),
					"Error during creation of edit offline MSExcel 97-2003 file: " + fileOut.getName(), e);
		}
		return fileOut;
	}

	@Override
	protected List<String> getSupportedMimeTypes() {
		return SUPPORTED_MIME_TYPES;
	}
}
