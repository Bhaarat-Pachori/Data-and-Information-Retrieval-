
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
public class NBClassifier {
	String[] trainingDocs;
	static ArrayList<Integer> trainingLabels = new ArrayList<Integer>();
	int numClasses;
	static int[] classCounts; //number of docs per class
	String[] classStrings; //concatenated string for a given class
	int[] classTokenCounts; //total number of tokens per class
	HashMap<String,Double>[] condProb;
	HashSet<String> vocabulary; //entire vocabuary
	
	public NBClassifier(String[] docs, int numC)
	{
		trainingDocs = docs;
		numClasses = numC;
		classCounts = new int[numClasses];
		classStrings = new String[numClasses];
		classTokenCounts = new int[numClasses];
		condProb = new HashMap[numClasses];
		vocabulary = new HashSet<String>();
		for(int i=0;i<numClasses;i++){
			classStrings[i] = "";
			condProb[i] = new HashMap<String,Double>();
		}
		for(int i=0;i<trainingLabels.size();i++){
			classCounts[trainingLabels.get(i)]++;
			classStrings[trainingLabels.get(i)] += (trainingDocs[i] + " ");
		}
		for(int i=0;i<numClasses;i++){
			String[] tokens = classStrings[i].split(" ");
			classTokenCounts[i] = tokens.length;
			
			//collecting the counts
			for(String token:tokens){
				vocabulary.add(token);
				if(condProb[i].containsKey(token)){
					double count = condProb[i].get(token);
					condProb[i].put(token, count+1);
				}
				else
					condProb[i].put(token, 1.0);
			}
		}
		//computing the class conditional probability
		for(int i=0;i<numClasses;i++){
			Iterator<Map.Entry<String, Double>> iterator = condProb[i].entrySet().iterator();
			int vSize = vocabulary.size();
			while(iterator.hasNext())
			{
				Map.Entry<String, Double> entry = iterator.next();
				String token = entry.getKey();
				Double count = entry.getValue();
				count = (count+1)/(classTokenCounts[i]+vSize);
				condProb[i].put(token, count);
			}
		}
	}
	
	public int classfiy(String doc){
	   /**
	   * This method put the labels or classify a single documents.
	   * @param folderType         :String of all the content of the documents in training dataset.
	   * @return        		   :It returns the class label. 0 if positive & 1 if negative.
	   */
		int label = 0;
		int vSize = vocabulary.size();
		double[] score = new double[numClasses];
		for(int i=0;i<score.length;i++){
			score[i] = Math.log(classCounts[i]*1.0/trainingDocs.length);
		}
		String[] tokens = doc.split(" ");
		for(int i=0;i<numClasses;i++){
			for(String token: tokens){
				if(condProb[i].containsKey(token))
					score[i] += Math.log(condProb[i].get(token));
				else
					score[i] += Math.log(1.0/(classTokenCounts[i]+vSize));
			}
		}
		double maxScore = score[0];
		for(int i=0;i<score.length;i++){
			if(score[i]>maxScore)
				label = i;
		}
		
		return label;
	}
	
	public int classfiyAll(String doc){
	   /**
	   * This method put the labels or classify all the testing documents.
	   * @param folderType         :String of all the content of the documents in training dataset.
	   * @return        		   :It returns the class label. 0 if positive & 1 if negative.
	   */
		int label = 0;
		int vSize = vocabulary.size();
		double[] score = new double[numClasses];
		for(int i=0;i<score.length;i++){
			score[i] = Math.log(classCounts[i]*1.0/trainingDocs.length);
		}
		String[] tokens = doc.split(" ");
		for(int i=0;i<numClasses;i++){
			for(String token: tokens){
				if(condProb[i].containsKey(token))
					score[i] += Math.log(condProb[i].get(token));
				else
					score[i] += Math.log(1.0/(classTokenCounts[i]+vSize));
			}
		}
		double maxScore = score[0];
		for(int i=0;i<score.length;i++){
			if(score[i]>maxScore)
				label = i;
		}
		
		return label;
	}
	
public static String[] parse(File[] listOfFiles) throws IOException {
	   /**
	   * This method parse the individual files.
	   * @param folderType         :Training or Testing data folder.
	   * @param folderName 		   :Positive or Negative data.
	   * @param MainfolderName 	   :Parent folder where training & testing data resides.
	   * @return        		   :It returns the overall string of current training document.
	   */
		
		String[] trainDocs = new String[listOfFiles.length];
		BufferedReader br = null;
		int itr = 0, len = 0;
		String[] tokens = null;
		
		//Reading file
		while(len < listOfFiles.length) {
			br = new BufferedReader(new FileReader(listOfFiles[itr])); 
		    String line = "";
		    String allLines = "";
		    while((line = br.readLine())!= null){
		    	allLines += line+" ";
		    }
		    tokens = allLines.split("[ `.,?!:;$%()\\^\\-\"'#/*+&{}\\d+_@=\\|><]+");
		    String str = Arrays.toString(tokens);
		    trainDocs[itr] = str;
		    itr++;
		    len++;
		}
	    br.close();
	    return trainDocs;
	}
	
