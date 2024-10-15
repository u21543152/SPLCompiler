package com.splcompiler.lexer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecSPLLexer {

    private final String input;
    private int pos = 0;
    private final int length;
    private final List<Token> tokens = new ArrayList<>();
    private int tokenId = 1;

    // Reserved keywords for RecSPL
    private static final Set<String> keywords = new HashSet<>(Arrays.asList(
            "main", "num", "text", "begin", "end", "skip", "halt", "print",
            "input", "if", "then", "else", "return", "while", "for", "do", "void"
    ));

    private static final Set<String> unops = new HashSet<>(Arrays.asList(
            "not", "sqrt"
    ));

    private static final Set<String> binops = new HashSet<>(Arrays.asList(
            "eq", "grt", "and", "or",
            "add", "sub", "mul", "div"
    ));
    // Regular expressions for token classes
    private static final Pattern VNAME_PATTERN = Pattern.compile("V_[a-z]([a-z]|[0-9])*");
    private static final Pattern FNAME_PATTERN = Pattern.compile("F_[a-z]([a-z]|[0-9])*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?(0|[1-9][0-9]*)(\\.[0-9]+)?");
    private static final Pattern TEXT_PATTERN = Pattern.compile("\"[A-Z][a-z]{0,7}\"");
    private static final Pattern OPERATOR_PATTERN = Pattern.compile(":=");
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[,;(){}]");

    public RecSPLLexer(String input) {
        this.input = input;
        this.length = input.length();
    }

    public List<Token> tokenize() {
        while (pos < length) {
            char current = input.charAt(pos);

            // Skip whitespace
            if (Character.isWhitespace(current)) {
                pos++;
                continue;
            }

            // Match variable names
            if (match(VNAME_PATTERN)) {
                tokens.add(new Token(tokenId++, TokenType.VNAME, extract(VNAME_PATTERN)));
                continue;
            }

            // Match function names
            if (match(FNAME_PATTERN)) {
                tokens.add(new Token(tokenId++, TokenType.FNAME, extract(FNAME_PATTERN)));
                continue;
            }

            // Match numbers
            if (match(NUMBER_PATTERN)) {
                tokens.add(new Token(tokenId++, TokenType.NUMBER, extract(NUMBER_PATTERN)));
                continue;
            }

            // Match text strings
            if (match(TEXT_PATTERN)) {
                tokens.add(new Token(tokenId++, TokenType.TEXT, extract(TEXT_PATTERN)));
                continue;
            }

            // Match keywords
            String word = extractWord();
            if (keywords.contains(word)) {
                tokens.add(new Token(tokenId++, TokenType.KEYWORD, word));
                continue;
            }

            if (unops.contains(word)) {
                tokens.add(new Token(tokenId++, TokenType.UNOP, word));
                continue;
            }

            if (binops.contains(word)) {
                tokens.add(new Token(tokenId++, TokenType.BINOP, word));
                continue;
            }

            // Match operators
            if (match(OPERATOR_PATTERN)) {
                tokens.add(new Token(tokenId++, TokenType.OPERATOR, extract(OPERATOR_PATTERN)));
                continue;
            }

            // Match punctuation
            if (match(PUNCTUATION_PATTERN)) {
                tokens.add(new Token(tokenId++, TokenType.PUNCTUATION, Character.toString(current)));
                pos++;
                continue;
            }

            // Handle lexical error (unknown token)
            throw new IllegalArgumentException("Lexical Error: Unrecognized token at position " + pos + ": " + current);
        }

        // End of input
        tokens.add(new Token(tokenId++, TokenType.EOF, ""));
        return tokens;
    }

    // Helper methods
    private boolean match(Pattern pattern) {
        Matcher matcher = pattern.matcher(input.substring(pos));
        return matcher.lookingAt();
    }

    private String extract(Pattern pattern) {
        Matcher matcher = pattern.matcher(input.substring(pos));
        if (matcher.lookingAt()) {
            String token = matcher.group();
            pos += token.length();
            return token;
        }
        return "";
    }

    private String extractWord() {
        StringBuilder sb = new StringBuilder();
        while (pos < length && Character.isLetterOrDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }
        return sb.toString();
    }

    // Write tokens to XML
    public void writeTokensToXML(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("<TOKENSTREAM>\n");
            for (Token token : tokens) {
                writer.write(token.toXML());
            }
            writer.write("</TOKENSTREAM>");
        }
    }
}
