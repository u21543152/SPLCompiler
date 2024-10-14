package com.splcompiler.lexer;

public class Token {

    private int id;
    private TokenType type;
    private String value;

    public Token(int id, TokenType type, String value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String toXML() {
        return "<TOK>\n"
                + "<ID>" + id + "</ID>\n"
                + "<CLASS>" + type + "</CLASS>\n"
                + "<WORD>" + value + "</WORD>\n"
                + "</TOK>\n";
    }
}
