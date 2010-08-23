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
package org.genmapp.genmappimport.ui;

import java.io.File;

import org.genmapp.genmappimport.reader.TextTableReader;

import cytoscape.Cytoscape;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.util.CyNetworkNaming;

/**
 *
 */
public class ImportAttributeTableTask implements Task {
	private TextTableReader reader;
	private String source;
	private TaskMonitor taskMonitor;

	/**
	 * Constructor.
	 * 
	 * @param file
	 *            File.
	 * @param fileType
	 *            FileType, e.g. Cytoscape.FILE_SIF or Cytoscape.FILE_GML.
	 */
	public ImportAttributeTableTask(TextTableReader reader, String source) {
		this.reader = reader;
		this.source = source;
	}

	/**
	 * Executes Task.
	 */
	public void run() {
		taskMonitor.setStatus("Loading data...");
		taskMonitor.setPercentCompleted(-1);

		try {
			reader.readTable();
			taskMonitor.setPercentCompleted(100);
			Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null,
					null);
		} catch (Exception e) {
			e.printStackTrace();
			taskMonitor.setException(e, "Unable to import data.");
		}
		// Create network from all loaded nodes and edges
		File tempFile = new File(source);
		String t = tempFile.getName();
		String title = CyNetworkNaming.getSuggestedNetworkTitle(t);
		Cytoscape.createNetwork(reader.getNodeIndexList(), Cytoscape.getRootGraph().getEdgeIndicesArray(), title);
		Cytoscape.firePropertyChange(Cytoscape.NETWORK_LOADED, null, title);

		informUserOfAnnotationStats();
	}

	/**
	 * Inform User of Network Stats.
	 */
	private void informUserOfAnnotationStats() {
		StringBuffer sb = new StringBuffer();

		// Give the user some confirmation
		sb.append("Succesfully loaded data from:\n\n");
		sb.append(source + "\n\n");

		sb.append(reader.getReport());

		taskMonitor.setStatus(sb.toString());
	}

	/**
	 * Halts the Task: Not Currently Implemented.
	 */
	public void halt() {
		// Task can not currently be halted.
	}

	/**
	 * Sets the Task Monitor.
	 * 
	 * @param taskMonitor
	 *            TaskMonitor Object.
	 */
	public void setTaskMonitor(TaskMonitor taskMonitor)
			throws IllegalThreadStateException {
		this.taskMonitor = taskMonitor;
	}

	/**
	 * Gets the Task Title.
	 * 
	 * @return Task Title.
	 */
	public String getTitle() {
		return new String("Loading Data");
	}
}
