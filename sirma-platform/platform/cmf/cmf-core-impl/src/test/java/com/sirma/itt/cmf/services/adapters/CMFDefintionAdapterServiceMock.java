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

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.definition.DefintionAdapterService;
import com.sirma.itt.seip.definition.DefintionAdapterServiceExtension;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.PluginUtil;

/**
 * The DefintionAdapterService mock loading from internal archive
 */
@ApplicationScoped
public class CMFDefintionAdapterServiceMock implements DefintionAdapterService {

	/** The extensions. */
	@Inject
	@ExtensionPoint(DefintionAdapterServiceExtension.TARGET_NAME)
	private Iterable<DefintionAdapterServiceExtension> extensions;
	@Inject
	private AdaptersConfiguration adaptersConfiguration;
	/** The mapping. */
	private Map<Class, DefintionAdapterServiceExtension> mapping;

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
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) {
		DefintionAdapterServiceExtension extension = mapping.get(definitionClass);
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
	protected List<FileDescriptor> searchDefinitionsInDms(String uri, Class<?> definitionClass) {
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
		List<FileDescriptor> defDescriptor = new LinkedList<>();
		for (VirtualFile virtualFile : children) {
			try {
				String name = virtualFile.getPhysicalFile().getName();
				if (name.equals(definitionClass.getSimpleName())) {
					List<VirtualFile> definitions = virtualFile.getChildren();
					for (VirtualFile nextDefinition : definitions) {
						if (nextDefinition.isFile()) {
							logger.debug("Definition added: " + nextDefinition.getName());
							defDescriptor.add(new VirtualFileDescriptor(nextDefinition,
									adaptersConfiguration.getDmsContainerId().get()));
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
	public String uploadDefinition(Class<?> definitionClass, FileAndPropertiesDescriptor descriptor) {
		// Not used method
		return null;
	}

}
