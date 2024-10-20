package com.splcompiler.symboltable;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable
{
	class Symbol 
	{
	    private int id;
	    private String name;
	    private String type;
	    private Object value;

	    // Constructor for a symbol
	    public Symbol(int id, String name, String type, Object value) 
	    {
	        this.id = id;
	        this.name = name;
	        this.type = type;
	        this.value = value;
	    }

	    // Getter methods for id, name, type, and value
	    public int getId() 
	    {
	        return id;
	    }

	    public String getName() 
	    {
	        return name;
	    }

	    public String getType() 
	    {
	        return type;
	    }

	    public Object getValue() 
	    {
	        return value;
	    }

	    // Setter method for value (if needed)
	    public void setValue(Object value) 
	    {
	        this.value = value;
	    }
	}
	
    // A HashMap to store symbols using their unique ID
    private Map<Integer, Symbol> table;

    // Constructor to initialize the symbol table
    public SymbolTable() 
    {
        table = new HashMap<>();
    }

    // Method to add a symbol to the table
    public void addReplaceSymbol(int id, String name, String type, Object value) 
    {
        Symbol symbol = new Symbol(id, name, type, value);
        table.put(id, symbol);
    }
    
    // Method to update a symbol in the table

    // Method to remove a symbol from the table by its ID
    public void removeSymbol(int id) 
    {
        table.remove(id);
    }

    // Method to retrieve a symbol's name by its ID
    public String getSymbolName(int id) 
    {
        Symbol symbol = table.get(id);
        if (symbol != null) {
            return symbol.getName();
        }
        return null; // or throw an exception if the symbol doesn't exist
    }

    // Method to retrieve a symbol's type by its ID
    public String getSymbolType(int id) 
    {
        Symbol symbol = table.get(id);
        if (symbol != null) {
            return symbol.getType();
        }
        return null; // or throw an exception if the symbol doesn't exist
    }

    // Method to retrieve a symbol's value by its ID
    public Object getSymbolValue(int id) 
    {
        Symbol symbol = table.get(id);
        if (symbol != null) 
        {
            return symbol.getValue();
        }
        return null; // or throw an exception if the symbol doesn't exist
    }
    
    public void printTable() {
        if (table.isEmpty()) {
            System.out.println("The symbol table is empty.");
        } else {
            System.out.println("ID\tName\tType\t\tValue");
            System.out.println("------------------------------------");
            for (Symbol symbol : table.values()) {
                System.out.println(symbol.getId() + "\t" +
                                   symbol.getName() + "\t" +
                                   symbol.getType() + "\t\t" +
                                   symbol.getValue());
            }
        }
    }
}