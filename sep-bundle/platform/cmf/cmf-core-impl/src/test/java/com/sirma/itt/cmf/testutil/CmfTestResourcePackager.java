package com.sirma.itt.cmf.testutil;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;

import com.sirma.itt.cmf.beans.definitions.CommonTemplateHolder;
import com.sirma.itt.cmf.beans.definitions.DocumentsDefinition;
import com.sirma.itt.cmf.beans.definitions.compile.CaseDefinitionCompilerCallback;
import com.sirma.itt.cmf.beans.definitions.compile.DocumentDefinitionCompilerCallback;
import com.sirma.itt.cmf.beans.definitions.compile.TaskDefinitionCompilerCallback;
import com.sirma.itt.cmf.beans.definitions.compile.TaskDefinitionTemplateCompilerCallback;
import com.sirma.itt.cmf.beans.definitions.compile.WorkflowDefinitionCompilerCallback;
import com.sirma.itt.cmf.beans.definitions.impl.CaseDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefProxy;
import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.SectionDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.SectionDefinitionProxy;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionTemplateImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TemplateDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.WorkflowDefinitionImpl;
import com.sirma.itt.cmf.beans.entity.AssignedUserTasks;
import com.sirma.itt.cmf.beans.entity.CaseEntity;
import com.sirma.itt.cmf.beans.entity.DocumentEntity;
import com.sirma.itt.cmf.beans.entity.DraftEntity;
import com.sirma.itt.cmf.beans.entity.DraftEntityId;
import com.sirma.itt.cmf.beans.entity.EntityIdType;
import com.sirma.itt.cmf.beans.entity.SectionEntity;
import com.sirma.itt.cmf.beans.entity.TaskEntity;
import com.sirma.itt.cmf.beans.entity.TemplateEntity;
import com.sirma.itt.cmf.beans.entity.WorkflowInstanceContextEntity;
import com.sirma.itt.cmf.beans.jaxb.Definition;
import com.sirma.itt.cmf.beans.jaxb.DocumentDefinition;
import com.sirma.itt.cmf.beans.jaxb.DocumentDefinitions;
import com.sirma.itt.cmf.beans.jaxb.ObjectFactory;
import com.sirma.itt.cmf.beans.jaxb.ParameterType;
import com.sirma.itt.cmf.beans.jaxb.SectionDefinition;
import com.sirma.itt.cmf.beans.jaxb.SectionsDefinition;
import com.sirma.itt.cmf.beans.jaxb.SubDefinitionType;
import com.sirma.itt.cmf.beans.jaxb.TaskDefinition;
import com.sirma.itt.cmf.beans.jaxb.TaskDefinitions;
import com.sirma.itt.cmf.beans.jaxb.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.VersionInfo;
import com.sirma.itt.cmf.beans.model.WorkflowInfo;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.config.CmfSystemConfigProvider;
import com.sirma.itt.cmf.config.DefaultConfigProvider;
import com.sirma.itt.cmf.db.QueryDao;
import com.sirma.itt.cmf.domain.CmfAllowedChildTypeMappingExtension;
import com.sirma.itt.cmf.dozer.provider.CaseDozerProvider;
import com.sirma.itt.cmf.dozer.provider.WorkflowDozerProvider;
import com.sirma.itt.cmf.instance.CaseAllowedChildrenProvider;
import com.sirma.itt.cmf.instance.CaseInstanceToServiceRegisterExtension;
import com.sirma.itt.cmf.instance.CmfEntityTypeProviderExtension;
import com.sirma.itt.cmf.instance.DocumentAllowedChildrenProvider;
import com.sirma.itt.cmf.instance.DocumentInstanceToServiceRegisterExtension;
import com.sirma.itt.cmf.instance.SectionInstanceToServiceRegisterExtension;
import com.sirma.itt.cmf.instance.StandaloneTaskInstanceToServiceRegisterExtension;
import com.sirma.itt.cmf.instance.TaskInstanceToServiceRegisterExtension;
import com.sirma.itt.cmf.instance.WorkflowInstanceToServiceRegisterExtension;
import com.sirma.itt.cmf.patch.CMFDBSchemaPatch;
import com.sirma.itt.cmf.search.DmsSearchEngineImpl;
import com.sirma.itt.cmf.security.evaluator.CaseRoleEvaluator;
import com.sirma.itt.cmf.security.evaluator.CmfDMSExternalEvaluator;
import com.sirma.itt.cmf.security.evaluator.DocumentRoleEvaluator;
import com.sirma.itt.cmf.security.evaluator.SectionRoleEvaluator;
import com.sirma.itt.cmf.security.evaluator.StandaloneTaskRoleEvaluator;
import com.sirma.itt.cmf.security.evaluator.TaskRoleEvaluator;
import com.sirma.itt.cmf.security.evaluator.WorkflowRoleEvaluator;
import com.sirma.itt.cmf.security.provider.AbstractRoleProviderExtension;
import com.sirma.itt.cmf.security.provider.ActivitiRoleProviderExtension;
import com.sirma.itt.cmf.security.provider.CaseActionProvider;
import com.sirma.itt.cmf.security.provider.CmfRoleProvider;
import com.sirma.itt.cmf.security.provider.CmfRoleProviderExtension;
import com.sirma.itt.cmf.security.provider.CollectableRoleProviderExtension;
import com.sirma.itt.cmf.security.provider.DocumentActionProvider;
import com.sirma.itt.cmf.security.provider.RoleProviderExtension;
import com.sirma.itt.cmf.security.provider.SectionActionProvider;
import com.sirma.itt.cmf.security.provider.StandaloneTaskActionProvider;
import com.sirma.itt.cmf.security.provider.WorkflowTaskActionProvider;
import com.sirma.itt.cmf.services.adapters.CMFCaseInstanceAdapterServiceMock;
import com.sirma.itt.cmf.services.adapters.CMFContentAdapterServiceMock;
import com.sirma.itt.cmf.services.adapters.CMFDefintionAdapterServiceMock;
import com.sirma.itt.cmf.services.adapters.CMFDocumentAdapterServiceMock;
import com.sirma.itt.cmf.services.adapters.CMFGroupServiceMock;
import com.sirma.itt.cmf.services.adapters.CMFInstanceAdapterService;
import com.sirma.itt.cmf.services.adapters.CMFMailNotificaionAdapterServiceMock;
import com.sirma.itt.cmf.services.adapters.CMFPermissionAdapterServiceMock;
import com.sirma.itt.cmf.services.adapters.CMFRenditionAdapterServiceMock;
import com.sirma.itt.cmf.services.adapters.CMFSearchAdapterServiceMock;
import com.sirma.itt.cmf.services.adapters.CMFUserServiceMock;
import com.sirma.itt.cmf.services.adapters.CMFWorkflowAdapterServiceMock;
import com.sirma.itt.cmf.services.adapters.CmfDefintionAdapterServiceExtensionMock;
import com.sirma.itt.cmf.services.adapters.RESTClientMock;
import com.sirma.itt.cmf.services.adapters.VirtualFileDescriptor;
import com.sirma.itt.cmf.services.impl.CaseDocumentDmsSearchEngineExtension;
import com.sirma.itt.cmf.services.impl.CaseServiceImpl;
import com.sirma.itt.cmf.services.impl.CmfDefinitionManagementServiceImpl;
import com.sirma.itt.cmf.services.impl.DocumentServiceImpl;
import com.sirma.itt.cmf.services.impl.DraftServiceImpl;
import com.sirma.itt.cmf.services.impl.FolderServiceImpl;
import com.sirma.itt.cmf.services.impl.GroupResourceExtension;
import com.sirma.itt.cmf.services.impl.MailNotificationHelperService;
import com.sirma.itt.cmf.services.impl.PeopleResourceExtension;
import com.sirma.itt.cmf.services.impl.SectionServiceImpl;
import com.sirma.itt.cmf.services.impl.ServerConfiguration;
import com.sirma.itt.cmf.services.impl.StandaloneTaskServiceImpl;
import com.sirma.itt.cmf.services.impl.TaskDmsSearchEngineExtension;
import com.sirma.itt.cmf.services.impl.TaskServiceImpl;
import com.sirma.itt.cmf.services.impl.WorkflowServiceImpl;
import com.sirma.itt.cmf.services.impl.WorkflowTaskServiceImpl;
import com.sirma.itt.cmf.services.impl.dao.BaseSectionInstanceDao;
import com.sirma.itt.cmf.services.impl.dao.CaseDefinitionAccessor;
import com.sirma.itt.cmf.services.impl.dao.CaseInstanceDao;
import com.sirma.itt.cmf.services.impl.dao.CaseInstancePropertyModelCallback;
import com.sirma.itt.cmf.services.impl.dao.DocumentInstanceDao;
import com.sirma.itt.cmf.services.impl.dao.DocumentTemplateDefinitionAccessor;
import com.sirma.itt.cmf.services.impl.dao.FolderInstanceDao;
import com.sirma.itt.cmf.services.impl.dao.SectionInstanceDao;
import com.sirma.itt.cmf.services.impl.dao.StandaloneTaskInstanceDao;
import com.sirma.itt.cmf.services.impl.dao.TaskDefinitionAccessor;
import com.sirma.itt.cmf.services.impl.dao.TaskInstanceDao;
import com.sirma.itt.cmf.services.impl.dao.TaskTemplateDefinitionAccessor;
import com.sirma.itt.cmf.services.impl.dao.WorkflowDefinitionAccessor;
import com.sirma.itt.cmf.services.impl.dao.WorkflowInstanceDao;
import com.sirma.itt.cmf.services.impl.dao.WorkflowInstancePropertyModelCallback;
import com.sirma.itt.cmf.services.mock.InstanceLinkExpressionEvaluator;
import com.sirma.itt.cmf.services.mock.UserLinkExpressionEvaluator;
import com.sirma.itt.cmf.state.AbstractTaskStateServiceExtension;
import com.sirma.itt.cmf.state.BaseCaseTreeStateServiceExtension;
import com.sirma.itt.cmf.state.CaseStateServiceExtension;
import com.sirma.itt.cmf.state.DocumentStateServiceExtension;
import com.sirma.itt.cmf.state.SectionStateServiceExtension;
import com.sirma.itt.cmf.state.StandaloneTaskStateServiceExtension;
import com.sirma.itt.cmf.state.TaskStateServiceExtension;
import com.sirma.itt.cmf.state.WorkflowStateServiceExtension;
import com.sirma.itt.cmf.util.CmfMergeableFactory;
import com.sirma.itt.cmf.util.datatype.AbstractInstanceToRestInstanceConverterProvider;
import com.sirma.itt.cmf.util.datatype.AbstractRestInstanceToInstanceConverterProvider;
import com.sirma.itt.cmf.util.datatype.InstanceToLinkSourceConverterProvider;
import com.sirma.itt.cmf.util.datatype.InstanceToRestInstanceConverterProvider;
import com.sirma.itt.cmf.util.datatype.RestInstanceToInstanceConverterProvider;
import com.sirma.itt.cmf.util.serialization.CmfKryoInitializer;
import com.sirma.itt.cmf.xml.XmlType;
import com.sirma.itt.cmf.xml.schema.CmfSchemaBuilder;
import com.sirma.itt.emf.cache.CacheFactory;
import com.sirma.itt.emf.cache.InMemoryCacheProvider;
import com.sirma.itt.emf.cache.MemoryCache;
import com.sirma.itt.emf.configuration.SystemConfigProvider;
import com.sirma.itt.emf.definition.dao.BaseTemplateDefinitionAccessor;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.exceptions.DefinitionValidationException;
import com.sirma.itt.emf.instance.dao.BaseInstanceDao;
import com.sirma.itt.emf.instance.dao.BaseInstanceDaoImpl;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.link.converters.AbstractInstanceToInstanceReferenceConverterProvider;
import com.sirma.itt.emf.state.BaseStateServiceExtension;
import com.sirma.itt.emf.state.StateServiceExtension;

