package main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;

import roll.util.Timer;
import roll.words.Alphabet;
import roll.words.Word;

public class DataGen {
	
	int numColors;
	int length;
	LinkedList<Pair[]> positionsStack;
	Alphabet alphabet;
	
	public DataGen(int numColors, int length) {
		this.numColors = numColors;
		this.length = length;
		this.positionsStack = new LinkedList<Pair[]>();
		this.alphabet = new Alphabet();
		for (int i = 0; i < numColors; i ++) {
			this.alphabet.addLetter((char)i);
		}
	}
	
	public void generate() throws FileNotFoundException {

		int[] word = new int[length];
		int[] currColor = new int[length];
		Arrays.fill(currColor, (char)0);
		int currIdx = 0;
		// if there are both even and odd loops, we do not 
		// need to generate the word
		int[] masks = new int[length];
		Arrays.fill(masks, 0);
		Pair[] defaultPositions = new Pair[numColors];
		Arrays.fill(defaultPositions, new Pair(-1, -1));
		LinkedList<Word> pos = new LinkedList<>();
		LinkedList<Word> neg = new LinkedList<>();
		int numPos = 0;
		int numNeg = 0;
//		positionsStack.addLast(positions);
		int numSamples = 0;
    	PrintWriter writer = new PrintWriter("data" + numColors + "-" + length + ".txt");
    	writer.print(numSamples + " " + numColors + "\n");
    	while(currIdx >= 0) {
			// new letter
			// going forward
			if (currColor[currIdx] < numColors) {
				
				word[currIdx] = currColor[currIdx];
				// add a letter and check whether the current 
				// prefix has both even and odd loops
				Pair[] currPos = positionsStack.isEmpty() ?
						defaultPositions : positionsStack.getLast();
				int currMask = this.addLetter(currColor[currIdx]
						, currIdx, currPos, word);
//				System.out.println("size pairs: " + positionsStack.size());
//				System.out.println("currMask: " + currMask);

				if (currIdx > 0) {
					currMask |= masks[currIdx - 1];
					masks[currIdx] = currMask;
				}else {
					masks[currIdx] = 0;
				}
				
//				System.out.println("mask: " + masks[currIdx]);
				if (currMask == 3) {
					// trace back, no need to traverse
					// the remaining words
					currColor[currIdx] ++;
					// we need to remove the last pair
					positionsStack.pollLast();
					continue;
				}
				
				// if we only have odd/even loops so far,
				// explore forward
				currIdx ++;
				if (currIdx == length) {
					// we already finished one word
//					System.out.println("mask: " + currMask);
//					System.out.println("size: " + positionsStack.size());
//					System.out.println("word: " + alphabet.getArrayWord(word).toString());
//					int[] copy = Arrays.copyOf(word, length);
					if (masks[currIdx-1] == 2) {
						numPos ++;
						writer.print("1");
//						pos.add(alphabet.getArrayWord(copy));
					}else if (masks[currIdx-1] == 1){
						numNeg ++;
//						neg.add(alphabet.getArrayWord(copy));
						writer.print("0");
					}
					for (int i = 0; i < length; i ++) {
						writer.print(" " + word[i]);
					}
					writer.println();
					currIdx --;
					positionsStack.pollLast();
					currColor[currIdx] ++;
					numSamples ++;
				}else {
					// we still need to explore further
					currColor[currIdx] = 0;
				}
				
			}else {
				// pop curr idx
				// we already finished the search for current Idx
				// need to back track to the previous level
//				
				currIdx --;
//				System.out.println("Index: " + currIdx + ", #Pos: " + positionsStack.size());
				if (currIdx >= 0) {
					currColor[currIdx] ++;
					// need to clean positions for current index
					positionsStack.pollLast();
				}
			}
			// trace backward
		}
		
		
//		writer.println();
//		for (Word w: pos) {
//			writer.print("1");
//			for (int letter : w) {
//				writer.print(" " + letter);
//			}
//			writer.println();
//		}
//		for (Word w: neg) {
//			writer.print("0");
//			for (int letter : w) {
//				writer.print(" " + letter);
//			}
//			writer.println();
//		}
		writer.close();
		System.out.println("#Samples: " + numSamples);
		System.out.println("#Positives: " + numPos);
		System.out.println("#Negatives: " + numNeg);
		
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
		int numColors = 6;
		int length = 11;
		
		Timer timer = new Timer();
		DataGen gen = new DataGen(numColors, length);
		timer.start();
		gen.generate();
		timer.stop();
		System.out.println("#Time: " + (timer.getTimeElapsed()/1000.0));
	}

}
