package main;

public enum WordType {
	
	DONTCARE,
	REJECT,
	ACCEPT;
	
	@Override
	public String toString() {
		switch(this) {
		case DONTCARE:
			return "dontcare";
		case REJECT:
			return "reject";
		case ACCEPT:
			return "accept";
		default:
			throw new RuntimeException("unreachable states in WordType");
		}
		
	}
}
