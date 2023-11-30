package main;

import java.io.FileNotFoundException;
import java.io.IOException;
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
		Alphabet alphabet = new Alphabet();
		Options options = new Options();

		timer.start();
		// now learn
		LinkedList<String> pos = new LinkedList<String>();
		LinkedList<String> neg = new LinkedList<String>();
		LinkedList<String> don = new LinkedList<String>();
		UtilSDFA.readSampleFile(inputFileName, newFormat, alphabet, pos, neg);
		timer.stop();
		long eq = timer.getTimeElapsed();
		
		System.out.println("Structure: " + structure);
		if (structure.equals("table")) {
			options.structure = Options.Structure.TABLE;
		} else {
			options.structure = Options.Structure.TREE;
		}
		
		runSDFALearner(options, alphabet, pos, neg, don, outputFileName);
		System.out.println("Finished reading file: " + eq / 1000.0);
		
		
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