	public static String[] train(String folderType, String[] folderName, String MainfolderName) 
			throws IOException {
	   /**
	   * This method trains the classifier with the training data.
	   * @param folderType         :Training or Testing data folder.
	   * @param folderName 		   :Positive or Negative data.
	   * @param MainfolderName 	   :Parent folder where training & testing data resides.
	   * @return        		   :It returns the overall string of all training documents.
	   */
		// This array of Strings have all the strings of pos and neg reviews.
		String [] trainDocs = new String[trainingLabels.size()];
		
		// Parse train docs
		File[] listOfFiles;
		String whichfolderName = MainfolderName + folderType + folderName[0];
		File folder = new File(whichfolderName);
		listOfFiles = folder.listFiles();

		String [] trainDocspos = parse(listOfFiles);
		for(int i=0;i<trainDocspos.length;i++) {
			trainDocs[i] = trainDocspos[i];
		}
		
		whichfolderName = "";
		whichfolderName = MainfolderName + folderType + folderName[1];
		folder = new File(whichfolderName);
		listOfFiles = folder.listFiles();
		
		String [] trainDocsneg = parse(listOfFiles);
		int index = 0;
		
		for(int i=0;i<trainDocs.length;i++) {
			if(trainDocs[i] != null)
				index++;
		}
		for(int i=0;i<trainDocsneg.length;i++) {
			trainDocs[index] = trainDocsneg[i];
			index++;
		}
		return trainDocs;
	}
	
	public static String parse_test(File file) throws IOException {
		
		String testDocs = new String();
		BufferedReader br = null;
		int itr = 0;
		String[] tokens = null;
		
		//Reading file
		br = new BufferedReader(new FileReader(file)); 
	    String line = "";
	    String allLines = "";
	    while((line = br.readLine())!= null){
	    	allLines += line+" ";
	    }
	    tokens = allLines.split("[ `.,?!:;$%()\\^\\-\"'#/*+&{}\\d+_@=\\|><]+");
	    testDocs = Arrays.toString(tokens);
		    
	    br.close();
	    return testDocs;
	}
	
	public static String test(File file) throws IOException {
		return parse_test(file);
	}
	
	/**
	 * Load the training documents
	 * @param trainDataFolder
	 */
	public static void preprocess(String trainDataFolder,int numClass, String[] folderType, String[] folderName)
	{
	   /**
	   * This method preprocesses the training data for building the classifier
	   * @param trainDataFolder    :Path to the training documents.
	   * @param numClass 		   :total number of classes.
	   * @param folderType         :Training or Testing data folder.
	   * @param folderName 		   :Positive or Negative data.
	   * @return        		   :None.
	   */
		File[] listOfFiles;
		// This while loops creates the labels for the pos and neg reviews.
		while(numClass != 0) {
			String whichfolderName = trainDataFolder + folderType[0] + folderName[2 - numClass];
			File folder = new File(whichfolderName);
			listOfFiles = folder.listFiles();
			if(whichfolderName.contains("pos")) {
				for(int i=0;i<listOfFiles.length;i++) {
					trainingLabels.add(0);
				}
			}
			else {
				for(int i=0;i<listOfFiles.length;i++) {
					trainingLabels.add(1);
				}
			}
			numClass--;
		}
	}
	
	public static double show_accuracy(ArrayList<Integer> result, int total_docs, ArrayList<Integer> actual_test_data, 
																		String folderName) {
	   /**
	   * This method calculates the accuracy of the classifier.
	   * @param result             :Name of the data file to be parsed.
	   * @param total_docs 		   :total testing documents.
	   * @param actual_test_data   :actual result of the classifier.
	   * @param folderName 		   :which folder pos or neg.
	   * @return        		   :It returns the accuracy of the classifier.
	   */
		int compare1 = 0, classified_right = 0;
		int compare2 = 0;
		double accuracy = 0.0;
		while(compare1 < result.size()) {
			if(actual_test_data.get(compare1).intValue() == result.get(compare2).intValue())
			{
				classified_right++;
			}
			compare1++;
			compare2++;
		}
		if(classified_right < total_docs)
			accuracy = (classified_right*1.0) % total_docs;
		classified_right = 0;
		System.out.println("Rightly classified "+ accuracy + " "+folderName +" documents: "+ accuracy + "/"+total_docs);
		System.out.println(result);
		return accuracy;
	}
	
	public static void main(String[] args) throws IOException{
		
		int numClass = 2;
		File[] listOfFiles = null;
		String [] folderName = {"pos", "neg"};
		String [] folderType = {"train/", "test/"};
		
		String MainfolderName = args[0];
		
		preprocess(MainfolderName, numClass, folderType, folderName);
		
		String [] trainDocs = new String[trainingLabels.size()];
		
		trainDocs = train(folderType[0], folderName, MainfolderName);
		NBClassifier nb = new NBClassifier(trainDocs, numClass);
		
		// Preparing/Parsing doc for testing.
		ArrayList<Integer> result = new ArrayList<Integer>();
		ArrayList<Integer> actual_test_data = new ArrayList<Integer>();
		double accuracy = 0.0;
		int total_docs_in_all_classes = 0;
		for(int i=0;i<numClass;i++) {
			String whichfolderName = MainfolderName + folderType[1] + folderName[i];
			File folder = new File(whichfolderName);
			listOfFiles = folder.listFiles();
			total_docs_in_all_classes += listOfFiles.length;
			
			int len = 0;
			for(int j=0; j<listOfFiles.length; j++) {
				if(whichfolderName.contains("pos")) {
					actual_test_data.add(0);
				}
				else
					actual_test_data.add(1);
			}
			while(len < listOfFiles.length) {
				String test = test(listOfFiles[len]);
					result.add(nb.classfiyAll(test));
				len++;
			}
			accuracy += show_accuracy(result, listOfFiles.length, actual_test_data, folderName[i]);
			result.clear();
			actual_test_data.clear();
		}
		System.out.println();
		System.out.println("Total rightly classified documents "+accuracy+" out of "+total_docs_in_all_classes);
		System.out.println("Classification Accuracy = "+accuracy/numClass+"%");
		
		String testDoc = "Chineese Chineese Chineese Japan Tokyo";
		System.out.println();
		System.out.println("Classified single given document as "+(nb.classfiy(testDoc) == 1?"Negative":"Positive"));
	}
}
