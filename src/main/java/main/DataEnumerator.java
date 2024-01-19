package main;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedList;

import dk.brics.automaton.Automaton;
import roll.automata.DFA;
import roll.automata.SDFA;
import roll.automata.operations.DFAOperations;
import roll.util.Timer;
import roll.words.Alphabet;

// the last word will repeat once, not sure why
public class DataEnumerator {
	
	int numColors;
	int length;
	Alphabet alphabet;
	
	// main data structures
	LinkedList<Pair[]> positionsStack;
	int currIdx;
	int[] masks;
	int[] currColor;
	Pair[] defaultPositions;
	int[] word;
	
	boolean isEven;
	String result = null;
	long numPos;
	long numNeg;
	
	public static int EVEN = 2;
	public static int ODD = 1;
	
	public DataEnumerator(int numColors, int length) {
		this.numColors = numColors;
		this.length = length;
		this.positionsStack = new LinkedList<Pair[]>();
		this.alphabet = new Alphabet();
		for (int i = 0; i < numColors; i ++) {
			this.alphabet.addLetter((char)i);
		}
		
		// initialisation
		currColor = new int[length];
		Arrays.fill(currColor, 0);
		// if there are both even and odd loops, we do not 
		// need to generate the word
		masks = new int[length];
		Arrays.fill(masks, 0);
		
		currIdx = 0;
		defaultPositions = new Pair[numColors];
		Arrays.fill(defaultPositions, new Pair(-1, -1));
		
		word = new int[length];
		this.numPos = 0;
		this.numNeg = 0;
	}
	
	public boolean hasNext() {
		// check current color
		boolean isLast = true;
		int maxColor = numColors - 1;
		for (int i = 0; i < length; i ++) {
			// strict less than
			if (word[i] < maxColor) {
				isLast = false;
				break;
			}
		}
		return !isLast;
	}
	
	public void advance() {
		
		while(true) {
			// new letter
			// going forward
			if (currColor[currIdx] < numColors) {
	
				word[currIdx] = currColor[currIdx];
				// add a letter and check whether the current
				// prefix has both even and odd loops
				Pair[] currPos = positionsStack.isEmpty() ? 
						defaultPositions
					: positionsStack.getLast();
				int currMask = this.addLetter(currColor[currIdx], currIdx, currPos, word);
				//				System.out.println("size pairs: " + positionsStack.size());
				//				System.out.println("currMask: " + currMask);
	
				if (currIdx > 0) {
					currMask |= masks[currIdx - 1];
					masks[currIdx] = currMask;
				} else {
					masks[currIdx] = 0;
				}
	
				//				System.out.println("mask: " + masks[currIdx]);
				if (currMask == 3) {
					// trace back, no need to traverse
					// the remaining words
					currColor[currIdx]++;
					// we need to remove the last pair
					positionsStack.pollLast();
					continue;
					// found one
				}
	
				// if we only have odd/even loops so far,
				// explore forward
				if (currIdx == length - 1) {
					// we already finished one word
					//					System.out.println("mask: " + currMask);
					//					System.out.println("size: " + positionsStack.size());
					//					System.out.println("word: " + alphabet.getArrayWord(word).toString());
					//					int[] copy = Arrays.copyOf(word, length);
					if (masks[currIdx] == 2) {
						isEven = true;
					}else if (masks[currIdx] == 1){
						isEven = false;
					}
					positionsStack.pollLast();
					currColor[currIdx]++;
					break;
				} else {
					currIdx ++;
					// we still need to explore further
					currColor[currIdx] = 0;
				}
	
			} else {
				// pop curr idx
				// we already finished the search for current Idx
				// need to back track to the previous level
				//				
				currIdx--;
				//				System.out.println("Index: " + currIdx + ", #Pos: " + positionsStack.size());
				if (currIdx >= 0) {
					currColor[currIdx]++;
					// need to clean positions for current index
					positionsStack.pollLast();
				}else {
					// we should get out of the loop now
					// all words have been generated
					break;
				}
			}
		}

	}
	
