package de.uni_hildesheim.sse.kernel_miner.util.parser;

import de.uni_hildesheim.sse.kernel_miner.util.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Conjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Disjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;
import de.uni_hildesheim.sse.kernel_miner.util.parser.Grammar;

/**
 * A {@link Grammar} for C-style boolean expressions.
 * 
 * <p>
 * Examples:
 * <ul>
 *      <li><code>A && B</code></li>
 *      <li><code>AaA_bcd || D && !E</code></li>
 *      <li><code>((A && B) || !(C || B)) && !E</code></li>
 * </ul>
 * </p>
 * 
 * @author Adam Krafczyk
 */
public class CStyleBooleanGrammar extends Grammar<Formula> {

    private VariableCache cache;
    
    public CStyleBooleanGrammar() {
    }
    
    public CStyleBooleanGrammar(VariableCache cache) {
        this.cache = cache;
    }
    
    @Override
    public String getOperator(char[] str, int i) {
        if (str[i] == '!') {
            return "!";
        }
        
        if (str[i] == '&' && str[i + 1] == '&') {
            return "&&";
        }
        
        if (str[i] == '|' && str[i + 1] == '|') {
            return "||";
        }
        
        return null;
    }

    @Override
    public boolean isWhitespaceChar(char[] str, int i) {
        return str[i] == ' ';
    }

    @Override
    public boolean isOpeningBracketChar(char[] str, int i) {
        return str[i] == '(';
    }

    @Override
    public boolean isClosingBracketChar(char[] str, int i) {
        return str[i] == ')';
    }

    @Override
    public boolean isIdentifierChar(char[] str, int i) {
        return (str[i] >= 'a' && str[i] <= 'z')
                || (str[i] >= 'A' && str[i] <= 'Z')
                || (str[i] >= '0' && str[i] <= '9')
                || (str[i] == '_');
    }

    @Override
    public boolean hasHigherPrecendece(String toCheck, String comparedTo) {
        return comparedTo.equals("!");
    }

    @Override
    public boolean isBinary(String operator) {
        return operator.equals("&&") || operator.equals("||");
    }

    @Override
    public Formula makeUnaryFormula(String operator, Formula child) throws ExpressionFormatException {
        return new Negation(child);
    }

    @Override
    public Formula makeBinaryFormula(String operator, Formula left, Formula right) throws ExpressionFormatException {
        if (operator.equals("&&")) {
            return new Conjunction(left, right);
        } else {
            return new Disjunction(left, right);
        }
    }

    @Override
    public Formula makeIdentifierFormula(String identifier) throws ExpressionFormatException {
        if (this.cache != null) {
            return this.cache.getVariable(identifier);
        }
        return new Variable(identifier);
    }

}
