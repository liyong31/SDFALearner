package main;

import java.io.IOException;

import dk.brics.automaton.Automaton;
import roll.automata.DFA;
import roll.automata.operations.DFAOperations;
import roll.util.Pair;

public class EquivCheck {
    
    public static void main(String[] args) throws IOException {

        String firstFileName = args[0];
        String secondFileName = args[1];

        Pair<DFA, DFA> first = UtilSDFA.readSDFAFile(firstFileName);
        Automaton first1 = DFAOperations.toDkDFA(first.getLeft());
        Automaton first2 = DFAOperations.toDkDFA(first.getRight());
        Pair<DFA, DFA> second = UtilSDFA.readSDFAFile(secondFileName);
        Automaton second1 = DFAOperations.toDkDFA(second.getLeft());
        Automaton second2 = DFAOperations.toDkDFA(second.getRight());
        String cex = UtilSDFA.checkEquivalence(first1, second1);
        if (cex != null) {
            System.out.println("Positive not equivalent");
            System.exit(0);
        }

        cex = UtilSDFA.checkEquivalence(first2, second2);
        if (cex != null) {
            System.out.println("Negative not equivalent");
            System.exit(0);
        }

        System.out.println("Equivalent");

    }
}
