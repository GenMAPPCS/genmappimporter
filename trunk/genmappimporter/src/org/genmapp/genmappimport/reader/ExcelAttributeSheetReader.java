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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.genmapp.genmappimport.commands.DatasetCommandHandler;

import cytoscape.data.CyAttributes;

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
	private final Sheet sheet;
	private final AttributeMappingParameters amp;
	private final AttributeLineParser parser;

	private final int startLineNumber;
	private int globalCounter = 0;

	/**
	 * Constructor.<br>
	 * 
	 * Takes one Excel sheet as parameter.
	 * 
	 * @param sheet2
	 * @param amp
	 */
	public ExcelAttributeSheetReader(final Sheet sheet2,
			final AttributeMappingParameters amp, final int startLineNumber) {
		this.sheet = sheet2;
		this.amp = amp;
		this.startLineNumber = startLineNumber;
		this.parser = new AttributeLineParser(amp);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public List getColumnNames() {
		List<String> colNamesList = new ArrayList<String>();

		for (String name : amp.getAttributeNames()) {
			colNamesList.add(name);
		}

		return colNamesList;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public void readTable() throws IOException {

		Row row;
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
		
		// Ship to Workspaces for CyDataset creation and mapping
		List<String> attrs = new ArrayList<String>();
		for (String a : amp.getAttributeNames())
			attrs.add(a);
		DatasetCommandHandler.updateWorkspaces2(amp.getTitle(), amp.getKeyType(), parser.getNodeIndexList(), attrs);
	}

	/**
	 * For a given Excel row, convert the cells into String.
	 * 
	 * @param row
	 * @return
	 */
	private String[] createElementStringArray(Row row) {
		String[] cells = new String[amp.getColumnCount()];
		Cell cell = null;

		for (int i = 0; i < amp.getColumnCount(); i++) {
			cell = row.getCell(i);

			if (cell == null) {
				cells[i] = null;
			} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				cells[i] = cell.getRichStringCellValue().getString();
			} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				if (amp.getAttributeTypes()[i] == CyAttributes.TYPE_INTEGER) {
					Double dblValue = cell.getNumericCellValue();
					Integer intValue = dblValue.intValue();
					cells[i] = intValue.toString();
				} else {
					cells[i] = Double.toString(cell.getNumericCellValue());
				}
			} else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
				cells[i] = Boolean.toString(cell.getBooleanCellValue());
			} else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
				cells[i] = null;
			} else if (cell.getCellType() == Cell.CELL_TYPE_ERROR) {
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

		if (invalid.size() > 0) {
			sb
					.append("\n\nThe following enties are invalid and were not imported:\n");
			int limit = 10;
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

}
