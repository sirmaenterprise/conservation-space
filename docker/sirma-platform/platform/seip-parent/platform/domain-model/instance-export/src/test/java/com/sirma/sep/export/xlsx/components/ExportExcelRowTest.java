package com.sirma.sep.export.xlsx.components;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.sep.export.xlsx.components.ExportExcelCell;
import com.sirma.sep.export.xlsx.components.ExportExcelRow;

/**
 * @author Boyan Tonchev.
 */
public class ExportExcelRowTest {

	@Test
	public void should_ExportExcelCell_When_PropertyIsSet() {
		String propertyName = "propertyName";
		String propertyValue = "propertyValue";
		ExportExcelRow row = new ExportExcelRow();
		ExportExcelCell cell = new ExportExcelCell(propertyName, propertyValue, ExportExcelCell.Type.OBJECT);
		row.addProperty(propertyName, cell);

		ExportExcelCell excelCell = row.getProperty(propertyName);

		Assert.assertEquals(propertyName, excelCell.getName());
		Assert.assertEquals(ExportExcelCell.Type.OBJECT, excelCell.getType());
		Assert.assertEquals(propertyValue, excelCell.getValue());
	}

	@Test
	public void should_ReturnEmptyExportExcelCell_When_PropertyIsNotFound() {
		String missingPropertyName = "missingPropertyName";
		ExportExcelRow row = new ExportExcelRow();

		ExportExcelCell excelCell = row.getProperty(missingPropertyName);

		Assert.assertEquals(missingPropertyName, excelCell.getName());
		Assert.assertEquals(ExportExcelCell.Type.OBJECT, excelCell.getType());
		Assert.assertEquals("", excelCell.getValue());
	}
}