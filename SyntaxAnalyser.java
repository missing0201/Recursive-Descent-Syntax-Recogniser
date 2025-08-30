import java.io.*;
import java.util.HashMap;

public class SyntaxAnalyser extends AbstractSyntaxAnalyser {
    
    public String file;
    private HashMap<String, Integer> recentCall = new HashMap<>();

    public SyntaxAnalyser(String fileString) throws IOException{
        lex = new LexicalAnalyser(fileString);
        file = fileString.substring(16);
    }

    @Override
    public void _statementPart_() throws IOException, CompilationException {
        //Main method for the start of the program
        myGenerate.commenceNonterminal("StatementPart");
        acceptTerminal(Token.beginSymbol);
    
        // Skip to the StatementList method
        StatementList();
    
        // Ensure the program ends with 'end'
        acceptTerminal(Token.endSymbol);
        
        myGenerate.finishNonterminal("StatementPart");

    }

    //Statement List Parsing
    private void StatementList() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("StatementList");
        
        // Skip to the Statement method
        statement();

        //parse recursion if loop for statements separated by semicolon
        if (nextToken.symbol == Token.semicolonSymbol) {
            acceptTerminal(Token.semicolonSymbol); 
            StatementList(); // Parse the next statement
        }
    
        myGenerate.finishNonterminal("StatementList");
    }

    //Statement Parsing
    private void statement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Statement");
        switch (nextToken.symbol) {
            case Token.identifier -> {updateStatementLine("Statement");  assignmentStatement();}
            case Token.ifSymbol -> {updateStatementLine("Statement");    ifStatement();}
            case Token.whileSymbol -> {updateStatementLine("Statement"); whileStatement();}
            case Token.callSymbol -> {updateStatementLine("Statement");  procedureStatement();}
            case Token.untilSymbol -> {updateStatementLine("Statement"); untilStatement();}
            case Token.forSymbol -> {updateStatementLine("Statement");   forStatement();}
            default -> myGenerate.reportError(nextToken," Error in Statement (line"+recentCall.get("Statement")+")["+file+"]");
        }

        myGenerate.finishNonterminal("Statement");
    }
    //All below are the Corresponding grammar rules
    
    //assignment statement grammar rule
    private void assignmentStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("AssignmentStatement");
        try{
            updateLine("AssignmentStatement");
            acceptTerminal(Token.identifier);
            acceptTerminal(Token.becomesSymbol);
            if(nextToken.symbol == Token.stringConstant){      
                acceptTerminal(Token.stringConstant);
            }else{
                expression();
            }
        }catch(CompilationException e){
            reportInternalError(nextToken,e,"Error in assignment statement (line "+recentCall.get("AssignmentStatement")+")["+file+"]");
        }
        myGenerate.finishNonterminal("AssignmentStatement");
    }

    //if statement grammar rule
    private void ifStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("IfStatement");
        try {
            updateLine("IfStatement");
            acceptTerminal(Token.ifSymbol);
            condition();
            acceptTerminal(Token.thenSymbol);
            StatementList();
            if(nextToken.symbol == Token.elseSymbol){
                acceptTerminal(Token.elseSymbol);
                StatementList();
                acceptTerminal(Token.endSymbol);
                acceptTerminal(Token.ifSymbol);
                myGenerate.finishNonterminal("IfStatement");
            }else{
                acceptTerminal(Token.endSymbol);
                acceptTerminal(Token.ifSymbol);
                myGenerate.finishNonterminal("IfStatement");
            }
        } catch (CompilationException e) {
            reportInternalError(nextToken,e,"Error in if statement (line "+recentCall.get("IfStatement")+")["+file+"]");
        }
          
    }

    //while statement grammar rule
    private void whileStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("WhileStatement");
        try{
            updateLine("WhileStatement");
            acceptTerminal(Token.whileSymbol);
            condition();
            acceptTerminal(Token.loopSymbol);
            StatementList();
            acceptTerminal(Token.endSymbol);
            acceptTerminal(Token.loopSymbol);
        }catch (CompilationException e) {
            reportInternalError(nextToken,e,"Error in while statement (line "+recentCall.get("WhileStatement")+")["+file+"]");
        }

        myGenerate.finishNonterminal("WhileStatement");
    }

    //procedure statement grammar rule
    private void procedureStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("ProcedureStatement");
        try{
            updateLine("ProcedureStatement");
            acceptTerminal(Token.callSymbol);
            acceptTerminal(Token.identifier);
            acceptTerminal(Token.leftParenthesis);
            argumentList();
            acceptTerminal(Token.rightParenthesis);
        }catch (CompilationException e) {
            reportInternalError(nextToken,e,"Error in procedure statement (line "+recentCall.get("ProcedureStatement")+")["+file+"]");
        }
        myGenerate.finishNonterminal("ProcedureStatement");
    }

    //until statement grammar rule
    private void untilStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("UntilStatement");
        acceptTerminal(Token.doSymbol);
        StatementList();
        acceptTerminal(Token.untilSymbol);
        condition();
        myGenerate.finishNonterminal("UntilStatement");
    }

    //for statement grammar rule
    private void forStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("ForStatement");
        try{
            updateLine("ForStatement");
            acceptTerminal(Token.forSymbol);
            acceptTerminal(Token.leftParenthesis);
            assignmentStatement();
            acceptTerminal(Token.semicolonSymbol);
            condition();
            acceptTerminal(Token.semicolonSymbol);
            assignmentStatement();
            acceptTerminal(Token.rightParenthesis);
            acceptTerminal(Token.doSymbol);
            StatementList();
            acceptTerminal(Token.endSymbol);
            acceptTerminal(Token.loopSymbol);
        }catch (CompilationException e) {
            reportInternalError(nextToken,e,"Error in for statement (line "+recentCall.get("ForStatement")+")["+file+"]");
        }
        myGenerate.finishNonterminal("ForStatement");
    }

    //argument list grammar rule
    private void argumentList() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("ArgumentList");
        try{
            updateLine("ArgumentList");
            acceptTerminal(Token.identifier);

            if (nextToken.symbol == Token.commaSymbol) {
                acceptTerminal(Token.commaSymbol);
                argumentList();
            }
        }catch (CompilationException e) {
            reportInternalError(nextToken,e,"Error in argument list (line "+recentCall.get("ArgumentList")+")["+file+"]");
        }
        myGenerate.finishNonterminal("ArgumentList");
    }

    //condition grammar rule
    private void condition() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Condition");
        try{
            updateLine("Condition");
            acceptTerminal(Token.identifier);
            conditionalOperator();
            switch (nextToken.symbol) {
                case Token.identifier-> acceptTerminal(Token.identifier);
                case Token.numberConstant->acceptTerminal(Token.numberConstant);
                case Token.stringConstant -> acceptTerminal(Token.stringConstant);
                default -> throw new CompilationException("Unexpected token in condition: " + Token.getName(nextToken.symbol));
            }
        }catch (CompilationException e) {
            reportInternalError(nextToken,e,"Error in condition (line "+recentCall.get("Condition")+")["+file+"]");
        }
        myGenerate.finishNonterminal("Condition");
        
    }

    //conditional operator grammar rule
    private void conditionalOperator() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("ConditionalOperator");
        try{
            updateLine("conditionalOperator");
            switch (nextToken.symbol) {
                case Token.greaterThanSymbol-> acceptTerminal(Token.greaterThanSymbol);
                case Token.greaterEqualSymbol-> acceptTerminal(Token.greaterEqualSymbol);
                case Token.equalSymbol-> acceptTerminal(Token.equalSymbol);
                case Token.notEqualSymbol-> acceptTerminal(Token.notEqualSymbol);
                case Token.lessThanSymbol-> acceptTerminal(Token.lessThanSymbol);
                case Token.lessEqualSymbol -> acceptTerminal(Token.lessEqualSymbol);
                default -> throw new CompilationException("Unexpected token in conditional operator: " + Token.getName(nextToken.symbol));
            }
        }catch (CompilationException e) {
            
            reportInternalError(nextToken,e,"Error in conditional operator (line "+recentCall.get("conditionalOperator")+")["+file+"]");
        }
        myGenerate.finishNonterminal("ConditionalOperator");
    }

    //expression grammar rule
    private void expression() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Expression");
        try{
            updateLine("Expression");
            term();

            if (nextToken.symbol == Token.plusSymbol || nextToken.symbol == Token.minusSymbol) {
                if (nextToken.symbol == Token.plusSymbol) {
                    acceptTerminal(Token.plusSymbol);
                } else if (nextToken.symbol == Token.minusSymbol) {
                    acceptTerminal(Token.minusSymbol);
                } else {
                    throw new CompilationException("Expected '+' or '-' token, but found: " + nextToken);
                }
                expression();
            }
        }catch (CompilationException e) {
            reportInternalError(nextToken,e,"Expected '+' or '-' token (line "+recentCall.get("Expression")+")["+file+"]");
        }    

        myGenerate.finishNonterminal("Expression");
    }

    //term grammar rule
    private void term() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Term");
        try{
            updateLine("Term");
            factor();

            if (nextToken.symbol == Token.timesSymbol || nextToken.symbol == Token.divideSymbol || nextToken.symbol == Token.modSymbol) {
                if (nextToken.symbol == Token.timesSymbol) {
                    acceptTerminal(Token.timesSymbol);
                } else if (nextToken.symbol == Token.divideSymbol) {
                    acceptTerminal(Token.divideSymbol);
                } else if (nextToken.symbol == Token.modSymbol) {
                    acceptTerminal(Token.modSymbol);
                } else {
                    throw new CompilationException("Expected '*' or '/' or '%' token, but found: " + nextToken);
                }
                term();
            }
        }catch (CompilationException e) {
            reportInternalError(nextToken,e,"Expected '*' or '/' or '%' token (line "+recentCall.get("Term")+")["+file+"]");
        }
        myGenerate.finishNonterminal("Term");
    }

    //factor grammar rule
    private void factor() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Factor");
        try{
            updateLine("Factor");
            if(nextToken.symbol == Token.identifier){
                acceptTerminal(Token.identifier);
            }else if(nextToken.symbol == Token.numberConstant){
                acceptTerminal(Token.numberConstant);
            }else if(nextToken.symbol == Token.leftParenthesis){
                acceptTerminal(Token.leftParenthesis);
                expression();
                acceptTerminal(Token.rightParenthesis);
            }
        }catch (CompilationException e) {
            reportInternalError(nextToken,e,"Error at Factor, expected identifier, number or left parenthesis (line "+recentCall.get("Factor")+")["+file+"]");
        }
        myGenerate.finishNonterminal("Factor");
    }
    

    @Override
    public void acceptTerminal(int symbol) throws IOException, CompilationException {
        //checks the current token
        if (nextToken.symbol == symbol) {
            myGenerate.insertTerminal(nextToken);
            nextToken = lex.getNextToken(); // Move to next token
        } else {
            //error outputter
            myGenerate.reportError(nextToken,"Expected " + Token.getName(symbol) + " but found " + nextToken+"["+file+"]");            
        }
    }

    public void reportInternalError(Token token,CompilationException e, String explanatoryMessage) throws CompilationException {
        //internal error reporter and exception passer
        String message="Error : " + explanatoryMessage;
        throw new CompilationException(message,e);
    }

    private void updateLine(String functionName) {
        //general Hashmap updater
        if (nextToken != null) {
            recentCall.put(functionName, nextToken.lineNumber);
        }
    }

    private void updateStatementLine(String functionName) {
        //special Assignment grammar Hashmap updater
        if (nextToken != null) {
            recentCall.put(functionName, nextToken.lineNumber+1);
        }
    }
}
