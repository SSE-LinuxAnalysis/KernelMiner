package de.uni_hildesheim.sse.kernel_miner.code;

import java.io.Serializable;

import de.fosd.typechef.LexerToken;
import de.uni_hildesheim.sse.kernel_miner.code.typechef.TypeChefPresenceConditionGrammar;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.parser.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_miner.util.parser.Parser;
import de.uni_hildesheim.sse.kernel_miner.util.parser.VariableCache;

public class CToken implements Serializable {
    
    private static final long serialVersionUID = -7830966867352639378L;

    private String text;
    
    private Formula pc;
    
    private int line;
    
    private int column;
    
    private String sourceName;
    
    public CToken(LexerToken token) throws ExpressionFormatException {
        this.text = token.getText();
        this.line = token.getLine();
        this.column = token.getColumn();
        this.sourceName = token.getSourceName();

        this.pc = new Parser<Formula>(new TypeChefPresenceConditionGrammar(new VariableCache())).parse(
                token.getFeature().toTextExpr());
    }
    
    /*
     * TODO:
     * 
     * isLanguageToken,
     * isEOF,
     * isKeywordOrIdentifier,
     * isNumberLiteral,
     * isStringLiteral,
     * isCharacterLiteral
     */
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public Formula getPc() {
        return pc;
    }
    
    public void setPc(Formula pc) {
        this.pc = pc;
    }
    
    public int getLine() {
        return line;
    }
    
    public void setLine(int line) {
        this.line = line;
    }
    
    public int getColumn() {
        return column;
    }
    
    public void setColumn(int column) {
        this.column = column;
    }
    
    public String getSourceName() {
        return sourceName;
    }
    
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
}
