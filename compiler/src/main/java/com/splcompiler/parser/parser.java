package com.splcompiler.parser;

import com.splcompiler.lexer.Token;
import com.splcompiler.lexer.TokenType;

import java.util.List;

public class parser {
    private final List<Token> tokens;
    private int current = 0;
    private int tcurrent = 0;

    public parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public rootNode parseProgram() {
        rootNode root = new rootNode(current++, "PROG");
        match("main");
        root.addChild(new leafNode(current++, "main", root));
        while (!check("begin")) {
            root.addChild(parseGlobalVar(root));
        }

        root.addChild(parseAlgo(root));
        if (!isAtEnd()) {
            root.addChild(parseFunctions(root));
        }

        return root;
    }

    private Node parseFunctions(Node parent) {
        innerNode node = new innerNode(current++, "FUNCTIONS", parent);
        node.addChild(parseDECL(node));
        if (!isAtEnd() && !check("end")) {
            node.addChild(parseFunctions(node));
        }
        return node;
    }

    private Node parseDECL(Node parent) {
        innerNode node = new innerNode(current++, "DECL", parent);
        node.addChild(parseHeader(node));
        node.addChild(parseBody(node));
        return node;
    }

    private Node parseBody(Node parent) {
        innerNode node = new innerNode(current++, "BODY", parent);
        match("{");
        node.addChild(parsePRO(node));
        node.addChild(parseLOCVARS(node));
        node.addChild(parseAlgo(node));
        node.addChild(parseEPI(node));
        match("}");
        if (!check("end")) {
            node.addChild(parseSUBFUNCS(node));
        }
        match("end");
        node.addChild(new leafNode(current++, "end", node));
        return node;
    }

    private Node parseSUBFUNCS(Node parent) {
        innerNode node = new innerNode(current++, "SUBFUNCS", parent);
        node.addChild(parseFunctions(node));

        return node;
    }

    private Node parseLOCVARS(Node parent) {
        innerNode node = new innerNode(current++, "LOCVARS", parent);
        node.addChild(parseLeaf(node, consume(TokenType.KEYWORD).value()));
        node.addChild(parseLeaf(node, consume(TokenType.VNAME).value()));
        match(",");
        node.addChild(parseLeaf(node, consume(TokenType.KEYWORD).value()));
        node.addChild(parseLeaf(node, consume(TokenType.VNAME).value()));
        match(",");
        node.addChild(parseLeaf(node, consume(TokenType.KEYWORD).value()));
        node.addChild(parseLeaf(node, consume(TokenType.VNAME).value()));
        match(",");
        return node;
    }

    private Node parsePRO(Node parent) {
        innerNode node = new innerNode(current++, "PROLOG", parent);
        node.addChild(new leafNode(current++, "{", node));
        return node;
    }

    private Node parseEPI(Node parent) {
        innerNode node = new innerNode(current++, "EPILOG", parent);
        node.addChild(new leafNode(current++, "}", node));
        return node;
    }

    private Node parseHeader(Node parent) {
        innerNode node = new innerNode(current++, "HEADER", parent);
        node.addChild(parseFTYPE(node));
        node.addChild(parseFName(node));
        match("(");
        node.addChild(new leafNode(current++, "(", node));
        node.addChild(parseVName(node));
        match(",");
        node.addChild(new leafNode(current++, ",", node));
        node.addChild(parseVName(node));
        match(",");
        node.addChild(new leafNode(current++, ",", node));
        node.addChild(parseVName(node));
        match(")");
        node.addChild(new leafNode(current++, ")", node));
        return node;
    }

    private Node parseAlgo(Node parent) {
        innerNode node = new innerNode(current++, "ALGO", parent);
        match("begin");
        node.addChild(new leafNode(current++, "begin", node));
        while (!check("end")) {
            node.addChild(parseInstruction(node));
        }
        match("end");
        node.addChild(new leafNode(current++, "end", node));
        return node;
    }

    private Node parseInstruction(Node parent) {
        innerNode node = new innerNode(current++, "INSTRUC", parent);
        node.addChild(parseCommand(node));
        match(";");
        node.addChild(new leafNode(current++, ";", node));
        while (!check("end")) {
            node.addChild(parseInstruction(node));
        }
        return node;
    }

