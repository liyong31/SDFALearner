package main;

import java.io.FileNotFoundException;

import roll.automata.SDFA;
import roll.util.Timer;
import roll.words.Alphabet;

public class SDFALearner3 {
	
	public static void main(String[] args) throws FileNotFoundException {
		Timer timer = new Timer();
		if (args.length < 3) {
			System.out.println("Usage: sdfalearner3 num_colors length output_file");
			System.exit(0);
		}
		int numColors = Integer.parseInt(args[0]);
		int length = Integer.parseInt(args[1]);
		String outputFileName = args[2];
		
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

		final WordUnionOperations builder = new WordUnionOperations();
		while(gen.hasNext()) {
			gen.advance();
			num ++;
			String sample = gen.next();
			if (gen.isEven()) {
				numPos ++;
				builder.add(sample, WordType.ACCEPT);
			}else {
				builder.add(sample, WordType.REJECT);
				numNeg ++;
			}
			if (num % 200000 == 0) {
				System.out.println("Already generated " + num + " samples");
			}
		}
		
		SDFA sdfa = WordUnionOperations.build(builder, numColors);
		System.out.println("Output dfa");
	    UtilSDFA.outputSDFA(sdfa, outputFileName);
	    timer.stop();
		long eq = timer.getTimeElapsed();
		System.out.println("#Pos: " + numPos + " #Neg: " + numNeg);
		System.out.println("#3DFA: " + sdfa.getStateSize());
		System.out.println("Finished construction: " + eq / 1000.0 + " secs");
		
		
	}


}