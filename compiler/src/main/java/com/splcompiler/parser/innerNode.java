package com.splcompiler.parser;

import java.util.ArrayList;
import java.util.List;

public class innerNode extends Node {
    public Node parent;
    public List<Node> children = new ArrayList<>();

    // Constructor for innerNode class
    public innerNode(int Unid, String Symbol, Node parent) {
        super(Unid, Symbol); // Call to superclass constructor
        this.parent = parent;
    }

    public void addChild(Node node) {
        children.add(node);
    }

    public String toXML() {
        StringBuilder Kids = new StringBuilder();
        for (Node kid : children) {
            Kids.append("\t\t\t<ID>").append(kid.Unid).append("</ID>\n");
        }
        return "\t<IN>\n"
                + "\t\t<PARENT>" + parent.Unid + "</PARENT>\n"
                + "\t\t<UNID>" + Unid + "</UNID>\n"
                + "\t\t<SYMB>" + Symbol + "</SYMB>\n"
                + "\t\t<CHILDREN>\n" + Kids + "\t\t</CHILDREN>\n"
                + "\t</IN>\n";
    }
}
