package com.splcompiler.parser;

import java.util.ArrayList;
import java.util.List;

// Base class for all AST nodes
public abstract class ASTNode {
    // Common functionality for all nodes can be added here
}

// Program Node
class ProgramNode extends ASTNode {
    private final List<ASTNode> globalVars = new ArrayList<>();
    private List<ASTNode> functions = new ArrayList<>();
    private ASTNode algorithm;

    public void addGlobalVar(ASTNode var) {
        globalVars.add(var);
    }

    public void setAlgorithm(ASTNode algo) {
        this.algorithm = algo;
    }

    public void addFunction(ASTNode function) {
        functions.add(function);
    }

    public List<ASTNode> getGlobalVars() {
        return globalVars;
    }

    public ASTNode getAlgorithm() {
        return algorithm;
    }

    public List<ASTNode> getFunctions() {
        return functions;
    }
}

// Global Variable Definition Node
class GlobalVar extends ASTNode {
    private final String type;  // Type of the variable (e.g. "num" or "text")
    private final String varName; // Variable name

    public GlobalVar(String type, String varName) {
        this.type = type;
        this.varName = varName;
    }

    public String getType() {
        return type;
    }

    public String getVarName() {
        return varName;
    }
}

// Instruction Node (INSTRUC)
class INSTRUCNode extends ASTNode {
    private final List<ASTNode> commands = new ArrayList<>();

    public void addCommand(ASTNode command) {
        commands.add(command);
    }

    public List<ASTNode> getCommands() {
        return commands;
    }
}

// Command Node
abstract class CommandNode extends ASTNode {
}

// Skip Node
class SkipNode extends CommandNode {
    @Override
    public String toString() {
        return "Skip Command";
    }
}

// Halt Node
class HaltNode extends CommandNode {
    @Override
    public String toString() {
        return "Halt Command";
    }
}

// Print Node
class PrintNode extends CommandNode {
    private final ASTNode atomic; // The variable or constant to print

    public PrintNode(ASTNode atomic) {
        this.atomic = atomic;
    }

    public ASTNode getAtomic() {
        return atomic;
    }

    @Override
    public String toString() {
        return "Print Command: " + atomic.toString();
    }
}

// Assignment Node
class AssignmentNode extends CommandNode {
    private final String varName;  // The variable name
    private final ASTNode term;    // The assigned term

    public AssignmentNode(String varName, ASTNode term) {
        this.varName = varName;
        this.term = term;
    }

    public String getVarName() {
        return varName;
    }

    public ASTNode getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return "Assignment: " + varName + " := " + term.toString();
    }
}

// Call Node (Function Call)
class CallNode extends CommandNode {
    private final FnameNode functionName;
    private final List<AtomicNode> arguments;

    public CallNode(FnameNode functionName, List<AtomicNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public FnameNode getFunctionName() {
        return functionName;
    }

    public List<AtomicNode> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "Function Call: " + functionName.toString() + " with arguments " + arguments.toString();
    }
}

// If Node (Branch)
class IfNode extends CommandNode {
    private final ASTNode condition;
    private final AlgoNode thenBranch;
    private final AlgoNode elseBranch;

    public IfNode(ASTNode condition, AlgoNode thenBranch, AlgoNode elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public AlgoNode getThenBranch() {
        return thenBranch;
    }

    public AlgoNode getElseBranch() {
        return elseBranch;
    }

    @Override
    public String toString() {
        return "Branch: if " + condition.toString() + " then " + thenBranch.toString() + " else " + elseBranch.toString();
    }
}

// Unary Operation Node
class UnopNode extends ASTNode {
    private final String operator;
    private final ASTNode argument;

    public UnopNode(String operator, ASTNode argument) {
        this.operator = operator;
        this.argument = argument;
    }

    @Override
    public String toString() {
        return "Unary Operation: " + operator + "(" + argument.toString() + ")";
    }
}

// Binary Operation Node
class BinopNode extends ASTNode {
    private final String operator;
    private final ASTNode left;
    private final ASTNode right;

    public BinopNode(String operator, ASTNode left, ASTNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "Binary Operation: " + operator + "(" + left.toString() + ", " + right.toString() + ")";
    }
}

// Condition Node
class ConditionNode extends ASTNode {
    private final BinopNode condition;

    public ConditionNode(BinopNode condition) {
        this.condition = condition;
    }

    public BinopNode getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        return "Condition: " + condition.toString();
    }
}

// Simple Condition Node
class SimpleNode extends ASTNode {
    private final BinopNode condition;

    public SimpleNode(BinopNode condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "Simple Condition: " + condition.toString();
    }
}

// Composite Condition Node
class CompNode extends ASTNode {
    private final ASTNode condition;

