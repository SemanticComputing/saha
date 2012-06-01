package test;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fi.seco.semweb.util.iterator.AIterableIterator;

public class IterableNodeList extends AIterableIterator<Node> {

	private NodeList list;
	private int index;
	
	public IterableNodeList(NodeList list) {
		this.list = list;
		this.index = 0;
	}
	
	@Override
	public boolean hasNext() {
		return index < list.getLength();
	}

	@Override
	public Node next() {
		return list.item(index++);
	}

}
