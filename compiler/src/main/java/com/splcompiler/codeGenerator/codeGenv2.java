package com.splcompiler.codeGenerator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class codeGenv2 {

    private static final Map<String, String> symbolTable = new HashMap<>();

    static {
        // Populating the symbol table
        symbolTable.put("V_a", "varA");          // Variable V_a maps to varA
        symbolTable.put("V_b", "varB");          // Variable V_b maps to varB
        symbolTable.put("F_foo", "functionFoo"); // Function F_foo maps to functionFoo
        symbolTable.put("V_var1", "var1");       // Local variable in F_foo
        symbolTable.put("V_var2", "var2");       // Local variable in F_foo
        symbolTable.put("V_var3", "var3");       // Local variable in F_foo
        symbolTable.put("F_bar", "functionBar"); // Function F_bar maps to functionBar
        symbolTable.put("V_var4", "var4");       // Local variable in F_bar
        symbolTable.put("V_var5", "var5");       // Local variable in F_bar
        symbolTable.put("V_var6", "var6");       // Local variable in F_bar
    }
    public static void main(String[] args) {
        codeGenv2 codeGen = new codeGenv2();
        try {
            File inputFile = new File("SPLCompiler/compiler/src/main/resources/syntax_tree.xml");
            if (!inputFile.exists()) {
                System.out.println("File not found!");
                return;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            // Print out the document element to ensure it's not null
            Element rootElement = doc.getDocumentElement();
            if (rootElement != null) {
                System.out.println("Root element: " + rootElement.getNodeName());
            } else {
                System.out.println("Root element is null.");
            }

            // Translate the root node (starting from ROOT)
            String code = codeGen.translateNode("0", doc);
            System.out.println(code);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            System.out.println("SAXException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }


    }

    public String translateNode(String UNID, Document doc) {
        // Call the other method with a default value for tempVar
        return translateNode(UNID, doc, "");
    }

    public String translateNode(String UNID, Document doc, String tempVar) {
        Element node = findElementByUNID(doc, UNID);
        String symb = node.getElementsByTagName("SYMB").item(0).getTextContent();
        StringBuilder code = new StringBuilder();
        List<String> children = new ArrayList<>();
        String Code = "\n";
        int id;
        switch (symb) {
            case "PROG":
                children = getChildrenUNIDS(node);
//                System.out.println(children.toString());
//                int id = 2;
//                if (children.size() == 4 || ) {
//                    id++;
//                }
//                System.out.println(children.get(id));
//                code.append(translateNode(children.get(id++), doc));//ALGO
                for (String s : children) {
                    code.append(translateNode(s, doc));//FUNC
                }
                break;
            case "ALGO":
                children = getChildrenUNIDS(node);
                code.append(translateNode(children.get(1), doc)).append("\nSTOP\n");//INSTRUC
                break;
            case "INSTRUC":
                children = getChildrenUNIDS(node);
                if (children.isEmpty()) {
                    code.append("REM END");
                }
                id = 0;
                Code += (translateNode(children.get(id++), doc));//COMMAND
                if (children.size() == 3) {
                    Code += (translateNode(children.get(++id), doc));//INSTRUC
                }
                return Code;
            case "COMMAND":
                children = getChildrenUNIDS(node);
                Code += translateNode(children.get(0), doc);//Single child commands
                if (children.size() == 2) {
                    Code += " " + (translateNode(children.get(1), doc));//COMMAND
                }
                return Code;
            case "skip":
                code.append("REM DO NOTHING");
                break;
            case "halt":
                code.append("STOP");
                break;
            case "print":
                code.append("PRINT");
                break;
            case "ATOMIC":
                children = getChildrenUNIDS(node);
                return translateNode(children.get(0),doc);
            case "VNAME":
                String child = getChildrenUNIDS(node).get(0);
                String originalVName = findElementByUNID(doc,child).getElementsByTagName("SYMB").item(0).getTextContent();
                return symbolTable.getOrDefault(originalVName, originalVName);
            case "CONST":
                return findElementByUNID(doc,getChildrenUNIDS(node).get(0)).getElementsByTagName("SYMB").item(0).getTextContent();
            case "ASSIGN":
                children = getChildrenUNIDS(node);
                if (children.size() == 3) {
                    if (tempVar == "") {
                        tempVar = this.newTempVar();
                    }
                    Code += tempVar + " := " + translateNode(children.get(2),doc) + "\n";
                    tempVar = this.newTempVar();
                    Code += translateNode(children.get(0),doc) + " := " + tempVar;
                    return Code;
                }else {

                    return translateNode(children.get(1),doc) + translateNode(children.get(0),doc);
                }
            case "TERM":
                children = getChildrenUNIDS(node);
                if(findElementByUNID(doc,children.get(0)).getElementsByTagName("SYMB").item(0).getTextContent()!="OP"){
                    tempCounter--;
                }
                return translateNode(children.get(0),doc);
            case "< input":
                return "INPUT ";
            case "CALL":
                //TODO
                break;
            case "OP":
                children = getChildrenUNIDS(node);
                if(children.size()==4) {
                    return translateUNOP(doc, children);
                }else if(children.size()==6) {
                    return translateBINOP(doc, children);
                }
            case "ARG":
                children = getChildrenUNIDS(node);
                return translateNode(children.get(0),doc);
            case "BRANCH":

            default:
                System.out.println(symb);
                return "";
        }
        return code.toString();
    }

    private String translateBINOP(Document doc, List<String> children) {
        String place1 = newTempVar();
        String place2 = newTempVar();
        String place = newTempVar();
        String code1 = translateNode(children.get(2),doc);
        String code2 = translateNode(children.get(4),doc);
        List<String> child = getChildrenUNIDS(findElementByUNID(doc,children.get(0)));
        String op = findElementByUNID(doc,child.get(0)).getElementsByTagName("SYMB").item(0).getTextContent();

        op = switch (op) {
            case "eq" -> "=";
            case "grt" -> ">";
            case "add" -> "+";
            case "sub" -> "-";
            case "mul" -> "*";
            case "div" -> "/";
            default -> throw new IllegalArgumentException("Unknown binary operation: " + op);
        };
        tempCounter--;
        return code1 + "\n" + place2 + " := " + code2 + "\n" + place + ":= " + op + "(" + place1 + "," + place2 +  ")";
    }

    private String translateUNOP(Document doc, List<String> children) {
        String place1 = newTempVar();
        String place = newTempVar();
        String code1 = translateNode(children.get(2),doc);
        List<String> child = getChildrenUNIDS(findElementByUNID(doc,children.get(0)));
        String op = findElementByUNID(doc,child.get(0)).getElementsByTagName("SYMB").item(0).getTextContent();

        switch (op) {
            case "not":
                //TODO
            case "sqrt":
                op = "SQR";
        }
        tempCounter--;
        return code1 + "\n" + place + ":= " + op + "(" + place1 + ")";
    }


    public static Element findElementByUNID(Document doc, String unid) {
        NodeList elements = doc.getElementsByTagName("*"); // Get all elements
        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                NodeList unidList = element.getElementsByTagName("UNID");
                if (unidList != null && unidList.getLength() > 0 && unidList.item(0).getTextContent().equals(unid)) {
                    return element; // Return the element with the matching UNID
                }
            }
        }
        return null; // Return null if no element is found with the given UNID
    }

    public static List<String> getChildrenUNIDS(Element node) {
        // Get the CHILDREN element directly under the PROG node
        List<String> idList = new ArrayList<String>();
        NodeList childrenNodes = node.getElementsByTagName("CHILDREN");
        if (childrenNodes.getLength() > 0) {
            Element childrenElement = (Element) childrenNodes.item(0);
            NodeList childIds = childrenElement.getChildNodes(); // Get all children of the CHILDREN element

            // Iterate through the child nodes
            for (int i = 0; i < childIds.getLength(); i++) {
                Node childNode = childIds.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) { // Check if it's an element node
                    Element child = (Element) childNode;
//                    System.out.println("Child ID: " + child.getTextContent());
                    idList.add(child.getTextContent());
                }
            }
        }
        return idList;
    }

    int tempCounter = 0;
    public String newTempVar() {
        return "t" + (++tempCounter);
    }
}