    private Node parseCommand(Node parent) {
        innerNode node = new innerNode(current++, "COMMAND", parent);
        if (matchIf("skip")) {
            node.addChild(new leafNode(current++, "skip", parent));  // Represents a no-op command
        } else if (matchIf("halt")) {
            node.addChild(new leafNode(current++, "halt", parent));
        } else if (matchIf("print")) {
            node.addChild(new leafNode(current++, "print", parent));
            node.addChild(parseAtomic(node));
        } else if (matchIf("return")) {
            node.addChild(new leafNode(current++, "return", parent));
            node.addChild(parseAtomic(node));
        } else if (check(TokenType.VNAME)) {
            node.addChild(parseAssignment(node));  // Handle assignment command
        } else if (check("if")) {
            node.addChild(parseBranch(node));  // Handle if command
        } else if (check(TokenType.FNAME)) {
            node.addChild(parseCall(node));
        } else {
            throw new RuntimeException("Expected a command at position " + tcurrent);
        }
        return node;
    }

    private Node parseBranch(Node parent) {
        innerNode node = new innerNode(current++, "BRANCH", parent);
        match("if");
        node.addChild(new leafNode(current++, "if", node));
        node.addChild(parseCOND(node));
        match("then");
        node.addChild(new leafNode(current++, "then", node));
        node.addChild(parseAlgo(node));
        match("else");
        node.addChild(new leafNode(current++, "else", node));
        node.addChild(parseAlgo(node));
        return node;
    }

    private Node parseCOND(Node parent) {
        innerNode node = new innerNode(current++, "COND", parent);
        if (tokens.get(tcurrent + 2).type() == TokenType.BINOP || tokens.get(tcurrent + 2).type() == TokenType.UNOP) {
            node.addChild(parseCOMP(node));
        } else {
            node.addChild(parseSIMPLE(node));
        }
        return node;
    }

    private Node parseCOMP(Node parent) {
        innerNode node = new innerNode(current++, "COMPOSIT", parent);
        if (check(TokenType.BINOP)) {
            node.addChild(parseBINOP(node));
            match("(");
            node.addChild(new leafNode(current++, "(", node));
            node.addChild(parseSIMPLE(node));
            match(",");
            node.addChild(new leafNode(current++, ",", node));
        } else if (check(TokenType.UNOP)) {
            node.addChild(parseUNOP(node));
            match("(");
            node.addChild(new leafNode(current++, "(", node));
        }
        node.addChild(parseSIMPLE(node));
        match(")");
        node.addChild(new leafNode(current++, ")", node));
        return node;
    }

    private Node parseSIMPLE(Node parent) {
        innerNode node = new innerNode(current++, "SIMPLE", parent);
        node.addChild(parseBINOP(node));
        match("(");
        node.addChild(new leafNode(current++, "(", node));
        node.addChild(parseAtomic(node));
        match(",");
        node.addChild(new leafNode(current++, ",", node));
        node.addChild(parseAtomic(node));
        match(")");
        node.addChild(new leafNode(current++, ")", node));
        return node;
    }

    private Node parseAssignment(Node parent) {
        innerNode node = new innerNode(current++, "ASSIGN", parent);
        node.addChild(parseVName(node));
        if (matchIf("< input")) {
            node.addChild(new leafNode(current++, "< input", node));
        } else if (matchIf("=")) {
            node.addChild(new leafNode(current++, "=", node));
            node.addChild(parseTerm(node));
        }
        return node;
    }

    private Node parseTerm(Node parent) {
        innerNode node = new innerNode(current++, "TERM", parent);
        if (check(TokenType.FNAME)) {
            node.addChild(parseCall(node));  // Handle function call
        } else if (check(TokenType.UNOP)) {
            node.addChild(parseOP(node));  // Handle unary operation
        } else if (check(TokenType.BINOP)) {
            node.addChild(parseOP(node));  // Handle binary operation
        } else {
            node.addChild(parseAtomic(node));  // Handle atomic value
        }
        return node;
    }

    private Node parseOP(Node parent) {
        innerNode node = new innerNode(current++, "OP", parent);
        if (check(TokenType.BINOP)) {
            node.addChild(parseBINOP(node));
            match("(");
            node.addChild(new leafNode(current++, "(", node));
            node.addChild(parseArg(node));
            match(",");
            node.addChild(new leafNode(current++, ",", node));
        } else if (check(TokenType.UNOP)) {
            node.addChild(parseUNOP(node));
            match("(");
            node.addChild(new leafNode(current++, "(", node));
        }
        node.addChild(parseArg(node));
        match(")");
        node.addChild(new leafNode(current++, ")", node));
        return node;
    }

