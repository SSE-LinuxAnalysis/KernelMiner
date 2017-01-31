package de.uni_hildesheim.sse.kernel_miner.code.typechef;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.fosd.typechef.conditional.Opt;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.parser.c.CompoundStatement;
import de.fosd.typechef.parser.c.Declarator;
import de.fosd.typechef.parser.c.Expr;
import de.fosd.typechef.parser.c.ExprStatement;
import de.fosd.typechef.parser.c.ExternalDef;
import de.fosd.typechef.parser.c.FunctionDef;
import de.fosd.typechef.parser.c.TranslationUnit;
import de.uni_hildesheim.sse.kernel_miner.code.ast.ErrorExpression;
import de.uni_hildesheim.sse.kernel_miner.code.ast.ErrorStatement;
import de.uni_hildesheim.sse.kernel_miner.code.ast.Expression;
import de.uni_hildesheim.sse.kernel_miner.code.ast.ExpressionStatement;
import de.uni_hildesheim.sse.kernel_miner.code.ast.File;
import de.uni_hildesheim.sse.kernel_miner.code.ast.Function;
import de.uni_hildesheim.sse.kernel_miner.code.ast.ReturnStatement;
import de.uni_hildesheim.sse.kernel_miner.code.ast.Sequence;
import de.uni_hildesheim.sse.kernel_miner.code.ast.Statement;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;
import de.uni_hildesheim.sse.kernel_miner.util.parser.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_miner.util.parser.Parser;
import de.uni_hildesheim.sse.kernel_miner.util.parser.VariableCache;

public class AstConverter {
    
    private static final VariableCache CACHE = new VariableCache();
    
    private static final TypeChefPresenceConditionGrammar GRAMMAR = new TypeChefPresenceConditionGrammar(CACHE);
    
    private static final Parser<Formula> PARSER = new Parser<>(GRAMMAR);

    private TranslationUnit unit;
    
    private File result;
    
    private List<ConverterException> exceptions;
    
    public AstConverter(TranslationUnit unit) {
        this.unit = unit;
    }
    
    public File convertToFile() {
        exceptions = new ArrayList<>();
        
        String name = "<unkown>";
        
        if (unit.getFile().isDefined()) {
            name = unit.getFile().get();
        }
        
        result = new File(name);
        
        for (Opt<ExternalDef> def : scalaIterator(unit.defs())) {
            convertExternalDef(def.entry(), def.condition());
        }
        
        return result;
    }
    
    public List<ConverterException> getExceptions() {
        return exceptions;
    }
    
    private void convertExternalDef(ExternalDef def, FeatureExpr condition) {
        if (def instanceof FunctionDef) {
            FunctionDef funcDef = (FunctionDef) def;
            // TODO: funcDef.specifiers()
            // TODO: funcDef.getFile()
            // TODO: funcDef.oldStyleParameters()
            
            Declarator decl = funcDef.declarator();
            // TODO: decl.extentions()
            // TODO: decl.pointers()
            
            Function function = new Function(decl.getName(), toFormula(condition));
            
            List<Statement> body = function.getBody().statements();
            for (Opt<de.fosd.typechef.parser.c.Statement> stmt : scalaIterator(funcDef.stmt().innerStatements())) {
                body.add(convertStatement(stmt.entry(), toFormula(stmt.condition())));
            }
            
            result.functions().add(function);
            
        } else {
            exceptions.add(new ConverterException("Unkown case in convertExternalDef (" + def.getClass() + ")"));
        }
    }
    
    private Statement convertStatement(de.fosd.typechef.parser.c.Statement stmt, Formula condition) {
        if (stmt instanceof de.fosd.typechef.parser.c.ReturnStatement) {
            de.fosd.typechef.parser.c.ReturnStatement ret = (de.fosd.typechef.parser.c.ReturnStatement) stmt;
            
            ReturnStatement result = new ReturnStatement(condition);
            
            if (ret.expr().isDefined()) {
                result.setReturnValue(convertExpression(ret.expr().get()));
            }
            
            return result;
            
        } else if (stmt instanceof ExprStatement) {
            ExprStatement exprSt = (ExprStatement) stmt;
            return new ExpressionStatement(condition, convertExpression(exprSt.expr()));
            
        } else if (stmt instanceof CompoundStatement) {
            CompoundStatement cStmt = (CompoundStatement) stmt;
            Sequence result = new Sequence(condition);
            
            for (Opt<de.fosd.typechef.parser.c.Statement> elem : scalaIterator(cStmt.innerStatements())) {
                result.statements().add(convertStatement(elem.entry(), toFormula(elem.condition())));
            }
            
            return result;
            
        } else {
            return new ErrorStatement(condition, "Unkown case in convertStatement (" + stmt.getClass() + ")");
        }
    }
    

    private Expression convertExpression(Expr expr) {
        return new ErrorExpression("Unkown case in convertExpression (" + expr.getClass() + ")");
    }
    
    
//------------------------------------------------------------------------------------------------------------//
    
    private static Formula toFormula(FeatureExpr featureExpr) {
        try {
            Formula formula = PARSER.parse(featureExpr.toTextExpr());
            CACHE.clear();
            return formula;
        } catch (ExpressionFormatException e) {
            // TODO
            e.printStackTrace();
            return new True();
        }
    }
    
    private static <T> Iterable<T> scalaIterator(final scala.collection.immutable.List<T> a) {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {

                    private scala.collection.Iterator<T> it = a.iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public T next() {
                        return it.next();
                    }
                };
            }
        };
        
    }
}
