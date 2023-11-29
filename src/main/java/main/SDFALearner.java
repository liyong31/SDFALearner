package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import roll.automata.DFA;
import roll.automata.SDFA;
import roll.learner.LearnerFA;
import roll.learner.sdfa.LearnerSDFATable;
import roll.learner.sdfa.LearnerSDFATree;
import roll.main.Options;
import roll.query.Query;
import roll.table.HashableValue;
import roll.util.Timer;
import roll.words.Alphabet;

public class SDFALearner {
    
	// this function split the string by space
	private static ArrayList<String> splitList(String str, int start) {
		ArrayList<String> words = new ArrayList<String>();
	    words.ensureCapacity(str.length()/5);
	    int left = start;
	    int i;
	    for (i = start; i < str.length(); i ++) {
	        if (str.charAt(i) == ' ') {
	            words.add(str.substring(left, i));
	            left = i + 1; // if there is another string
	        }
	    }
	    // just before next line
        words.add(str.substring(left, i));
	    return words;
	}
	
	public static void main(String[] args) throws IOException {
		Timer timer = new Timer();
		if (args.length < 3) {
			throw new RuntimeException("Need input/output filenames and data structure");
		}
		String inputFileName = args[0];
		String outputFileName = args[1];
		String structure = args[2];
		boolean newFormat = false;
		if (args.length >= 4) {
			newFormat = true;
		}
		long eq = System.currentTimeMillis();
		Alphabet alphabet = new Alphabet();
		Options options = new Options();
		BufferedReader br = new BufferedReader(new FileReader(inputFileName));

		try {
			timer.start();
		    String line = br.readLine();
		    // this is the first line, read the numbers
		    //String[] splitStr = line.split("\\s+");
		    ArrayList<String> splitStr = splitList(line, 0);
//		    ArrayList<String> splitStr = splitList(line);
		    System.out.println("Start reading samples: " + splitStr.get(0));
//		    System.out.println(splitStr.get(1));
		    int numLetters = Integer.parseInt(splitStr.get(1));
		    for (int i = 0; i < numLetters; i ++) {
//		    	alphabet.addLetter((char)(48+i));
		    	// the index is also the character value
			    alphabet.addLetter((char)i);
		    }
		    LinkedList<String> pos = new LinkedList<String>();
		    LinkedList<String> neg = new LinkedList<String>();
		    LinkedList<String> don = new LinkedList<String>();
		    
		    while ((line = br.readLine()) != null) {
		       //membership, length, ....
		    	int blankNr = line.indexOf(' ');
		    	int mq = Integer.parseInt(line.substring(0, blankNr));
		    	if (mq == -1) {
		    		// ignore all don't care words
		    		continue;
		    	}
		    	// first: length then word
		    	splitStr = splitList(line, blankNr + 1);
//		    	splitStr = splitList(line);
		    	
		    	int len = -1;
		    	int start = 1;
		    	if (newFormat) {
		    		// we do not have length, then it is word now
		    		len = splitStr.size();
		    		start = 0;
		    	}else {
		    		len = Integer.parseInt(splitStr.get(0));
		    	}
		    	char[] w = new char[len];
		    	final int end = start + len;

		    	for (int i = start; i < end; i ++) {
		    		w[i - start] = (char)Integer.parseInt(splitStr.get(i));
		    	}
		    	String word = new String(w);
		        if (mq == 1) {
//			        System.out.println("pos word: " + word.toString());
		        	pos.add(word);
		        }else if (mq == 0){
		        	neg.add(word);
//			        System.out.println("neg word: " + word.toString());
		        }else {
		        	throw new RuntimeException(
		        			"Not supposed to see values other than 1, 0 and -1");
		        }
		    }
		    // now learn
		    System.out.println("Structure: " + structure);
		    if (structure.equals("table")) {
		    	options.structure = Options.Structure.TABLE;
		    }else {
		    	options.structure = Options.Structure.TREE;
		    }
		    eq = System.currentTimeMillis() - eq;
		    runSDFALearner(options, alphabet, pos, neg, don, outputFileName);
		    System.out.println("Finished reading file: " + eq/1000.0);
		} finally {
		    br.close();
		}
		
	}
	
    private static boolean canRefine(SDFA sdfa, Query<HashableValue> ceQuery) {
    	HashableValue mqSDFA = sdfa.run(ceQuery.getQueriedWord());
    	HashableValue mqResult = ceQuery.getQueryAnswer();
    	return !mqSDFA.valueEqual(mqResult);
    }
	
	private static void runSDFALearner(Options options, Alphabet alphabet, LinkedList<String> pos
			, LinkedList<String> neg, LinkedList<String> don, String fileName) throws FileNotFoundException {
	    System.out.println("Preparing the teacher...: ");
	    long time = System.currentTimeMillis();
	    TeacherSDFA teacher = new TeacherSDFA(options, alphabet, pos, neg, don);
//	    TeacherSDFASmall teacher = new TeacherSDFASmall(options, alphabet, pos, neg, don);
	    LearnerFA<DFA> learner = null;
	    if (options.structure.isTable()) {
	    	learner = new LearnerSDFATable(options, alphabet, teacher);
	    }else {
	    	learner = new LearnerSDFATree(options, alphabet, teacher);
	    }
//	    LearnerSDFATable learner = new LearnerSDFATable(options, alphabet, teacher);
		System.out.println("starting learning");
		learner.startLearning();
        int num = 1;
		while(true) {
//			System.out.println("Table is both closed and consistent\n" + learner.toString());
			SDFA model = (SDFA)learner.getHypothesis();
//			System.out.println("Model:\n" + model.toString());
			// along with ce
			long eq = System.currentTimeMillis();
			Query<HashableValue> ceQuery = teacher.answerEquivalenceQuery(model);
			eq = System.currentTimeMillis() - eq;
			System.out.println("The " + num + " equivalence query...");
			num ++;
			boolean isEq = ceQuery.getQueryAnswer().get();
			if(isEq) {
//				System.out.println(model.toString());
				SDFA reduced = UtilSDFA.reduce(model, pos, neg);
//				System.out.println("Reduced:\n" + reduced.toString());
		        System.out.println("Output dfa");
		        UtilSDFA.outputSDFA(reduced, fileName);
		        System.out.println("Final states: " + reduced.getFinalSize());
		        System.out.println("Reject states: " + reduced.getRejectSize());
				break;
			}
			HashableValue mqResult = teacher.answerMembershipQuery(ceQuery);
			ceQuery.answerQuery(mqResult);
//			System.out.println("Cex: " + ceQuery.toString() + ": " + mqResult);
//			System.out.println("SDFA mq: " + model.run(ceQuery.getQueriedWord()));
			while (canRefine(model, ceQuery)) {
				learner.refineHypothesis(ceQuery);
				model = (SDFA)learner.getHypothesis();
			}
		}
		
		System.out.println("Finished learning: " + (System.currentTimeMillis() - time)/1000.0);
	}


}