/**
 * Helper class for building test archives using the ShrinkWrap API. Example of usage <br>
 * <code><strong>		TestResourceBuilder testResourceBuilder = new TestResourceBuilder(); <br>
				WebArchive packagedWar = testResourceBuilder.buildJar( <br>Packages.BASIC, Packages.DOZER,
				Packages.SECURITY, Packages.ADAPTERS_MOCK).packageWar(WarPackages.ADAPTERS); <br>
				</strong>	</code><br>
 * This builds the war with all needed modules for creation instances, applying permission,
 * connections to dms except mocked connections to dms. More modules could be added as well
 *
 * @author bbanchev
 * @author BBonev
 */
public class CmfTestResourcePackager implements TestPackageBuilder {

	/** The Constant EMTPTY_ARRAY. */
	protected static final Class<?>[] EMTPTY_ARRAY = new Class<?>[] {};
	/** The Constant DEFAULT_MOCK_BEANS_XML. */
	// @formatter:off
	protected static final String DEFAULT_MOCK_BEANS_XML = "<beans\n"
			+ "xmlns=\"http://java.sun.com/xml/ns/javaee\"\n"
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
			+ "xsi:schemaLocation=\"\n" + "http://java.sun.com/xml/ns/javaee\n"
			+ "http://java.sun.com/xml/ns/javaee/beans_1_0.xsd\">\n" + "<interceptors>\n"
			+ "<class>com.sirma.itt.emf.security.context.EmfSecurityInterceptor</class>\n"
			+ "</interceptors>\n" + "<alternatives>\n" + "{0}\n" + "</alternatives>\n" + "</beans>";
	// @formatter:on

