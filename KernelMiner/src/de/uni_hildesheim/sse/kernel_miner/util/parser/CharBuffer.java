package de.uni_hildesheim.sse.kernel_miner.util.parser;

public class CharBuffer {

    private char[] content;
    
    private int pos;
    
    public CharBuffer(String content) {
        this.content = content.toCharArray();
        pos = 0;
    }
    
    public char getNextChar() {
        return content[pos++];
    }
    
    public char peekNextChar() {
        return content[pos];
    }
    
    public char peekPreviousChar() {
        return content[pos - 1];
    }

}
