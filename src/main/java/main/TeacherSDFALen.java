package main;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import roll.automata.DFA;
import roll.automata.SDFA;
import roll.automata.operations.DFAOperations;
import roll.main.Options;
import roll.oracle.TeacherAbstract;
import roll.query.Query;
import roll.table.HashableValue;
import roll.table.HashableValueEnum;
import roll.words.Alphabet;
import roll.words.Word;

public class TeacherSDFALen extends TeacherAbstract<SDFA> {
	private int numColors;
	private int length;
	private Alphabet alphabet;
	private Automaton autLen;
	private Automaton autNonLen;

    public TeacherSDFALen(Options options
    		, int numColors, int length) {
		super(options);
		this.alphabet = new Alphabet();
		this.numColors = numColors;
		this.length = length;
		for (int i = 0; i < numColors; i ++) {
			this.alphabet.addLetter((char)i);
		}
		// now add states
		State curr = new State();
		autLen.setInitialState(curr);
		for (int i = 0; i < length; i ++) {
			State next = new State();
			curr.addTransition(new Transition((char)0, (char)(numColors - 1), next));
			curr = next;
		}
		curr.setAccept(true);
		autNonLen = autLen.complement();
	}

    private boolean isEvenLoop(Word word, int left, int right) {
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
    
    class Pair {
    	int left;
    	int right;
    	
    	Pair(int lft, int rgt) {
    		this.left = lft;
    		this.right = rgt;
    	}
    	
    }
    
    private HashableValue decideMembership(Word word) {
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
            System.out.println("lft = " + pair.left + ", rgt=" + pair.right 
            		+ ", is_even=" + isEven + ", has_loop=" + hasLoop);
            if (hasLoop && isEven)
                mask |= 2;
            else if ( hasLoop && ! isEven)
                mask |= 1;
            System.out.println("loop_mask=" + mask);
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

	@Override
	protected HashableValue checkMembership(Query<HashableValue> query) {
		Word word = query.getQueriedWord();
		if (word.length() != length) {
			return new HashableValueEnum(0);
		}
		// then we check whether it is even or odd
		return decideMembership(word);
	}

	@Override
	protected Query<HashableValue> checkEquivalence(SDFA hypothesis) {
		DFA posDFA = hypothesis.getDFA(true);
		Automaton dkPos = DFAOperations.toDkDFA(posDFA);
		Automaton inter = dkPos.intersection(autNonLen);
		String cexStr = inter.getShortestExample(true);
		if (cexStr != null) {
			return UtilSDFA.makeCex(alphabet, cexStr);
		}
		DFA negDFA = hypothesis.getDFA(false);
		Automaton dkNeg = DFAOperations.toDkDFA(negDFA);
		inter = dkNeg.intersection(autNonLen);
		cexStr = inter.getShortestExample(true);
		if (cexStr != null) {
			return UtilSDFA.makeCex(alphabet, cexStr);
		}
		Automaton comp = dkPos.complement();
		comp.intersection(autLen);
		return null;
	}

}
