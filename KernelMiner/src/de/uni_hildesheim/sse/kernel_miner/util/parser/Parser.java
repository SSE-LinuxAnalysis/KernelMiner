package de.uni_hildesheim.sse.kernel_miner.util.parser;

import java.util.LinkedList;
import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.util.ExpressionFormatException;

public class Parser<T> {
    
    private Grammar<T> grammar;
    
    public Parser(Grammar<T> grammar) {
        this.grammar = grammar;
    }
    
    public T parse(String expression) throws ExpressionFormatException  {
        return parse(lex(expression));
    }

    private List<Element> lex(String expression) throws ExpressionFormatException {
        List<Element> result = new LinkedList<>();
        
        Identifier currentIdentifier = null;
        
        for (int i = 0; i < expression.length();) {
            String op = grammar.getOperator(expression, i);
            
            if (grammar.isWhitespaceChar(expression, i)) {
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
    
    private T parse(List<Element> elements) throws ExpressionFormatException {
        if (elements.size() == 0) {
            throw new ExpressionFormatException("Expected identifier");
        }
        
        if (elements.size() == 1) {
            if (!(elements.get(0) instanceof Identifier)) {
                throw new ExpressionFormatException("Expected identifier, got " + elements.get(0));
            }
            
            return grammar.makeIdentifierFormula(((Identifier) elements.get(0)).name.toString());
        }
        
        int highestOperandLevel = -1;
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
                if (
                        highestOperandLevel == -1
                        || bracketDepth < highestOperandLevel
                        || (highestOperandLevel == bracketDepth && !highestOp.equals(op.op) && grammar.hasHigherPrecendece(op.op, highestOp))) {
                    highestOpPos = i;
                    highestOp = op.op;
                    highestOperandLevel = bracketDepth;
                }
            }
        }
        
        if (bracketDepth != 0) {
            throw new ExpressionFormatException("Unbalanced brackets");
        }
        
        T result = null;
        
        if (highestOperandLevel == 0) {
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
    
    private static class Element {
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