    private Node parseBINOP(Node parent) {
        innerNode node = new innerNode(current++, "BINOP", parent);
        node.addChild(new leafNode(current++, advance().value(), node));
        return node;
    }

    private Node parseUNOP(Node parent) {
        innerNode node = new innerNode(current++, "UNOP", parent);
        node.addChild(new leafNode(current++, advance().value(), node));
        return node;
    }

    private Node parseArg(Node parent) {
        innerNode node = new innerNode(current++, "ARG", parent);
        if (check(TokenType.UNOP)) {
            node.addChild(parseOP(node));  // Handle unary operation
        } else if (check(TokenType.BINOP)) {
            node.addChild(parseOP(node));  // Handle binary operation
        } else {
            node.addChild(parseAtomic(node));  // Handle atomic value
        }
        return node;
    }

    private Node parseAtomic(Node parent) {
        innerNode node = new innerNode(current++, "ATOMIC", parent);
        if (check(TokenType.VNAME)) {
            node.addChild(parseVName(node));
        } else {
            node.addChild(parseConst(node));
        }
        return node;
    }

    private Node parseConst(Node parent) {
        innerNode node = new innerNode(current++, "CONST", parent);
        String value = advance().value(); // Get the token value

        // If the value starts and ends with quotes, escape them
        if (value.startsWith("\"") && value.endsWith("\"")) {
            // You can escape the inner quotes
            value = value.substring(1, value.length() - 1); // Remove the quotes
            value = "\\\"" + value + "\\\""; // Add escaped quotes back
        }

        node.addChild(new leafNode(current++, value, node)); // Add the modified value to the leaf node
        return node;
    }


    private Node parseVName(Node parent) {
        innerNode node = new innerNode(current++, "VNAME", parent);
        node.addChild(new leafNode(current++, advance().value(), node));
        return node;
    }

    private Node parseFName(Node parent) {
        innerNode node = new innerNode(current++, "FNAME", parent);
        node.addChild(new leafNode(current++, advance().value(), node));
        return node;
    }

    private Node parseFTYPE(Node parent) {
        innerNode node = new innerNode(current++, "FTYP", parent);
        node.addChild(new leafNode(current++, advance().value(), node));
        return node;
    }

    private Node parseVTYPE(Node parent) {
        innerNode node = new innerNode(current++, "VTYP", parent);
        node.addChild(new leafNode(current++, advance().value(), node));
        return node;
    }

    private Node parseCall(Node parent) {
        innerNode node = new innerNode(current++, "CALL", parent);
        node.addChild(parseFName(node));
        match("(");
        node.addChild(new leafNode(current++, "(", node));
        node.addChild(parseAtomic(node));
        match(",");
        node.addChild(new leafNode(current++, ",", node));
        node.addChild(parseAtomic(node));
        match(",");
        node.addChild(new leafNode(current++, ",", node));
        node.addChild(parseAtomic(node));
        match(")");
        node.addChild(new leafNode(current++, ")", node));
        return node;
    }

    private Node parseGlobalVar(Node parent) {
        innerNode node = new innerNode(current++, "GlobVar", parent);
        node.addChild(parseVTYPE(node));
        node.addChild(parseVName(node));
        match(",");
        node.addChild(new leafNode(current++, ",", node));
        while (!check("begin")) {
            node.addChild(parseGlobalVar(node));
        }
        return node;
    }

    private Node parseLeaf(innerNode node, String value) {
        return new leafNode(current++, value, node);
    }

    // Match helper methods as before...
    private Token consume(TokenType type) {
        if (check(type)) return advance();
        throw new RuntimeException("Expected " + type + " at position " + tcurrent);
    }

    private boolean match(String expected) {
        if (check(expected)) {
            advance();
            return true;
        }
        throw new RuntimeException("Expected '" + expected + "' at position " + tcurrent + " Got: " + tokens.get(tcurrent).value());
    }

    private boolean matchIf(String expected) {
        if (check(expected)) {
            advance();
            return true;
        }
        return false;
    }

    private Token previous() {
        return tokens.get(tcurrent - 1);
    }

    private boolean check(TokenType type) {
        //System.out.println(tokens.get(tcurrent).type() + " " + type);
        return !isAtEnd() && tokens.get(tcurrent).type() == type;
    }

    private boolean check(String expected) {
        return !isAtEnd() && tokens.get(tcurrent).value().equals(expected);
    }

    private boolean isAtEnd() {
        return tcurrent >= tokens.size()-1;
    }

    private Token advance() {
        if (!isAtEnd()) {
            tcurrent++;
        }
        return previous();
    }


}
