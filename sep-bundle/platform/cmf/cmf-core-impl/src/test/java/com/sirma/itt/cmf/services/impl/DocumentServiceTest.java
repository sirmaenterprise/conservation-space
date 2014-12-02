package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.HashMap;

import org.junit.Assert;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.testutil.CmfTest;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.exceptions.InstanceDeletedException;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.InstanceProxyMock;

/**
 * The Class DocumentServiceTest.
 *
 * @author BBonev
 */
@Test
public class DocumentServiceTest extends CmfTest {

	/**
	 * Test document refresh.
	 */
	public void testDocumentRefresh() {
		createTypeConverter();

		DocumentServiceImpl service = new DocumentServiceImpl();
		CaseServiceImpl caseService = new CaseServiceImpl();
		InstanceDao instanceDao = Mockito.mock(InstanceDao.class);
		ReflectionUtils.setField(caseService, "instanceDao", instanceDao);
		ReflectionUtils.setField(service, "documentInstanceDao", instanceDao);

		CaseInstance cachedInstance = createCase("caseId");
		cachedInstance.getSections().add(
				createSection("section1", "document1", "document2", "document3"));
		cachedInstance.getSections().add(createSection("section2", "document4", "documen5"));
		cachedInstance.initBidirection();
		cachedInstance.getSections().get(0).getContent().get(1).getProperties().put("key", "value");

		ReflectionUtils.setField(service, "caseInstanceService",
				new InstanceProxyMock<CaseService>(caseService));

		Mockito.when(instanceDao.loadInstance("caseId", null, false)).thenReturn(cachedInstance);

		CaseInstance modifiedCase = createCase("caseId");
		modifiedCase.getSections().add(
				createSection("section1", "document1", "document2", "document3"));
		modifiedCase.getSections().add(createSection("section2", "document4", "documen5"));
		modifiedCase.initBidirection();

		DocumentInstance instance = (DocumentInstance) modifiedCase.getSections().get(0)
				.getContent().get(1);
		service.refresh(instance);
		Assert.assertEquals(instance.getId(), "document2");
		Assert.assertEquals(instance.getOwningInstance().getId(), "section1");
		Assert.assertEquals(((SectionInstance) instance.getOwningInstance()).getContent().size(), 3);
	}

	/**
	 * Test refresh on deleted document.
	 */
	@Test(expectedExceptions = InstanceDeletedException.class, expectedExceptionsMessageRegExp = "Document.+")
	public void testRefreshOnDeletedDocument() {
		createTypeConverter();

		DocumentServiceImpl service = new DocumentServiceImpl();
		CaseServiceImpl caseService = new CaseServiceImpl();
		InstanceDao instanceDao = Mockito.mock(InstanceDao.class);
		ReflectionUtils.setField(caseService, "instanceDao", instanceDao);
		ReflectionUtils.setField(service, "documentInstanceDao", instanceDao);

		CaseInstance cachedInstance = createCase("caseId");
		cachedInstance.getSections().add(createSection("section1", "document1", "documen2"));
		cachedInstance.getSections().add(createSection("section2", "document4", "documen5"));
		cachedInstance.initBidirection();
		cachedInstance.getSections().get(0).getContent().get(1).getProperties().put("key", "value");

		ReflectionUtils.setField(service, "caseInstanceService",
				new InstanceProxyMock<CaseService>(caseService));

		Mockito.when(instanceDao.loadInstance("caseId", null, false)).thenReturn(cachedInstance);

		CaseInstance modifiedCase = createCase("caseId");
		modifiedCase.getSections().add(
				createSection("section1", "document1", "documen2", "document3"));
		modifiedCase.getSections().add(createSection("section2", "document4", "documen5"));
		modifiedCase.initBidirection();

		Instance instance = modifiedCase.getSections().get(0).getContent().get(2);
		service.refresh((DocumentInstance) instance);
	}

