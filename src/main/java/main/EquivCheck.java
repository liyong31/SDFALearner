package main;

import java.io.IOException;

import dk.brics.automaton.Automaton;
import roll.util.Pair;

public class EquivCheck {
    
    public static void main(String[] args) throws IOException {

        String firstFileName = args[0];
        String secondFileName = args[1];

        Pair<Automaton, Automaton> first = UtilSDFA.readSDFAFile(firstFileName);
        roll.util.Pair<Automaton, Automaton> second = UtilSDFA.readSDFAFile(secondFileName);
        String cex = UtilSDFA.checkEquivalence(first.getLeft(), second.getLeft());
        if (cex != null) {
            System.out.println("Positive not equivalent");
            System.exit(0);
        }

        cex = UtilSDFA.checkEquivalence(first.getRight(), second.getRight());
        if (cex != null) {
            System.out.println("Negative not equivalent");
            System.exit(0);
        }

        System.out.println("Equivalent");

    }
}
