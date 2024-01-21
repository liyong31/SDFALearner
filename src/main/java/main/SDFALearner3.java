package main;

import java.io.FileNotFoundException;

import dk.brics.automaton.Automaton;
import roll.automata.DFA;
import roll.automata.SDFA;
import roll.automata.operations.DFAOperations;
import roll.util.Timer;
import roll.words.Alphabet;

public class SDFALearner3 {
	
	private final static int STEP = 50000000;
	
	public static void main(String[] args) throws FileNotFoundException {
		Timer timer = new Timer();
		if (args.length < 3) {
			System.out.println("Usage: sdfalearner3 num_colors length output_file sep");
			System.exit(0);
		}
		int numColors = Integer.parseInt(args[0]);
		int length = Integer.parseInt(args[1]);
		String outputFileName = args[2];
		
		boolean sepDFA = args.length > 3;
		
		Alphabet alphabet = new Alphabet();
		for (int i = 0; i < numColors; i ++) {
			alphabet.addLetter((char)i);
		}
		
		long numPos = 0;
		long numNeg = 0;
		long num = 0;

		timer.start();
		
		DataEnumerator gen = new DataEnumerator(numColors, length);
		timer.start();
		System.out.println("Generating samples...");
		final LocalStringUnionOperations builderPos = new LocalStringUnionOperations(); 
		final LocalStringUnionOperations builderNeg = new LocalStringUnionOperations(); 
		final WordUnionOperations builder = new WordUnionOperations();
		while(gen.hasNext()) {
			gen.advance();
			num ++;
			String sample = gen.next();
			if (gen.isEven()) {
				numPos ++;
				if (!sepDFA) builder.add(sample, WordType.ACCEPT);
				else builderPos.add(sample);
			}else {
				if (!sepDFA) builder.add(sample, WordType.REJECT);
				else builderNeg.add(sample);
				numNeg ++;
			}
			if (num % STEP == 0) {
				System.out.println("Already generated " + num + " samples");
			}
		}
		System.out.println("Output dfa");

		if (!sepDFA) {
			SDFA sdfa = WordUnionOperations.build(builder, numColors);
			UtilSDFA.outputSDFA(sdfa, outputFileName);
			System.out.println("#3DFA: " + sdfa.getStateSize());
			System.out.println("#F: " + sdfa.getFinalSize() + " #R: " + sdfa.getRejectSize());
			
		}else {
			Automaton pos = new Automaton();
			Automaton neg = new Automaton();
			
			dk.brics.automaton.State posInit = LocalStringUnionOperations.build(builderPos);
			pos.setInitialState(posInit);
			
			
			dk.brics.automaton.State negInit = LocalStringUnionOperations.build(builderNeg);
			neg.setInitialState(negInit);
			DFA dfaPos = DFAOperations.fromDkDFA(alphabet, pos);
			DFA dfaNeg = DFAOperations.fromDkDFA(alphabet, neg);
			System.out.println("#PosDFA: " + pos.getStates().size());
			System.out.println("#NegDFA: " + neg.getStates().size());
			UtilSDFA.outputSDFA(dfaPos, dfaNeg, outputFileName);
		}
	    
	    timer.stop();
		long eq = timer.getTimeElapsed();
		System.out.println("#Pos: " + numPos + " #Neg: " + numNeg);
		

		System.out.println("Finished construction: " + eq / 1000.0 + " secs");
		
		
	}


}