	/**
	 * Test refresh on deleted section.
	 */
	@Test(expectedExceptions = InstanceDeletedException.class, expectedExceptionsMessageRegExp = "Section.+")
	public void testRefreshOnDeletedSection() {
		createTypeConverter();

		DocumentServiceImpl service = new DocumentServiceImpl();
		CaseServiceImpl caseService = new CaseServiceImpl();
		InstanceDao instanceDao = Mockito.mock(InstanceDao.class);
		ReflectionUtils.setField(caseService, "instanceDao", instanceDao);
		ReflectionUtils.setField(service, "documentInstanceDao", instanceDao);

		CaseInstance cachedInstance = createCase("caseId");
		cachedInstance.getSections().add(createSection("section2", "document4", "documen5"));
		cachedInstance.initBidirection();
		cachedInstance.getSections().get(0).getContent().get(1).getProperties().put("key", "value");

		ReflectionUtils.setField(service, "caseInstanceService",
				new InstanceProxyMock<CaseService>(caseService));

		Mockito.when(instanceDao.loadInstance("caseId", null, false)).thenReturn(cachedInstance);

		CaseInstance modifiedCase = createCase("caseId");
		modifiedCase.getSections().add(
				createSection("section1", "document1", "documen2", "document3"));
		modifiedCase.getSections().add(createSection("section2", "document4", "documen5"));
		modifiedCase.initBidirection();

		Instance instance = modifiedCase.getSections().get(0).getContent().get(2);
		service.refresh((DocumentInstance) instance);
	}

	/**
	 * Test refresh on moved document.
	 */
	public void testRefreshOnMovedDocument() {
		createTypeConverter();

		DocumentServiceImpl service = new DocumentServiceImpl();
		CaseServiceImpl caseService = new CaseServiceImpl();
		InstanceDao instanceDao = Mockito.mock(InstanceDao.class);
		ReflectionUtils.setField(caseService, "instanceDao", instanceDao);
		ReflectionUtils.setField(service, "documentInstanceDao", instanceDao);

		CaseInstance cachedInstance = createCase("caseId");
		cachedInstance.getSections().add(createSection("section1", "document1", "document3"));
		cachedInstance.getSections().add(createSection("section2", "document2", "documen5"));
		cachedInstance.initBidirection();
		cachedInstance.getSections().get(0).getContent().get(1).getProperties().put("key", "value");

		ReflectionUtils.setField(service, "caseInstanceService",
				new InstanceProxyMock<CaseService>(caseService));

		Mockito.when(instanceDao.loadInstance("caseId", null, false)).thenReturn(cachedInstance);

		CaseInstance modifiedCase = createCase("caseId");
		modifiedCase.getSections().add(
				createSection("section1", "document1", "document2", "document3"));
		modifiedCase.getSections().add(createSection("section2", "document4", "documen5"));
		modifiedCase.initBidirection();

		DocumentInstance instance = (DocumentInstance) modifiedCase.getSections().get(0)
				.getContent().get(1);
		service.refresh(instance);
		Assert.assertEquals(instance.getOwningInstance().getId(), "section2");
		Assert.assertEquals(((SectionInstance) instance.getOwningInstance()).getContent().size(), 2);
		Assert.assertEquals(cachedInstance.getSections().get(1).getContent().size(), 2);
		Assert.assertSame(instance.getOwningInstance(), modifiedCase.getSections().get(1));

	}

	/**
	 * Creates the case.
	 *
	 * @param id
	 *            the case id
	 * @return the case instance
	 */
	private CaseInstance createCase(String id) {
		CaseInstance cachedInstance = new CaseInstance();
		cachedInstance.setId(id);
		cachedInstance.setProperties(new HashMap<String, Serializable>());
		return cachedInstance;
	}

	/**
	 * Creates the document.
	 *
	 * @param id
	 *            the id
	 * @return the document instance
	 */
	private DocumentInstance createDocument(String id) {
		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setId(id);
		documentInstance.setProperties(new HashMap<String, Serializable>());
		return documentInstance;
	}

	/**
	 * Creates the section.
	 *
	 * @param sectionId
	 *            the section id
	 * @param documentIds
	 *            the document ids
	 * @return the section instance
	 */
	private SectionInstance createSection(String sectionId, String... documentIds) {
		SectionInstance section1 = new SectionInstance();
		section1.setId(sectionId);
		section1.setProperties(new HashMap<String, Serializable>());
		if (documentIds != null) {
			for (int i = 0; i < documentIds.length; i++) {
				String string = documentIds[i];
				section1.getContent().add(createDocument(string));
			}
		}
		return section1;
	}
}
