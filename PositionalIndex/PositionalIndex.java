
import java.util.ArrayList;


public class PositionalIndex {
	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<ArrayList<DocId>> docLists;
	ArrayList<DocId> prev_docList = new ArrayList<DocId>();
	
	/**
	 * This constructor fills all the data structures 
	 * required for the operation.
	 * Construct a positional index 
	 * @param docs List of input strings or file names
	 * 
	 */
	public PositionalIndex(String[] docs)
	{
		myDocs = docs;
		termList = new ArrayList<String>();
		docLists = new ArrayList<ArrayList<DocId>>();
		ArrayList<DocId> docList;
		for(int i=0;i<myDocs.length;i++){
			String[] tokens = myDocs[i].split(" ");
			String token;
			for(int j=0;j<tokens.length;j++){
				token = tokens[j];
				if(!termList.contains(token)){
					termList.add(token);
					docList = new ArrayList<DocId>();
					DocId doid = new DocId(i,j);
					docList.add(doid);
					docLists.add(docList);
				}
				else{ //existing term
					int index = termList.indexOf(token);
					docList = docLists.get(index);
					int k=0;
					boolean match = false;
					//search the postings for a document id, if match, insert a new position
					//number to the document id
					for(DocId doid:docList)
					{
						if(doid.docId==i)
						{
							doid.insertPosition(j);
							docList.set(k, doid);
							match = true;
							break;
						}
						k++;
					}
					//if no match, add a new document id along with the position number
					if(!match)
					{
						DocId doid = new DocId(i,j);
						docList.add(doid);
					}
				}
			}
		}
	}
	
	/**
	 * Return the string representation of a positional index
	 */
	public String toString()
	{
		String matrixString = new String();
		ArrayList<DocId> docList;
		for(int i=0;i<termList.size();i++){
				matrixString += String.format("%-15s", termList.get(i));
				docList = docLists.get(i);
				for(int j=0;j<docList.size();j++)
				{
					matrixString += docList.get(j)+ "\t";
				}
				matrixString += "\n";
			}
		return matrixString;
	}
	
	/**
	 * This method is called when there are only two terms in the phrase query.
	 * @param l1 first postings
	 * @param l2 second postings
	 * @param result contains the list of all the documents that has the query terms in it.
	 * @return merged result of two postings.
	 */
	public ArrayList<DocId> merge_two(ArrayList<DocId> l1, ArrayList<DocId> l2)
	{
		//ArrayList<Integer> mergedList = new ArrayList<Integer>();
		ArrayList<DocId> docList = new ArrayList<DocId>();
		int id1=0, id2=0;
		while(id1<l1.size()&&id2<l2.size()){
			//if both terms appear in the same document
			if(l1.get(id1).docId==l2.get(id2).docId){
				//get the position information for both terms
				ArrayList<Integer> pp1 = l1.get(id1).positionList;
				ArrayList<Integer> pp2 = l2.get(id2).positionList;
				int pid1 =0, pid2=0;
				while(pid1<pp1.size()){
					boolean match = false;
					while(pid2<pp2.size()){
						//if the two terms appear together, we find a match
						if(Math.abs(pp1.get(pid1)-pp2.get(pid2))==1){
							match = true;
							DocId doid = new DocId(l1.get(id1).docId,pp1.get(pid1));
							docList.add(doid);
							break;
						}
						else if(pp2.get(pid2)>pp1.get(pid1))
							break;
						pid2++;
					}
					if(match) //if a match if found, the search for the current document can be stopped
						break;
					pid1++;
				}
				id1++;
				id2++;
			}
			else if(l1.get(id1).docId<l2.get(id2).docId)
				id1++;
			else
				id2++;
			}
		return docList;
	}
	
