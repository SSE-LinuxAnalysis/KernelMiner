package de.uni_hildesheim.sse.kernel_miner.util.parser;

import java.util.LinkedList;
import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.util.ExpressionFormatException;

/**
 * Parsers strings based on {@link Grammar}s.
 * 
 * @author Adam Krafczyk
 */
public class Parser<T> {
    
    private Grammar<T> grammar;
    
    /**
     * Creates a new parser for the given {@link Grammar}.
     * 
     * @param grammar The {@link Grammar} that describes the format to parse.
     */
    public Parser(Grammar<T> grammar) {
        this.grammar = grammar;
    }
    
    /**
     * Parses the given string based on the {@link Grammar} this parser was
     * created for.
     * 
     * @param expression The expression to parse.
     * @return The parsed expression, as created by the make* methods in the given {@link Grammar}.
     * 
     * @throws ExpressionFormatException If the supplied string is not a valid expression for the given {@link Grammar}.
     */
    public T parse(String expression) throws ExpressionFormatException  {
        return parse(lex(expression));
    }

    /**
     * Lexes the given expression, based on the {@link Grammar} this parser was created for.
     * 
     * @param expression The expression to lex.
     * @return A flat list of {@link Element} that represent the tokens in the expression.
     * 
     * @throws ExpressionFormatException If the expression contains characters not allowed by the {@link Grammar}.
     */
    private List<Element> lex(String expression) throws ExpressionFormatException {
        List<Element> result = new LinkedList<>();
        
        Identifier currentIdentifier = null;
        
        // iterate over the string; i is incremented based on which element was identified
        for (int i = 0; i < expression.length();) {
            String op = grammar.getOperator(expression, i);
            
            if (grammar.isWhitespaceChar(expression, i)) {
                // whitespaces are ignored
                i++;
                
            } else  if (grammar.isOpeningBracketChar(expression, i)) {
                currentIdentifier = null;
                result.add(new Bracket(false));
                i += 1;
                
            } else if (grammar.isClosingBracketChar(expression, i)) {
                currentIdentifier = null;
                result.add(new Bracket(true));
                i += 1;
                
            } else if (op != null) {
                currentIdentifier = null;
                result.add(new Operator(op));
                i += op.length();
                
            } else if (grammar.isIdentifierChar(expression, i)) {
                if (currentIdentifier == null) {
                    currentIdentifier = new Identifier();
                    result.add(currentIdentifier);
                }
                currentIdentifier.name.append(expression.charAt(i));
                i++;
                
            } else {
                throw new ExpressionFormatException("Invalid character in expression: " + expression.charAt(i));
                
            }
        }
        
        return result;
    }
    
