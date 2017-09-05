import java.util.*;
import java.io.*;
class InvertedIndex {
	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<ArrayList<Integer>> docLists;
	
	public void CreateInvertedIndex(ArrayList<String> termList2, 
			ArrayList<ArrayList<Integer>> docLists2, ArrayList<Integer>docList, 
			ArrayList<String> tokens, int j)
	{
		/* This method parses the data files and stop words file depending
		   * on the boolean flag "isStop". If true it parses the stop word file
		   * else it parses the data file.
		   * @param termList2: Holds the terms of the documents.
		   * @param docLists2: it holds the indexes of docList argument.
		   * @param docLists : it holds the array of docList IDs.
		   * @return         : None.
		   */
		
		for(String token: tokens) 
		{
			// Check if the token is a new term.
			if(!termList2.contains(token))
			{
				termList2.add(token);
				docList = new ArrayList<Integer>();
				docList.add(new Integer(j));
				docLists2.add(docList);
			}
			//Check if token is an existing term
			else 
			{
				int index = termList2.indexOf(token);
				docList = docLists2.get(index);
				if(!docList.contains(new Integer(j)))
				{
					docList.add(new Integer(j));
					docLists2.set(index, docList);
				}
			}
		}
	}
}