
package com.sirma.itt.cmf.integration.webscript;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerOptions;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.sirma.itt.cmf.integration.ServiceProxy;
import com.sirma.itt.cmf.integration.exception.SEIPRuntimeException;
import com.sirma.itt.cmf.integration.model.CMFModel;
import com.sirma.itt.cmf.integration.service.CMFService;

/**
 * Gets a node provided by the request and returns the pdf content child url. First is created if not.
 *
 * {@link BaseAlfrescoScript}
 *
 * @author bbanchev
 */
public class ContentTransformScript extends BaseAlfrescoScript {
	/** The Constant CONTENT_DOWNLOAD_PROP_URL. */
	private static final String CONTENT_PROP_URL = "/d/d/{0}/{1}/{2}/{3}?property={4}";

	/** The Constant DOWNLOAD_URL. */
	private static final String DOWNLOAD_URL = "downloadURL";

	/** The Constant APP_PDF_MIME_TYPE. */
	private static final String APP_PDF_MIME_TYPE = "application/pdf";
	/** The content service. */
	private ContentService contentService;

	/** The mimetype service. */
	private SysAdminParams sysAdminParams;

	/** The mimetype service. */
	private MimetypeService mimetypeService;

	/** The filefolder service. */
	private FileFolderService filefolderService;

	/** The Constant CC_COPY_STAMP_FORMAT. */
	private static final String CC_COPY_STAMP_FORMAT = "MM/dd/yyyy hh:mm aaa";

	/** The base url. */
	private String baseURL;

	/** The transformer service. */
	private DocumentsTransformerService transformerService;

	private Repository repository;

	private Map<String, NodeRef> versionedPreviewSpaces = new TreeMap<String, NodeRef>();

	/**
	 * Creates a temporary node with unique name that will service for temporary usage by the content transformer.
	 *
	 * @param nodeToCopy
	 *            is the node to create temporary for
	 * @return instance of the created node
	 */
	private NodeRef addTempPrintNode(NodeRef nodeToCopy) {
		if ("versionStore".equals(nodeToCopy.getStoreRef().getProtocol())) {
			NodeRef previewSpace = getVersionedPreviewSpace();
			Serializable version = nodeService.getProperty(nodeToCopy, ContentModel.PROP_VERSION_LABEL);
			NodeRef nodeSpace = nodeService.getChildByName(previewSpace, ContentModel.ASSOC_CONTAINS,
					nodeToCopy.getId());
			NodeRef versionNode = null;
			if (nodeSpace == null) {
				nodeSpace = createNode(previewSpace, nodeToCopy.getId(), ContentModel.TYPE_FOLDER);
			} else {
				versionNode = nodeService.getChildByName(nodeSpace, ContentModel.ASSOC_CONTAINS, version.toString());
			}
			if (versionNode == null) {
				versionNode = createNode(nodeSpace, version, ContentModel.TYPE_THUMBNAIL);
				nodeService.addAspect(versionNode, ContentModel.ASPECT_TEMPORARY, null);
			}
			return versionNode;
		}
		return getServiceProxy().createPdfPreview(nodeToCopy);
	}

