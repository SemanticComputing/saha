package fi.seco.saha3.index.category;

import java.util.SortedSet;
import java.util.TreeSet;

public class UICategoryNode implements Comparable<UICategoryNode> {
	private String propertyUri;
	
	private String uri;
	private String label;
	
	private int itemCount;
	private int recItemCount = -1;
	
	private String selectQuery;
	private String backQuery;
	
	private SortedSet<UICategoryNode> children = new TreeSet<UICategoryNode>();
	
	public UICategoryNode(String propertyUri, String uri, String label, int itemCount, 
			String selectQuery, String backQuery) 
	{
		this.propertyUri = propertyUri;
		this.uri = uri;
		this.label = label;
		this.itemCount = itemCount;
		this.selectQuery = selectQuery;
		this.backQuery = backQuery;
	}
	
	public String getPropertyUri() {
		return propertyUri;
	}
	
	public String getUri() {
		return uri;
	}

	public String getLabel() {
		return label;
	}

	public int getItemCount() {
		return itemCount;
	}
	
	public int getRecursiveItemCount() {
		if (recItemCount == -1) {
			recItemCount = getItemCount();
			for (UICategoryNode child : children)
				recItemCount += child.getRecursiveItemCount();
		}
		return recItemCount;
	}
	
	public String getSelectQuery() {
		return selectQuery;
	}
	
	public String getBackQuery() {
		return backQuery;
	}
	
	public void setChildren(SortedSet<UICategoryNode> children) {
		this.children = children;
	}
	
	public SortedSet<UICategoryNode> getChildren() {
		return children;
	}
	
	@Override
	public String toString() {
		return label + " ["+ itemCount + "]";
	}

	public int compareTo(UICategoryNode o) {
		return getLabel().toLowerCase().compareTo(o.getLabel().toLowerCase());
	}
}
