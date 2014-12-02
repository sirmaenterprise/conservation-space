package com.sirma.cmf.web.stat;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.info.VersionInfo;
import com.sirma.itt.emf.plugin.ExtensionPoint;

/**
 * The VersionServlet provides version info for all registered modules
 */
@WebServlet(urlPatterns = { "/versioninfo" }, name = "VersionInfoServlet")
public class VersionServlet extends HttpServlet {
	/** The versions info. */
	@Inject
	@ExtensionPoint(value = VersionInfo.TARGET_NAME)
	private Iterable<VersionInfo> versionInfo;
	@Config(name = EmfConfigurationProperties.RELEASE_INFO_FILE_LOCATION, defaultValue = "/META-INF/MANIFEST.MF")
	@Inject
	private String location;
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2183396362227515964L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(VersionServlet.class);

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");

		StringBuilder builder = new StringBuilder();

		InputStream inputStream = null;
		try {
			// get the general info
			ServletContext application = getServletConfig().getServletContext();
			inputStream = application.getResourceAsStream(location);
			Manifest manifest = new Manifest(inputStream);
			Attributes mainAttributes = manifest.getMainAttributes();
			builder.append("<div style=\"font-size: 18pt;\">");
			builder.append("General Info");
			builder.append("</div>");
			// show it first with custom css
			Attributes.Name releaseInfoId = new Attributes.Name("Release-Version");
			if (mainAttributes.get(releaseInfoId) != null) {
				Object releaseVersion = mainAttributes.remove(releaseInfoId);
				builder.append("<div style=\"font-size: 12pt;padding-left:16pt;\"><span style=\"font-weight:bold;\">");
				builder.append("Release-Version: </span>");
				builder.append(releaseVersion);
				builder.append("</div>");
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
				builder.append("</div>");
			}
			builder.append("<BR>");
			// get the modules info
			Iterator<VersionInfo> iteratorInfo = versionInfo.iterator();
			while (iteratorInfo.hasNext()) {
				VersionInfo versionInfo = (VersionInfo) iteratorInfo.next();
				builder.append("<div style=\"font-size: 18pt;\">");
				builder.append(versionInfo.getModuleDescription());
				builder.append("</div>");
				Properties fileInfo = versionInfo.getFileInfo();
				Set<Entry<Object, Object>> entrySet = fileInfo.entrySet();
				for (Entry<Object, Object> infoEntry : entrySet) {
					builder.append("<div style=\"font-size: 12pt;padding-left:16pt;\">");
					builder.append("<span style=\"font-weight:bold;\">");
					builder.append(infoEntry.getKey());
					builder.append("</span> : ");
					builder.append(infoEntry.getValue());
					builder.append("</div>");
				}
				builder.append("<BR>");
			}

		} catch (Exception e) {
			LOGGER.error("Failed getting info.", e);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}

		PrintWriter writer = response.getWriter();
		writer.write(builder.toString());
		writer.close();
	}

}