	/**
	 * Internal create node with specified name.
	 *
	 * @param parent
	 *            is the target node
	 * @param name
	 *            is the name to set
	 * @param type
	 *            is the file type
	 * @return the creted node.
	 */
	private NodeRef createNode(NodeRef parent, Serializable name, QName type) {
		Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
		props.put(ContentModel.PROP_NAME, name);
		return nodeService
				.createNode(parent, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name.toString()), type, props)
				.getChildRef();
	}

	/**
	 * The Class DocumentsTransformerService.
	 */
	static class DocumentsTransformerService {

		/** The registry. */
		private ServiceRegistry registry;

		/** The Constant PLAIN_TEXT_MIME_TYPE. */
		private static final String PLAIN_TEXT_MIME_TYPE = MimetypeMap.MIMETYPE_TEXT_PLAIN;

		/** The Constant IMAGE_PNG_MIME_TYPE. */
		private static final String IMAGE_PNG_MIME_TYPE = MimetypeMap.MIMETYPE_IMAGE_PNG;

		/** The Constant APP_PDF_MIME_TYPE. */
		private static final String APP_PDF_MIME_TYPE = MimetypeMap.MIMETYPE_PDF;

		/** The proxy. */
		private ServiceProxy proxy;

		/**
		 * Instantiates a new documents transformer service.
		 *
		 * @param proxy
		 *            the proxy
		 * @param serviceRegistry
		 *            the service registry
		 */
		public DocumentsTransformerService(ServiceProxy proxy, ServiceRegistry serviceRegistry) {
			this.proxy = proxy;
			registry = serviceRegistry;
		}

		/**
		 * nothing to init.
		 */
		public void init() {
			//
		}

		/**
		 * The method checks the mimetype of the document and if it is not pdf transform it to. For images is 2 step
		 * process: convert to jpeg, add to pdf.
		 *
		 * @param transformableNode
		 *            is the node to transform to pdf
		 * @return the transformed/same node on sucess
		 */
		public NodeRef transformToPDF(NodeRef transformableNode) {
			String sourceMimetype = guessMimetype(transformableNode);
			if (sourceMimetype.startsWith("image/")) {
				transformFromImage(transformableNode, sourceMimetype);
			} else if (sourceMimetype.equals("message/rfc822")) {
				transformFromPdf(transformableNode, sourceMimetype);
			} else if (!APP_PDF_MIME_TYPE.equals(sourceMimetype)) {
				transformFromAnyDocument(transformableNode, sourceMimetype);
			}
			// rename to pdf extension
			setNewName(transformableNode, sourceMimetype);
			return transformableNode;
		}

		/**
		 * Transforms node to pdf by relaying on the direct transforming. The result node is the same
		 *
		 * @param transformableNode
		 *            is the node to transform
		 * @param sourceMimetype
		 *            is the source mimetype
		 */
		private void transformFromAnyDocument(NodeRef transformableNode, String sourceMimetype) {
			TransformationOptions options = new RuntimeExecutableContentTransformerOptions();
			// create the new node
			// set up transformation options
			options.setSourceContentProperty(ContentModel.PROP_CONTENT);
			options.setSourceNodeRef(transformableNode);
			options.setTargetContentProperty(ContentModel.PROP_CONTENT);
			options.setTargetNodeRef(transformableNode);

			ContentTransformer transformer = proxy.getTransformer(sourceMimetype, APP_PDF_MIME_TYPE, options);
			// if we don't have a transformer, throw an error
			if (transformer == null) {
				throw new WebScriptException(500, "Unable to locate transformer");
			}
			// establish a content reader (from source)
			ContentReader contentReader = getContentService().getReader(transformableNode, ContentModel.PROP_CONTENT);
			// establish a content writer (to destination)
			ContentWriter contentWriter = getContentService().getWriter(transformableNode, ContentModel.PROP_CONTENT,
					true);
			contentWriter.setMimetype(APP_PDF_MIME_TYPE);
			// do the transformation
			transformer.transform(contentReader, contentWriter, options);

		}

		/**
		 * Set new name of the document by setting pdf extension.
		 *
		 * @param stampablePDFNode
		 *            is the node to rename
		 * @param sourceMimeType
		 *            its type to determine the extension and replace with pdf
		 */
		private void setNewName(NodeRef stampablePDFNode, String sourceMimeType) {
			try {
				String name = getFilename(stampablePDFNode);
				String newName = null;
				// with dot to prevent incorrect substitution
				String ext = '.' + registry.getMimetypeService().getExtension(sourceMimeType);
				if (!name.endsWith(ext)) {
					int extIndex = name.lastIndexOf('.');
					String tempExt = name.substring(extIndex, name.length()).toLowerCase();
					if (ext.contains(tempExt) || tempExt.contains(ext)) {
						ext = name.substring(extIndex, name.length());
					}
				}
				newName = name.replaceAll(ext, ".pdf");
				registry.getNodeService().setProperty(stampablePDFNode, ContentModel.PROP_NAME, newName);
			} catch (Exception e) {
				//
			}
		}

		/**
		 * Creates a pdf from image: 1. create a jpeg image (pdf box works with it) from the document 2. add the jpeg
		 * image to a new pdf .
		 *
		 * @param document
		 *            is the document to print - some image
		 * @param sourceMimetype
		 *            is the source mimetype
		 * @return the created pdf node if the image is created successfully, null otherwise
		 */
		private NodeRef transformFromImage(NodeRef document, String sourceMimetype) {

			Document createPDFFromImage = createPDFFromImage(document, document, sourceMimetype);
			return createPDFFromImage != null ? document : null;
		}

		/**
		 * Creates a pdf from image: 1. create a jpeg image (pdf box works with it) from the document 2. add the jpeg
		 * image to a new pdf .
		 *
		 * @param document
		 *            is the document to print - some image
		 * @param sourceMimetype
		 *            is the mimetype of the document
		 * @return the created pdf node if the image is created successfully, null otherwise
		 */
		private NodeRef transformFromPdf(NodeRef document, String sourceMimetype) {

			NodeRef mail = createPDFFromMail(document, document, sourceMimetype);

			return mail != null ? document : null;
		}

		/**
		 * Try to convert image to jpeg and add the resulted image to pdf. When converting to jpeg image is resized as
		 * well to fit A4.
		 *
		 * @param document
		 *            is the input node - the image
		 * @param targetNode
		 *            - is the node to contain the image as pdf - might be the same as input
		 * @param sourceMimetype
		 *            is the source mimetype
		 * @return the created pdf document
		 */
		public Document createPDFFromImage(NodeRef document, NodeRef targetNode, String sourceMimetype) {
			Document pdf = null;
			File pngFile = null;
			try {
				int marginOffset = 40;
				// convert to jpeg
				ContentReader contentReader = getContentService().getReader(document, ContentModel.PROP_CONTENT);
				BufferedInputStream streamInput = new BufferedInputStream(contentReader.getContentInputStream());
				Image image = null;
				if (IMAGE_PNG_MIME_TYPE.equals(sourceMimetype)) {
					BufferedImage read = ImageIO.read(streamInput);
					pngFile = File.createTempFile(GUID.generate(), null);
					ImageIO.write(read, "jpeg", pngFile);
					image = Image.getInstance(pngFile.getAbsolutePath());
				} else {
					image = Image.getInstance(convertInputStreamToByteStream(streamInput).toByteArray());
				}
				Rectangle pageSizeA4 = PageSize.A4;
				// calculate sizes
				float imageWidth = image.getWidth();
				float imageHeight = image.getHeight();
				float fitWidth = pageSizeA4.getWidth() - marginOffset;
				float fitHeight = pageSizeA4.getHeight() - marginOffset;

				if (imageWidth < fitWidth) {
					if (imageHeight > fitHeight) {
						image.scaleToFit(fitWidth - 20, fitHeight - 20);
					}
				} else {
					if (imageHeight < fitWidth) {
						pageSizeA4 = pageSizeA4.rotate();
						if (imageWidth > fitHeight) {
							image.scaleToFit(fitWidth - 20, fitHeight - 20);
						}
					} else {
						image.scaleToFit(fitWidth - 20, fitHeight - 20);
					}
				}

				ContentWriter contentWriter = getContentService().getWriter(targetNode, ContentModel.PROP_CONTENT,
						true);
				contentWriter.setMimetype(APP_PDF_MIME_TYPE);
				// new pdf
				pdf = new Document(pageSizeA4);
				// it is needed
				PdfWriter.getInstance(pdf, contentWriter.getContentOutputStream());
				// set only top and left - image is 'marginOffset' smaller
				pdf.setMargins(marginOffset / 2, 0, marginOffset / 2, 0);

				pdf.open();
				pdf.add(image);
				return pdf;
			} catch (Exception e) {
				e.printStackTrace();
				throw new WebScriptException(e.getLocalizedMessage());
			} finally {
				if (pdf != null) {
					pdf.close();
				}
				if (pngFile != null) {
					pngFile.delete();
				}
			}
		}

		/**
		 * Reads input stream to byte array stream.
		 *
		 * @param streamInput
		 *            is the input stream to convert
		 * @return {@link ByteArrayOutputStream} instance with read bytes
		 * @throws IOException
		 *             on io error
		 */
		private ByteArrayOutputStream convertInputStreamToByteStream(InputStream streamInput) throws IOException {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(streamInput.available());
			int read = 0;
			while ((read = streamInput.read()) != -1) {
				byteArrayOutputStream.write(read);
			}
			return byteArrayOutputStream;
		}

		/**
		 * Try to convert mail to plain text and the the text to a pdf.
		 *
		 * @param document
		 *            is the input node - the email
		 * @param targetNode
		 *            - is the node to contain the email as pdf - might be the same as input
		 * @param sourceMimetype
		 *            is the mimetype
		 * @return the created pdf document
		 */
		public NodeRef createPDFFromMail(NodeRef document, NodeRef targetNode, String sourceMimetype) {
			NodeRef plainTextNode = convertEmailToPlain(document, targetNode, sourceMimetype);

			ContentReader contentReader = getContentService().getReader(plainTextNode, ContentModel.PROP_CONTENT);
			// establish a content writer (to destination)
			ContentWriter contentWriter = getContentService().getWriter(plainTextNode, ContentModel.PROP_CONTENT, true);
			contentWriter.setMimetype(APP_PDF_MIME_TYPE);

			TransformationOptions options = new TransformationOptions();
			// set up transformation options
			options.setSourceContentProperty(ContentModel.PROP_CONTENT);
			options.setSourceNodeRef(plainTextNode);
			options.setTargetContentProperty(ContentModel.PROP_CONTENT);
			options.setTargetNodeRef(plainTextNode);
			ContentTransformer transformer = proxy.getTransformer(PLAIN_TEXT_MIME_TYPE, APP_PDF_MIME_TYPE, options);
			if (transformer == null) {
				throw new WebScriptException(500, "Unable to locate plain text transformer");
			}
			// do the transformation
			transformer.transform(contentReader, contentWriter, options);
			return plainTextNode;
		}

		/**
		 * internal convert email to plain text.
		 *
		 * @param document
		 *            is the document to process
		 * @param plainTextNode
		 *            is the node to put the plain text in
		 * @param sourceMimetype
		 *            is the mimetype of input
		 * @return the created plain text node
		 */
		private NodeRef convertEmailToPlain(NodeRef document, NodeRef plainTextNode, String sourceMimetype) {

			TransformationOptions options = new TransformationOptions();

			// set up transformation options
			options.setSourceContentProperty(ContentModel.PROP_CONTENT);
			options.setSourceNodeRef(document);
			options.setTargetContentProperty(ContentModel.PROP_CONTENT);
			options.setTargetNodeRef(plainTextNode);
			ContentTransformer transformer = proxy.getTransformer(sourceMimetype, PLAIN_TEXT_MIME_TYPE, options);
			// if we don't have a transformer, throw an error
			if (transformer == null) {
				throw new WebScriptException(500, "Unable to locate email transformer");
			}
			// establish a content reader (from source)
			ContentReader contentReader = getContentService().getReader(document, ContentModel.PROP_CONTENT);
			// establish a content writer (to destination)
			ContentWriter contentWriter = getContentService().getWriter(plainTextNode, ContentModel.PROP_CONTENT, true);
			contentWriter.setMimetype(PLAIN_TEXT_MIME_TYPE);
			// do the transformation
			transformer.transform(contentReader, contentWriter, options);
			return plainTextNode;
		}

		/**
		 * Gets the content service.
		 *
		 * @return the service
		 */
		private ContentService getContentService() {
			return registry.getContentService();
		}

		/**
		 * get the filename of node using {@link FileFolderService}.
		 *
		 * @param nodeRef
		 *            is the node
		 * @return the filename
		 */
		protected String getFilename(NodeRef nodeRef) {
			return getFileService().getFileInfo(nodeRef).getName();
		}

		/**
		 * Gets the file service.
		 *
		 * @return the file service
		 */
		private FileFolderService getFileService() {
			return registry.getFileFolderService();
		}

		/**
		 * get the mimetype of node using {@link MimetypeService}.
		 *
		 * @param nodeRef
		 *            is the node
		 * @return the mimetype
		 */
		protected String guessMimetype(NodeRef nodeRef) {
			String filename = getFilename(nodeRef);
			return getMimetypeService().guessMimetype(filename);
		}

		/**
		 * Gets the mimetype service.
		 *
		 * @return the mimetype service
		 */
		private MimetypeService getMimetypeService() {
			return registry.getMimetypeService();
		}

		/**
		 * Sets the registry.
		 *
		 * @param registry
		 *            the registry to set
		 */
		public void setRegistry(ServiceRegistry registry) {
			this.registry = registry;
		}

		/**
		 * Gets the registry.
		 *
		 * @return the registry
		 */
		public ServiceRegistry getRegistry() {
			return registry;
		}

	}

	/**
	 * Retrieve the versioned node space, where version previews are stored.
	 *
	 * @return the store
	 */
	private NodeRef getVersionedPreviewSpace() {
		synchronized (versionedPreviewSpaces) {

			String tenantId = CMFService.getTenantId();
			NodeRef versionedPreviewSpace = versionedPreviewSpaces.get(tenantId);
			if (versionedPreviewSpace != null) {
				return versionedPreviewSpace;
			}

			List<ChildAssociationRef> childAssocs = getNodeService().getChildAssocs(repository.getRootHome());
			NodeRef systemNode = null;
			for (ChildAssociationRef childAssociationRef : childAssocs) {
				if (CMFModel.SYSTEM_QNAME.equals(childAssociationRef.getQName())) {
					systemNode = childAssociationRef.getChildRef();
					break;
				}
			}
			if (systemNode == null) {
				throw new SEIPRuntimeException("System space not found!");
			}
			String previewNodeName = "versionedNodesPreviewSpace";
			versionedPreviewSpace = cmfService.getChildContainerByName(systemNode, previewNodeName);
			QName versionNodeName = QName.createQName(CMFModel.CMF_MODEL_1_0_URI, previewNodeName);
			if (versionedPreviewSpace == null) {
				childAssocs = getNodeService().getChildAssocs(systemNode);
				for (ChildAssociationRef childAssociationRef : childAssocs) {
					if (versionNodeName.equals(childAssociationRef.getQName())) {
						versionedPreviewSpace = childAssociationRef.getChildRef();
						break;
					}
				}

			}

			if (versionedPreviewSpace == null) {
				Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
				props.put(ContentModel.PROP_NAME, previewNodeName);
				versionedPreviewSpace = nodeService.createNode(systemNode, ContentModel.ASSOC_CHILDREN, versionNodeName,
						ContentModel.TYPE_FOLDER, props).getChildRef();
			}
			versionedPreviewSpaces.put(tenantId, versionedPreviewSpace);
			return versionedPreviewSpace;
		}
	}

	/**
	 * Sets the repository.
	 *
	 * @param repository
	 *            the repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * Gets the repository.
	 *
	 * @return the repository
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * Execute internal. Wrapper for system user action.
	 *
	 * @param req
	 *            the original request
	 * @return the updated model
	 */
	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		NodeRef transformable = getNodeRef(req);
		if ((transformable == null) || !nodeService.exists(transformable)) {
			throw new WebScriptException(404, "Unable to locate document");
		}
		String targetMimetype = req.getParameter("mimetype") == null ? APP_PDF_MIME_TYPE : req.getParameter("mimetype");
		Map<String, Object> model = new HashMap<String, Object>(1);
		NodeRef newNodeRef = null;
		String lockUser = null;
		try {
			ContentData contentData = (ContentData) getNodeService().getProperty(transformable,
					ContentModel.PROP_CONTENT);
			if (contentData.getSize() == 0) {
				throw new WebScriptException(204, "File don't have Content");
			}
			String sourceMimetype = guessMimetype(transformable);
			if (!targetMimetype.equals(sourceMimetype)) {
				if ("versionStore".equals(transformable.getStoreRef().getProtocol())) {
					// version are cached in other temp space
					newNodeRef = findVersionNodePreview(transformable);
				} else {
					newNodeRef = getServiceProxy().findPdfPreview(transformable);
				}
				if (newNodeRef == null) {
					lockUser = cmfLockService.unlockNode(transformable);
					TransformationOptions options = new RuntimeExecutableContentTransformerOptions();
					// set up transformation options
					options.setSourceContentProperty(ContentModel.PROP_CONTENT);
					options.setSourceNodeRef(transformable);
					options.setTargetContentProperty(ContentModel.PROP_CONTENT);
					options.setIncludeEmbedded(Boolean.TRUE);
					ContentTransformer transformer = getServiceProxy().getTransformer(sourceMimetype, targetMimetype,
							options);
					// if we don't have a transformer, throw an error
					if (transformer == null) {
						if (sourceMimetype.startsWith("image/")) {
							// create the new node
							newNodeRef = addTempPrintNode(transformable);
							options.setTargetNodeRef(newNodeRef);
							// alaways pdf as fallback
							newNodeRef = transformNodeToImage(transformable, newNodeRef, sourceMimetype);
							if (newNodeRef == null) {
								throw new WebScriptException(500, "Unable to convert from image");
							}
						} else {
							throw new WebScriptException(204,
									"Unable to locate transformer for " + sourceMimetype + " to " + targetMimetype);
						}
					} else {
						// create the new node
						newNodeRef = addTempPrintNode(transformable);
						options.setTargetNodeRef(newNodeRef);
						// establish a content reader (from source)
						ContentReader contentReader = getContentService().getReader(transformable,
								ContentModel.PROP_CONTENT);
						// establish a content writer (to destination)
						ContentWriter contentWriter = getContentService().getWriter(newNodeRef,
								ContentModel.PROP_CONTENT, true);
						contentWriter.setMimetype(targetMimetype);
						// do the transformation
						transformer.transform(contentReader, contentWriter, options);
					}
				}
			} else {
				newNodeRef = transformable;
			}
			String url = MessageFormat.format(CONTENT_PROP_URL,
					new Object[] { newNodeRef.getStoreRef().getProtocol(), newNodeRef.getStoreRef().getIdentifier(),
							newNodeRef.getId(), URLEncoder.encode(getName(newNodeRef)),
							URLEncoder.encode(ContentModel.PROP_CONTENT.toString()) });
			model.put(DOWNLOAD_URL, getBasePath() + url);
			model.put(KEY_NODEID, newNodeRef.toString());

		} catch (Exception e) {
			e.printStackTrace();
			throw createStatus(500, e.toString());
		} finally {
			if (lockUser != null) {
				cmfLockService.lockNode(transformable, lockUser);
			}
		}
		return model;
	}

	/**
	 * Finds a stored preview for the provided node
	 *
	 * @param versionNode
	 *            is the node to look for preview
	 * @return the found node or null otherwise
	 */
	private NodeRef findVersionNodePreview(NodeRef versionNode) {
		NodeRef previewSpace = getVersionedPreviewSpace();
		Serializable version = nodeService.getProperty(versionNode, ContentModel.PROP_VERSION_LABEL);
		NodeRef nodeSpace = nodeService.getChildByName(previewSpace, ContentModel.ASSOC_CONTAINS, versionNode.getId());
		if (nodeSpace == null) {
			return null;
		}
		return nodeService.getChildByName(nodeSpace, ContentModel.ASSOC_CONTAINS, version.toString());
	}

	/**
	 * Creates a pdf from image: 1. create a jpeg image (pdf box works with it) from the printedDocument 2. add the jpeg
	 * image to a new pdf .
	 *
	 * @param printedDocument
	 *            is the document to print - some image
	 * @param newNodeRef
	 *            the new node ref
	 * @param sourceMimetype
	 *            is the source mimetype
	 * @return the created pdf node if the image is created successfully, null otherwise
	 */
	private NodeRef transformNodeToImage(NodeRef printedDocument, NodeRef newNodeRef, String sourceMimetype) {
		Document createPDFFromImage = getTransformService().createPDFFromImage(printedDocument, newNodeRef,
				sourceMimetype);
		return createPDFFromImage != null ? newNodeRef : null;
	}

	/**
	 * Gets the base path.
	 *
	 * @return the base path
	 */
	private String getBasePath() {
		if (baseURL == null) {
			baseURL = new StringBuilder(getSysAdminParams().getAlfrescoProtocol()).append("://")
					.append(getSysAdminParams().getAlfrescoHost()).append(":")
					.append(getSysAdminParams().getAlfrescoPort()).append("/")
					.append(getSysAdminParams().getAlfrescoContext()).toString();
		}
		return baseURL;
	}

	/**
	 * Gets the name for given node.
	 *
	 * @param node
	 *            the node
	 * @return the name
	 */
	public String getName(NodeRef node) {
		Serializable name = null;
		// try and get the name from the properties first
		name = nodeService.getProperty(node, ContentModel.PROP_NAME);

		// if we didn't find it as a property get the name from the
		// association name
		if (name == null) {
			ChildAssociationRef parentRef = nodeService.getPrimaryParent(node);
			if ((parentRef != null) && (parentRef.getQName() != null)) {
				name = parentRef.getQName().getLocalName();
			} else {
				name = "";
			}
		}

		return name.toString();
	}

	/**
	 * Gets the content service.
	 *
	 * @return the content service
	 */
	private ContentService getContentService() {
		if (contentService == null) {
			contentService = serviceRegistry.getContentService();
		}
		return contentService;
	}

	/**
	 * gets the date for {@value #CC_COPY_STAMP_FORMAT} format.
	 *
	 * @param node
	 *            is the node that holds properties
	 * @param key
	 *            is the key
	 * @return the formated date
	 */
	public String getDateProperty(NodeRef node, QName key) {
		Map<QName, Serializable> properties = getNodeService().getProperties(node);
		Serializable serializable = properties.get(key);
		if (serializable instanceof Date) {
			return new SimpleDateFormat(CC_COPY_STAMP_FORMAT).format(serializable);
		}
		return "";
	}

	/**
	 * get the mimetype of node using {@link MimetypeService}.
	 *
	 * @param nodeRef
	 *            is the node to check
	 * @return the mimetype or on uknown return the tika detected from content mimetype
	 */
	protected String guessMimetype(NodeRef nodeRef) {
		FileInfo fileInfo = getFileService().getFileInfo(nodeRef);
		String filename = fileInfo.getName();
		String guessMimetype = getMimetypeService().guessMimetype(filename);
		if ("application/octet-stream".equals(guessMimetype)) {
			try {
				ContentData contentData = fileInfo.getContentData();
				ContentReader rawReader = getContentService().getRawReader(contentData.getContentUrl());
				TikaInputStream tikaInputStream = TikaInputStream.get(rawReader.getContentInputStream());
				Metadata metadata = new Metadata();
				metadata.set(TikaCoreProperties.IDENTIFIER, filename);
				MediaType detect = new AutoDetectParser().getDetector().detect(tikaInputStream, metadata);
				return detect.toString();
			} catch (Exception e) {
				getLogger().warn("Mimetype could not be detected: " + e.getMessage(), e);
			}
		}
		return guessMimetype;
	}

	/**
	 * Gets the mimetype service.
	 *
	 * @return the mimetype service
	 */
	private MimetypeService getMimetypeService() {
		if (mimetypeService == null) {
			mimetypeService = serviceRegistry.getMimetypeService();
		}
		return mimetypeService;
	}

	/**
	 * Gets the file service.
	 *
	 * @return the file service
	 */
	private FileFolderService getFileService() {
		if (filefolderService == null) {
			filefolderService = serviceRegistry.getFileFolderService();
		}
		return filefolderService;
	}

	/**
	 * Gets the transform util.
	 *
	 * @return the transform util
	 */
	private DocumentsTransformerService getTransformService() {
		if (transformerService == null) {
			transformerService = new DocumentsTransformerService(getServiceProxy(), getServiceRegistry());
		}
		return transformerService;
	}

	/**
	 * Gets the sys admin params.
	 *
	 * @return the sys admin params
	 */
	public SysAdminParams getSysAdminParams() {
		return sysAdminParams;
	}

	/**
	 * Sets the sys admin params.
	 *
	 * @param sysAdminParams
	 *            the new sys admin params
	 */
	public void setSysAdminParams(SysAdminParams sysAdminParams) {
		this.sysAdminParams = sysAdminParams;
	}

}