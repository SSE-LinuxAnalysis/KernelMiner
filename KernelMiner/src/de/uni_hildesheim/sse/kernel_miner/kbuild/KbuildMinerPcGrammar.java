package de.uni_hildesheim.sse.kernel_miner.kbuild;

import de.uni_hildesheim.sse.kernel_miner.util.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;
import de.uni_hildesheim.sse.kernel_miner.util.parser.SimpleCStyleBooleanGrammar;

public class KbuildMinerPcGrammar extends SimpleCStyleBooleanGrammar {

    @Override
    public String getOperator(String str, int i) {
        if (str.charAt(i) == '!' && str.charAt(i + 1) != '=') {
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
    public boolean isIdentifierChar(String str, int i) {
        return str.substring(i, i + 1).matches("[a-zA-Z0-9_!=\"\\[\\]]");
    }
    
    @Override
    public Formula makeIdentifierFormula(String identifier) throws ExpressionFormatException {
        if (identifier.contains("[") || identifier.contains("]")) {
            if (identifier.equals("[TRUE]")) {
                return new True();
            } else {
                throw new ExpressionFormatException("Invalid identifier: " + identifier);
            }
        }

        if (identifier.contains("==")) {
            int equalPos = identifier.indexOf('=');
            String varName = identifier.substring(0, equalPos);
            
            if (identifier.substring(equalPos + 2).equals("\"y\"")
                    || identifier.substring(equalPos + 2).equals("\"yes\"")) {
                return new Variable("CONFIG_" + varName);
                
            } else if (identifier.substring(equalPos + 2).equals("\"m\"")) {
                return new Variable("CONFIG_" + varName + "_MODULE");
                
            } else {
                throw new ExpressionFormatException("Invalid identifier: " + identifier);
            }
            
        } else if (identifier.contains("!=")) {
            return new Negation(makeIdentifierFormula(identifier.replace('!', '=')));
            
        } else {
            if (!identifier.matches("[a-zA-Z0-9_]+")) {
                throw new ExpressionFormatException("Invalid identifier: " + identifier);
            }
            return new Variable(identifier);
            
        }
        
    }
    
}
