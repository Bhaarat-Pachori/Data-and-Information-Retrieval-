/**
* The Parser.java program parses the individual documents
* and creates an Inverted Index for the terms extracted.
*
* @author  Bhaarat Pachori
* @version 1.0
*  
*/
import java.util.*;
import java.util.Map.Entry;
import java.io.*;

public class Parser {
  /**
   * This is the main driving class .
   */
	static String[] myDocs;
	static ArrayList<String> termList = new ArrayList<String>();
	static ArrayList<ArrayList<Integer>> docLists = new ArrayList<ArrayList<Integer>>();
	ArrayList<Integer> docList = new ArrayList<Integer>();
	static HashSet<String> stopList = new HashSet<String>();
	
   /**
   * This constructor extract the data files from the given 
   * directory and also parses the stop words by comparing the
   * words from the stopwords.txt files.
   * @param folderName: Directory name which has the data files.
   * @param stopWords:  Directory containing the stop words list.
   */
	// This is constructor of the class.
	public Parser(String folderName, String stopWords)
	{
	  
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();
		myDocs = new String[listOfFiles.length];
		Stemmer st = new Stemmer();
		for(int i=0;i<listOfFiles.length;i++)
		{
			myDocs[i] = listOfFiles[i].getName();
		}
		// Creating the absolute path for stopwords file.
		parse(stopWords + "/" + "stopwords.txt", true);
		
		// Creating an object of InvertedIndex class.
		InvertedIndex iIdx = new InvertedIndex();
		
		// Extracting the data files from the given directory.
		for(int j=0;j<listOfFiles.length;j++) {
			ArrayList<String> stemmed = new ArrayList<String>();
			String[] tokens = parse(folderName + "/" + myDocs[j], false);
			
			for(int i=0;i<tokens.length;i++) {
				// Check to see if the token is a stop word or not.
				// If the token is a stop word then we will not add
				// this token too our term dictionary.
				if(!stopList.contains(tokens[i]))
				{
					// Stemming is done here to reduce the words to their roots.
					st.add(tokens[i].toCharArray(),tokens[i].length());
					st.stem();
					stemmed.add(st.toString());
				}
				else
				{
					continue;
				}
			}
			// Calling the supporting method from InvertedIndex.java
			iIdx.CreateInvertedIndex(termList, docLists, docList, stemmed, j);
		}
		//System.out.println("Done!!");
	}


	public String[] parse(String fileName, boolean isStop)
	{
	   /**
	   * This method parses the data files and stop words file depending
	   * on the boolean flag "isStop". If true it parses the stop word file
	   * else it parses the data file.
	   * @param fileName: Name of the data file to be parsed.
	   * @param isStop  : Flag to indicate that file is a stop word file
	   *                  not a data file.
	   * @return        : It returns the array of tokens parsed from the 
	   *                  data file.
	   */
		String[] tokens = null;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String allLines = new String();
			String line = null;
			while((line=reader.readLine())!=null){
				if(!isStop) {
				allLines += line.toLowerCase(); //case folding
				}
				else {
					stopList.add(line);
				}
			}
			// RegEx to parse the tokens separated by the various delimeters.
			tokens = allLines.split("[ .,?!:;$%()\\^\\-\"'#/*+&]+");
			Iterator<String> itr=stopList.iterator();  
			  while(itr.hasNext() && isStop){  
			   itr.next();  
			  }
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return tokens;
	}
	

	public String toString()
	{
	   /**
	   * This toString method overrides the default toString() to 
	   * print the output as desired. 
	   * @param  None
	   * @return None
	   */
		String matrixString = new String();
		ArrayList<Integer> docList;
		for(int i=0;i<termList.size();i++){
			matrixString += String.format("%-15s", termList.get(i));
			docList = docLists.get(i);
			for(int j=0;j<docList.size();j++)
				matrixString += docList.get(j) + "\t";
			matrixString += "\n";
		}
		return matrixString;
	}


