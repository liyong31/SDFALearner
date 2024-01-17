package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dk.brics.automaton.Automaton;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import roll.automata.SDFA;
import roll.automata.StateNFA;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.table.HashableValueEnum;
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
	
    public static SDFA reduce(SDFA sdfa) {
    	SDFA res = new SDFA(sdfa.getAlphabet());
    	ISet remaining = UtilISet.newISet();
    	// first, record the transitions that we will keep
    	ISet careStates = UtilISet.newISet();
    	for (int s = 0; s < sdfa.getStateSize(); s ++) {
    		remaining.set(s);
    		res.createState();
    		if (sdfa.isFinal(s)) {
    			res.setFinal(s);
    			careStates.set(s);
    		}else if (sdfa.isReject(s)) {
    			res.setReject(s);
    			careStates.set(s);
    		}
    	}
    	ISet removed = UtilISet.newISet();
    	for (int s : careStates) {
    		for (int c : sdfa.getState(s).getEnabledLetters()) {
    			int t = sdfa.getSuccessor(s, c);
    			removed.set(t);
    		}
    	}
    	remaining.andNot(removed);
    	for (int s = 0; s < sdfa.getStateSize(); s ++) {
    		if (removed.get(s)) continue;
    		for (int c : sdfa.getState(s).getEnabledLetters()) {
    			int t = sdfa.getSuccessor(s, c);
    			if (removed.get(t)) continue;
    			res.getState(s).addTransition(c, t);
    		}
    	}
//    	for (String word : positives) {
//    		int curr = sdfa.getInitialState();
//    		int index = 0;
//    		while (index < word.length()) {
//    			int letter = word.charAt(index);
//    			int succ = sdfa.getSuccessor(curr, letter);
//    			res.getState(curr).addTransition(letter, succ);
//    			curr = succ;
//    			remaining.set(curr);
//    			++ index;
//    		}
//    	}
//    	
//    	for (String word : negatives) {
//    		int curr = sdfa.getInitialState();
//    		int index = 0;
//    		while (index < word.length()) {
//    			int letter = word.charAt(index);
//    			int succ = sdfa.getSuccessor(curr, letter);
//    			res.getState(curr).addTransition(letter, succ);
//    			curr = succ;
//    			remaining.set(curr);
//    			++ index;
//    		}
//    	}
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
    
	// this function split the string by space
	public static ArrayList<String> splitList(String str, int start) {
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
    
    public static void readSampleFile(
    		String inputFileName
    		, boolean newFormat 
    		, Alphabet alphabet
    		, List<String> pos
    		, List<String> neg) throws IOException {
    	
		BufferedReader br = new BufferedReader(new FileReader(inputFileName));
		
		try {
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
		    
		    while ((line = br.readLine()) != null) {
		       //membership, length, ....
		    	int blankNr = line.indexOf(' ');
		    	if (blankNr == -1) {
		    		// this means the line is empty
		    		continue;
		    	}
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
		}catch (IOException o) {
			System.err.println(o.fillInStackTrace());
		}finally {
			br.close();
		}
    }
    

    public static boolean isEvenLoop(Word word, int left, int right) {
    	int max_color = -1;
        for (int j = left; j <= right; j ++) {
        	 max_color = Math.max(max_color, word.getLetter(j));
        }    
        if ((max_color & 1) == 0) {
        	return true;
        }else {
        	return false;
        }
    }
    
    public static HashableValue decideMembership(Word word, int numColors) {
    	Pair[] positions = new Pair[numColors];
    	for (int i = 0; i < numColors; i ++) {
    		positions[i] = new Pair(-1, -1);
    	}
        
        int mask = 0;
        for (int i = 0; i < word.length(); i ++) {
        	int color = word.getLetter(i);
            Pair pair = positions[color];
            boolean hasLoop = false;
            if (pair.left == -1)
                positions[color].left = i;
            else if(pair.right == -1) {
                positions[color].right = i;
                hasLoop = true;
            } else {
                positions[color].left = positions[color].right;
                positions[color].right = i;
                hasLoop = true;
            }
           
            boolean isEven = isEvenLoop(word, pair.left, pair.right);
//            System.out.println("lft = " + pair.left + ", rgt=" + pair.right 
//            		+ ", is_even=" + isEven + ", has_loop=" + hasLoop);
            if (hasLoop && isEven)
                mask |= 2;
            else if ( hasLoop && ! isEven)
                mask |= 1;
//            System.out.println("loop_mask=" + mask);
            if (mask >= 3)
                return new HashableValueEnum(0);
        }
        
        if (mask == 2)
            return new HashableValueEnum(1);
        else if( mask == 1)
            return new HashableValueEnum(-1);
        else
            return new HashableValueEnum(0);
    }
    
	public static String checkEquivalence(Automaton first, Automaton second) {
		Automaton comp = second.complement();
		Automaton inter = first.intersection(comp);
		String cexStr = inter.getShortestExample(true);
		if (cexStr != null) return cexStr;
		comp = first.complement();
		inter = second.intersection(comp);
		cexStr = inter.getShortestExample(true);
		return cexStr;
	}

}
