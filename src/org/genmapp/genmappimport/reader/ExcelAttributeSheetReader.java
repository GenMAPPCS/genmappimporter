/*******************************************************************************
 * Copyright 2010 Alexander Pico
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.genmapp.genmappimport.reader;

import cytoscape.data.CyAttributes;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import giny.model.Node;

import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reader for Excel attribute workbook.<br>
 * This class creates string array and pass it to the AttributeLineParser.<br>
 * 
 * <p>
 * This reader takes one sheet at a time.
 * </p>
 * 
 */
public class ExcelAttributeSheetReader implements TextTableReader {
	private final HSSFSheet sheet;
	private final AttributeMappingParameters mapping;
	private final AttributeLineParser parser;
	
	private final int startLineNumber;
	private int globalCounter = 0;

	/**
	 * Constructor.<br>
	 * 
	 * Takes one Excel sheet as parameter.
	 * 
	 * @param sheet
	 * @param mapping
	 */
	public ExcelAttributeSheetReader(final HSSFSheet sheet,
			final AttributeMappingParameters mapping, final int startLineNumber) {
		this.sheet = sheet;
		this.mapping = mapping;
		this.startLineNumber = startLineNumber;
		this.parser = new AttributeLineParser(mapping);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public List getColumnNames() {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public void readTable() throws IOException {
		HSSFRow row;
		int rowCount = startLineNumber;
		String[] cellsInOneRow;

		while ((row = sheet.getRow(rowCount)) != null) {
			cellsInOneRow = createElementStringArray(row);
			try {
				parser.parseAll(cellsInOneRow);
			} catch (Exception ex) {
				System.out.println("Couldn't parse row: " + rowCount);
				ex.printStackTrace();
			}

			rowCount++;
			globalCounter++;
		}
	}

	/**
	 * For a given Excel row, convert the cells into String.
	 * 
	 * @param row
	 * @return
	 */
	private String[] createElementStringArray(HSSFRow row) {
		String[] cells = new String[mapping.getColumnCount()];
		HSSFCell cell = null;

		for (short i = 0; i < mapping.getColumnCount(); i++) {
			cell = row.getCell(i);

			if (cell == null) {
				cells[i] = null;
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
				cells[i] = cell.getRichStringCellValue().getString();
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
				if (mapping.getAttributeTypes()[i] == CyAttributes.TYPE_INTEGER) {
					Double dblValue = cell.getNumericCellValue();
					Integer intValue = dblValue.intValue();
					cells[i] = intValue.toString();
				} else {
					cells[i] = Double.toString(cell.getNumericCellValue());
				}
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
				cells[i] = Boolean.toString(cell.getBooleanCellValue());
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
				cells[i] = null;
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_ERROR) {
				cells[i] = null;
				System.out.println("Error found when reading a cell!");
			}
		}

		return cells;
	}

	/**
	 * Produce report on import stats
	 * 
	 * @return string
	 */
	public String getReport() {
		final StringBuilder sb = new StringBuilder();
		final Map<String, Object> invalid = parser.getInvalidMap();
		sb.append(globalCounter + " rows were loaded.");

		int limit = 10;
		if (invalid.size() > 0) {
			sb
					.append("\n\nThe following enties are invalid and were not imported:\n");

			for (String key : invalid.keySet()) {
				sb.append(key + " = " + invalid.get(key) + "\n");
				if (limit-- <= 0) {
					sb.append("Approximately " + (invalid.size() - 10)
							+ " additional entries were not imported...");
					break;
				}

			}
		}

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.genmapp.genmappimport.reader.TextTableReader#getNodeList()
	 */
	public int[] getNodeIndexList() {
		return parser.getNodeIndexList();
	}

	public void firstRead() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
}
