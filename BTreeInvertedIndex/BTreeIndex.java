
import java.lang.reflect.Array;
import java.util.*;

public class BTreeIndex {
	String[] myDocs;
	static BinaryTree termList;
	static BTNode root;
	static ArrayList<BTNode>matched_nodes = new ArrayList<BTNode>();
	
	/**
	 * Construct binary search tree to store the term dictionary 
	 * @param docs List of input strings
	 * 
	 */
	public BTreeIndex(String[] docs)
	{
		myDocs = docs;
		termList = new BinaryTree();
		String rootTerm;
		ArrayList<String> temp_termList = new ArrayList<String>();
		ArrayList<String> sort_temp_termList = new ArrayList<String>();
		ArrayList<ArrayList<Integer>> temp_docLists = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> docList;
		for(int i=0;i<myDocs.length;i++){
			String[] tokens = myDocs[i].split(" ");
			for(String token:tokens){
				if(!temp_termList.contains(token)){//a new term
					temp_termList.add(token);
					docList = new ArrayList<Integer>();
					docList.add(new Integer(i));
					temp_docLists.add(docList);
				}
				else{//an existing term
					int index = temp_termList.indexOf(token);
					docList = temp_docLists.get(index);
					if(!docList.contains(new Integer(i))){
						docList.add(new Integer(i));
						temp_docLists.set(index, docList);
					}
				}
			}
		}
		for(int j=0;j<temp_termList.size();j++) {
			String val = temp_termList.get(j);
			sort_temp_termList.add(val);
		}
		// Find the mid value of the tokens to create a 
		// Balanced Binary Tree.				
		Collections.sort(sort_temp_termList);
		int rootTermIdx = sort_temp_termList.size() / 2;
		rootTerm = sort_temp_termList.get(rootTermIdx);
		int idx = 0;
		ArrayList<Integer> temp_list = new ArrayList<Integer>();
		idx = temp_termList.indexOf(rootTerm);
		temp_list = temp_docLists.get(idx);
		root = new BTNode(rootTerm,temp_list);
		
		for(String term:temp_termList) {
			// As we have already added the root Node
			// it is not required to add again.
			if(!term.equals(rootTerm)) {
				idx = temp_termList.indexOf(term);
				temp_list = temp_docLists.get(idx);
				BTNode temp_node = new BTNode(term, temp_list);
				termList.add(root, temp_node);
			}
		}
	}
	
	/**
	 * Single keyword search
	 * @param query the query string
	 * @return doclists that contain the term
	 */
	public ArrayList<Integer> search(String query)
	{
			BTNode node = termList.search(root, query);
			if(node==null)
				return null;
			return node.docLists;
	}
	
	/**
	 * conjunctive query search
	 * @param query the set of query terms
	 * @return doclists that contain all the query terms
	 */
	public ArrayList<Integer> search(String[] query)
	{
		ArrayList<Integer> result = search(query[0]);
		//BTNode result = termList.search(root, query[0]);
		int termId = 1;
		while(termId<query.length)
		{
			ArrayList<Integer> result1 = null;
			if(query[1].equals("or")) {
				termId++;
				result1 = search(query[termId]);
				if(result1 == null && result == null) {
					return null;
				}
				termId++;
				result = merge(result, result1, "or");
			}
			if(!query[1].equals("or")) {
				result1 = search(query[termId]);
				result = merge(result,result1,"notOr");
				termId++;
			}
		}		
		return result;
	}
	
