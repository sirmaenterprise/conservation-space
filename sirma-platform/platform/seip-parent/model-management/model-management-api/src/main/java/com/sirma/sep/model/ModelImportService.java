package com.sirma.sep.model;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Validates and imports all models in the system, given a collection of input streams representing the files.</br>
 * The operation is executed in two phases: validation and import. Import is not executed if any of the validations
 * detect errors.
 * 
 * @author Vilizar Tsonev
 */
public interface ModelImportService {

	/**
	 * Validates and imports all models in the system. The operation is executed in two phases:
	 * <ol>
	 * <li>Validations phase - all models are validated for formal and logical errors. If any errors are detected, the
	 * method returns them as a list and does not proceed with the import</li>
	 * <li>Import phase - if validation is successful, the models are imported. The passed file streams are atomatically
	 * recognized (if definition, bpmn, template, role, or zip archive with models) and imported sequentially.</li>
	 * </ol>
	 * If a .zip file is passed for import, it gets unzipped and its contents are imported. The directory structure and
	 * naming doesn't matter - model files are auto-recognized if definition, template, bpmn, etc and imported. Model
	 * files and an archive can be passed simultaneously, but only one zip file will be processed per import.
	 * 
	 * @param files is map of files to import where the key is the original file name and the value is an
	 *        {@link InputStream} representing the file content. Supported files are: zip archive with models inside,
	 *        .XMLs and .BPMNs. If a zip is passed, it gets unzipped, its contents are auto-recognized and passed for
	 *        import. Currently, only one zip is supported.
	 * @return a list of detected errors, or an empty list if all models are valid
	 */
	List<String> importModel(Map<String, InputStream> files);

	/**
	 * Exports the requested models as files. If only one file is requested (or available) it is directly returned as an
	 * xml. If more than one files are requested, they get zip-archived first and the zip file is returned. If templates
	 * and definitions are requested simultaneously, they are grouped into separate folders for convenience -
	 * "template", "definition", etc. and then archived.
	 * 
	 * @param request is the request carrying the export parameters.
	 * @return an exported xml file, in case one file is requested, or a zip archive in case multiple files are
	 *         requested
	 */
	File exportModel(ModelExportRequest request);
}
