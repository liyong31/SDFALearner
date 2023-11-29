package main;

import java.util.LinkedList;

import dk.brics.automaton.Automaton;
import roll.automata.DFA;
import roll.automata.SDFA;
import roll.automata.operations.DFAOperations;
import roll.main.Options;
import roll.oracle.TeacherAbstract;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.table.HashableValueEnum;
import roll.util.Timer;
import roll.words.Alphabet;
import roll.words.Word;

// general teacher for answering questions about
// words of possibly different lengths 

public class TeacherSDFA extends TeacherAbstract<SDFA> {
	
    private Alphabet alphabet;
    Automaton posDkDFA;
    Automaton negDkDFA;
    
	public TeacherSDFA(Options options, Alphabet alphabet, 
		LinkedList<String> positives, LinkedList<String> negatives,
		LinkedList<String> dontcares) {
		super(options);
		this.alphabet = alphabet;
//		System.out.println(alphabet.toString());
		constructSampleDFA(positives, negatives);
	}
	
    
    private void constructSampleDFA(LinkedList<String> positives
    		, LinkedList<String> negatives) {
    	String[] newWords = new String[positives.size()];
    	int index = 0;
    	for (String word : positives) {
    		newWords[index ++] = word;
    	}

    	Timer timer = new Timer();
    	timer.start();
    	// returned DFA is already minimal and deterministic
    	posDkDFA = Automaton.makeStringUnion(newWords);
//    	System.out.println("Eq: " + checkEquivalence(pos, posDkDFA));
//    	posDkDFA.minimize();
    	timer.stop();
    	System.out.println("TimePos: " + timer.getTimeElapsed()/1000);
//    	System.out.println("#States: " + posDkDFA.getStateSize());
    	System.out.println("#posDFA: " + posDkDFA.getStates().size());
    	
    	newWords = new String[negatives.size()];
    	index = 0;
    	for (String word : negatives) {
    		newWords[index ++] = word;// wordStr;
    	}
    	timer.start();
//    	negDkDFA.minimize();
    	negDkDFA = Automaton.makeStringUnion(newWords);
//    	System.out.println("Eq: " + checkEquivalence(neg, negDkDFA));
    	timer.stop();
    	System.out.println("TimeNeg: " + timer.getTimeElapsed()/1000);
    	System.out.println("#negDFA: " + negDkDFA.getStates().size());
	}
	

	@Override
	protected HashableValue checkMembership(Query<HashableValue> query) {
		Word word = query.getQueriedWord();
		StringBuilder sb = new StringBuilder();
		String wordStr = "";
		for (int i = 0; i < word.length(); i ++) {
			sb.append((char)word.getLetter(i));
		}
		wordStr = sb.toString();
//		System.out.println("Query word: " + word.toString());
		if (posDkDFA.run(wordStr)) {
//			System.out.println("accept: " + word.toString());
			return new HashableValueEnum(1); 
		}
		if (negDkDFA.run(wordStr)) {
//			System.out.println("reject: " + word.toString());
			return new HashableValueEnum(-1);
		}
//		System.out.println("node = " + (node == null? "null" : node.getLabel().toString()));
		return new HashableValueEnum(0);
	}
	
	private String checkEquivalence(Automaton first, Automaton second) {
		Automaton comp = second.complement();
		Automaton inter = first.intersection(comp);
		String cexStr = inter.getShortestExample(true);
		if (cexStr != null) return cexStr;
		comp = first.complement();
		inter = second.intersection(comp);
		cexStr = inter.getShortestExample(true);
		return cexStr;
	}

	@Override
	protected Query<HashableValue> checkEquivalence(SDFA hypothesis) {
		Word cex = alphabet.getEmptyWord();
		Query<HashableValue> ceQuery = null;
		DFA dfa = hypothesis.getDFA(true);
		Automaton dkDFA = DFAOperations.toDkDFA(dfa);
		String cexStr = checkEquivalence(posDkDFA, dkDFA);
		if (cexStr != null) {
			return UtilSDFA.makeCex(alphabet, cexStr);
		}
		dfa = hypothesis.getDFA(false);
		dkDFA = DFAOperations.toDkDFA(dfa);
		cexStr = checkEquivalence(negDkDFA, dkDFA);
		if (cexStr != null) {
			return UtilSDFA.makeCex(alphabet, cexStr);
		}
    	ceQuery = new QuerySimple<HashableValue>(cex);
        ceQuery.answerQuery(new HashableValueBoolean(true));
        return ceQuery;
	} 

}
