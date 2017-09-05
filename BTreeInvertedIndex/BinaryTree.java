
import java.util.*;

/**
 * 
 * @author 
 * a node in a binary search tree
 */
class BTNode{
	BTNode left, right;
	String term;
	ArrayList<Integer> docLists;
	
	/**
	 * Create a tree node using a term and a document list
	 * @param term the term in the node
	 * @param docList the ids of the documents that contain the term
	 */
	public BTNode(String term, ArrayList<Integer> docList)
	{
		this.term = term;
		this.docLists = docList;
	}
	
}

/**
 * 
 * @author qyuvks
 * Binary search tree structure to store the term dictionary
 */
public class BinaryTree {

	/**
	 * insert a node to a subtree 
	 * @param node root node of a subtree
	 * @param iNode the node to be inserted into the subtree
	 */
	public void add(BTNode node, BTNode iNode)
	{
		if(node.term.compareTo(iNode.term) > 0) {
			if(node.left != null) {
				add(node.left, iNode);
			}
			else {
				node.left = new BTNode(iNode.term, iNode.docLists);
			}
		}
		else {
			if(node.right != null) {
				add(node.right, iNode);
			}
			else {
				node.right = new BTNode(iNode.term, iNode.docLists);
			}
		}
	}
	
	/**
	 * Search a term in a subtree
	 * @param n root node of a subtree
	 * @param key a query term
	 * @return tree nodes with term that match the query term or null if no match
	 */
	public BTNode search(BTNode n, String key)
	{
		if(n==null) 
			return null;
		if (n.term.equals(key))
			return n;
		else if (n.term.compareTo(key) > 0) 
			return search(n.left,key);
		else 
			return search(n.right,key);
	}
	
	/**
	 * Do a wildcard search in a subtree
	 * @param n the root node of a subtree
	 * @param key a wild card term, e.g., ho (terms like home will be returned)
	 * @return tree nodes that match the wild card
	 */
	@SuppressWarnings("unused")
	public ArrayList<BTNode> wildCardSearch(BTNode n, String key, ArrayList<BTNode> wild_nodes)
	{
		if(n!=null){
			wildCardSearch(n.left, key, wild_nodes);
			//if(n.left !=null)
			//System.out.println("Wild left::"+n.left.term);
			if(n.term.startsWith(key, 0)) {
				//System.out.println("Wild::"+n.term);
				if(!wild_nodes.contains(n))
				wild_nodes.add(n);
			}
			wildCardSearch(n.right, key, wild_nodes);
			//if(n.right !=null)
			//System.out.println("Wild right::"+n.right.term);
		}
		return wild_nodes;
	}
	
	/**
	 * Print the inverted index based on the increasing order of the terms in a subtree
	 * @param node the root node of the subtree
	 */
	public void printInOrder(BTNode node)
	{
		if(node!=null){
			printInOrder(node.left);
			System.out.println(node.term);
			printInOrder(node.right);
		}
	}
}