	/**
	 * This method is called when there are more than 2 query term. Also this method verify
	 * previous docIds that are stored in "result" variable. For new l1 and l2 if the docs
	 * containing l1 and l2 are present in the "result" variable then only it proceeds further.
	 * @param l1 first postings
	 * @param l2 second postings
	 * @param result contains the list of all the documents that has the query terms in it.
	 * @return merged result of two postings.
	 */
	public ArrayList<DocId> merge_many(ArrayList<DocId> l1, ArrayList<DocId> l2, ArrayList<Integer> result)
	{
		ArrayList<DocId> docList = new ArrayList<DocId>();
		int id1=0, id2=0;
		while(id1<l1.size()&&id2<l2.size()) {
			// For multiple phrase terms in a query we first
			// find the common doc containing first two terms.
			// Once we get those documents, we know that the
			// phrase has to exist in the this common doc only.
			// So, we restrict the code by allowing further 
			// but in the common docs only. If the third term doc
			// is not common doc of previous two we know this phrase
			// is not present in the docs.
			if((result.contains(l1.get(id1).docId) || result.contains(l2.get(id2).docId) )) {
			//if both terms appear in the same document
			if(l1.get(id1).docId==l2.get(id2).docId){
				//get the position information for both terms
				ArrayList<Integer> pp1 = l1.get(id1).positionList;
				ArrayList<Integer> pp2 = l2.get(id2).positionList;
				int pid1 =0, pid2=0;
				while(pid1<pp1.size()){
					boolean match = false;
					while(pid2<pp2.size()){
						//if the two terms appear together, we find a match
						if(Math.abs(pp1.get(pid1)-pp2.get(pid2))==1){
							match = true;
							DocId doid = new DocId(l1.get(id1).docId,pp1.get(pid1));
							docList.add(doid);
							// Save the details of the pid2 i.e position 
							break;
						}
						else if(pp2.get(pid2)>pp1.get(pid1))
							break;
						pid2++;
					}
					if(match) //if a match if found, the search for the current document can be stopped
						break;
					pid1++;
				}
				id1++;
				id2++;
			}
			else if(l1.get(id1).docId<l2.get(id2).docId)
				id1++;
			else if(l2.get(id2).docId<l1.get(id1).docId)
				id2++;
			else {
				id1++;
				id2++;
			}
		}
			else if(l1.get(id1).docId<l2.get(id2).docId)
				id1++;
			else if(l2.get(id2).docId<l1.get(id1).docId)
				id2++;
			else {
					id1++;
					id2++;
				}
			}
		return docList;
	}
	

	
	/**
	 * This method extract the queries from the command line arguments and then send the query
	 * terms for further processing.
	 * @param query a phrase query that consists of any number of terms in the sequential order
	 * @param pi the class object.
	 * @return ids of documents that contain the phrase.
	 */
	public ArrayList<DocId> phraseQuery(ArrayList<String> query, PositionalIndex pi)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		//boolean test_intersect = false;
		ArrayList<DocId> l1 = null;
		ArrayList<DocId> l2 = null;
		
