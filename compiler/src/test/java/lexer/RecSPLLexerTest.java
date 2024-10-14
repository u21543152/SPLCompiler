package lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.splcompiler.lexer.RecSPLLexer;
import com.splcompiler.lexer.Token;

public class RecSPLLexerTest {

    public static void main(String[] args) {
        try {
            // Read the input RecSPL program from a text file
            String input = readFile("SPLCompiler\\compiler\\src\\main\\resources\\input.txt");

            // Create a lexer and tokenize the input
            RecSPLLexer lexer = new RecSPLLexer(input);
            List<Token> tokens = lexer.tokenize();

            // Write the tokens to an XML file
            lexer.writeTokensToXML("output.xml");

            System.out.println("Tokenization successful! Output written to output.xml.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Helper method to read the content of a file into a string
    public static String readFile(String fileName) throws IOException {
        StringBuilder sb;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
