package fi.seco.saha3.index.category;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

public class UICategories {

	private Map<String,SortedSet<UICategoryNode>> rootNodes = new HashMap<String,SortedSet<UICategoryNode>>();
	private Map<String,UICategoryNode> allNodes = new HashMap<String,UICategoryNode>();
	
	public UICategories() {}
	
	public UICategories(Map<String,SortedSet<UICategoryNode>> rootNodes, Map<String,UICategoryNode> allNodes) {
		this.rootNodes = rootNodes;
		this.allNodes = allNodes;
	}
	
	public Map<String,UICategoryNode> getAllNodes() {
		return allNodes;
	}
	
	public Map<String,SortedSet<UICategoryNode>> getRootNodes() {
		return rootNodes;
	}
	
}
