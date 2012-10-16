/**
 * 
 */
package fi.seco.saha3.model;

import java.util.HashSet;
import java.util.Set;

public class TreeNode {
	private final String uri;
	private final Set<TreeNode> children = new HashSet<TreeNode>();

	TreeNode(String uri) {
		if (uri == null) throw new IllegalArgumentException("uri can't be null.");
		this.uri = uri;
	}

	public void addChild(TreeNode child) {
		children.add(child);
	}

	public String getUri() {
		return uri;
	}

	public Set<? extends TreeNode> getChildren() {
		return children;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o.getClass().equals(getClass())) return ((TreeNode) o).getUri().equals(getUri());
		return false;
	}

	@Override
	public int hashCode() {
		return getUri().hashCode();
	}
}