    public CompNode(ASTNode condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "Composite Condition: " + condition.toString();
    }
}

// Algorithm Node
class AlgoNode extends ASTNode {
    private final INSTRUCNode instructions;

    public AlgoNode(INSTRUCNode instructions) {
        this.instructions = instructions;
    }

    public INSTRUCNode getInstructions() {
        return instructions;
    }

    @Override
    public String toString() {
        return "Algorithm:\n" + instructions.toString();
    }
}

// Function Node
class FunctionNode extends ASTNode {
    private final HeaderNode header;  // Function header (contains function type, name, and parameters)
    private final BodyNode body;      // Function body (contains prolog, local variables, algorithm, epilog, and sub-functions)

    public FunctionNode(HeaderNode header, BodyNode body) {
        this.header = header;
        this.body = body;
    }

    public HeaderNode getHeader() {
        return header;
    }

    public BodyNode getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Function: " + header.toString() + "\n" + body.toString();
    }
}


// Function Header Node (HeaderNode)
class HeaderNode extends ASTNode {
    private final String fType;
    private final String fName;
    private final ASTNode variable1;
    private final ASTNode variable2;
    private final ASTNode variable3;

    public HeaderNode(String fType, String fName, ASTNode variable1, ASTNode variable2, ASTNode variable3) {
        this.fType = fType;
        this.fName = fName;
        this.variable1 = variable1;
        this.variable2 = variable2;
        this.variable3 = variable3;
    }

    public String getfType() {
        return fType;
    }

    public String getfName() {
        return fName;
    }

    public ASTNode getVariable1() {
        return variable1;
    }

    public ASTNode getVariable2() {
        return variable2;
    }

    public ASTNode getVariable3() {
        return variable3;
    }

    @Override
    public String toString() {
        return fType + " " + fName + "(" + variable1.toString() + ", " + variable2.toString() + ", " + variable3.toString() + ")";
    }
}

// Function Body Node
class BodyNode extends ASTNode {
    private final PrologNode prologNode;
    private final List<locVarNode> locVars;
    private final AlgoNode algo;
    private final EpilogNode epilogNode;
    private final SubFuncsNode subFuncsNode;

    public BodyNode(PrologNode prologNode, List<locVarNode> locVars, AlgoNode algo, EpilogNode epilogNode, SubFuncsNode subFuncsNode) {
        this.prologNode = prologNode;
        this.locVars = locVars;
        this.algo = algo;
        this.epilogNode = epilogNode;
        this.subFuncsNode = subFuncsNode;
    }

    @Override
    public String toString() {
        return "Body: " + prologNode.toString() + locVars.toString() + algo.toString() + epilogNode.toString() + subFuncsNode.toString();
    }
}

// Prolog Node
class PrologNode extends ASTNode {
    private final String value = "{";
    @Override
    public String toString() {
        return value;
    }
}

// Epilog Node
class EpilogNode extends ASTNode {
    private final String value = "}";
    @Override
    public String toString() {
        return value;
    }
}

// Local Variable Node
class locVarNode extends ASTNode {
    private final String type;
    private final String varName;

    public locVarNode(String type, String varName) {
        this.type = type;
        this.varName = varName;
    }

    public String getType() {
        return type;
    }

    public String getVarName() {
        return varName;
    }

    @Override
    public String toString() {
        return type + " " + varName;
    }
}

// Sub Functions Node
class SubFuncsNode extends ASTNode {
    private final List<ASTNode> functions;

    public SubFuncsNode(List<ASTNode> functions) {
        this.functions = functions;
    }

    public List<ASTNode> getFunctions() {
        return functions;
    }

    @Override
    public String toString() {
        return "Sub Functions: " + functions.toString();
    }
}

// Function Name Node (FNAME)
class FnameNode extends ASTNode {
    private final String functionName;

    public FnameNode(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public String toString() {
        return functionName;
    }
}

// Atomic Node (for ATOMIC)
class AtomicNode extends ASTNode {
    private final ASTNode atomic;

    public AtomicNode(ASTNode atomic) {
        this.atomic = atomic;
    }

    public ASTNode getAtomic() {
        return atomic;
    }

    @Override
    public String toString() {
        return atomic.toString();
    }
}

// Variable Node (for VNAME)
class VariableNode extends ASTNode {
    private final String varName;

    public VariableNode(String varName) {
        this.varName = varName;
    }

    public String getVarName() {
        return varName;
    }

    @Override
    public String toString() {
        return varName;
    }
}

// Number Node (for CONST N)
class NumberNode extends ASTNode {
    private final int value;

    public NumberNode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

// Text Node (for CONST T)
class TextNode extends ASTNode {
    private final String value;

    public TextNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }
}