	public int[] nextArray() {
		return word;
	}
	

	public String next() {
		char[] res = new char[length];
		if (isEven) {
			numPos ++;
		}else {
			numNeg ++;
		}
//		System.out.print(isEven ? "1" : "0");
		for (int i = 0; i < length; i ++) {
			res[i] = (char)word[i];
//			System.out.print(" " + word[i]);
		}
//		System.out.println();
		return new String(res);
	}
	
	public boolean isEven() {
		return isEven;
	}

	
    public boolean isEvenLoop(int[] word, int left, int right) {
    	int max_color = -1;
        for (int j = left; j <= right; j ++) {
        	 max_color = Math.max(max_color, word[j]);
        }    
        if ((max_color & 1) == 0) {
        	return true;
        }else {
        	return false;
        }
    }
	
	private int addLetter(int color, int index, final Pair[] positions, int[]word) {
		
		Pair[] currPos = new Pair[this.numColors];
		for (int i = 0; i < this.numColors; i ++) {
			currPos[i] = positions[i].copy();
		}
		
		// new loop
		Pair pair = currPos[color];
        boolean hasLoop = false;
        if (pair.left == -1)
        	currPos[color].left = index;
        else if(pair.right == -1) {
        	currPos[color].right = index;
            hasLoop = true;
        } else {
        	currPos[color].left = currPos[color].right;
        	currPos[color].right = index;
            hasLoop = true;
        }
        
        // put this in the rear
        this.positionsStack.addLast(currPos);
        // check whether adding this letter will change the mask
        if (! hasLoop) {
        	return 0;
        }
        
        boolean isEven = isEvenLoop(word, pair.left, pair.right);
        
        if (isEven)
            return 2;
        else if (! isEven)
            return 1;
		return 0;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		int numColors = 5;
		int length = 11;
		
		Timer timer = new Timer();
		DataEnumerator gen = new DataEnumerator(numColors, length);
		timer.start();
		long num = 0;
//		final LocalStringUnionOperations builderPos = new LocalStringUnionOperations(); 
//		final LocalStringUnionOperations builderNeg = new LocalStringUnionOperations(); 
		final WordUnionOperations builder = new WordUnionOperations();
		while(gen.hasNext()) {
			gen.advance();
			num ++;
			String sample = gen.next();
			if (gen.isEven()) {
//				builderPos.add(sample);
				builder.add(sample, WordType.ACCEPT);
			}else {
//				builderNeg.add(sample);
				builder.add(sample, WordType.REJECT);
			}
		}
		// now construct the automaton
//		dk.brics.automaton.State posInit = LocalStringUnionOperations.build(builderPos);
//		pos.setInitialState(posInit);
		
		
//		dk.brics.automaton.State negInit = LocalStringUnionOperations.build(builderNeg);
//		neg.setInitialState(negInit);
		
		SDFA sdfa = WordUnionOperations.build(builder, numColors);
		
//		System.out.println(pos.getStates().size());
//		System.out.println(neg.getStates().size());
		
		Alphabet alphabet = new Alphabet();
		for (int i = 0; i < numColors; i ++) {
			alphabet.addLetter((char)i);
		}
//		DFA dfa = DFAOperations.fromDkDFA(alphabet, neg);
//		System.out.println("Negatives:\n" + dfa.toString());
		
//		dfa = DFAOperations.fromDkDFA(alphabet, pos);
//		System.out.println("Positives:\n" + dfa.toString());
		
		System.out.println("SDFA:\n" + sdfa.toString());
		
		timer.stop();
		System.out.println("#Time: " + (timer.getTimeElapsed()/1000.0) + " #samples = " + num);
		System.out.println("#Pos: " + gen.numPos + " #Neg: " + gen.numNeg);

	}

}