		ArrayList<DocId> resultant = new ArrayList<DocId>();
		if(termList.contains(query.get(0)) && termList.contains(query.get(1)))
		{
			l1 = docLists.get(termList.indexOf(query.get(0)));
			l2 = docLists.get(termList.indexOf(query.get(1)));
		}
		else 
		{
			System.out.println("\nSearch String(s):"+ query);
			System.out.println("not found in documents.");
			System.exit(0);
		}
		if(query.size() > 2) {
			for(int i=0;i<query.size() -1;i++)
			{
				if(i==0 && l1 != null && l2 != null) 
				{
					// For multiple phrase terms in a query we first
					// find the common doc containing first two terms.
					// Once we get those documents, we know that the
					// phrase has to exist in the this common doc only.
					// So, we restrict the code by allowing further 
					// but in the common docs only. If the third term doc
					// is not common doc of previous two we know this phrase
					// is not present in the docs.
					resultant = pi.merge_two(l1,l2);
					l1 = null;
					l2 = null;
				}
				else
				{
					if(termList.contains(query.get(i)) && termList.contains(query.get(i+1)))
					{
						l1 = docLists.get(termList.indexOf(query.get(i)));
						l2 = docLists.get(termList.indexOf(query.get(i+1)));
					}
					else {
						l1=null;
						l2=null;
					}
					for(DocId j:resultant) {
						result.add(j.docId);
					}
					resultant = null;
					if(l1 != null && l2 != null)
					{
						resultant = pi.merge_many(l1, l2, result);
					}
				}
			}
		}
		// Else executes if there are only two terms in the phrase query.
		else {
			if(l1 != null && l2 != null) {
			resultant = pi.merge_two(l1, l2);
			}
		}
		if(resultant == null || resultant.size() == 0)
		{
			System.out.println("\nSearch String(s):"+ query);
			System.out.println("not found in documents.");
			System.exit(0);
		}
		return resultant;
	}

	/**
	* This is main driving method.
	* @param args     : contains the Command line arguments.
	* @return         : None
	*/
	public static void main(String[] args)
	{
		String[] docs = {	"new home sales top forecasts",
				 			"home sales rise in july",
				 			"increase in home sales in july",
				 			"july new home sales rise"
						};
		PositionalIndex pi = new PositionalIndex(docs);
		System.out.print(pi);
		ArrayList<String> queries = new ArrayList<String>();
		for(String query:args) {
			queries.add(query);
		}
		if(args.length >= 2) {
		ArrayList<DocId> result = pi.phraseQuery(queries, pi);
		System.out.println("\nFor Search queries:"+queries);
		System.out.println();
		if(result!=null && result.size() > 0 && !result.contains(-1))
		{
			for(DocId i:result) 
				System.out.println("Doc "+(i.docId + 1)+" has relevant info!!");
		}
		else
			System.out.println("No relevant document found!!");
		}
		else {
			System.out.println("\nPlease input at least 2 search terms!!");
			System.exit(0);
		}
	}
}

/**
 * 
 * @author qyuvks
 * Document id class that contains the document id and the position list
 */
class DocId{
	int docId;
	ArrayList<Integer> positionList;
	public DocId(int did)
	{
		docId = did;
		positionList = new ArrayList<Integer>();
	}
	public DocId(int did, int position)
	{
		docId = did;
		positionList = new ArrayList<Integer>();
		positionList.add(new Integer(position));
	}
	
	public void insertPosition(int position)
	{
		positionList.add(new Integer(position));
	}
	
	
	public String toString()
	{
		String docIdString = ""+docId + ":<";
		for(Integer pos:positionList)
			docIdString += pos + ",";
		docIdString = docIdString.substring(0,docIdString.length()-1) + ">";
		return docIdString;		
	}
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
	 * 						POSITIVE TEST CASES
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
	 * [NOTE]: For testing the code we need to give at lease 2 arguments 
	 * from command line (Run Configuration)
	 * 
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Test Case 1:
	 * Input: home sales rise
	 * 
	 * Output: 
	 * For Search queries:[home, sales, rise]
	 * 
	 * Doc 2 has relevant info!!
	 * Doc 4 has relevant info!!
	 * 
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Test Case 2:
	 * [Input]: increase in home sales in july
	 * 
	 * [Output]: 
	 * For Search queries:[increase, in, home, sales, in, july]
	 * 
	 * Doc 3 has relevant info!!
	 * 
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *   
	 * Test Case 3:
	 * [Input]: in increase
	 * 
	 * [Output]:
	 * For Search queries:[in, increase]
	 * 
	 * Doc 3 has relevant info!!
	 * 
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  
	 * Test Case 4:
	 * [Input]: home sales
	 * 
	 * [Output]:
	 * For Search queries:[home, sales]
	 * 
	 * Doc 1 has relevant info!!
	 * Doc 2 has relevant info!!
	 * Doc 3 has relevant info!!
	 * Doc 4 has relevant info!!
	 *   
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  
	 * 
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
	 * 						NEGATIVE TEST CASE(S)
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 	
	 *  
	 * Test Case 1:
	 * [Input]: homes sale increasing
	 * 
	 * [Output]:
	 * 
	 * Search String(s):[homes, sale, increasing]
	 * not found in documents.
	 * 
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
	 * Test Case 2:
	 * [Input]: 
	 * 
	 * [Output]:
	 * Please input at least 2 search terms!!
	 * 
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Test Case 3:
	 * [Input]: sales sales
	 * 
	 * [Output]:
	 * For Search queries:[sales, sales]
	 * 
	 * No relevant document found!!
	 * */
}
