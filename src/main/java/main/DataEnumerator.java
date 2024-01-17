package main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedList;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.StringUnionOperations;
import main.LocalStringUnionOperations.State;
import roll.util.Timer;
import roll.words.Alphabet;
import roll.words.Word;

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
	int numPos;
	int numNeg;
	
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
		Arrays.fill(currColor, (char)0);
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
		return currIdx >= 0;
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
				currIdx++;
				if (currIdx == length) {
					// we already finished one word
					//					System.out.println("mask: " + currMask);
					//					System.out.println("size: " + positionsStack.size());
					//					System.out.println("word: " + alphabet.getArrayWord(word).toString());
					//					int[] copy = Arrays.copyOf(word, length);
					if (masks[currIdx-1] == 2) {
						isEven = true;
					}else if (masks[currIdx-1] == 1){
						isEven = false;
					}
					currIdx--;
					positionsStack.pollLast();
					currColor[currIdx]++;
					break;
				} else {
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
		int numColors = 3;
		int length = 4;
		
		Timer timer = new Timer();
		DataEnumerator gen = new DataEnumerator(numColors, length);
		timer.start();
		int num = 0;
		Automaton pos = new Automaton();
		Automaton neg = new Automaton();
		final LocalStringUnionOperations builderPos = new LocalStringUnionOperations(); 
		final LocalStringUnionOperations builderNeg = new LocalStringUnionOperations(); 

		while(gen.hasNext()) {
			gen.advance();
			num ++;
			String sample = gen.next();
			if (gen.isEven()) {
				builderPos.add(sample);
			}else {
				builderNeg.add(sample);
			}
		}
		// now construct the automaton
		dk.brics.automaton.State posInit = LocalStringUnionOperations.build(builderPos);
		pos.setInitialState(posInit);
		
		dk.brics.automaton.State negInit = LocalStringUnionOperations.build(builderNeg);
		neg.setInitialState(negInit);
		
		System.out.println(pos.getStates().size());
		System.out.println(neg.getStates().size());
		
		
		timer.stop();
		System.out.println("#Time: " + (timer.getTimeElapsed()/1000.0) + " #samples = " + num);
		System.out.println("#Pos: " + gen.numPos + " #Neg: " + gen.numNeg);

	}

}
