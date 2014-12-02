package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.HashMap;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.testutil.CmfTest;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.instance.dao.BaseInstanceService;
import com.sirma.itt.emf.instance.dao.InstanceDao;

/**
 * The Class CaseServiceTest.
 */
@Test
public class CaseServiceTest extends CmfTest {

	/**
	 * Initializes the.
	 */
	@BeforeMethod
	public void init() {
		createTypeConverter();
	}

	/**
	 * Test case refresh_removed document.
	 */
	public void testCaseRefresh_removedDocument() {
		BaseInstanceService<CaseInstance, CaseDefinition> service = new CaseServiceImpl();
		InstanceDao instanceDao = Mockito.mock(InstanceDao.class);
		ReflectionUtils.setField(service, "instanceDao", instanceDao);
		CaseInstance cachedInstance = createCase("caseId");
		cachedInstance.getSections().add(createSection("section1"));

		Mockito.when(instanceDao.loadInstance("caseId", null, false)).thenReturn(cachedInstance);
		Mockito.when(instanceDao.isModified(Mockito.any(CaseInstance.class))).thenReturn(true);

		// extra documents, need to delete all
		CaseInstance instance = createCase("caseId");
		instance.getSections().add(createSection("section1", "document1", "document2"));
		instance.initBidirection();

		service.refresh(instance);
		Assert.assertEquals(instance.getSections().size(), 1);
		Assert.assertEquals(instance.getSections().get(0).getContent().size(), 0);

		// added document in the working copy and need to be removed
		instance = createCase("caseId");
		instance.getSections().add(createSection("section1", "document1", "document2"));
		instance.initBidirection();
		cachedInstance.getSections().add(createSection("section1", "document1"));
		cachedInstance.initBidirection();

		service.refresh(instance);
		Assert.assertEquals(instance.getSections().size(), 1);
		Assert.assertEquals(instance.getSections().get(0).getContent().size(), 1);
	}

	/**
	 * Test case refresh_added document.
	 */
	public void testCaseRefresh_addedDocument() {
		BaseInstanceService<CaseInstance, CaseDefinition> service = new CaseServiceImpl();
		InstanceDao instanceDao = Mockito.mock(InstanceDao.class);
		ReflectionUtils.setField(service, "instanceDao", instanceDao);
		CaseInstance cachedInstance = createCase("caseId");

		Mockito.when(instanceDao.loadInstance("caseId", null, false)).thenReturn(cachedInstance);
		Mockito.when(instanceDao.isModified(Mockito.any(CaseInstance.class))).thenReturn(true);

		// removed document from the working copy should be added after refresh
		CaseInstance instance = createCase("caseId");
		instance.getSections().add(createSection("section1", "document1", "document2"));
		instance.initBidirection();

		cachedInstance.getSections().add(
				createSection("section1", "document1", "document2", "document3"));
		cachedInstance.initBidirection();

		service.refresh(instance);
		Assert.assertEquals(instance.getSections().size(), 1);
		Assert.assertEquals(instance.getSections().get(0).getContent().size(), 3);
		Assert.assertEquals(instance.getSections().get(0).getContent().get(2).getId(), "document3");
	}

	/**
	 * Test case refresh_not changed.
	 */
	public void testCaseRefresh_notChanged() {
		BaseInstanceService<CaseInstance, CaseDefinition> service = new CaseServiceImpl();
		InstanceDao instanceDao = Mockito.mock(InstanceDao.class);
		ReflectionUtils.setField(service, "instanceDao", instanceDao);
		CaseInstance cachedInstance = createCase("caseId");

		Mockito.when(instanceDao.loadInstance("caseId", null, false)).thenReturn(cachedInstance);
		Mockito.when(instanceDao.isModified(Mockito.any(CaseInstance.class))).thenReturn(true);

		CaseInstance instance = createCase("caseId");
		// the same count - nothing to be touched
		instance.getSections().add(createSection("section1", "document1", "document2"));
		instance.initBidirection();
		cachedInstance.getSections().add(createSection("section1", "document1", "document2"));
		cachedInstance.initBidirection();

		service.refresh(instance);
		Assert.assertEquals(instance.getSections().size(), 1);
		Assert.assertEquals(instance.getSections().get(0).getContent().size(), 2);
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
