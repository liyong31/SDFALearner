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

	@Override
	protected HashableValue checkMembership(Query<HashableValue> query) {
		Word word = query.getQueriedWord();
		if (word.length() != length) {
			return new HashableValueEnum(0);
		}
		// then we check whether it is even or odd
		return UtilSDFA.decideMembership(word, numColors);
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
