/**
 *
 */
package com.sirma.itt.seip.db.patch;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Base patch that allow loading tenant patches depending on the tenant name. Note that this functionality is closely
 * linked to the tenant name.
 * <p>
 * The implementation searches for patches in in folders located on the class path like:
 * <ul>
 * <li>/patches/[current_tenant]/{@link #getPatchPath()}
 * <li>/[current_package]/[current_tenant]/{@link #getPatchPath()}
 * </ul>
 * If no patch is found or security context is not active empty patch file will be returned
 *
 * @author BBonev
 */
public abstract class AbstractTenantPatch implements DbPatch {

	private static final String PATCHES_FOLDER = "/patches/";
	public static final String EMPTY_CHANGELOG_PATH = "empty-changelog.xml";

	@Inject
	private SecurityContext securityContext;

	@Override
	public String getPath() {
		String path = getPatchPath();
		if (StringUtils.isBlank(path) || !securityContext.isActive()) {
			return buildPathFromCurrentPackage(EMPTY_CHANGELOG_PATH);
		}

		String currentTenantId = securityContext.getCurrentTenantId();
		String nameWithSlash = path.startsWith("/") ? path : "/" + path;
		String tenantPath = currentTenantId + nameWithSlash;
		// first try to find the file in the patches folder for the current tenant
		String rootPath = PATCHES_FOLDER + tenantPath;
		if (fileExists(rootPath)) {
			return rootPath;
		}

		// if not in the patches folder try in current package
		String packagePath = buildPathFromCurrentPackage(tenantPath);
		if (fileExists(packagePath)) {
			return packagePath;
		}

		// not found for the current tenant anything so return empty patch file
		return buildPathFromCurrentPackage(EMPTY_CHANGELOG_PATH);
	}

	private boolean fileExists(String path) {
		return getClass().getResource(path) != null;
	}

	/**
	 * Gets the patch path. The path should be file name or folder path relative to one of the:
	 * <ul>
	 * <li>/patches/[current_tenant]/
	 * <li>/[current_package]/[current_tenant]/
	 * </ul>
	 *
	 * @return the patch path
	 */
	protected abstract String getPatchPath();

}
