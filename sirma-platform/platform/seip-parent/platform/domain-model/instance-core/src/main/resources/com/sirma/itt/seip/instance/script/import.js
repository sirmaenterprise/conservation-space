/**
 * Imports the given script defined in a external definition identified by the script path.
 *
 * @param id the path to the script to import
 */
function importScript(path) {
	if (typeof path == 'string') {
		var scripts = importer.loadScript(path);
		if (scripts && scripts.length > 0) {
			for (var index = 0; index < scripts.length; index++) {
				var script = scripts[index];
				if (script) {
					// load the script into the engine
					loadScript(script);
				}
			}
		} else {
			log.warn("Script/s for path " + path + " not found!")
		}
	} else {
		importScripts(path);
	}
}

/**
 * Import multiple scripts identified by array of paths.
 *
 * @param paths the path to load
 */
function importScripts(paths) {
	for (var index in paths) {
		importScript(paths[index]);
	}
}