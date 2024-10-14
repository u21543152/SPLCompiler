package com.splcompiler.lexer;

public record Token(int id, TokenType type, String value) {

    public String toXML() {
        return "<TOK>\n"
                + "<ID>" + id + "</ID>\n"
                + "<CLASS>" + type + "</CLASS>\n"
                + "<WORD>" + value + "</WORD>\n"
                + "</TOK>\n";
    }
}
