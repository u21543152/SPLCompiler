package com.splcompiler.parser;

import com.splcompiler.lexer.Token;
import com.splcompiler.lexer.TokenType;
import org.w3c.dom.*;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class lexerXMLParser {

    // Method to read tokens from the XML file and return a list of tokens
    public static List<Token> readTokensFromXML(String filePath) {
        List<Token> tokens = new ArrayList<>();
        try {
            File inputFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            System.out.println(doc);
            NodeList tokenList = doc.getElementsByTagName("TOK");
            for (int i = 0; i < tokenList.getLength(); i++) {
                org.w3c.dom.Node tokenNode = tokenList.item(i);
                if (tokenNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element tokenElement = (Element) tokenNode;

                    // Extract token information (ID, CLASS, WORD)
                    String tokenClass = tokenElement.getElementsByTagName("CLASS").item(0).getTextContent();
                    String tokenValue = tokenElement.getElementsByTagName("WORD").item(0).getTextContent();
                    int tokenId = Integer.parseInt(tokenElement.getElementsByTagName("ID").item(0).getTextContent());
                    // Assuming TokenType is an enum and we convert CLASS to TokenType
                    TokenType tokenType = TokenType.valueOf(tokenClass.toUpperCase());  // Convert CLASS to enum
                    Token token = new Token(tokenId, tokenType, tokenValue);
                    tokens.add(token);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tokens;
    }
}

