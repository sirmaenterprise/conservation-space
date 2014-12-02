package com.sirma.itt.cmf.services.adapters;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.jboss.vfs.VirtualFile;

import com.sirma.itt.emf.adapter.DMSDefintionAdapterService;
import com.sirma.itt.emf.adapter.DMSDefintionAdapterServiceExtension;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.plugin.PluginUtil;

/**
 * The DMSDefintionAdapterService mock loading from internal archive
 */
@ApplicationScoped
public class CMFDefintionAdapterServiceMock implements DMSDefintionAdapterService {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4550889011642948909L;

	/** The extensions. */
	@Inject
	@ExtensionPoint(DMSDefintionAdapterServiceExtension.TARGET_NAME)
	private Iterable<DMSDefintionAdapterServiceExtension> extensions;
	@Inject
	@Config(name = EmfConfigurationProperties.DEFAULT_CONTAINER)
	private String containerId;
	/** The mapping. */
	private Map<Class<?>, DMSDefintionAdapterServiceExtension> mapping;

	private Logger logger = Logger.getLogger(getClass());

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		mapping = PluginUtil.parseSupportedObjects(extensions, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) throws DMSException {
		DMSDefintionAdapterServiceExtension extension = mapping.get(definitionClass);
		if (extension == null) {
			// we can throw an exception also
			return Collections.emptyList();
		}
		return searchDefinitionsInDms(extension.getSearchPath(definitionClass), definitionClass);
	}

	/**
	 * Search in dms for definitions based on the provided url and returns list of dms descriptors.
	 *
	 * @param uri
	 *            the uri to use. see dms for more informations about available locations
	 * @param definitionClass
	 *            is the class to search definition for
	 * @return the list of defintions
	 * @throws DMSException
	 *             the dMS exception
	 */
	protected List<FileDescriptor> searchDefinitionsInDms(String uri, Class<?> definitionClass)
			throws DMSException {
		List<VirtualFile> children = null;
		try {
			URL resource = new URL(uri);

			VirtualFile content = (VirtualFile) resource.getContent();
			children = content.getParent().getChildren();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (children == null) {
			return Collections.emptyList();
		}
		List<FileDescriptor> defDescriptor = new LinkedList<FileDescriptor>();
		for (VirtualFile virtualFile : children) {
			try {
				String name = virtualFile.getPhysicalFile().getName();
				if (name.equals(definitionClass.getSimpleName())) {
					List<VirtualFile> definitions = virtualFile.getChildren();
					for (VirtualFile nextDefinition : definitions) {
						if (nextDefinition.isFile()) {
							logger.debug("Definition added: " + nextDefinition.getName());
							defDescriptor
									.add(new VirtualFileDescriptor(nextDefinition, containerId));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return defDescriptor;
	}

	@Override
	public String uploadDefinition(Class<?> definitionClass,
			FileAndPropertiesDescriptor descriptor) throws DMSException {
		// TODO Auto-generated method stub
		return null;
	}

}
