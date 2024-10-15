package com.splcompiler.parser;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class XMLSyntaxTreeWriter {

    private static int uniqueIdCounter = 1;

    // Method to write the AST to the XML syntax tree format
    public static void writeSyntaxTree(ASTNode rootNode, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("<SYNTREE>\n");

            // Step 1: Write the root node
            writer.write("<ROOT>\n");
            writeNode(rootNode, writer, null);
            writer.write("</ROOT>\n");

            // Step 2: Write inner nodes and leaf nodes
            List<ASTNode> innerNodes = new ArrayList<>();
            List<ASTNode> leafNodes = new ArrayList<>();
            gatherNodes(rootNode, innerNodes, leafNodes);

            writer.write("<INNERNODES>\n");
            for (ASTNode innerNode : innerNodes) {
                writeInnerNode(innerNode, writer);
            }
            writer.write("</INNERNODES>\n");

            writer.write("<LEAFNODES>\n");
            for (ASTNode leafNode : leafNodes) {
                writeLeafNode(leafNode, writer);
            }
            writer.write("</LEAFNODES>\n");

            writer.write("</SYNTREE>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeNode(ASTNode node, FileWriter writer, ASTNode parent) throws IOException {
        int nodeId = uniqueIdCounter++;

        if (parent == null) {  // This is the root node
            writer.write("<UNID>" + nodeId + "</UNID>\n");
            writer.write("<SYMB>program</SYMB>\n");  // Root symbol is usually the start symbol of the grammar
            writer.write("<CHILDREN>\n");

            for (ASTNode child : getChildren(node)) {
                writer.write("<ID>" + uniqueIdCounter + "</ID>\n");
            }
            writer.write("</CHILDREN>\n");
        }
    }

    private static void writeInnerNode(ASTNode node, FileWriter writer) throws IOException {
        int nodeId = uniqueIdCounter++;

        writer.write("<IN>\n");
        writer.write("<PARENT>" + getParentId(node) + "</PARENT>\n");
        writer.write("<UNID>" + nodeId + "</UNID>\n");
        writer.write("<SYMB>" + getNodeSymbol(node) + "</SYMB>\n");
        writer.write("<CHILDREN>\n");
        for (ASTNode child : getChildren(node)) {
            writer.write("<ID>" + uniqueIdCounter + "</ID>\n");
        }
        writer.write("</CHILDREN>\n");
        writer.write("</IN>\n");
    }

    private static void writeLeafNode(ASTNode node, FileWriter writer) throws IOException {
        int nodeId = uniqueIdCounter++;

        writer.write("<LEAF>\n");
        writer.write("<PARENT>" + getParentId(node) + "</PARENT>\n");
        writer.write("<UNID>" + nodeId + "</UNID>\n");
        writer.write("<TERMINAL>\n");
        // Copy the terminal symbol from the lexer (you need to implement this part)
        writer.write(getTokenFromNode(node));  // Implement this based on how you store the lexer tokens
        writer.write("</TERMINAL>\n");
        writer.write("</LEAF>\n");
    }

    // Helper function to gather inner nodes and leaf nodes
    private static void gatherNodes(ASTNode node, List<ASTNode> innerNodes, List<ASTNode> leafNodes) {
        if (isLeafNode(node)) {
            leafNodes.add(node);
        } else {
            innerNodes.add(node);
            for (ASTNode child : getChildren(node)) {
                gatherNodes(child, innerNodes, leafNodes);
            }
        }
    }

    // Helper functions to get children, parent, symbols, etc.
    private static List<ASTNode> getChildren(ASTNode node) {
        // Implement this based on how you define children in your ASTNode class
        return new ArrayList<>();  // Placeholder: return actual list of children
    }

    private static boolean isLeafNode(ASTNode node) {
        // Implement this to check if the node is a leaf (i.e., has no children)
        return false;  // Placeholder: implement actual check
    }

    private static String getNodeSymbol(ASTNode node) {
        // Implement this to get the grammar symbol associated with the node
        return "symbol";  // Placeholder: return the actual grammar symbol
    }

    private static int getParentId(ASTNode node) {
        // Implement this to get the parent's unique ID (if any)
        return 0;  // Placeholder: return the parent's unique ID
    }

    private static String getTokenFromNode(ASTNode node) {
        // Implement this to return the terminal symbol as it appeared in the lexer XML
        return "<TOKEN ...>";  // Placeholder: return the actual token data
    }
}
