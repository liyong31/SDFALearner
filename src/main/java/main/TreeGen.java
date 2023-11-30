package main;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import roll.table.HashableValue;
import roll.table.HashableValueEnum;
import roll.util.Timer;
import roll.words.Alphabet;

public class TreeGen {
	
	private Node tree;
	private int numNodes;
	
	class Node {
		int id;
		TCharObjectMap<Node> children;
		HashableValue membership;
		
		public Node(int node) {
			this.children = new TCharObjectHashMap<Node>();
			this.id = node;
			this.membership = new HashableValueEnum(0);
		}
		
		public void setMembership(HashableValue mq) {
			this.membership = mq;
		}
		
		public Node getChild(char branch) {
			return children.get(branch);
		}
		
		public void addChild(char branch, Node child) {
			children.put(branch, child);
		}
	}
	
	public TreeGen(List<String> positives, List<String> negatives) {
		this.numNodes = 0;
		tree = new Node(this.numNodes ++);
		constructPrefixTree(positives, negatives);
	}
	
	private void constructPrefixTree(List<String> positives, List<String> negatives) {
		for (String w : positives) {
			Node node = searchNode(w);
			node.setMembership(new HashableValueEnum(1));
		}
		
		for (String w : negatives) {
			Node node = searchNode(w);
			node.setMembership(new HashableValueEnum(-1));
		}
	}

	public Node searchNode(String str) {
		if (str.isEmpty()) {
			return tree;
		}
//		System.out.println("size: " + str.length());
		int currIdx = 0;
		Node currNode = tree;
		while (currIdx < str.length()) {
			char letter = str.charAt(currIdx ++);
			Node child = currNode.getChild(letter);
			if (child == null) {
				child = new Node(this.numNodes ++);
				currNode.addChild(letter, child);
			}
			currNode = child;
		}
		return currNode;
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			throw new RuntimeException("Need input filenames");
		}
		String inputFileName = args[0];
		
		boolean newFormat = false;
		if (args.length >= 2) {
			newFormat = true;
		}

		Alphabet alphabet = new Alphabet();

		Timer timer = new Timer();
		timer.start();
		// now learn
		LinkedList<String> pos = new LinkedList<String>();
		LinkedList<String> neg = new LinkedList<String>();
		LinkedList<String> don = new LinkedList<String>();
		UtilSDFA.readSampleFile(inputFileName, newFormat, alphabet, pos, neg);
		timer.stop();
		long eq = timer.getTimeElapsed();
		
		timer.start();
		TreeGen tree = new TreeGen(pos, neg);
		timer.stop();
		long genTree = timer.getTimeElapsed();
		System.out.println("#Tree: " + tree.numNodes);
		System.out.println("#Pos: " + pos.size());
		System.out.println("#Neg: " + neg.size());
		System.out.println("Finished reading file: " + eq / 1000.0);
		System.out.println("Finished constructing tree: " + genTree / 1000.0);
		
	}

}
