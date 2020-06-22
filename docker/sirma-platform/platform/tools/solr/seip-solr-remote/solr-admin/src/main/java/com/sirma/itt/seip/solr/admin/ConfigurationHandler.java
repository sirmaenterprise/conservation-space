package com.sirma.itt.seip.solr.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

/**
 * The ModelUpdateAdminHandler is servlet to handle solr update requestss.
 */
public class ConfigurationHandler extends HttpServlet {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6854362621743232252L;
	private Path sourceConfigSetPath;
	private Path targetConfigSetRootPath;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String configValue = getConfigValue("solr.audit.template.directory", null);
		if (configValue == null) {
			throw new ServletException(
					"Missing -Dsolr.audit.template.directory parameter pointing to template directory!");
		}
		sourceConfigSetPath = Paths.get(new File(configValue).toURI());

		configValue = getConfigValue("solr.configset.directory", null);
		if (configValue == null) {
			throw new ServletException("Missing -Dsolr.configset.directory parameter pointing to configset location!");
		}
		targetConfigSetRootPath = Paths.get(new File(configValue).toURI());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().append("Configuration servlet installed!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			if (req.getRequestURI().endsWith("config/upload")) {
				uploadConfiguration(req);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Upload configuration and creates new configuration set based on existing
	 * template
	 *
	 * @param req
	 *            the request
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FileUploadException
	 *             the file upload exception
	 */
	private void uploadConfiguration(HttpServletRequest req) throws IOException, FileUploadException {
		ServletFileUpload upload = new ServletFileUpload();
		upload.setFileItemFactory(new DiskFileItemFactory());
		if (!ServletFileUpload.isMultipartContent(req)) {
			return;
		}
		String configName = null;
		// Parse the request
		List<FileItem> items = upload.parseRequest(req);
		Iterator<FileItem> iterator = items.iterator();
		while (iterator.hasNext()) {
			FileItem fileItem = (FileItem) iterator.next();
			if ("configname".equals(fileItem.getFieldName())) {
				configName = fileItem.getString("UTF-8");
				iterator.remove();
				break;
			}
		}
		Path resolved = targetConfigSetRootPath.resolve(Paths.get(configName));
		// copy base
		java.nio.file.Files.walkFileTree(sourceConfigSetPath, new CopyDirectoryVisitor(sourceConfigSetPath, resolved));
		iterator = items.iterator();
		while (iterator.hasNext()) {
			FileItem fileItem = (FileItem) iterator.next();

			String name = fileItem.getFieldName();
			String[] split = name.split("\\|");
			Path filePath = targetConfigSetRootPath.resolve(Paths.get(configName, split));

			try (InputStream inputStream = fileItem.getInputStream();
					OutputStream output = new FileOutputStream(filePath.toFile())) {
				IOUtils.copy(inputStream, output);
			}

		}
	}

	private String getConfigValue(String id, String defaultValue) {
		return System.getProperty(id, defaultValue);
	}

}
