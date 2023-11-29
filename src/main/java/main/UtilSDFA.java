package main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import roll.automata.SDFA;
import roll.automata.StateNFA;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

public class UtilSDFA {
	
//	private StateNFA run(DFA dfa, String word) {
//		StateNFA currState = dfa.getState(dfa.getInitialState());
//		if (word.isEmpty()) {
//			return currState;
//		}
//		int val = 0;
//		
//		int currIndex = 0;
//		while(true) {
//			StateNFA child;
//			int letter = word.charAt(currIndex);
////			System.out.println(" letter: " + letter);
////			System.out.println("Current branch: " + branch);
//			int succ = currState.getSuccessor(letter);
//			if (succ == -1) {
//				child = dfa.createState();
//				currState.addTransition(letter, child.getId());
//			}else {
//				child = dfa.getState(succ);
//			}
//			++ currIndex;
//			currState = child;
//			if (currIndex >= word.length()) {
//				return child;
//			}
//		}
//	}
	
	public static Query<HashableValue> makeCex(Alphabet alphabet, String cexStr) {
		Word cex = alphabet.getWordFromString(cexStr);
		Query<HashableValue> ceQuery = new QuerySimple<HashableValue>(cex);
        ceQuery.answerQuery(new HashableValueBoolean(false));
        return ceQuery;
	}
	
    public static SDFA reduce(SDFA sdfa, LinkedList<String> positives
    		, LinkedList<String> negatives) {
    	SDFA res = new SDFA(sdfa.getAlphabet());
    	ISet remaining = UtilISet.newISet();
    	// first, record the transitions that we will keep
    	for (int s = 0; s < sdfa.getStateSize(); s ++) {
    		res.createState();
    		if (sdfa.isFinal(s)) {
    			res.setFinal(s);
    		}else if (sdfa.isReject(s)) {
    			res.setReject(s);
    		}
    	}
    	remaining.set(sdfa.getInitialState());
    	for (String word : positives) {
    		int curr = sdfa.getInitialState();
    		int index = 0;
    		while (index < word.length()) {
    			int letter = word.charAt(index);
    			int succ = sdfa.getSuccessor(curr, letter);
    			res.getState(curr).addTransition(letter, succ);
    			curr = succ;
    			remaining.set(curr);
    			++ index;
    		}
    	}
    	
    	for (String word : negatives) {
    		int curr = sdfa.getInitialState();
    		int index = 0;
    		while (index < word.length()) {
    			int letter = word.charAt(index);
    			int succ = sdfa.getSuccessor(curr, letter);
    			res.getState(curr).addTransition(letter, succ);
    			curr = succ;
    			remaining.set(curr);
    			++ index;
    		}
    	}
    	res.setInitial(sdfa.getInitialState());
//		System.out.println("Reduced 1:\n" + res.toString());
    	final int numRemaining = remaining.cardinality();
    	// now we remove all unreachable states
    	// and compute their corresponding indices
    	int index = 0;
    	TIntIntMap map = new TIntIntHashMap();

    	for (int s : remaining) {
    		map.put(s, index ++ );
//    		System.out.println(s + " -> " + map.get(s));
    	}
    	
    	SDFA copy = new SDFA(sdfa.getAlphabet());
    	for (int s = 0; s < numRemaining; s ++) {
    		copy.createState();
    	}
    	copy.setInitial(map.get(res.getInitialState()));
    	for (int s = 0; s < res.getStateSize(); s ++) {
    		int curr = map.get(s);
    		if (res.isFinal(s)) {
    			copy.setFinal(curr);
    		}else if (res.isReject(s)) {
    			copy.setReject(curr);
    		}
    		// now transitions
    		StateNFA currState = res.getState(s);
    		for (int letter : currState.getEnabledLetters()) {
    			int t = currState.getSuccessor(letter);
    			copy.getState(curr).addTransition(letter, map.get(t));
    		}
    	}
		System.out.println("Reduced 2:\n" + copy.toString());
    	return copy;
    }
    
    // file format
    // Ln1: #States #Letters
    // Ln2: State letter State
    // Ln.: a State
    // Ln.: r state
    public static void outputSDFA(SDFA sdfa, String fileName) 
    		throws FileNotFoundException {
    	PrintWriter writer = new PrintWriter(fileName);
        writer.println(sdfa.getStateSize() + " " + sdfa.getAlphabetSize());
        writer.println(sdfa.getInitialState());
        for (int s = 0; s < sdfa.getStateSize(); s ++) {
        	for (int letter = 0; letter < sdfa.getAlphabetSize(); letter++) {
        		int succ = sdfa.getState(s).getSuccessor(letter);
        		if (succ != -1) writer.println("t " + s + " " + letter + " " + succ);
        	}
        }
//        ISet finalStates = sdfa.getFinalStates();
        for (int s : sdfa.getFinalStates()) {
        	writer.println("a " + s);
        }
//        ISet rejectStates = sdfa.getRejectStates();
        for (int s : sdfa.getRejectStates()) {
        	writer.println("r " + s);
        }
        writer.close();
    }

}
