package com.splcompiler.lexer;

public enum TokenType {
    VNAME, // Variable name (e.g., V_a123)
    FNAME, // Function name (e.g., F_sum)
    NUMBER, // Number (e.g., 0, -3.14, 42)
    TEXT, // String literal (e.g., "Hello", "World")
    KEYWORD, // Reserved keywords (e.g., main, num, text)
    OPERATOR, // Operators (e.g., +, -, :=, etc.)
    PUNCTUATION, // Punctuation (e.g., , ; { } ( ))
    EOF            // End of input
}
