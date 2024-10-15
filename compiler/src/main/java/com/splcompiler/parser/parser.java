package com.splcompiler.parser;

import com.splcompiler.lexer.Token;
import com.splcompiler.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class parser {
    private List<Token> tokens;
    private int current = -1;

    public parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Main entry point for parsing the program
    public ProgramNode parseProgram() {
        ProgramNode programNode = new ProgramNode();

        match("main");  // Expect 'main'
        while (!check("begin")){
            programNode.addGlobalVar(parseGlobalVar());
        }
        programNode.setAlgorithm(parseAlgorithm()); // Parse the algorithm
//        programNode.setFunctions(parseFunctions());  // Parse functions

        return programNode; // Return the full AST for the program
    }

    private ASTNode parseGlobalVar() {
        String varType = consume(TokenType.KEYWORD).value();
        String varName = consume(TokenType.VNAME).value();
        match(",");
        return new GlobalVar(varType, varName);
    }

    private AlgoNode parseAlgorithm() {
        match("begin");
        INSTRUCNode instructions = parseInstructions();  // Parse instructions
        match("end");
        return new AlgoNode(instructions); // Return the instruction node
    }

    private List<ASTNode> parseFunctions() {
        List<ASTNode> functionList = new ArrayList<>();

        while (check(TokenType.KEYWORD)&&(check("num")||check("void)"))) {
            functionList.add(parseFunction());
        }

        return functionList;
    }

    private ASTNode parseFunction() {
        HeaderNode header = parseHead();
        BodyNode body = parseBody();
        return new FunctionNode(header, body);
    }

    private BodyNode parseBody() {
        match("{");
        PrologNode prolog = new PrologNode();  // Parse the prolog
        List<locVarNode> locVars = parseLocVars(); // Parse local variables
        AlgoNode algo = parseAlgorithm();  // Parse the algorithm
        EpilogNode epilog = new EpilogNode();  // Parse the epilog
        SubFuncsNode subFuncs = parseSubFuncs();  // Parse sub-functions if any
        match("}");
        return new BodyNode(prolog, locVars, algo, epilog, subFuncs);
    }

    private List<locVarNode> parseLocVars() {
        List<locVarNode> locVars = new ArrayList<>();
        for (int i = 0; i < 3; i++) {  // Each function has 3 local variables
            String type = consume(TokenType.KEYWORD).value();
            String varName = consume(TokenType.VNAME).value();
            locVars.add(new locVarNode(type, varName));
            if (i < 2) {
                match(",");  // Expect ',' between variables
            }
        }
        return locVars;
    }

    private SubFuncsNode parseSubFuncs() {
        if (check("num") || check("void")) {
            return new SubFuncsNode(parseFunctions());
        }
        return new SubFuncsNode(new ArrayList<>());  // No sub-functions
    }


    private HeaderNode parseHead() {
        String type = consume(TokenType.KEYWORD).value();
        String name = consume(TokenType.FNAME).value();
        match("(");
        ASTNode variable1 = new VariableNode(consume(TokenType.VNAME).value());
        match(",");
        ASTNode variable2 = new VariableNode(consume(TokenType.VNAME).value());
        match(",");
        ASTNode variable3 = new VariableNode(consume(TokenType.VNAME).value());
        match(")");
        return new HeaderNode(type,name,variable1,variable2,variable3);
    }

    // Parsing instructions
    private INSTRUCNode parseInstructions() {
        INSTRUCNode instructionsNode = new INSTRUCNode();

        while (!check("end")) {  // Continue until 'end' is reached
            instructionsNode.addCommand(parseCommand());
            match(";");  // Expect ';'
        }

        return instructionsNode;
    }

    // Parsing a single command
    private ASTNode parseCommand() {
        if (matchIf("skip")) {
            return new SkipNode();  // Represents a no-op command
        } else if (matchIf("halt")) {
            return new HaltNode();  // Represents a halt command
        } else if (matchIf("print")) {
            return parsePrint();  // Handle print command
        } else if (check(TokenType.VNAME) && lookahead(1) != null && lookahead(1).value().equals(":=")) {
            return parseAssignment();  // Handle assignment command
        } else if (matchIf("if")) {
            return parseIfStatement();  // Handle if command
        } else if (check(TokenType.FNAME)) {
            return parseCall();
        }

        throw new RuntimeException("Expected a command at position " + current);
    }

    private Token lookahead(int num) {
        // Check if we are at the end of the token list
        if (isAtEnd()) {
            return null; // Or throw an exception, depending on your design
        }
        return tokens.get(current+num); // Return the current token
    }

    // Parsing a print command
    private PrintNode parsePrint() {
        ASTNode atomic = parseAtomic();  // Expect variable name
        return new PrintNode(atomic); // Create the print node
    }

    // Parsing assignment statement: VNAME = TERM
    private AssignmentNode parseAssignment() {
        String varName = consume(TokenType.VNAME).value();  // Expect variable name
        match(":=");  // Expect ':='
        ASTNode term = parseTerm();  // Parse the term on the right-hand side
        return new AssignmentNode(varName, term); // Return the AssignmentNode
    }


    // Parsing if-then-else statement
    private IfNode parseIfStatement() {
        ASTNode condition = parseCondition();  // Parse the condition
        match("then");
        AlgoNode thenBranch = parseAlgorithm();  // Parse the 'then' branch
        match("else");
        AlgoNode elseBranch = parseAlgorithm();  // Parse the 'else' branch
        return new IfNode(condition, thenBranch, elseBranch); // Create the if node
    }

    // Parsing a condition
    private ASTNode parseCondition() {
        if (lookahead(2).type() == TokenType.UNOP ||  lookahead(2).type() == TokenType.BINOP) {
            return new ConditionNode(parseCompCond());
        } else {
            return new ConditionNode(parseSimpleCond());
        }// Create the condition node
    }

    private SimpleNode parseSimpleCond() {
        return new SimpleNode(parseBinop());
    }

    private ASTNode parseCompCond() {
        if (check(TokenType.UNOP)) {
            return new CompNode(new UnopNode(advance().value(),parseSimpleCond()));
        }else if(check(TokenType.BINOP)) {
            return new CompNode(new BinopNode(advance().value(),parseSimpleCond(),parseSimpleCond()));
        }
        throw new RuntimeException("Expected a condition at position " + current);
    }

    // Parsing a term
    private ASTNode parseTerm() {
        if (check(TokenType.FNAME)) {
            return parseCall();  // Handle function call
        } else if (check(TokenType.UNOP)) {
            return parseUnop();  // Handle unary operation
        } else if (check(TokenType.BINOP)) {
            return parseBinop();  // Handle binary operation
        } else {
            return parseAtomic();  // Handle atomic value
        }
    }


    private BinopNode parseBinop() {
        String operator = advance().value();
        match("(");
        ASTNode arg1 = parseARG();
        match(",");
        ASTNode arg2 = parseARG();
        match(")");
        return new BinopNode(operator,arg1, arg2);
    }

    private ASTNode parseUnop() {
        String operator = advance().value();
        match("(");
        ASTNode arg = parseARG();
        match(")");
        return new UnopNode(operator,arg);
    }

    private ASTNode parseARG() {
        if (check(TokenType.UNOP)) {
            return parseUnop();
        }else if (check(TokenType.BINOP)) {
            return parseBinop();
        }else {
            return parseAtomic();
        }
    }

    private ASTNode parseCall() {
        FnameNode funName = new FnameNode(advance().value());
        List<AtomicNode> atoms = new ArrayList<>();
        match("(");
        atoms.add(parseAtomic());
        match(",");
        atoms.add(parseAtomic());
        match(",");
        atoms.add(parseAtomic());
        match(")");
        return new CallNode(funName,atoms);
    }

    private AtomicNode parseAtomic() {
        if (check(TokenType.NUMBER)) {
            return new AtomicNode(new NumberNode(Integer.parseInt(advance().value())));
        } else if (check(TokenType.TEXT)) {
            return new AtomicNode(new TextNode(advance().value()));
        } else if (check(TokenType.VNAME)) {
            return new AtomicNode(new VariableNode(advance().value()));
        }
        throw new RuntimeException("Expected an atomic at position " + current);
    }

    // Match helper methods as before...
    private Token consume(TokenType type) {
        if (check(type)) return advance();
        throw new RuntimeException("Expected " + type + " at position " + current);
    }

    private boolean match(String expected) {
        if (check(expected)) {
            advance();
            return true;
        }
        throw new RuntimeException("Expected '" + expected + "' at position " + current);
    }

    private boolean matchIf(String expected) {
        if (check(expected)) {
            advance();
            return true;
        }
        return false;
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean check(TokenType type) {
        System.out.println(tokens.get(current).type() + " " + type);
        return !isAtEnd() && tokens.get(current).type() == type;
    }

    private boolean check(String expected) {
        return !isAtEnd() && tokens.get(current).value().equals(expected);
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
}