	/** The pattern. */
	protected static Pattern DYNAMIC_MODULE_MATCHER = Pattern.compile(".+\\d{10,25}\\.jar");
	/** The testable jar. */
	protected JavaArchive testableJar;

	/** The web archive. */
	protected WebArchive webArchive;

	/** The resolver. */
	protected MavenResolverSystem resolver;

	/** The dependencies. */
	protected Map<File, MavenResolvedArtifact> dependencies;
	/** Excluded artifact ids. */
	protected List<Pair<String, String>> exclusion;
	/** Added classes as alternatives */
	private List<Class<?>> alternatives = new ArrayList<Class<?>>();
	/** the logger. */
	protected Logger logger = Logger.getLogger(getClass());
	protected String mavenModule = "pom.xml";

	/**
	 * Instantiates a new test resource builder.
	 */
	public CmfTestResourcePackager() {
		resolver = Maven.resolver();
	}

	/**
	 * Init a builder with specified pom.xml name found in the same classpath location.
	 *
	 * @param mavenModule
	 *            is custom module to capsulates modules.
	 */
	public CmfTestResourcePackager(String mavenModule) {
		this();
		this.mavenModule = mavenModule;
	}

	/**
	 * Concatenate.
	 *
	 * @param <T>
	 *            the generic type
	 * @param firstArray
	 *            the fist array to add
	 * @param secondArray
	 *            the second array to add
	 * @return the t[]
	 */
	protected <T> T[] concatenate(T[] firstArray, T[] secondArray) {
		int aLen = firstArray.length;
		int bLen = secondArray.length;

		@SuppressWarnings("unchecked")
		T[] C = (T[]) Array.newInstance(firstArray.getClass().getComponentType(), aLen + bLen);
		System.arraycopy(firstArray, 0, C, 0, aLen);
		System.arraycopy(secondArray, 0, C, aLen, bLen);
		firstArray = null;
		secondArray = null;
		return C;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaArchive activateAlternatives(JavaArchive e, Class<?>... classes) {
		return e.addAsManifestResource(createAlternativesBeanXml(classes),
				ArchivePaths.create("beans.xml"));
	}

	/**
	 * {@inheritDoc}
	 */
	public WebArchive activateAlternatives(WebArchive e, Class<?>... classes) {
		return e.addAsResource(createAlternativesBeanXml(classes), "META-INF/beans.xml");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Asset createAlternativesBeanXml(Class<?>... clazz) {
		if (clazz == null) {
			return EmptyAsset.INSTANCE;
		}
		String loadTemplate = DEFAULT_MOCK_BEANS_XML;
		StringBuilder args = new StringBuilder();
		for (int i = 0; i < clazz.length; i++) {
			Class<?> c = clazz[i];
			args.append("<class>").append(c.getName()).append("</class>");
			if ((i + 1) != clazz.length) {
				args.append("\n\t");
			}
		}

		String format = MessageFormat.format(loadTemplate, args.toString());
		return new StringAsset(format);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WebArchive createDeploymentContainer() {
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, getDeploymentWarName());
		if (testableJar == null) {
			testableJar = createBaseArchive();
		}
		webArchive.addAsLibrary(testableJar);

		importLibraries(webArchive, dependencies);

		webArchive = webArchive
				.addAsResource(this.getClass().getResource("hibernate.cfg.xml"),
						"hibernate.cfg.xml")
				.addAsResource(this.getClass().getResource("persistence.xml"),
						"META-INF/persistence.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		return webArchive;
	}

	/**
	 * Import resources.
	 *
	 * @param libs
	 *            the libs
	 */
	protected void importResources(Map<File, MavenResolvedArtifact> libs) {

	}

	/**
	 * Creates the base cmf archive.
	 *
	 * @return the java archive
	 */
	protected JavaArchive createBaseArchive() {
		JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class, getBaseJarName());
		javaArchive.addAsManifestResource(EmptyAsset.INSTANCE,
				"services/com.sirma.itt.emf.DisableConfigurationValidation").addAsManifestResource(
				EmptyAsset.INSTANCE, "beans.xml");
		dependencies = getLibrariesFromDeployment();
		javaArchive = javaArchive.addClass(BaseArquillianCITest.class);
		return javaArchive;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TestPackageBuilder buildJar(TestableJarModules... jarModules) {
		addOptional(jarModules);
		if (logger.isTraceEnabled()) {
			testableJar.writeTo(System.out, Formatters.VERBOSE);
			System.out.println();
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaArchive addOptional(TestableJarModules... packages) {
		if (testableJar == null) {
			testableJar = createBaseArchive();
		}
		if (packages != null) {
			for (TestableJarModules testableModules : packages) {
				testableJar = testableModules.add(this, testableJar);
			}
		}
		return testableJar;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaArchive addOptionalClasses(boolean alternative, Class<?>... classes) {
		if (testableJar == null) {
			testableJar = createBaseArchive();
		}
		if (classes != null) {
			testableJar = testableJar.addClasses(classes);
			if (alternative) {
				alternatives.addAll(Arrays.asList(classes));
			}
		}
		return testableJar;
	}

	@SafeVarargs
	@Override
	public final TestPackageBuilder exclude(Pair<String, String>... artifactInfromation) {
		if (artifactInfromation != null) {
			exclusion = Arrays.asList(artifactInfromation);
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaArchive getTestableJar() {
		return testableJar;
	}

	/**
	 * Load from artifacts.
	 *
	 * @param asResolvedArtifact
	 *            the resolved artifact to get sub artifacts from
	 * @param resolver
	 *            the resolver
	 * @param matches
	 *            the matched names
	 * @return the list the artifacts
	 * @throws Exception
	 *             on any error during resolving
	 */
	protected Map<File, MavenResolvedArtifact> loadFromArtifacts(
			MavenResolvedArtifact[] asResolvedArtifact, PomEquippedResolveStage resolver,
			String[] matches) throws Exception {

		Map<File, MavenResolvedArtifact> files = new HashMap<File, MavenResolvedArtifact>();

		for (MavenResolvedArtifact mavenResolvedArtifact : asResolvedArtifact) {
			File local = mavenResolvedArtifact.asFile();
			String artifactName = local.getName();
			MavenCoordinate coordinate = mavenResolvedArtifact.getCoordinate();
			// System.out.println(" -->  " + coordinate.getArtifactId() + " "
			// + coordinate.getClassifier() + " " + local);
			boolean continueResolving = true;
			if (matches != null) {
				continueResolving = false;
				for (String file : matches) {
					if (artifactName.contains(file)) {
						MavenResolvedArtifact[] filesWithDep = resolver
								.resolve(coordinate.toCanonicalForm()).withTransitivity()
								.asResolvedArtifact();
						Map<File, MavenResolvedArtifact> loadFromArtifacts = loadFromArtifacts(
								filesWithDep, resolver, null);
						files.putAll(loadFromArtifacts);
						continueResolving = true;
						break;
					}
				}
			}
			if (!continueResolving) {
				continue;
			}
			if (DYNAMIC_MODULE_MATCHER.matcher(artifactName).find()) {
				String filename = coordinate.getArtifactId();
				if ((coordinate.getClassifier() != null) && !coordinate.getClassifier().isEmpty()) {
					filename += ("-" + coordinate.getClassifier());
				}
				filename += ".jar";// coordinate.getPackaging()
				logger.trace("Rename: " + local + " scope:" + mavenResolvedArtifact.getScope()
						+ " to: " + filename);
				File target = new File(local.getParent(), filename);
				Files.copy(local.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
				local = target;
			}
			files.put(local, mavenResolvedArtifact);

		}
		return files;
	}

	/**
	 * Import libraries in the final war file
	 *
	 * @param webArchive
	 *            the war archive
	 * @param libraries
	 *            the import adapters
	 * @return the web archive
	 */
	protected WebArchive importLibraries(WebArchive webArchive,
			Map<File, MavenResolvedArtifact> libraries) {
		for (Entry<File, MavenResolvedArtifact> entry : libraries.entrySet()) {
			webArchive.addAsLibrary(entry.getKey(), calculateNameForWarLib(entry));
		}
		return webArchive;
	}

	/**
	 * Gets the libraries from the pom file.
	 *
	 * @return the libraries as pairs of file and artifact info
	 */
	protected Map<File, MavenResolvedArtifact> getLibrariesFromDeployment() {
		try {
			File createTempFile = File.createTempFile("" + System.currentTimeMillis(), ".xml");
			try (FileOutputStream stream = new FileOutputStream(createTempFile)) {
				IOUtils.copy(CmfTestResourcePackager.class.getResourceAsStream(mavenModule), stream);
			} catch (Exception e) {
				logger.error(e);
			}
			PomEquippedResolveStage loadPomFromDeploy = resolver.loadPomFromFile(createTempFile);
			MavenResolvedArtifact[] asResolvedArtifact = loadPomFromDeploy
					.importRuntimeAndTestDependencies().resolve().withTransitivity()
					.asResolvedArtifact();
			Map<File, MavenResolvedArtifact> loadFromArtifacts = loadFromArtifacts(
					asResolvedArtifact, loadPomFromDeploy, null);
			loadFromArtifacts = filterOutputLibraries(loadFromArtifacts);
			Map<File, MavenResolvedArtifact> libs = new HashMap<>(loadFromArtifacts);
			createTempFile.delete();
			return libs;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Filter out some libs to prevent slow startup and not needed code
	 *
	 * @param libs
	 *            are the libs to filter out
	 * @return the filtered map
	 */
	protected Map<File, MavenResolvedArtifact> filterOutputLibraries(
			Map<File, MavenResolvedArtifact> libs) {
		if (exclusion != null) {
			Iterator<Entry<File, MavenResolvedArtifact>> iterator = libs.entrySet().iterator();
			Pair<String, String> tempPair = new Pair<String, String>(null, null);
			Map<File, MavenResolvedArtifact> libsCopy = new HashMap<File, MavenResolvedArtifact>(
					libs.size() - exclusion.size());
			while (iterator.hasNext()) {
				Map.Entry<File, MavenResolvedArtifact> entry = iterator.next();
				String artifactId = entry.getValue().getCoordinate().getArtifactId();
				tempPair.setFirst(artifactId);
				tempPair.setSecond(entry.getValue().getCoordinate().getClassifier());
				if (!exclusion.contains(tempPair)) {
					libsCopy.put(entry.getKey(), entry.getValue());
				} else {
					logger.info("Skiping from final war: " + entry.getKey());
				}
			}
			return libsCopy;
		}
		return libs;
	}

	/**
	 * Calculate name for war lib (artifact name + group name).
	 *
	 * @param entry
	 *            the entry of file and artifact info
	 * @return the jar name to be added as
	 */
	protected String calculateNameForWarLib(Entry<File, MavenResolvedArtifact> entry) {
		MavenCoordinate coordinate = entry.getValue().getCoordinate();
		String extName = null;
		// if (coordinate.getGroupId().contains("com.sirma.itt")) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(coordinate.getGroupId());
		stringBuilder.append("-");
		stringBuilder.append(coordinate.getArtifactId());
		if (coordinate.getClassifier().length() > 0) {
			stringBuilder.append("-");
			stringBuilder.append(coordinate.getClassifier());
		}
		stringBuilder.append(".");
		stringBuilder.append(coordinate.getType());
		extName = stringBuilder.toString();
		return extName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WebArchive packageWar(TestableWarModules... packages) {
		if (webArchive == null) {
			webArchive = createDeploymentContainer();
		}
		// active alternatives
		activateAlternatives(testableJar, alternatives.toArray(new Class[alternatives.size()]));

		for (TestableWarModules testableModules : packages) {
			webArchive = testableModules.add(this, webArchive);
		}
		return webArchive;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildMockAdapters() {
		return new Class<?>[] { CMFUserServiceMock.class, CMFGroupServiceMock.class,
				CMFContentAdapterServiceMock.class, CMFDefintionAdapterServiceMock.class,
				CMFCaseInstanceAdapterServiceMock.class, CMFPermissionAdapterServiceMock.class,
				CMFWorkflowAdapterServiceMock.class, CMFMailNotificaionAdapterServiceMock.class,
				CMFSearchAdapterServiceMock.class, RESTClientMock.class,
				CMFRenditionAdapterServiceMock.class, CMFDocumentAdapterServiceMock.class,
				CMFInstanceAdapterService.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildMockAdaptersExtensions() {
		return new Class<?>[] { CmfDefintionAdapterServiceExtensionMock.class,
				VirtualFileDescriptor.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildCacheClasses() {
		return new Class<?>[] { InMemoryCacheProvider.class, MemoryCache.class, CacheFactory.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildDaoClasses() {
		return new Class<?>[] { CMFDBSchemaPatch.class, CaseDefinitionAccessor.class,
				InstanceDao.class, BaseInstanceDao.class, BaseInstanceDaoImpl.class,
				CaseInstanceDao.class, CaseInstancePropertyModelCallback.class,
				CmfAllowedChildTypeMappingExtension.class, DocumentInstanceDao.class,
				DocumentTemplateDefinitionAccessor.class, StandaloneTaskInstanceDao.class,
				TaskDefinitionAccessor.class, TaskInstanceDao.class,
				TaskTemplateDefinitionAccessor.class, WorkflowDefinitionAccessor.class,
				WorkflowInstanceDao.class, WorkflowInstancePropertyModelCallback.class,
				SectionInstanceDao.class, FolderInstanceDao.class, BaseSectionInstanceDao.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildDefinitionCallbacks() {
		return new Class<?>[] { CaseDefinitionCompilerCallback.class, DefaultConfigProvider.class,
				DocumentDefinitionCompilerCallback.class, SystemConfigProvider.class,
				TaskDefinitionCompilerCallback.class, TaskDefinitionTemplateCompilerCallback.class,
				WorkflowDefinitionCompilerCallback.class, TemplateDefinitionImpl.class,
				BaseTemplateDefinitionAccessor.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildDefinitions() {
		return new Class<?>[] { CaseDefinitionImpl.class, DocumentDefinitionImpl.class,
				DocumentDefinitionRefImpl.class, SectionDefinitionImpl.class,
				TaskDefinitionImpl.class, TaskDefinitionRefImpl.class,
				TaskDefinitionTemplateImpl.class, WorkflowDefinitionImpl.class,
				CommonTemplateHolder.class, DocumentsDefinition.class,
				com.sirma.itt.cmf.beans.definitions.TaskDefinitions.class,
				DocumentDefinitionRefProxy.class, CmfKryoInitializer.class,
				CmfMergeableFactory.class, GenericDefinitionImpl.class, DataTypeDefinition.class,
				SectionDefinitionProxy.class, DefinitionValidationException.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildConverters() {
		return new Class<?>[] { InstanceToRestInstanceConverterProvider.class,
				AbstractInstanceToInstanceReferenceConverterProvider.class,
				InstanceToLinkSourceConverterProvider.class,
				AbstractInstanceToRestInstanceConverterProvider.class,
				RestInstanceToInstanceConverterProvider.class,
				AbstractRestInstanceToInstanceConverterProvider.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildTypeInstances() {
		return new Class<?>[] { SectionInstanceToServiceRegisterExtension.class,
				CaseInstanceToServiceRegisterExtension.class,
				DocumentInstanceToServiceRegisterExtension.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildEntityClasses() {
		return new Class<?>[] { AssignedUserTasks.class, CaseEntity.class, DocumentEntity.class,
				EntityIdType.class, SectionEntity.class, TaskEntity.class,
				WorkflowInstanceContextEntity.class, TemplateEntity.class, DraftEntity.class,
				DraftEntityId.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildImplServices() {
		return new Class<?>[] { CaseServiceImpl.class, CmfDefinitionManagementServiceImpl.class,
				DocumentServiceImpl.class, GroupResourceExtension.class,
				PeopleResourceExtension.class, CaseDocumentDmsSearchEngineExtension.class,
				ServerConfiguration.class, StandaloneTaskServiceImpl.class, TaskServiceImpl.class,
				TaskDmsSearchEngineExtension.class, WorkflowServiceImpl.class,
				WorkflowTaskServiceImpl.class, DmsSearchEngineImpl.class,
				SectionServiceImpl.class,
				// evaluators
				InstanceLinkExpressionEvaluator.class,
				UserLinkExpressionEvaluator.class,
				// service dao
				com.sirma.itt.cmf.db.DbQueryTemplates.class, QueryDao.class,
				MailNotificationHelperService.class, FolderServiceImpl.class,
				DraftServiceImpl.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildStateExtensionsServices() {
		return new Class<?>[] { StateServiceExtension.class, BaseStateServiceExtension.class,
				CaseStateServiceExtension.class, BaseCaseTreeStateServiceExtension.class,
				DocumentStateServiceExtension.class, SectionStateServiceExtension.class,
				AbstractTaskStateServiceExtension.class, StandaloneTaskStateServiceExtension.class,
				TaskStateServiceExtension.class, WorkflowStateServiceExtension.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildPermissionServices() {
		return new Class<?>[] { AbstractRoleProviderExtension.class,
				ActivitiRoleProviderExtension.class, CmfRoleProviderExtension.class,
				CollectableRoleProviderExtension.class, CaseRoleEvaluator.class,
				CmfDMSExternalEvaluator.class, DocumentRoleEvaluator.class,
				SectionRoleEvaluator.class, StandaloneTaskRoleEvaluator.class,
				TaskRoleEvaluator.class, WorkflowRoleEvaluator.class, CmfRoleProvider.class,
				CaseActionProvider.class, DocumentActionProvider.class,
				SectionActionProvider.class, StandaloneTaskActionProvider.class,
				WorkflowTaskActionProvider.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildJaxBDefinitions() {
		return new Class<?>[] { com.sirma.itt.cmf.beans.jaxb.CaseDefinition.class,
				Definition.class, SubDefinitionType.class, DocumentDefinition.class,
				DocumentDefinitions.class, ObjectFactory.class,
				com.sirma.itt.cmf.beans.jaxb.TemplateDefinition.class, ParameterType.class,
				SectionDefinition.class, SectionsDefinition.class, TaskDefinition.class,
				TaskDefinitions.class, WorkflowDefinition.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildDozerExtensions() {
		return new Class<?>[] { CaseDozerProvider.class, WorkflowDozerProvider.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildModelCasses() {
		return new Class<?>[] { CaseInstance.class, DocumentInstance.class, SectionInstance.class,
				StandaloneTaskInstance.class, TaskInstance.class, TaskState.class,
				VersionInfo.class, WorkflowInfo.class, WorkflowInstanceContext.class,
				RoleProviderExtension.class, FolderInstance.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildUtilityClasses() {
		return new Class<?>[] { CaseAllowedChildrenProvider.class,
				DocumentAllowedChildrenProvider.class,
				CaseInstanceToServiceRegisterExtension.class, CmfEntityTypeProviderExtension.class,
				DocumentInstanceToServiceRegisterExtension.class,
				StandaloneTaskInstanceToServiceRegisterExtension.class,
				TaskInstanceToServiceRegisterExtension.class,
				WorkflowInstanceToServiceRegisterExtension.class, CmfSystemConfigProvider.class,
				DefaultConfigProvider.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildXmlClasses() {
		return new Class<?>[] { XmlType.class, CmfSchemaBuilder.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] buildConfigClasses() {
		return EMTPTY_ARRAY;
	}

	/**
	 * Gets the deployment war name.
	 *
	 * @return the deployment war name
	 */
	protected String getDeploymentWarName() {
		return "cmf-integration-test.war";
	}

	/**
	 * Gets the base jar name. Should be in format groupId-artifactId to match peristance.xml
	 *
	 * @return the base jar name
	 */
	protected String getBaseJarName() {
		return "com.sirma.itt.cmf-cmf-core-impl.jar";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<File, MavenResolvedArtifact> dependencyLibraries() {
		return dependencies;
	}

	@Override
	public Map<File, MavenResolvedArtifact> adaptersLibraries() {
		// see previous revision for sample
		return new HashMap<File, MavenResolvedArtifact>(1);
	}

}
