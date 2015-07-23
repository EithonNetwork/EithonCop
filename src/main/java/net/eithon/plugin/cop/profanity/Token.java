package net.eithon.plugin.cop.profanity;

class Token {
	private String _in;
	private String _transformed;
	
	Token(String in, String transformed) {
		this._in = in;
		this._transformed = transformed;
	}
	
	String getIn() { return this._in; }
	String getTransformed() { return this._transformed; }
	
	boolean equalsIgnoreCase(String string) { return this._transformed.equalsIgnoreCase(string); }
	public int length() { return this._transformed.length(); }
	
	@Override
	public String toString() { return String.format("{%s}/{%s}", this._in, this._transformed); }
}
