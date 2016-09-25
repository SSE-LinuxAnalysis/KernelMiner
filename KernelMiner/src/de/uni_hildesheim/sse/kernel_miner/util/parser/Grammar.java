package de.uni_hildesheim.sse.kernel_miner.util.parser;

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
 * @see CStyleBooleanGrammar
 * 
 * @author Adam Krafczyk
 */
public abstract class Grammar<T> {

    /**
     * Checks whether <code>i</code> points to a valid operator in the string <code>str</null>.
     * 
     * @param str The string where the operator may be present.
     * @param i The pointer to the location in <code>str</null> where the operator may be.
     * @return The operator or <code>null</code> if <code>i</code> is not pointing to a valid operator.
     */
    public abstract Operator getOperator(char[] str, int i);
    
    /**
     * Checks whether <code>i</code> points to a whitespace character in the string <code>str</code>.
     * Whitespace characters are ignored in the parsing of expressions (i.e. they
     * don't result in new tokens), but they delimit identifiers.
     * 
     * @param str The string that may contain the whitespace character.
     * @param i The pointer to the location in <code>str</code> of the possible whitespace character.
     * @return <code>true</code>, if <code>str[i]</code> is a whitespace character; <code>false</code> otherwise.
     */
    public abstract boolean isWhitespaceChar(char[] str, int i);
    
    /**
     * Checks whether <code>i</code> points to an opening bracket in the string <code>str</code>.
     * 
     * @param str The string that may contain the opening bracket.
     * @param i The pointer to the location in <code>str</code> of the possible opening bracket.
     * @return <code>true</code>, if <code>str[i]</code> is an opening bracket; <code>false</code> otherwise.
     */
    public abstract boolean isOpeningBracketChar(char[] str, int i);
    
    /**
     * Checks whether <code>i</code> points to a closing bracket in the string <code>str</code>.
     * 
     * @param str The string that may contain the closing bracket.
     * @param i The pointer to the location in <code>str</code> of the possible closing bracket.
     * @return <code>true</code>, if <code>str[i]</code> is a closing bracket; <code>false</code> otherwise.
     */
    public abstract boolean isClosingBracketChar(char[] str, int i);
    
    /**
     * Checks whether <code>i</code> points to an identifier character in the string <code>str</code>.
     * 
     * @param str The string that may contain the identifier character.
     * @param i The pointer to the location in <code>str</code> of the possible identifier character.
     * @return <code>true</code>, if <code>str[i]</code> is an identifier character; <code>false</code> otherwise.
     */
    public abstract boolean isIdentifierChar(char[] str, int i);
    
    /**
     * Constructs a formula for the given unary operator.
     * This method is only called on operators, that were returned by {@link #getOperator(char[], int)}.
     * 
     * @param operator The unary operator.
     * @param child The formula that is "inside" the operator (i.e. it's child in the AST).
     * @return A formula representing the operator applied to the child.
     */
    public abstract T makeUnaryFormula(Operator operator, T child) throws ExpressionFormatException;
    
    
    /**
     * Constructs a formula for the given binary operator.
     * This method is only called on operators, that were returned by {@link #getOperator(char[], int)}.
     * 
     * @param operator The binary operator.
     * @param left The left side of the binary operation.
     * @param right The right side of the binary operation.
     * @return A formula representing the binary operation with the given left and right arguments.
     */
    public abstract T makeBinaryFormula(Operator operator, T left, T right) throws ExpressionFormatException;
    
    /**
     * Constructs a formula from the given identifier.
     * This usually returns a {@link Variable} object.
     * 
     * @param identifier The identifier to turn into a formula.
     * @return A formula representing the identifier.
     */
    public abstract T makeIdentifierFormula(String identifier) throws ExpressionFormatException;
    
}
