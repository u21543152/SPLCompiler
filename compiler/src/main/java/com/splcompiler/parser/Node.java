package com.splcompiler.parser;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    public int Unid;
    public String Symbol;
    public List<Node> children;
    // Constructor for Node class
    public Node(int Unid, String Symbol) {
        this.Unid = Unid;
        this.Symbol = Symbol;
        this.children = new ArrayList<Node>();
    }
    
    public String toString()
    {
    	return Unid+" / "+Symbol+" / "+children;
    }
}

