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

import giny.model.Node;

import java.io.IOException;
import java.util.List;


/**
 * Interface of all text table readers.<br>
 *
 */
public interface TextTableReader {

	/**
	 * @throws IOException
	 */
	public void readTable() throws IOException;

	/**
	 * @return list of column names
	 */
	public List getColumnNames();

	/**
	 * Report the result of import as a string.
	 * @return string
	 */
	public String getReport();
	
	/**
	 * Make list of nodes from parser available
	 * @return
	 */
	public int[] getNodeIndexList();
}
