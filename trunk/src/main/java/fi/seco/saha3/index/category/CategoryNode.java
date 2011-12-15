package fi.seco.saha3.index.category;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * A single internal node for a category (facet) tree in HAKO.
 *
 */
public class CategoryNode {
	
	private String uri;
	private Set<CategoryNode> children = new HashSet<CategoryNode>();
	
	public CategoryNode(String uri) {
		this.uri = uri;
	}
	
	public String getUri() {
		return uri;
	}

	public void setChildren(Set<CategoryNode> children) {
		this.children = children;
	}
	
	public CategoryNode[] getChildren() {
		return children.toArray(new CategoryNode[children.size()]);
	}

	public void addChild(CategoryNode child) {
		children.add(child);
	}
	
	public Set<CategoryNode> getAllChildren() {
		Set<CategoryNode> allChildren = new HashSet<CategoryNode>();
		
		Queue<CategoryNode> queue = new LinkedList<CategoryNode>();
		queue.addAll(children);
		
		while (!queue.isEmpty()) {
			CategoryNode child = queue.remove();
			allChildren.add(child);
			for (CategoryNode c : child.getChildren())
				if (!allChildren.contains(c))
					queue.add(c);	
		}
		
		return allChildren;
	}
	
	@Override
	public int hashCode() {
		return getUri().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !o.getClass().equals(getClass()))
			return false;
		return ((CategoryNode)o).getUri().equals(getUri());
	}

}
