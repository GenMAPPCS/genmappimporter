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
package org.genmapp.genmappimport.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cytoscape.command.CyCommandException;
import cytoscape.command.CyCommandHandler;
import cytoscape.command.CyCommandResult;
import cytoscape.layout.Tunable;

/**
 * CyCommandHandler registration/execution
 * 
 * @author apico
 * 
 */
public class GenMAPPImportCyCommandHandler implements CyCommandHandler {

	public final static String NAMESPACE = "genmapp importer";
	public final static String CREATE_NETWORK = "create network";
	public static boolean CREATE_NETWORK_TOGGLE = false;

	private Map<String, List<Tunable>> settings = new HashMap<String, List<Tunable>>();

	public GenMAPPImportCyCommandHandler() {
		Tunable t = new Tunable("toggle",
				"Whether to create a network from imported table data",
				Tunable.STRING, "false");
		List<Tunable> list = new ArrayList<Tunable>();
		list.add(t);
		settings.put(CREATE_NETWORK, list);
	}

	public CyCommandResult execute(String string, Collection<Tunable> clctn)
			throws CyCommandException {
		Map<String, Object> kvSettings = new HashMap<String, Object>();
		for (Tunable t : clctn) {
			Object v = t.getValue();
			if (v != null)
				kvSettings.put(t.getName(), v.toString());
			else
				kvSettings.put(t.getName(), null);
		}
		return execute(string, kvSettings);
	}

	public CyCommandResult execute(String command, Map<String, Object> map)
			throws CyCommandException {
		CyCommandResult result = new CyCommandResult();
		if (CREATE_NETWORK.equals(command)) {
			boolean val = Boolean.parseBoolean((String) map.get("toggle"));
			CREATE_NETWORK_TOGGLE = val;
			result.addMessage("toggle set to " + val);
		} else {
			result.addError("Command not supported: " + command);
		}

		return result;
	}

    public List<String> getCommands() {
        List<String> val = new ArrayList<String>();
        val.add(CREATE_NETWORK);
        return val;
    }

    public List<String> getArguments(String command) {
        List<Tunable> list = settings.get(command);
        if (list == null) return null;
        List<String> args = new ArrayList<String>();
        for (Tunable t : list) {
            args.add(t.getName());
        }
        return args;
    }

    public Map<String, Object> getSettings(String command) {
        List<Tunable> list = settings.get(command);
        Map<String, Object> val = new HashMap<String, Object>();
        for (Tunable t: list) {
            val.put(t.getName(), t.getValue());
        }
        return val;
    }

	public String getDescription(String arg0) {
		return "Set toggle to create network and view from imported nodes";
	}


    public Map<String, Tunable> getTunables(String command) {
        Map<String, Tunable> map = new HashMap<String, Tunable>();
        List<Tunable> list = settings.get(command);
        for (Tunable t : list) {
            map.put(t.getName(), t);
        }
        return map;
    }

}
