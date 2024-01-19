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
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.table.HashableValueEnum;
import roll.words.Alphabet;
import roll.words.Word;

public class TeacherSDFALen extends TeacherAbstract<SDFA> {
	private int numColors;
	private int length;
	private Alphabet alphabet;
	private Automaton autLen;
	private Automaton autNonLen;
	private DataEnumerator de;
	boolean hasLoop;
	Automaton dataPos ;
	Automaton dataNeg ;
	
	public long numPos;
	public long numNeg;

    public TeacherSDFALen(Options options, Alphabet alphabet
    		, int numColors, int length) {
		super(options);
		this.alphabet = new Alphabet();
		this.numColors = numColors;
		this.length = length;
		this.alphabet = alphabet;
//		for (int i = 0; i < numColors; i ++) {
//			this.alphabet.addLetter((char)i);
//		}
		autLen = new Automaton();
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
		dataPos = new Automaton();
		dataNeg = new Automaton();
		this.de = new DataEnumerator(numColors, length);
		final LocalStringUnionOperations builderPos = new LocalStringUnionOperations(); 
		final LocalStringUnionOperations builderNeg = new LocalStringUnionOperations(); 

		// now build two automata
		numPos = 0;
		numNeg = 0;
		while (de.hasNext()) {
			de.advance();
			String sample = de.next();
//			String sample = gen.next();
			if (de.isEven()) {
				builderPos.add(sample);
				numPos ++;
			}else {
				builderNeg.add(sample);
				numNeg ++;
			}
		}
		System.out.println("#pos: " + numPos + " #neg: " + numNeg);
		// now construct the automaton
		dk.brics.automaton.State posInit = LocalStringUnionOperations.build(builderPos);
		dataPos.setInitialState(posInit);
				
		dk.brics.automaton.State negInit = LocalStringUnionOperations.build(builderNeg);
		dataNeg.setInitialState(negInit);
				
		System.out.println("Postive DFA size: " + dataPos.getStates().size());
		System.out.println("Negative DFA size: " + dataNeg.getStates().size());
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
	
//	private String exaustiveCheck(Automaton dkPos, Automaton dkNeg) {
//		while (de.hasNext()) {
//			de.advance();
//			String sample = de.next();
////			System.out.println("Sample: " + sample.length() + " Str: "+ sample );
////			for (int i = 0; i < sample.length(); i ++) {
////				System.out.println((int)sample.charAt(i));
////			}
//			boolean isEven = de.isEven();
//			boolean acc = dkPos.run(sample);
////			System.out.println("acc: " + acc);
//			if (isEven != acc) {
//				return sample;
//			}
//			boolean rej = dkNeg.run(sample);
//			if ((!isEven) != rej) {
//				return sample;
//			}
//		}
//		return null;
//	}

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
		// hard part
		Word cex = alphabet.getEmptyWord();
		Query<HashableValue> ceQuery = null;
		cexStr = UtilSDFA.checkEquivalence(dataPos, dkPos);
		if (cexStr != null) {
			return UtilSDFA.makeCex(alphabet, cexStr);
		}
		cexStr = UtilSDFA.checkEquivalence(dataNeg, dkNeg);
		if (cexStr != null) {
			return UtilSDFA.makeCex(alphabet, cexStr);
		}
    	ceQuery = new QuerySimple<HashableValue>(cex);
        ceQuery.answerQuery(new HashableValueBoolean(true));
        return ceQuery;
	}

}