	/**
	 * 
	 * @param wildcard the wildcard query, e.g., ho (so that home can be located)
	 * @return a list of ids of documents that contain terms matching the wild card
	 */
	public ArrayList<Integer> wildCardSearch(String wildcard)
	{
		@SuppressWarnings("unused")
		ArrayList<Integer> node_list = new ArrayList<Integer>();
		ArrayList<BTNode> wild_nodes = new ArrayList<BTNode>();
		
		// This check ensures the running time of Wild card query 
		// around O(log n) where n is the number of nodes in the BT.
		if(wildcard.compareTo(root.term) > 0) {
			matched_nodes = termList.wildCardSearch(root.right, wildcard, wild_nodes);
		}
		else if(wildcard.compareTo(root.term) < 0 && wildcard.length() > 1){
			matched_nodes = termList.wildCardSearch(root.left, wildcard, wild_nodes);
		}
		// This is the case when user enters a single character wild card for example
		// j*, so the above checks are not sufficient to search because in above cases
		// if it goes to left subtree then there is no guarantee the tree has all the
		// values smaller then root and starts with 'j' so we will not find the nodes.
		else {
			matched_nodes = termList.wildCardSearch(root, wildcard, wild_nodes);
		}
		
		// Process BTNodes array list and find the documents number from it.
		if(matched_nodes.size() > 0) {
			for(int i=0;i<matched_nodes.size();i++) {
				if(matched_nodes.get(i).docLists.size() > 0) {
					int len = matched_nodes.get(i).docLists.size();
					for(int j=0; j<len;j++) {
						if(!node_list.contains(matched_nodes.get(i).docLists.get(j))) {
						node_list.add(matched_nodes.get(i).docLists.get(j));
						}
					}
				}
			}
		}
		// No relevant documents found so setting the ArrayList to null
		// for the caller to use.
		if(matched_nodes.size() == 0) {
			node_list = null;
		}
		return node_list;
	}
	
	
	private ArrayList<Integer> merge(ArrayList<Integer> l1, ArrayList<Integer> l2, String isOred)
	{
		ArrayList<Integer> mergedList = new ArrayList<Integer>();
		int id1 = 0, id2=0;
		
		// Code to handle OR queries of user.
		if(isOred.equals("or")) {
			if(l1 != null)
			mergedList.addAll(l1);
			if(l2 != null)
			mergedList.addAll(l2);
			
			//Code to remove the duplicates from the mergedList
			LinkedHashSet<Integer> listToSet = new LinkedHashSet<Integer>(mergedList);
	        //Creating ArrayList without duplicate values
	        mergedList = new ArrayList<Integer>(listToSet);
	        return mergedList;
		}
		if(l1 != null && l2!= null) {
		while(id1<l1.size()&&id2<l2.size()){
			if(l1.get(id1).intValue()==l2.get(id2).intValue()){
				mergedList.add(l1.get(id1));
				id1++;
				id2++;
			}
			else if(l1.get(id1)<l2.get(id2))
				id1++;
			else
				id2++;
		}
	}
		return mergedList;
	}
	
	
	/**
	 * Test cases
	 * @param args commandline input
	 */
	public static void main(String[] args)
	{
		String[] docs = {"new home sales top forecasts",
				 		 "home sales rise in july",
				 		 "increase in home sales in july",
				 		 "july new home sales rise"
						};
		String[] queries = new String[args.length];
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		for(int i=0;i<args.length;i++) {
			queries[i] = args[i];
		}
		
		BTreeIndex btIdx = new BTreeIndex(docs);
		if(args.length == 1 && !args[0].contains("*") && !args[args.length -1].equals("inorder")) {
			result = btIdx.search(args[0]);
			ArrayList<String> temp = new ArrayList<String>();
			for(String q:queries) {
				temp.add(q);
			}		
			if(result != null && result.size() > 0) {
				System.out.println("Finding document(s) for query"+temp);
				for(Integer i:result) {
					System.out.printf("\nDocument %d has relevant information \n",i+1);
				}
			}
			else {
				System.out.println("No relevant Document(s) Found for queries  "+temp);
			}
		}
		else if(args.length > 1 && !args[0].contains("*") && !args[args.length -1].equals("inorder")) {
			result = btIdx.search(queries);
			ArrayList<String> temp = new ArrayList<String>();
			for(String q:queries) {
				temp.add(q);
			}
			if(result != null && result.size() > 0) {
				System.out.println("Finding document(s) for queries "+temp+" are:");
				for(Integer i:result) {
					System.out.printf("Document %d has relevant information\n",i+1);
				}
			}
			else {
				System.out.println("No relevant Document(s) Found for queries "+temp);
			}
		}
		if(args.length > 0) {
			// Wild card queries.
			if(args[0].contains("*")) {
				char[] query=new char[args[0].length()-1];
				for(int i=0;i<args[0].length();i++) {
					if(args[0].charAt(i) != '*') {
						query[i] = args[0].charAt(i);
					}
				}
				ArrayList<Integer>list = btIdx.wildCardSearch(new String(query));
				if(list != null) {
					if(list.size() > 0) {
						System.out.println("Finding document(s) for wild card query ["+new String(query)+"*]\n");
					}
					// Print the matching node list here.
					if(matched_nodes.size() > 0) {
						for(int i=0;i<matched_nodes.size();i++) {
							if(matched_nodes.get(i).term.length() > 0) {
								System.out.println("Matching terms ["+matched_nodes.get(i).term+"]");
							}
						}
						System.out.println();
					}
					for(Integer i:list) {
						System.out.printf("Document %d has relevant information\n",i+1);
					}
				}
				else {
					System.out.println("No relevant Document(s) Found for query ["+new String(query)+"*]");
				}
			}
			if(args[args.length -1].equals("inorder")) {
				System.out.println("Traversing balanced [In-Order] Binary Tree\n");
				System.out.println("\tRoot Node:: "+root.term);
				termList.printInOrder(root);
			}
		}
		else {
			System.out.println("Enter at least one search term!!");
		}
	}
	/**
	 * For testing the code we need to give arguments from command line.
	 * 
	 * For testing conjunctive queries use below format:
	 * query1 or query2 for ORed queries
	 * query1 query2 for ANDed queries --> No need to write AND b/w queries
	 * 
	 * --------------------------------------------------------------------
	 * 					TEST CASES FOR SINGLE TERM 
	 * --------------------------------------------------------------------
	 * Part 1:
	 * Test Case 1:
	 * Input: increase
	 * 
	 * Output: 
	 * Finding document(s) for query [increase] is/are:
	 * Document 3 has relevant information 
	 * 
	 * Test Case 2:
	 * Input: testing
	 * 
	 * Output:
	 * No relevant Document(s) Found for query  [testing]
	 * 
	 * --------------------------------------------------------------------
	 * 					TEST CASES FOR CONCUNJCTIVE ANDed QUERIES 
	 * -------------------------------------------------------------------- 
	 * Part 2:
	 * Test Case 1:
	 * Input : july increase
	 * 
	 * Output:
	 * Finding document(s) for queries [july, increase] are:
	 * Document 3 has relevant information
	 * 
	 * Test Case 2:
	 * Input: july august
	 * 
	 * Output:
	 * No relevant Document(s) Found for queries [july, august]
	 * 
	 * Test Case 3:
	 * Input: home sales
	 * 
	 * Finding document(s) for queries [home, sales] are:
	 * Document 1 has relevant information
	 * Document 2 has relevant information
	 * Document 3 has relevant information
	 * Document 4 has relevant information
	 * 
	 * --------------------------------------------------------------------
	 * 					TEST CASES FOR ORed QUERIES 
	 * -------------------------------------------------------------------- 
	 * 
	 * Part 3:
	 * Test Case 1:
	 * Input: july or august
	 * 
	 * Output: 
	 * Finding document(s) for queries [july, or, august] are:
	 * Document 2 has relevant information
	 * Document 3 has relevant information
	 * Document 4 has relevant information
	 * 
	 * Test Case 2:
	 * Input: july or new
	 * 
	 * Output: 
	 * Finding document(s) for queries [july, or, new] are:
	 * Document 2 has relevant information
	 * Document 3 has relevant information
	 * Document 4 has relevant information
	 * Document 1 has relevant information
	 * 
	 * Test Case 3:
	 * Input: january or august
	 * 
	 * Output:
	 * No relevant Document(s) Found for queries [january, or, august]
	 * 
	 * --------------------------------------------------------------------
	 * 					TEST CASES FOR INORDER TRAVERSAL 
	 * --------------------------------------------------------------------    
	 * Test Case 1:
	 * Input: inorder
	 * 
	 * Output:
	 * Traversing balanced [In-Order] Binary Tree
	 * 
	 * 		Root Node:: july
	 * forecasts
	 * home
	 * in
	 * increase
	 * july
	 * new
	 * rise
	 * sales
	 * top
	 * 
	 * --------------------------------------------------------------------
	 * 					TEST CASES FOR WILD CARD QUERIES 
	 * --------------------------------------------------------------------
	 * NOTE: TRY CHANGING THE DOCUMENTS AS SHOWN BELOW
	 * 
	 * String[] docs = {"new homes sales top forecasts jargon",
	 * 					"house sales rise in july ",
	 *  				"increase in hombress sales in justice",
	 * 					"july new instant home in sales rise jambo"
	 *  				};
	 * Test Case 1:
	 * Input: in*
	 * 
	 * Output:
	 * Finding document(s) for wild card query [i*]
	 * 
	 * Matching terms [in]
	 * Matching terms [increase]
	 * Matching terms [instant]
	 * 
	 * Document 2 has relevant information
	 * Document 3 has relevant information
	 * Document 4 has relevant information
	 * 
	 * Test Case 2:
	 * Input: ho*
	 * 
	 * Output:
	 * Finding document(s) for wild card query [ho*]
	 * 
	 * Matching terms [hombress]
	 * Matching terms [home]
	 * Matching terms [house]
	 * Matching terms [homes]
	 * 
	 * Document 3 has relevant information
	 * Document 4 has relevant information
	 * Document 2 has relevant information
	 * Document 1 has relevant information
	 * 
	 * Test Case 3:
	 * Input: sal*
	 * 
	 * Output:
	 * Finding document(s) for wild card query [sal*]
	 * 
	 * Matching terms [sales]
	 * 
	 * Document 1 has relevant information
	 * Document 2 has relevant information
	 * Document 3 has relevant information
	 * Document 4 has relevant information
	 * 
	 * Test Case 4:
	 * Input: sales*
	 * 
	 * Output:
	 * Finding document(s) for wild card query [sales*]
	 * 
	 * Matching terms [sales]
	 * 
	 * Document 1 has relevant information
	 * Document 2 has relevant information
	 * Document 3 has relevant information
	 * Document 4 has relevant information
	 * 
	 * 
	 * Test Case 5:
	 * Input: j*
	 * 
	 * Output:
	 * Finding document(s) for wild card query [j*]
	 * 
	 * Matching terms [jambo] 
	 * Matching terms [jargon] 
	 * Matching terms [july] 
	 * Matching terms [justice]
	 * 
	 * Document 1 has relevant information
	 * Document 2 has relevant information
	 * Document 3 has relevant information
	 * Document 4 has relevant information
	 * 
	 * 
	 * Test Case 6:
	 * Input: *
	 * 
	 * Output:
	 * Finding document(s) for wild card query [*]
	 * 
	 * Matching terms [forecasts]
	 * Matching terms [hombress]
	 * Matching terms [home]
	 * Matching terms [homeb]
	 * Matching terms [homes]
	 * Matching terms [in]
	 * Matching terms [increase]
	 * Matching terms [instant]
	 * Matching terms [jambo] 
	 * Matching terms [jargon] 
	 * Matching terms [july] 
	 * Matching terms [justice]
	 * Matching terms [new]
	 * Matching terms [rise]
	 * Matching terms [sales]
	 * Matching terms [top]
	 * 
	 * Document 1 has relevant information
	 * Document 3 has relevant information
	 * Document 4 has relevant information
	 * Document 2 has relevant information
	 * 
	 * Test Case 7:
	 * Input: test*
	 * 
	 * Output:
	 * No relevant Document(s) Found for query [test*]
	 * 					
	 * 
	 * --------------------------------------------------------------------
	 * 					SANITY TEST CASE WITH ZERO ARGUMENT
	 * -------------------------------------------------------------------- 
	 * 
	 * Input: 
	 * 
	 * Output:
	 * Enter at least one search term!!
	 * 
	 * */
}