package main;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import dk.brics.automaton.Automaton;

public class DataGenerate implements Callable<Automaton> {
	
	final int STEP = 50000000;
	
	int numColors;
	int length;
	LinkedList<IntPair[]> positionsStack;
	long numSamples;
	int counterMask;
	
	public DataGenerate(int numColors, int length, boolean isEven) {
		this.numColors = numColors;
		this.length = length;
		this.positionsStack = new LinkedList<IntPair[]>();
		this.counterMask = isEven ? 1 : 2;
	}
	
	boolean canIgnore(int currMask) {
//		System.out.println(currMask + " " + counterMask);
//		System.out.println("value? " + (currMask & counterMask) );
//		System.out.println("ignore? " + ((currMask & counterMask) > 0));
		if ((currMask & counterMask) > 0) return true;
		return false;
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
	
	private int addLetter(int color, int index, final IntPair[] positions, int[]word) {
		
		IntPair[] currPos = new IntPair[this.numColors];
		for (int i = 0; i < this.numColors; i ++) {
			currPos[i] = positions[i].copy();
		}
		
		// new loop
		IntPair pair = currPos[color];
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

	@Override
	public synchronized Automaton call() throws Exception {
		
		int[] word = new int[length];
		int[] currColor = new int[length];
		Arrays.fill(currColor, 0);
		int currIdx = 0;
		// if there are both even and odd loops, we do not 
		// need to generate the word
		int[] masks = new int[length];
		Arrays.fill(masks, 0);
		IntPair[] defaultPositions = new IntPair[numColors];
		Arrays.fill(defaultPositions, new IntPair(-1, -1));

		final LocalStringUnionOperations builder = new LocalStringUnionOperations(); 

    	while(currIdx >= 0) {
    		if (numSamples % STEP == 0) {
    			System.out.println("Already generated " + numSamples + " samples");
    		}
			// new letter
			// going forward
			if (currColor[currIdx] < numColors) {
				
				word[currIdx] = currColor[currIdx];
				// add a letter and check whether the current 
				// prefix has both even and odd loops
				IntPair[] currPos = positionsStack.isEmpty() ?
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
//				System.out.println("currMask: " + currMask);

				if (canIgnore(currMask)) {
					// trace back, no need to traverse
					// the remaining words
					currColor[currIdx] ++;
					// we need to remove the last pair
					positionsStack.pollLast();
					continue;
				}
				
				// if we only have odd/even loops so far,
				// explore forward
//				System.out.println("currIdx = "+ currIdx + " length=" + length);
				if (currIdx >= length - 1) {
					// we already finished one word
					char[] res = new char[length];
//					System.out.print(" " + ((counterMask & 1) == 1) );
					for (int i = 0; i < length; i ++) {
						res[i] = (char)word[i];
//						System.out.print(" " + word[i]);
					}
//					System.out.println();
					builder.add(new String(res));
					positionsStack.pollLast();
					currColor[currIdx] ++;
					numSamples ++;
				}else {
					currIdx ++;
					// we still need to explore further
					currColor[currIdx] = 0;
				}
				
			}else {	
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
    	Automaton aut = new Automaton();
    	dk.brics.automaton.State init = LocalStringUnionOperations.build(builder);
		aut.setInitialState(init);
		return aut;
	}

}
