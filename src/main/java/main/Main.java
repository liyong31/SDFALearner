package main;


public class Main {
	
	private static void printUsage() {
		System.out.println("SDFALearner: separated DFA learner");
		System.out.println("");
		System.out.print(
                "Usage: java -jar sdfalearner.jar <learn|generate> [options]\n\n");
		
		System.out.print(
                "-h            show this page\n");
		System.out.print(
                "-i <file>     input sample file\n");
		System.out.print(
                "-o <file>     SDFA output file\n");
		System.out.print(
                "-c            number of colors in generation mode\n");
		System.out.print(
                "-l            length of words in generation mode\n");
		System.out.print(
                "-t            learning with table structure (tree by default)\n");
		System.out.print(
                "-n            no length of words in sample file\n");
		System.exit(0);
	}
	
	class Options {
		String inputFile;
		String outputFile;
		int numColors;
		int length;
		boolean noLength;
		boolean isTable;
	}
	
	private static void parseOptions(String[] args) {
		if (args.length <= 0) {
			printUsage();
		}
		for(int i = 0; i < args.length; i ++) {
            if(args[i].compareTo("-h")==0) {
                printUsage();
            }
            
        }
	}
	
	public static void main(String[] args) {
		printUsage();
	}

}
