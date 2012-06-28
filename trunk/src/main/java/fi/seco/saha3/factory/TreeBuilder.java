package fi.seco.saha3.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.seco.saha3.index.ResourceIndexSearcher;
import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;
import fi.seco.semweb.util.rdf.OntologyUtil;

public class TreeBuilder {

	public class UITreeNode extends TreeNode implements Comparable<UITreeNode> {
		private Locale locale;
		private Set<UITreeNode> children = new TreeSet<UITreeNode>();
		
		public UITreeNode(TreeNode node, Locale locale) {
			super(node.getUri());
			this.locale = locale;
		}
		
		public String getLabel() {
			return getLabel(locale);
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
			if (o != null && o.getClass().equals(getClass())) 
				return ((UITreeNode)o).getUri().equals(getUri());
			return false;
		}

		public int compareTo(UITreeNode o) {
			return String.CASE_INSENSITIVE_ORDER.compare(getLabel(),o.getLabel());
		}
	}
	
	public class TreeNode {
		private String uri;
		private Set<TreeNode> children = new HashSet<TreeNode>();
		
		private TreeNode(String uri) {
			if (uri == null) throw new IllegalArgumentException("uri can't be null.");
			this.uri = uri;
		}
		
		public void addChild(TreeNode child) {
			children.add(child);
		}
		
		public String getUri() {
			return uri;
		}
		
		public String getLabel(Locale locale) {
			return searcher.getLabel(getUri(),locale);
		}
		
		public int getInstanceCount() {
			return searcher.getInstanceCount(getUri());
		}
		
		public Set<? extends TreeNode> getChildren() {
			return children;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o != null && o.getClass().equals(getClass())) 
				return ((TreeNode)o).getUri().equals(getUri());
			return false;
		}
		
		@Override
		public int hashCode() {
			return getUri().hashCode();
		}
	}

	private Model model;
	private ResourceIndexSearcher searcher;
	
	private Set<TreeNode> rootClasses;
	private Map<String,TreeNode> nodeMap;
	
	@Autowired
	public void setModel(Model model) {
		this.model = model;
	}
	
	@Autowired
	public void setSearcher(ResourceIndexSearcher searcher) {
		this.searcher = searcher;
	}
	
	public Set<UITreeNode> getLocalizedRootNodes(Locale locale) {
		Set<UITreeNode> rootClasses = new TreeSet<UITreeNode>();
		for (TreeNode node : getRootClasses())
			rootClasses.add(transformNode(node,locale));
		return rootClasses;
	}
	
	public Set<UITreeNode> getLocalizedTreeNodes(Collection<String> uris, Locale locale) {
		if (nodeMap == null) buildTree();
		Set<UITreeNode> treeNodes = new TreeSet<UITreeNode>();
		for (String uri : uris)
			if (nodeMap.containsKey(uri))
				treeNodes.add(transformNode(nodeMap.get(uri),locale));
		return treeNodes;
	}
	
	private UITreeNode transformNode(TreeNode node, Locale locale) {
		UITreeNode uiNode = new UITreeNode(node,locale);
		for (TreeNode child : node.getChildren())
			uiNode.addChild(transformNode(child,locale));
		return uiNode;
	}
	
	private Set<TreeNode> getRootClasses() {
		if (rootClasses == null) buildTree();
		return rootClasses;
	}
	
	private void buildTree() {
		rootClasses = new HashSet<TreeNode>();
		nodeMap = new HashMap<String,TreeNode>();
		
		for (Resource classType : OntologyUtil.getTransitiveClosure(model,OWL.Class,RDF.type))
			for (Statement s : new IteratorToIIterableIterator<Statement>(model.listStatements(null,RDF.type,classType)))
				if (s.getSubject().isURIResource())
					nodeMap.put(s.getSubject().getURI(),new TreeNode(s.getSubject().getURI()));
		
		rootClasses.addAll(nodeMap.values());
		
		for (TreeNode node : nodeMap.values())
			for (String object : searcher.getAncestors(node.getUri()))
				if (nodeMap.containsKey(object)) {
					nodeMap.get(object).addChild(node);
					rootClasses.remove(node);
				}
	}
	
	public void clear() {
		rootClasses = null;
		nodeMap = null;
	}
	
}
