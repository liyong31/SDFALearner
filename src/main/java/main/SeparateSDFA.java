package main;

import java.io.IOException;

import dk.brics.automaton.Automaton;
import roll.automata.DFA;
import roll.automata.operations.DFAOperations;
import roll.util.Pair;

public class SeparateSDFA {
	
    public static void main(String[] args) throws IOException {

        String firstFileName = args[0];
        String secondFileName = args[1];

        Pair<DFA, DFA> first = UtilSDFA.readSDFAFile(firstFileName);
        DFA pos = first.getLeft();
        Automaton dkPos = DFAOperations.toDkDFA(pos);
        dkPos.minimize();
        dkPos.reduce();
        DFA dfaPos = DFAOperations.fromDkDFA(pos.getAlphabet(), dkPos);
        DFA neg = first.getRight();
        Automaton dkNeg = DFAOperations.toDkDFA(neg);
        dkNeg.minimize();
        dkNeg.reduce();
        DFA dfaNeg = DFAOperations.fromDkDFA(pos.getAlphabet(), dkNeg);

        UtilSDFA.outputSDFA(dfaPos, dfaNeg, secondFileName);
        System.out.println("#posDFA: " + dfaPos.getStateSize());
        System.out.println("#negDFA: " + dfaNeg.getStateSize());

    }

}
