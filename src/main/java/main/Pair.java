package main;

public class Pair {
	
	public int left;
	public int right;
	
	Pair(int lft, int rgt) {
		this.left = lft;
		this.right = rgt;
	}
	
	public Pair copy() {
		return new Pair(left, right);
	}
	
	@Override
	public String toString() {
		return "(" + left + ", " + right + ")";
	}
	
	

}
