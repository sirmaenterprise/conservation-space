package com.sirma.sep.model.management.deploy;

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.sirma.sep.model.management.Path;

/**
 * Utilities related to the deployment of models.
 *
 * @author Mihail Radkov
 * @since 17/09/2018
 */
public class ModelDeploymentUtils {

	private ModelDeploymentUtils() {
		// Prevent instantiation for util class.
	}

	/**
	 * Returns mapped identifiers of the provided list with {@link Path}.
	 *
	 * @param nodes the paths to map
	 * @return list of {@link Path} identifiers
	 */
	public static List<String> getIds(List<Path> nodes) {
		return nodes.stream().map(Path::getValue).collect(toList());
	}

}
