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
import java.util.*;


public class codeGenv2 {

    private static Map<String, String> symbolTable = new HashMap<>();

    public void setTable(Map<String, String> table) {
        symbolTable = table;
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
        String Code = "";
        int id;
        switch (symb) {
            case "PROG":
                children = getChildrenUNIDS(node);
                code.append(translateNode(children.get(2), doc)).append("\nSTOP\n");//FUNC
//                }
                break;
            case "ALGO":
                children = getChildrenUNIDS(node);
                code.append(translateNode(children.get(1), doc));//INSTRUC
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
            case "ATOMIC", "COND":
                children = getChildrenUNIDS(node);
                return translateNode(children.get(0), doc);
            case "VNAME", "FNAME":
                String child = getChildrenUNIDS(node).get(0);
                String originalVName = findElementByUNID(doc, child).getElementsByTagName("SYMB").item(0).getTextContent();
                return getKeyByValue(symbolTable, originalVName);
            case "CONST":
                return findElementByUNID(doc, getChildrenUNIDS(node).get(0)).getElementsByTagName("SYMB").item(0).getTextContent();
            case "ASSIGN":
                children = getChildrenUNIDS(node);
                if (children.size() == 3) {
                    Element child1 = findElementByUNID(doc, getChildrenUNIDS(node).get(2));
                    assert child1 != null;
                    Element child2 = findElementByUNID(doc, getChildrenUNIDS(child1).get(0));
                    assert child2 != null;
                    if (Objects.equals(findElementByUNID(doc, getChildrenUNIDS(child2).get(0)).getElementsByTagName("SYMB").item(0).getTextContent(), "BINOP")) {
                        tempVar = this.newTempVar();
                        Code += translateNode(children.get(2), doc) + "\n";
                    } else {
                        tempVar = this.newTempVar();
                        Code += tempVar + " := " + translateNode(children.get(2), doc) + "\n";
                    }

                    tempVar = this.newTempVar();
                    Code += translateNode(children.get(0), doc) + " := " + tempVar;
                    return Code + "\n";
                } else {

                    return translateNode(children.get(1), doc) + translateNode(children.get(0), doc) + "\n";
                }
            case "TERM":
                children = getChildrenUNIDS(node);
                if (findElementByUNID(doc, children.get(0)).getElementsByTagName("SYMB").item(0).getTextContent() != "OP") {
                    tempCounter--;
                }
                return translateNode(children.get(0), doc);
            case "< input":
                return "INPUT ";
            case "CALL":
                children = getChildrenUNIDS(node);
                return "CALL_" + translateNode(children.get(0), doc) + "(" + translateNode(children.get(2), doc) + "," + translateNode(children.get(4), doc) + "," + translateNode(children.get(6), doc) + ")";
            case "OP", "COMPOSITE":
                children = getChildrenUNIDS(node);
                if (children.size() == 4) {
                    return translateUNOP(doc, children);
                } else if (children.size() == 6) {
                    return translateBINOP(doc, children);
                }
            case "ARG":
                children = getChildrenUNIDS(node);
                return translateNode(children.get(0), doc);
            case "BRANCH":
                children = getChildrenUNIDS(node);
                List<String> kids = getChildrenUNIDS(findElementByUNID(doc, children.get(1)));
                System.out.println(findElementByUNID(doc, kids.get(0)).getElementsByTagName("SYMB").item(0).getTextContent());
                if (Objects.equals(findElementByUNID(doc, kids.get(0)).getElementsByTagName("SYMB").item(0).getTextContent(), "SIMPLE")) {
                    return translateSIMPLE(doc, children);
                } else {
                    return translateCOMP(doc, children);
                }

            case "SIMPLE":
                children = getChildrenUNIDS(node);
                return translateBINOP(doc, children);
            default:
                return "";
        }
        return code.toString();
    }

