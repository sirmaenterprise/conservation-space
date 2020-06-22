package com.sirma.itt.seip.instance.editoffline.updaters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.sirma.itt.seip.instance.content.MimetypeConstants;
import com.sirma.itt.seip.instance.editoffline.exception.FileCustomPropertiesUpdateException;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Custom property updater of MSOffice files with mime types as follows:
 *
 * <pre>
 * 		application/vnd.openxmlformats-officedocument.spreadsheetml.sheet - XLSX
 * 		application/vnd.openxmlformats-officedocument.spreadsheetml.template - XLTX
 *		application/vnd.ms-excel.sheet.macroEnabled.12 - XLSM
 *		application/vnd.ms-excel.template.macroEnabled.12 - XLTM
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
@Extension(target = AbstractMSOfficeCustomPropertyUpdater.PLUGIN_NAME, order = 3)
public class XlsxCustomPropertyUpdater extends AbstractMSOfficeCustomPropertyUpdater {
	private static final List<String> SUPPORTED_MIME_TYPES = Arrays.asList(MimetypeConstants.XLSX,
			MimetypeConstants.XLTX, MimetypeConstants.XLSM, MimetypeConstants.XLTM);

	@Override
	protected File updateFile(File fileIn, File fileOut, Serializable instanceId) {
		try (FileInputStream inputDocument = new FileInputStream(fileIn);
				XSSFWorkbook workBook = new XSSFWorkbook(inputDocument);
				FileOutputStream out = new FileOutputStream(fileOut)) {

			addCustomPropertiesMSOffice2007(workBook.getProperties(), instanceId);

			workBook.write(out);
		} catch (Exception e) {
			throw new FileCustomPropertiesUpdateException(fileOut.getName(),
					"Error during creation of edit offline MSExcel file: " + fileOut.getName(), e);
		}
		return fileOut;
	}

	@Override
	protected List<String> getSupportedMimeTypes() {
		return SUPPORTED_MIME_TYPES;
	}
}
