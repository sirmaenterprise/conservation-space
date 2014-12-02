package com.sirma.itt.emf.semantic.model.init;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.event.AllDefinitionsLoaded;
import com.sirma.itt.emf.definition.event.TopLevelDefinitionsLoaded;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.configuration.SemanticConfigurationProperties;

/**
 * Class responsible for registering properties as semantic properties to be returned from queries.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class SemanticPropertyRegister {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticPropertyRegister.class);

	/** Collect all installed accessors. */
	@Inject
	@Any
	private javax.enterprise.inject.Instance<DefinitionAccessor> accessors;

	/** The authentication service instance. */
	@Inject
	private javax.enterprise.inject.Instance<AuthenticationService> authenticationServiceInstance;

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The repository connection. */
	@Inject
	private Instance<RepositoryConnection> repositoryConnection;

	/** The value factory. */
	@Inject
	private ValueFactory valueFactory;

	/** The registry service. */
	@Inject
	private NamespaceRegistryService registryService;

	/** The context name. */
	@Inject
	@Config(name = SemanticConfigurationProperties.SEMANTIC_DB_CONTEXT_NAME, defaultValue = "http://ittruse.ittbg.com/data/enterpriseManagementFramework")
	private String contextName;

	/** The managed properties in shout URI format. */
	private Set<String> managedProperties = new LinkedHashSet<String>();

	/** The context. */
	private URI context;

	/**
	 * Initializes the.
	 */
	@PostConstruct
	public void init() {
		context = valueFactory.createURI(contextName);
	}

	/**
	 * Post startup event.
	 *
	 * @param event
	 *            the event
	 */
	public void postStartupEvent(@Observes AllDefinitionsLoaded event) {
		LOGGER.info("Triggered update due to completed definition update/reload");
		reload();
	}

	/**
	 * Post definition loading event.
	 * 
	 * @param event
	 *            the event
	 */
	public void postDefinitionLoadingEvent(@Observes TopLevelDefinitionsLoaded event) {
		LOGGER.info("Triggered update due to completed update/reload of top level definitions");
		reload();
	}

	/**
	 * Gets the current container.
	 *
	 * @return the current container
	 */
	private String getCurrentContainer() {
		return SecurityContextManager.getCurrentContainer(authenticationServiceInstance);
	}

	/**
	 * Reload all.
	 */
	public void reload() {
		String container = getCurrentContainer();
		Model model = new LinkedHashModel();
		Set<String> propertyNames = new LinkedHashSet<String>();
		for (DefinitionAccessor accessor : accessors) {
			// change to use dictionary service and his cache to access all definitions
			List<DefinitionModel> definitions = accessor.getAllDefinitions(container);
			for (DefinitionModel definition : definitions) {
				collectUriesFromDefinition(definition, propertyNames);
			}
		}

		LOGGER.debug("Registering {} properties as semantic properties: {}", propertyNames.size(),
				propertyNames);


		for (String uri : propertyNames) {
			// check if already defined
			if (!isManagedProperty(uri)) {
				Statement statement = createStatement(uri);
				if (statement != null) {
					model.add(statement);
				}
			}
		}

		if (!model.isEmpty()) {
			saveModel(model);

			managedProperties.addAll(propertyNames);
		}

	}

	/**
	 * Save model.
	 * 
	 * @param model
	 *            the model
	 */
	private void saveModel(Model model) {
		RepositoryConnection connection = repositoryConnection.get();

		try {
			connection.remove(model);
			connection.add(model);
		} catch (RepositoryException e) {
			LOGGER.error("Failed to register properties due to {}", e.getMessage(), e);
			try {
				connection.rollback();
			} catch (RepositoryException e1) {
				LOGGER.error("Failed to rollback changes due to {}", e1.getMessage(), e1);
			}
		}
	}

	/**
	 * Creates the statement.
	 * 
	 * @param uri
	 *            the uri
	 * @return the statement
	 */
	private Statement createStatement(Object uri) {
		URI subject = null;
		if (uri instanceof String) {
			subject = valueFactory.createURI(registryService.buildFullUri((String) uri));
		} else if (uri instanceof URI) {
			subject = (URI) uri;
		} else if (uri instanceof Uri) {
			subject = valueFactory.createURI(registryService.buildFullUri(uri.toString()));
		} else {
			return null;
		}
		return valueFactory.createStatement(subject, RDF.TYPE, OWL.DATATYPEPROPERTY, context);
	}

	/**
	 * Register property.
	 * 
	 * @param uri
	 *            the uri
	 */
	public void registerProperty(Uri uri) {
		registerProperties(Arrays.asList(uri));
	}

	/**
	 * Register properties.
	 * 
	 * @param uries
	 *            the uries
	 */
	public void registerProperties(Collection<Uri> uries) {
		Model model = new LinkedHashModel();
		Set<String> names = CollectionUtils.createLinkedHashSet(uries.size());
		for (Uri uri : uries) {
			String shortUri = registryService.getShortUri(uri);
			// if we already know it does not process it again
			if (!isManagedProperty(shortUri)) {
				Statement statement = createStatement(uri);
				if (statement != null) {
					model.add(statement);
					names.add(shortUri);
				} else {
					LOGGER.warn("Invalid property URI {}. Skipping it.", uri);
				}
			}
		}

		if (!model.isEmpty()) {
			saveModel(model);

			managedProperties.addAll(names);
		}
	}

	/**
	 * Checks if is managed property.
	 * 
	 * @param shortUri
	 *            the short uri
	 * @return true, if is managed property
	 */
	private boolean isManagedProperty(String shortUri) {
		return managedProperties.contains(shortUri);
	}

	/**
	 * Collect URIs from definition.
	 *
	 * @param definition
	 *            the definition
	 * @param uries
	 *            the URIs
	 */
	private void collectUriesFromDefinition(DefinitionModel definition, Set<String> uries) {
		if (definition instanceof RegionDefinitionModel) {
			for (RegionDefinition regionDefinition : ((RegionDefinitionModel) definition)
					.getRegions()) {
				collectUriesFromDefinition(regionDefinition, uries);
			}
		}
		for (PropertyDefinition property : definition.getFields()) {
			String uri = null;
			// if the field itself is an uri
			if (property.getIdentifier().contains(":")) {
				uri = property.getIdentifier();
			}
			// check if not overridden
			if (StringUtils.isNotNullOrEmpty(property.getUri())
					&& !DefaultProperties.NOT_USED_PROPERTY_VALUE.equals(property.getUri())) {
				uri = property.getUri();
			}
			if (uri != null) {
				uries.add(uri);
			}
			if (property.getControlDefinition() != null) {
				collectUriesFromDefinition(property.getControlDefinition(), uries);
			}
		}
	}
}