    private String translateCOMP(Document doc, List<String> children) {
        String label1 = newTempLabel();
        String label2 = newTempLabel();
        String label3 = newTempLabel();
        Element child1 = findElementByUNID(doc, children.get(1));//COND
        assert child1 != null;
        Element child2 = findElementByUNID(doc, getChildrenUNIDS(child1).get(0));//COMP
        assert child2 != null;

        String codeCond1 = translateNode(getChildrenUNIDS(child2).get(2), doc);//SIMPLE

        String codeStat1 = translateNode(children.get(3), doc);//ALGO1
        String codeStat2 = translateNode(children.get(5), doc);
        String condVar = newTempVar();
        // Combine the translated code according to the structure



        Element operator = findElementByUNID(doc, getChildrenUNIDS(child2).get(0));
        Element op = findElementByUNID(doc, getChildrenUNIDS(operator).get(0));
        System.out.println("BINOP: " + operator.getElementsByTagName("SYMB").item(0).getTextContent());
        if (Objects.equals(operator.getElementsByTagName("SYMB").item(0).getTextContent(), "BINOP")) {
            String codeCond2 = translateNode(getChildrenUNIDS(child2).get(4), doc);//SIMPLE
            String condVar2 = newTempVar();
            String label4 = newTempLabel();
            if (Objects.equals(op.getElementsByTagName("SYMB").item(0).getTextContent(), "or")) {
                return "\n" + codeCond1 + "\nIF " + condVar + " THEN "
                        + label1 + " ELSE " + label2 + "\n[LABEL " + label2 + "]"
                        + codeCond2 + "\nIF " + condVar2 + " THEN "
                        + label1 + " ELSE " + label3 + "\n[LABEL " + label1 + "]\n"
                        + codeStat1 + "\nGOTO "
                        + label4 + "\n[LABEL " + label3 + "]\n"
                        + codeStat2 + "\n[LABEL " + label4 + "]";
            } else if (Objects.equals(op.getElementsByTagName("SYMB").item(0).getTextContent(), "and")) {
                return "\n" + codeCond1 + "\nIF " + condVar + " THEN "
                        + label1 + " ELSE " + label3 + "\n[LABEL " + label1 + "]"
                        + codeCond2 + "\nIF " + condVar2 + " THEN "
                        + label2 + " ELSE " + label3 + "\n[LABEL " + label2 + "]\n"
                        + codeStat1 + "\nGOTO "
                        + label4 + "\n[LABEL " + label3 + "]\n"
                        + codeStat2 + "\n[LABEL " + label4 + "] ";
            }
        } else {
            return "\n" + codeCond1 + "\nIF " + condVar + " THEN "
                    + label2 + " ELSE " + label1 + "\n[LABEL "
                    + label1 + "]\n" + codeStat1 + "\nGOTO "
                    + label3 + "\n[LABEL " + label2 + "]\n"
                    + codeStat2 + "\n[LABEL " + label3 + "]";
        }
        return "";
    }

    private String translateSIMPLE(Document doc, List<String> children) {
        String label1 = newTempLabel();
        String label2 = newTempLabel();
        String label3 = newTempLabel();
        Element child1 = findElementByUNID(doc, children.get(1));//COND
        assert child1 != null;
        Element child2 = findElementByUNID(doc, getChildrenUNIDS(child1).get(0));//SIMPLE
        assert child2 != null;

        String codeCond = translateNode(children.get(1), doc);//COND
        String codeStat1 = translateNode(children.get(3), doc);//ALGO1
        String codeStat2 = translateNode(children.get(5), doc);
        String condVar = newTempVar();
        // Combine the translated code according to the structure

        return "\n" + codeCond + "\nIF " + condVar + " THEN "
                + label1 + " ELSE " + label2 + "\n[LABEL "
                + label1 + "]\n" + codeStat1 + "\nGOTO "
                + label3 + "\n[LABEL " + label2 + "]\n"
                + codeStat2 + "\n[LABEL " + label3 + "]";

    }

    private String translateBINOP(Document doc, List<String> children) {
        String place1 = newTempVar();
        String place2 = newTempVar();
        String place = newTempVar();
        String code1 = translateNode(children.get(2), doc);
        String code2 = translateNode(children.get(4), doc);
        List<String> child = getChildrenUNIDS(findElementByUNID(doc, children.get(0)));
        String op = findElementByUNID(doc, child.get(0)).getElementsByTagName("SYMB").item(0).getTextContent();

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
        return "\n" + place1 + " := " + code1 + "\n" + place2 + " := " + code2 + "\n" + place + " := " + op + "(" + place1 + "," + place2 + ")";
    }

    private String translateBINOPCond(Document doc, List<String> children) {
        String place1 = newTempVar();
        String place2 = newTempVar();
        String place = newTempVar();
        String code1 = translateNode(children.get(2), doc);
        String code2 = translateNode(children.get(4), doc);
        List<String> child = getChildrenUNIDS(findElementByUNID(doc, children.get(0)));
        String op = findElementByUNID(doc, child.get(0)).getElementsByTagName("SYMB").item(0).getTextContent();

        switch (op) {
            case "eq":
                op = "=";
                break;
            case "grt":
                op = ">";
                break;
            case "add":
                op = "+";
                break;
            case "sub":
                op = "-";
                break;
            case "mul":
                op = "*";
                break;
            case "div":
                op = "/";
                break;

            default:
                throw new IllegalArgumentException("Unknown binary operation: " + op);
        }
        ;
        tempCounter--;
        return "\n" + place1 + " := " + code1 + "\n" + place2 + " := " + code2 + "\n" + place + " := " + op + "(" + place1 + "," + place2 + ")";
    }

    private String translateUNOP(Document doc, List<String> children) {
        String place1 = newTempVar();
        String place = newTempVar();
        String code1 = translateNode(children.get(2), doc);
        List<String> child = getChildrenUNIDS(findElementByUNID(doc, children.get(0)));
        String op = findElementByUNID(doc, child.get(0)).getElementsByTagName("SYMB").item(0).getTextContent();

        switch (op) {
            case "not":
                throw new IllegalArgumentException("Incorrect Operator");
            case "sqrt":
                op = "SQR";
        }
        tempCounter--;
        return code1 + "\n" + place + " := " + op + "(" + place1 + ")";
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

    int tempLabelCounter = 0;

    public String newTempLabel() {
        return "L" + (++tempLabelCounter);
    }

    public static String getKeyByValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return value; // or any default value if not found
    }
}