	public static ArrayList<Integer> search(String query, ArrayList<String> termList2 ){
	  /**
	   * This method do the basic search depending upon the query word.
	   * Once a key word is found we find the index of that word and 
	   * returns the values (array of document IDs) stored in "docLists".
	   * @param query: It is the query word given by user to find the 
	   *               related documents containing the query word.
	   * @param termList2: This is the list of terms we have parsed earlier
	   *                   and kept in our dictionary.
	   * @return Integer ArrayList of the document IDs.
	   */
		int index = termList2.indexOf(query);
		if(index <0)
			return null;
		return docLists.get(index);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static ArrayList<Integer> search_optimize(String[] query, ArrayList<String> termList2,
			String[] opt_queries) 
	{
	  /**
	   * This method do the search of the query terms and creates a 
	   * HashMap of the query terms and then sorts the HashMap in ascending
	   * order of the size of the docList. This way this method make sure
	   * that the queries are processed in optimized way i.e. a query with
	   * least docList size is processed then query with greater size and
	   * so on. This HashMap uses the query as the "key" and its length as
	   * "val" as any other conventional HashMaps.
	   * @param query    : It is the array of query words given by user to 
	   *                   find the related documents containing the query 
	   *                   word.
	   * @param termList2: This is the list of terms we have parsed earlier
	   *                   and kept in our dictionary.
	   * @return         : Integer ArrayList of the document IDs.
	   */
		ArrayList<String> queries = new ArrayList<String>();
		HashMap<String, Integer> hm = new HashMap();
		int postingsizes[] = new int[query.length];
		ArrayList<Integer> result1 = null;
		boolean foundNull = false;
		for(int i=0;i<query.length;i++) {
			// Adding all the terms of the query to the Hashmap.
			ArrayList<Integer> result = search(query[i],termList2);
			if(result != null) {
			hm.put(query[i], result.size());
			postingsizes[i] = result.size();
			foundNull = false;
			}
			else {
				foundNull = true;
			}
		}
		if(!foundNull) {
		System.out.println("--------------------------------------");
		
		// Converting the hashmap to array so that we can sort the queries 
		// based on their size of docList, which optimize the merging process.
		Object[] a = hm.entrySet().toArray();
		Arrays.sort(a, new Comparator() {
		    public int compare(Object obj1, Object obj2) {
		        return ((Map.Entry<String, Integer>) obj1).getValue()
		                   .compareTo(((Map.Entry<String, Integer>) obj2).getValue());
		    }
		});
		// Adding the sorted HashMap entries to an ArrayList of Strings for
		// later processing.
		for (Object e : a) {
			queries.add(((Entry<String, Integer>) e).getKey());
		}
		// Code to print the order in which queries are processed.
		System.out.println("Processing queries in following order.");
		System.out.println("--------------------------------------");
		for(int j=0;j<queries.size();j++) {
			for(int k=0;k<opt_queries.length;k++) {
				if(opt_queries[k].contains(queries.get(j))) {
					System.out.printf("%d. %s \n",j+1,opt_queries[k]);
				}
			}
		}
		// Sending the query terms in the optimized order we have created
		// above based on the docLists size.
		ArrayList<Integer> result = search(queries.get(0),termList2);
		int termId = 1;
		while(termId<queries.size()){
			result1 = search(queries.get(termId),termList2);
			if(result1 == null) {
				return null;
			}
			else {
				result = merge(result, result1, "optimize");
				termId++;
				}
		}
			System.out.println("--------------------------------------");
			return result;
		}
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ArrayList<Integer> search(String[] query, ArrayList<String> termList2)
	{
	   /**
	   * This method do the basic search depending upon the query words.
	   * Once a key word is found, we find the index of that word and 
	   * returns the values (array of document IDs) stored in "docLists".
	   * @param query    : It is the query word given by user to find the 
	   *                   related documents containing the query word.
	   * @param termList2: This is the list of terms we have parsed earlier
	   *                   and kept in our dictionary.
	   * @return         :Integer ArrayList of the document IDs.
	   */
		ArrayList<Integer> result = search(query[0],termList2);
		int termId = 1;
		while(termId<query.length){
			ArrayList<Integer> result1 = null;
			//System.out.println(query[termId]);
			// Code to handle query case where user wants the documents
			// if any of the query term is found or all the terms are found.
			/* This code handles Task2 Query processing part three. */
			if(query[1].equals("or")) {
				termId++;
				result1 = search(query[termId],termList2);
				if(result1 == null && result == null) {
					return null;
				}
				termId++;
				result = merge(result, result1, "or");
			}
			// This code handles other cases except the "OR" query case.
			if(!query[1].equals("or")) {
				result1 = search(query[termId],termList2);
				if(result1 == null) {
					return null;
				}
				else {
					result = merge(result, result1, "usual");
					termId++;
					}
			}
		}
		return result;
	}
	
	private static ArrayList<Integer> merge(ArrayList<Integer> l1, ArrayList<Integer> l2, String isOred)
	{
	   /**
	   * This method merge only those documents which contains the complete user
	   * query (Intersection of doc IDs), with an exception i.e. if the boolean 
	   * flag "isOred" is true returns the Union of document IDs.
	   * @param l1       : List of all the doc IDs containing the first query 
	   *                   term.
	   * @param termList2: List of all the doc IDs containing the second query 
	   *                   term.
	   * @param isOred   : flag indicating that user wants the Union of doc IDs.
	   * @return         :Integer ArrayList of the document IDs.
	   */
		ArrayList<Integer> mergedList = new ArrayList<Integer>();
		int id1=0, id2=0;
		
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
		if(l1 != null && l2 != null) {
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

	
	
	public static void main(String[] args)
	{
	   /**
	   * This is main driving method.
	   * @param args     : contains the Command line arguments.
	   * @return         : None
	   */
		String query = null;
		String queries[] = new String[args.length - 2];
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> searching_for = new ArrayList<String>();
		ArrayList<Integer> result = null;
		int queryWords = 0;
		boolean foundStopWord = false;
		boolean isOptimized = true;
		String whichTest = null;
		Stemmer st = new Stemmer();
		System.out.println("Total number of arguments::"+args.length);
		
		Parser p = new Parser(args[0], args[1]);
		System.out.println(p);
		
		// Code to handle one keyword in query
		if(args.length == 3)
		{
			st.add(args[2].toCharArray(),args[2].length());
			st.stem();
			if(!stopList.contains(query)) {
				query = st.toString();
			}
			else {
				System.out.print("Query only has Stop word !!");
				System.exit(0);
			}
			result = search(query, termList);
			whichTest = "Testing for single Keyword.";
		}
		// Code to handle two keywords with an OR query
		else if(args.length == 5 && args[3].equals("or"))
		{
			for(int i=2;i<args.length;i++) {
				if(!stopList.contains(st.toString()) || args[3].equals("or")) {
					st.add(args[i].toCharArray(),args[i].length());
					st.stem();
					queries[i-2] = st.toString();
					queryWords++;
				}
			}
			result = search(queries, termList);
			whichTest = "Testing OR for two Keywords.";
		}
		// Code to handle only two or more keyword with and AND in query
		else {
			for(int i=2;i<args.length;i++) {
				if(!stopList.contains(args[i])) {
					
					st.add(args[i].toCharArray(),args[i].length());
					st.stem();
					if(!args[i].equals("opt_srch")) {
						queries[i-2] = st.toString();
						queryWords++;
					}
				}
				else
				{
					foundStopWord = true;
				}
			}
			if(foundStopWord || args[args.length -1].equals("opt_srch")) {
				for (String s : queries)
				    if (s != null)
				        list.add(s);
				queries = list.toArray(new String[list.size()]);
			}
			if(args.length > 3) {
				if(args[args.length -1].equals("opt_srch")) {
					result = search_optimize(queries, termList, args);
					whichTest = "Testing [Optimized] AND for 2 or more keywords";
				}
				else {
					result = search(queries, termList);
					whichTest = "Testing AND for 2 or more keywords";
				}
			}
		}
		System.out.println(whichTest);
		for(int i=2; i<args.length; i++) {
			if(!args[i].equals("or") && !args[i].equals("opt_srch"))
			searching_for.add(args[i]);
		}
		System.out.println("Finding documents for query(ies)::"+searching_for);
		if(result == null || result.size() == 0) {
			System.out.println("No relevant relevant document for the query.");
			System.exit(0);
		}
		for(Integer i:result)
			System.out.printf("Document %d has the relevant information.\n",i+1);
	}
/**
 * For testing the code we need to give arguments from command line.
 * 
 * args[1] example: /123/456/Documents/workspace/Lab1/src/Lab1_Data 
 * Lab1_Data contains the data files 1.txt, 2.txt,...,5.txt
 * 
 * args[2] example: /123/456/Documents/workspace/Lab1/src
 * src directory contains the stopwords.txt
 * 
 * Part 1:
 * Test Case 1:
 * Input: plot
 * 
 * Output: 
 * Testing for single Keyword.
 * Finding documents for query(ies)::[plot]
 * Document 1 has the relevant information.
 * Document 3 has the relevant information.
 * Document 5 has the relevant information.
 * 
 * Test Case 2:
 * Input: sound
 * Output:
 * Testing for single Keyword.
 * Finding documents for query(ies)::[sound]
 * Document 3 has the relevant information.
 * Document 4 has the relevant information.
 * Document 5 has the relevant information.
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  
 * Part 2:
 * Test Case 1:
 * Input : drink accident
 * Output:
 * Testing AND for 2 or more keywords
 * Finding documents for query(ies)::[drink, accident]
 * Document 1 has the relevant information.
 * 
 * Test Case 2:
 * Input: video actors
 * Output:
 * Testing AND for 2 or more keywords
 * Finding documents for query(ies)::[video, actors]
 * Document 1 has the relevant information.
 * Document 5 has the relevant information.
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 * 
 * Part 3:
 * Test Case 1:
 * Input: big or recycle
 * Output: 
 * Testing OR for two Keywords.
 * Finding documents for query(ies)::[big, recycle] 
 * Document 1 has the relevant information.
 * Document 2 has the relevant information.
 * Document 4 has the relevant information.
 * Document 5 has the relevant information.
 * Document 3 has the relevant information.
 * 
 * Test Case 2:
 * Input: star or stake
 * Output:
 * Testing OR for two Keywords.
 * Finding documents for query(ies)::[star, stake]
 * Document 2 has the relevant information.
 * Document 5 has the relevant information.
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *   
 * Part 4;
 * Test Case 1:
 * Input: reject plot accident opt_srch
 * Output:
 * --------------------------------------
 * Processing queries in following order.
 * --------------------------------------
 * 1. stake 
 * 2. mood 
 * 3. reject 
 * 4. thriller 
 * 5. accident 
 * 6. plot 
 * --------------------------------------
 * Testing [Optimized] AND for 2 or more keywords
 * Finding documents for query(ies)::[reject, plot, accident, stake, mood, thriller]
 * Document 5 has the relevant information.
 * 
 * Test Case 2:
 * Input: neighborhood video watch opt_srch opt_srch
 * Output: 
 * --------------------------------------
 * Processing queries in following order.
 * --------------------------------------
 * 1. neighborhood 
 * 2. video 
 * 3. watch 
 * --------------------------------------
 * Testing [Optimized] AND for 2 or more keywords
 * Finding documents for query(ies)::[neighborhood, video, watch]
 * Document 1 has the relevant information.
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 * 
 * Additional Test Case: If user enters a stop word in search it is ignored
 *                       and search is performed.
 * Input: television and and
 * Output:
 * Testing AND for 2 or more keywords
 * Finding documents for query(ies)::[television, and]
 * Document 3 has the relevant information.
 * Document 4 has the relevant information.
 * Document 5 has the relevant information.
 * 
 * */
}