# SPLCompiler

## Usage

1. **Input File**:
    - Before running the lexer, make sure to place your **lexer text file** (e.g., `input.txt`) in the `src/main/resources/` directory. This is where the lexer will look for the input file to read the RecSPL code.

2. **Running the Lexer**:
    - Compile and run the project using your preferred IDE (e.g., IntelliJ IDEA, Eclipse) or through the command line with Maven.
    - Execute the `RecSPLLexerTest` class, which will invoke the lexer on the `input.txt` file, tokenize the contents, and output the tokens to an XML file.

3. **Output**:
    - The tokens will be written to an XML file (e.g., `lexerOutput.xml`) in the `src/main/resources/` directory, which can then be used by the parser.


## Requirements

- JDK 11 or higher
- Maven (if using Maven for build management)
