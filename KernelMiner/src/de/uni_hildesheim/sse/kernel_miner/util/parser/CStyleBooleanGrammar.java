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

    @Override
    public String getOperator(String str, int i) {
        if (str.charAt(i) == '!') {
            return "!";
        }
        
        if (str.substring(i).startsWith("&&")) {
            return "&&";
        }
        
        if (str.substring(i).startsWith("||")) {
            return "||";
        }
        
        return null;
    }

    @Override
    public boolean isWhitespaceChar(String str, int i) {
        return str.charAt(i) == ' ';
    }

    @Override
    public boolean isOpeningBracketChar(String str, int i) {
        return str.charAt(i) == '(';
    }

    @Override
    public boolean isClosingBracketChar(String str, int i) {
        return str.charAt(i) == ')';
    }

    @Override
    public boolean isIdentifierChar(String str, int i) {
        return str.substring(i, i + 1).matches("[a-zA-Z0-9_]");
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
        return new Variable(identifier);
    }

}
