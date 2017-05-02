package com.sirma.itt.emf.cls.persister;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.Assert;

import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeListSheet;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Tests for the {@link CodeListProcessor}.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class CodeListProcessorTest {
	@Mock
	private EntityManager em;

	@Mock
	private InstanceDao codeListInstanceDao;

	@Mock
	private InstanceDao codeValueInstanceDao;

	@Mock
	private EventService eventService;

	@Mock
	private TransactionSupport support;

	@Inject
	private CodeListProcessor processor = new CodeListProcessor();

	/**
	 * Test the {@link CodeListSheet} merging.
	 */
	@Test
	public void should_mergeCodelists_forValidSheets() {
		CodeList cl = new CodeList();
		cl.setValue("1");
		CodeValue value = new CodeValue();
		value.setValue("value");
		value.setExtra1("extra");
		cl.setCodeValues(new ArrayList<>(Arrays.asList(value)));
		CodeListSheet sheet = new CodeListSheet();
		sheet.addCodeList(cl);

		CodeList secondCL = new CodeList();
		secondCL.setValue("2");
		// This codelist will overwrite codelist 1 and it's values from the first sheet.
		CodeList thirdCL = new CodeList();
		thirdCL.setValue("1");
		CodeValue secondValue = new CodeValue();
		secondValue.setValue("value");
		secondValue.setExtra1("overwrite");
		thirdCL.setCodeValues(new ArrayList<>(Arrays.asList(secondValue)));

		CodeListSheet secondSheet = new CodeListSheet();
		secondSheet.addCodeList(secondCL);
		secondSheet.addCodeList(thirdCL);

		CodeListSheet result = processor.mergeSheets(Arrays.asList(sheet, secondSheet));

		Assert.assertEquals(result.getCodeLists().size(), 2);
		// Assert that the new codelist has been inserted.
		Assert.assertEquals(result.getCodeLists().get(1).getValue(), "2");
		// Assert that the codevalue has been overwritten
		Assert.assertEquals(result.getCodeLists().get(0).getCodeValues().get(0).getExtra1(), "overwrite");
	}
}
