package com.sirma.sep.systeminfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sirma.itt.emf.info.VersionInfo;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * The VersionServlet provides version info for all registered modules.
 */
@WebServlet(urlPatterns = { "/versioninfo" }, name = "VersionInfoServlet")
public class VersionServlet extends HttpServlet {

	private static final String DIV_END = "</div>";

	@Inject
	@ExtensionPoint(value = VersionInfo.TARGET_NAME)
	private Iterable<VersionInfo> versionInfo;

	/** The release info file location. */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "info.release.file.location", defaultValue = "/META-INF/MANIFEST.MF", sensitive = true, system = true, label = "Info file to read the build info from. Should be properties like")
	private ConfigurationProperty<String> location;

	private static final long serialVersionUID = -2183396362227515964L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");

		StringBuilder builder = new StringBuilder();

		ServletContext application = getServletConfig().getServletContext();
		Attributes mainAttributes = getManifestAttribues(application);

		builder.append("<div style=\"font-size: 18pt;\">");
		builder.append("General Info");
		builder.append(DIV_END);
		// show it first with custom css
		Attributes.Name releaseInfoId = new Attributes.Name("Release-Version");
		if (mainAttributes.get(releaseInfoId) != null) {
			Object releaseVersion = mainAttributes.remove(releaseInfoId);
			builder.append("<div style=\"font-size: 12pt;padding-left:16pt;\"><span style=\"font-weight:bold;\">");
			builder.append("Release-Version: </span>");
			builder.append(releaseVersion);
			builder.append(DIV_END);
		}
		// show the rest build properties
		Iterator<Entry<Object, Object>> iterator = mainAttributes.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Object, Object> entry = iterator.next();
			builder.append("<div style=\"font-size: 12pt;padding-left:16pt;\">");
			builder.append("<span style=\"font-weight:bold;\">");
			builder.append(entry.getKey());
			builder.append("</span> : ");
			builder.append(entry.getValue());
			builder.append(DIV_END);
		}
		builder.append("<BR>");
		// get the modules info
		Iterator<VersionInfo> iteratorInfo = versionInfo.iterator();
		while (iteratorInfo.hasNext()) {
			VersionInfo info = iteratorInfo.next();
			builder.append("<div style=\"font-size: 18pt;\">");
			builder.append(info.getModuleDescription());
			builder.append(DIV_END);
			Properties fileInfo = info.getFileInfo();
			Set<Entry<Object, Object>> entrySet = fileInfo.entrySet();
			for (Entry<Object, Object> infoEntry : entrySet) {
				builder.append("<div style=\"font-size: 12pt;padding-left:16pt;\">");
				builder.append("<span style=\"font-weight:bold;\">");
				builder.append(infoEntry.getKey());
				builder.append("</span> : ");
				builder.append(infoEntry.getValue());
				builder.append(DIV_END);
			}
			builder.append("<BR>");
		}

		try (PrintWriter writer = response.getWriter()) {
			writer.write(builder.toString());
		}
	}

	private Attributes getManifestAttribues(ServletContext application) throws IOException {
		Attributes mainAttributes;
		try (InputStream inputStream = application.getResourceAsStream(location.get())) {
			if (inputStream == null) {
				return new Attributes(0);
			}
			// get the general info
			Manifest manifest = new Manifest(inputStream);
			mainAttributes = manifest.getMainAttributes();
		}
		return mainAttributes;
	}

}
