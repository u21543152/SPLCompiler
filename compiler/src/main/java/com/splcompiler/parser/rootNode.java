package com.splcompiler.parser;

import java.util.ArrayList;
import java.util.List;

public class rootNode extends Node {  // Make this class public

    // Constructor for rootNode class
    public rootNode(int Unid, String Symbol) {
        super(Unid, Symbol); // Call to superclass constructor
    }

    public void addChild(Node node) {
        children.add(node);
    }

    public String toXML() {
        StringBuilder Kids = new StringBuilder();
        for (Node kid : children) {
            Kids.append("\t\t<ID>").append(kid.Unid).append("</ID>\n");
        }
        return "<ROOT>\n"
                + "\t<UNID>" + Unid + "</UNID>\n"
                + "\t<SYMB>" + Symbol + "</SYMB>\n"
                + "\t<CHILDREN>\n" + Kids + "\t</CHILDREN>\n"
                + "</ROOT>\n";
    }
}
