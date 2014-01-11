package com.rosaloves.bitlyj;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * $Id: ParameterMap.java 123 2010-07-20 12:01:48Z chris@rosaloves.com $
 * 
 * @author clewis Jul 18, 2010
 * 
 */
class ParameterMap extends AbstractCollection<Entry<String, List<String>>> {

	private Map<String, List<String>> parameters = new HashMap<String, List<String>>();

	public void add(String name, String value) {
		List<String> values = parameters.get(name);
		if (values == null)
			values = new ArrayList<String>();
		values.add(value);
		parameters.put(name, values);
	}

	public List<String> get(String name) {
		return parameters.get(name);
	}

	@Override
	public Iterator<Entry<String, List<String>>> iterator() {
		return parameters.entrySet().iterator();
	}

	@Override
	public int size() {
		return parameters.size();
	}

	@Override
	public String toString() {
		return "ParameterMap [parameters=" + parameters + "]";
	}

}