    /**
     * Parses the flat list of elements that the lexer found, based on the {@link Grammar}
     * this parser was created for.
     * 
     * @param elements The flat list of elements; the output of {@link #lex(String)}.
     * @return The parsed expression.
     * 
     * @throws ExpressionFormatException If the expression denoted by elements is malformed.
     */
    private T parse(List<Element> elements) throws ExpressionFormatException {
        // this method calls itself recursively

        // if we have  no elements left then something went wrong
        if (elements.size() == 0) {
            throw new ExpressionFormatException("Expected identifier");
        }
        
        // if we only have one element left, then it must be an identifier
        if (elements.size() == 1) {
            if (!(elements.get(0) instanceof Identifier)) {
                throw new ExpressionFormatException("Expected identifier, got " + elements.get(0));
            }
            
            return grammar.makeIdentifierFormula(((Identifier) elements.get(0)).name.toString());
        }
        
        // find the "highest" operator in the bracket tree
        
        int highestOperatorLevel = -1;
        int highestOpPos = -1;
        String highestOp = null;
        
        int bracketDepth = 0;
        
        for (int i = 0; i < elements.size(); i++) {
            Element e = elements.get(i);
            if (e instanceof Bracket) {
                if (((Bracket) e).closing) {
                    bracketDepth--;
                } else {
                    bracketDepth++;
                }
                
                if (bracketDepth < 0) {
                    throw new ExpressionFormatException("Unbalanced brackets");
                }
                
            } else if (e instanceof Operator) {
                Operator op = (Operator) e;
                
                // if ...
                if (
                        highestOperatorLevel == -1 // .. we haven't found any operator yet
                        || bracketDepth < highestOperatorLevel // ... the current operator is "higher" in the bracket structure
                        || (
                                highestOperatorLevel == bracketDepth
                                && !highestOp.equals(op.op)
                                && grammar.hasHigherPrecendece(op.op, highestOp)
                        ) // ... the current operator has the same level as the previously found one, but
                          // it has a higher precedence
                ) {
                    highestOpPos = i;
                    highestOp = op.op;
                    highestOperatorLevel = bracketDepth;
                }
            }
        }
        
        if (bracketDepth != 0) {
            throw new ExpressionFormatException("Unbalanced brackets");
        }
        
        T result = null;

        // if there is an operator that is not nested in any brackets
        if (highestOperatorLevel == 0) {
            // recursively parse the nested parts based on whether the operator is binary or not
            // and pass the results to the grammer to create the result
            
            if (grammar.isBinary(highestOp)) {
                List<Element> leftElements = new LinkedList<>();
                List<Element> rightElements = new LinkedList<>();
                for (int i = 0; i < elements.size(); i++) {
                    if (i < highestOpPos) {
                        leftElements.add(elements.get(i));
                    } else if (i > highestOpPos) {
                        rightElements.add(elements.get(i));
                    }
                }
                
                T leftTree = parse(leftElements);
                T rightTree = parse(rightElements);
                result = grammar.makeBinaryFormula(highestOp, leftTree, rightTree);
                
            } else {
                if (highestOpPos != 0) {
                    throw new ExpressionFormatException("Unary operator is not on the left");
                }
                
                List<Element> childElements = new LinkedList<>();
                for (int i = 1; i < elements.size(); i++) {
                    childElements.add(elements.get(i));
                }
                T childFormula = parse(childElements);
                result = grammar.makeUnaryFormula(highestOp, childFormula);
                
            }
        } else {
            // unpack the brackets and recursively call parse()
            
            if (!(elements.get(0) instanceof Bracket) || !(elements.get(elements.size() - 1) instanceof Bracket)) {
                throw new ExpressionFormatException("Couldn't find operator");
            }
            
            Bracket first = (Bracket) elements.get(0);
            Bracket last = (Bracket) elements.get(elements.size() - 1);
            
            if (first.closing || !last.closing) {
                throw new ExpressionFormatException("Unbalanced brackets");
            }
            
            List<Element> innerElements = new LinkedList<>();
            for (int i = 1; i < elements.size() - 1; i++) {
                innerElements.add(elements.get(i));
            }
            
            result = parse(innerElements);
        }
        
        return result;
    }
    
    /**
     * A token identified by the lexer. One of the three child classes:
     * <ul>
     *      <li>{@link Bracket}</li>
     *      <li>{@link Operator}</li>
     *      <li>{@link Identifier}</li>
     * </ul>
     */
    private abstract static class Element {
    }
    
    private static final class Operator extends Element {
        private String op;
        
        public Operator(String op) {
            this.op = op;
        }
        
        @Override
        public String toString() {
            return "[Operator: " + op + "]";
        }
    }
    
    private static final class Identifier extends Element {
        private StringBuffer name = new StringBuffer();
        
        @Override
        public String toString() {
            return "[Identifier: " + name + "]";
        }
    }
    
    private static final class Bracket extends Element {
        private boolean closing;
        
        public Bracket(boolean closing) {
            this.closing = closing;
        }
        
        
        @Override
        public String toString() {
            return "[Bracket: " + (closing ? "closing" : "open") + "]";
        }
    }
    
}
