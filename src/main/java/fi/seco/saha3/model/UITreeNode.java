/**
 * 
 */
package fi.seco.saha3.model;

import java.util.Set;
import java.util.TreeSet;

public class UITreeNode extends TreeNode implements Comparable<UITreeNode> {
	private final String label;
	private final int instanceCount;
	private final Set<UITreeNode> children = new TreeSet<UITreeNode>();

	public UITreeNode(String uri, String label, int instanceCount) {
		super(uri);
		this.instanceCount = instanceCount;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public int getInstanceCount() {
		return instanceCount;
	}

	public void addChild(UITreeNode child) {
		children.add(child);
	}

	@Override
	public Set<UITreeNode> getChildren() {
		return children;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o.getClass().equals(getClass())) return ((UITreeNode) o).getUri().equals(getUri());
		return false;
	}

	@Override
	public int compareTo(UITreeNode o) {
		return String.CASE_INSENSITIVE_ORDER.compare(getLabel(), o.getLabel());
	}
}