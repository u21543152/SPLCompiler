package parser;

import com.splcompiler.lexer.Token;
import com.splcompiler.parser.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParserTest {
    public static void main(String[] args) throws IOException {
        List<innerNode> innerNodes = new ArrayList<>();
        List<leafNode> leafNodes = new ArrayList<>();
// Step 1: Read tokens from the XML file
        String lexerOutputFile = "SPLCompiler/compiler/src/main/resources/output.xml";  // This is the XML file generated by the lexer
        List<Token> tokens = lexerXMLParser.readTokensFromXML(lexerOutputFile);

        // Step 2: Parse the tokens to create an AST
        parser parser = new parser(tokens);
        rootNode root = parser.parseProgram();
        System.out.println(root.toXML());
        for (Node child : root.children)
        {
            categorizeNodes(child,innerNodes,leafNodes);
        }

        writeTreeToXML("SPLCompiler/compiler/src/main/resources/syntax_tree.xml",root,innerNodes,leafNodes);
        System.out.println("syntax_tree.xml has been created!");

        // Step 2: Generate DOT file to visualize
        FileWriter writer = new FileWriter("SPLCompiler/compiler/src/main/resources/syntax_tree.dot");

        // Step 3: Write DOT format structure
        writer.write("digraph SyntaxTree {\n");
        generateDOT(root, writer);
        writer.write("}\n");

        writer.close();

        System.out.println("DOT file created! Visualize with Graphviz.");
    }

    private static void writeTreeToXML(String filePath, rootNode root, List<innerNode> innerNodes, List<leafNode> leafNodes) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(root.toXML());
            writer.write("<INNERNODES>\n");
            for (innerNode node: innerNodes) {
                writer.write(node.toXML());
            }
            writer.write("</INNERNODES>\n");
            writer.write("<LEAFNODES>\n");
            for (leafNode node: leafNodes) {
                writer.write(node.toXML());
            }
            writer.write("</LEAFNODES>\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Example method to generate the DOT structure
    public static void generateDOT(Node node, FileWriter writer) throws IOException {
        // Write the node's information
        writer.write(node.Unid + " [label=\"" + node.Symbol + "\"];\n");

        // Check if the node has children, write the edges, and recurse for each child
        if (node instanceof rootNode root) {
            for (Node child : root.children) {
                // Write the edge from parent to child
                writer.write(node.Unid + " -> " + child.Unid + ";\n");
                // Recursively process the child node
                generateDOT(child, writer);
            }
        } else if (node instanceof innerNode inner) {
            for (Node child : inner.children) {
                // Write the edge from parent to child
                writer.write(node.Unid + " -> " + child.Unid + ";\n");
                // Recursively process the child node
                generateDOT(child, writer);
            }
        }
    }

    public static void categorizeNodes(Node node, List<innerNode> innerNodes, List<leafNode> leafNodes) {
        // Check if the current node is an innerNode or a leafNode
        if (node instanceof innerNode inner) {
            // Add to the inner nodes list
            innerNodes.add((innerNode) node);

            // Recursively process its children
            for (Node child : inner.children) {
                categorizeNodes(child, innerNodes, leafNodes);
            }
        } else if (node instanceof leafNode leaf) {
            // Add to the leaf nodes list
            leafNodes.add((leafNode) node);
        }
    }




}