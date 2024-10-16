package com.splcompiler.parser;

public class leafNode extends Node {
    public Node parent;

    // Constructor for leafNode class
    public leafNode(int Unid, String Symbol, Node parent) {
        super(Unid, Symbol); // Call to superclass constructor
        this.parent = parent;
    }

    public String toXML() {
        return "\t<LEAF>\n"
                + "\t\t<PARENT>" + parent.Unid + "</PARENT>\n"
                + "\t\t<UNID>" + Unid + "</UNID>\n"
                + "\t\t<SYMB>" + Symbol + "</SYMB>\n"
                + "\t</LEAF>\n";
    }
}
