package de.uni_hildesheim.sse.kernel_miner.code.typechef;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.fosd.typechef.LexerToken;
import de.fosd.typechef.VALexer;
import de.fosd.typechef.conditional.Conditional;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.lexer.LexerException;
import de.fosd.typechef.lexer.LexerFrontend;
import de.fosd.typechef.lexer.LexerFrontend.LexerError;
import de.fosd.typechef.lexer.LexerFrontend.LexerResult;
import de.fosd.typechef.lexer.LexerFrontend.LexerSuccess;
import de.fosd.typechef.lexer.options.ILexerOptions;
import de.fosd.typechef.options.FrontendOptionsWithConfigFiles;
import de.fosd.typechef.options.OptionException;
import de.uni_hildesheim.sse.kernel_miner.code.CToken;
import de.uni_hildesheim.sse.kernel_miner.util.parser.ExpressionFormatException;
import scala.Tuple2;

public class TypeChefRunner {
    
    private Socket socket;
    
    private ObjectOutputStream out;
    
    private ObjectInputStream in;
    
    private List<LexerToken> lexerTokens;
    
    private List<CToken> tokens;
    
    private List<LexerError> lexerErrors;
    
    private List<String> errors;
    
    public TypeChefRunner(int port) throws IOException {
        socket = new Socket("localhost", port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }
    
    public void run() throws Exception {
        try {
             ILexerOptions conf = readParameters();
             runTypeChef(conf);
             convertResult();
             sendResult();
        } finally {
            sendResult();
            close();
        }
    }
    
    @SuppressWarnings("unchecked")
    private ILexerOptions readParameters() throws ClassNotFoundException, IOException, OptionException {
        
        List<String> params = (List<String>) in.readObject();
        FrontendOptionsWithConfigFiles conf = new FrontendOptionsWithConfigFiles() {
            @Override
            public boolean isPrintLexingSuccess() {
                return false;
            }
        };
        conf.parseOptions(params.toArray(new String[0]));
        
        return conf;
    }
    
    private void runTypeChef(final ILexerOptions conf) throws LexerException, IOException {
        LexerFrontend lexer = new LexerFrontend();
        Conditional<LexerResult> result = lexer.run(new VALexer.LexerFactory() {
            @Override
            public VALexer create(FeatureModel model) {
                return new XtcPreprocessor(conf.getMacroFilter(), model);
            }
        }, conf, true);
        
        scala.collection.immutable.List<Tuple2<FeatureExpr, LexerResult>> list = result.toList();
        
        lexerErrors = new LinkedList<>();
        
        for (int i = 0; i < list.size(); i++) {
            Tuple2<FeatureExpr, LexerResult> t = list.apply(i);
            if (t._2 instanceof LexerSuccess) {
                lexerTokens = ((LexerSuccess) t._2).getTokens();
                
            } else if (t._2 instanceof LexerError) {
                LexerError err = (LexerError) t._2;
                lexerErrors.add(err);
                
            } else {
                System.err.println("Unexpected lexer output object: " + t._2);
            }
        }
    }
    
    private void convertResult() throws ExpressionFormatException {
        errors = new ArrayList<>(lexerErrors.size());
        for (LexerError error : lexerErrors) {
            errors.add(error.getPositionStr() + " " + error.getMessage());
        }
        
        tokens = new ArrayList<>(lexerTokens.size());
        for (LexerToken original : lexerTokens) {
            CToken converted = new CToken(original);
            tokens.add(converted);
        }
    }
    
    private void sendResult() {
        try {
            out.writeObject(tokens);
            out.writeObject(errors);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName("TypeChefRunner");
        int port = Integer.parseInt(args[0]);
        
        TypeChefRunner runner = new TypeChefRunner(port);
        runner.run();
    }

}
