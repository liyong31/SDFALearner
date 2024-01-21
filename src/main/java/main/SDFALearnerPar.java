package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import dk.brics.automaton.Automaton;
import roll.automata.DFA;
import roll.automata.operations.DFAOperations;
import roll.util.Timer;
import roll.words.Alphabet;



public class SDFALearnerPar {
    public static void main(String[] args) throws ExecutionException, FileNotFoundException {
    	Timer timer = new Timer();
		if (args.length < 3) {
			System.out.println("Usage: sdfalearnerpar num_colors length output_file");
			System.exit(0);
		}
		int numColors = Integer.parseInt(args[0]);
		int length = Integer.parseInt(args[1]);
		String outputFileName = args[2];
		
		Alphabet alphabet = new Alphabet();
		for (int i = 0; i < numColors; i ++) {
			alphabet.addLetter((char)i);
		}

		timer.start();
        try {
        	int cores = Runtime.getRuntime().availableProcessors();
            System.out.println("Number of cores: " + cores); //8 cores
        	ExecutorService service = Executors.newFixedThreadPool(2);
        	DataGenerate even = new DataGenerate(numColors, length, true);
        	DataGenerate odd = new DataGenerate(numColors, length, false); 
            Future<Automaton> evenRet = service.submit(even);
            Future<Automaton> oddRet = service.submit(odd);

            service.shutdown();
            try {
            	service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            System.out.println();
            
            timer.stop();
    		long eq = timer.getTimeElapsed();
    		System.out.println("#Pos: " + even.numSamples);
    		System.out.println("#Neg: " + odd.numSamples);

    		System.out.println("#Pos: " + evenRet.get().getStates().size());
    		System.out.println("#Neg: " + oddRet.get().getStates().size());
    		DFA dfaPos = DFAOperations.fromDkDFA(alphabet, evenRet.get());
    		DFA dfaNeg = DFAOperations.fromDkDFA(alphabet, oddRet.get());

    		UtilSDFA.outputSDFA(dfaPos, dfaNeg, outputFileName);
    		System.out.println("Finished construction: " + eq / 1000.0 + " secs");
           

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
