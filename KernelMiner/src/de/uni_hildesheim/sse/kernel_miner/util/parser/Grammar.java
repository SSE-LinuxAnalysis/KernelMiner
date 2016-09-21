package de.uni_hildesheim.sse.kernel_miner.util.parser;

import de.uni_hildesheim.sse.kernel_miner.util.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;

/**
 * Describes a format for expressions to be parsed by a {@link Parser}.
 * 
 * <p>
 * The grammar consists of identifiers, operators and brackets. Operators can be
 * either unary or binary. Unary operators are always on the left side of the
 * expression they are applied to. Binary operators are always in between the
 * two expressions they are applied to.
 * </p>
 * 
 * @see SimpleCStyleBooleanGrammar
 * 
 * @author Adam Krafczyk
 */
public abstract class Grammar<T> {

    /**
     * Checks whether <code>i</code> points to a valid operator in the string <code>str</null>.
     * 
     * @param str The string where the operator may be present.
     * @param i The pointer to the location in <code>str</null> where the operator may be.
     * @return The operator as a string (e.g. "&&") if a valid operator; <code>null</code> otherwise.
     */
    public abstract String getOperator(String str, int i);
    
    /**
     * Checks whether <code>i</code> points to a whitespace character in the string <code>str</code>.
     * Whitespace characters are ignored in the parsing of expressions.
     * 
     * @param str The string that may contain the whitespace character.
     * @param i The pointer to the location in <code>str</code> of the possible whitespace character.
     * @return <code>true</code>, if <code>str.charAt(i)</code> is a whitespace character; <code>false</code> otherwise.
     */
    public abstract boolean isWhitespaceChar(String str, int i);
    
    /**
     * Checks whether <code>i</code> points to an opening bracket in the string <code>str</code>.
     * 
     * @param str The string that may contain the opening bracket.
     * @param i The pointer to the location in <code>str</code> of the possible opening bracket.
     * @return <code>true</code>, if <code>str.charAt(i)</code> is an opening bracket; <code>false</code> otherwise.
     */
    public abstract boolean isOpeningBracketChar(String str, int i);
    
    /**
     * Checks whether <code>i</code> points to a closing bracket in the string <code>str</code>.
     * 
     * @param str The string that may contain the closing bracket.
     * @param i The pointer to the location in <code>str</code> of the possible closing bracket.
     * @return <code>true</code>, if <code>str.charAt(i)</code> is a closing bracket; <code>false</code> otherwise.
     */
    public abstract boolean isClosingBracketChar(String str, int i);
    
    /**
     * Checks whether <code>i</code> points to an identifier character in the string <code>str</code>.
     * 
     * @param str The string that may contain the identifier character.
     * @param i The pointer to the location in <code>str</code> of the possible identifier character.
     * @return <code>true</code>, if <code>str.charAt(i)</code> is an identifier character; <code>false</code> otherwise.
     */
    public abstract boolean isIdentifierChar(String str, int i);
    
    /**
     * Checks whether the operator <code>toCheck</code> has a higher precedence than <code>comparedTo</code>.
     * This method is only called on operator Strings, that were returned by {@link #getOperator(String, int)}.
     * This method is only called with different <code>toCheck</code> and <code>comparedTo</code>, i.e.
     * <code>toCheck.equals(comparedTo)</code> is always <code>false</code> for the arguments of this method.
     * 
     * @param toCheck The operator that may have a higher precedence than <code>comparedTo</code>
     * @param comparedTo The operator that <code>toCheck</code> is compared against.
     * @return <code>true</code> if <code>toCheck</code> has a higher operator precedence than <code>comparedTo</code>;
     *      <code>false</code> otherwise.
     */
    public abstract boolean hasHigherPrecendece(String toCheck, String comparedTo);
    
    /**
     * Checks whether the given operator is unary or binary.
     * 
     * @param operator The operator to check.
     * @return <code>true</code> if the operator is binary; <code>false</code> if the operator is unary.
     */
    public abstract boolean isBinary(String operator);
    
    /**
     * Constructs a {@link Formula} for the given unary operator.
     * This method is only called on operator Strings, that were returned by {@link #getOperator(String, int)}.
     * 
     * @param operator The unary operator.
     * @param child The {@link Formula} that is "inside" the operator (i.e. it's child in the AST).
     * @return A {@link Formula} representing the operator applied to the child.
     */
    public abstract T makeUnaryFormula(String operator, T child) throws ExpressionFormatException;
    
    
    /**
     * Constructs a {@link Formula} for the given binary operator.
     * This method is only called on operator Strings, that were returned by {@link #getOperator(String, int)}.
     * 
     * @param operator The binary operator.
     * @param left The left side of the binary operation.
     * @param right The right side of the binary operation.
     * @return A {@link Formula} representing the binary operation with the given left and right arguments.
     */
    public abstract T makeBinaryFormula(String operator, T left, T right) throws ExpressionFormatException;
    
    /**
     * Constructs a {@link Formula} from the given identifier.
     * This usually returns a {@link Variable} object.
     * 
     * @param identifier The identifier to turn into a {@link Formula}
     * @return A {@link Formula} representing the identifier.
     */
    public abstract T makeIdentifierFormula(String identifier) throws ExpressionFormatException;
    
}
