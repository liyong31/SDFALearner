package main;

public class IntPair {
	
	public int left;
	public int right;
	
	IntPair(int lft, int rgt) {
		this.left = lft;
		this.right = rgt;
	}
	
	public IntPair copy() {
		return new IntPair(left, right);
	}
	
	@Override
	public String toString() {
		return "(" + left + ", " + right + ")";
	}
	
	

}
