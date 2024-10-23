package com.splcompiler.symboltable;

import java.awt.datatransfer.SystemFlavorMap;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable
{
	public class Symbol 
	{
	    private int id;
	    private String name;
	    private String type;
	    private Object value;
	    private String note;

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
	    
	    public String getNote()
	    {
	    	return note;
	    }

	    // Setter method for value (if needed)
	    public void setValue(Object value) 
	    {
	        this.value = value;
	    }
	    
	    //set the note where necessary
	    public void setNote(String note)
	    {
	    	this.note = note;
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
    	//System.out.println("Calling addReplaceSymbol("+id+", "+name+", "+type+", "+value+")");
    	boolean found = false;
    	int temp = -1;
    	for (Map.Entry<Integer, Symbol> i : table.entrySet())
    	{
    		if (i.getValue().getName().equals(name))
    		{
    			found = true;
    			temp = i.getKey();
    		}
    	}
    	if (found) //replace existing - if there is no type supplied, use the old type, if an incorrect type is supplied, throw error
    	{
    		//System.out.println("Found existing symbol");
    		Symbol symbol = new Symbol(id, null, null, null); //to be replaced in below code
    		String oldType = getSymbolType(temp); //fetches the type from the current symbol
    		if (type == "" || type == " " || type == null) //there is no type supplied, such as in an input check: use the old type
    		{
    			symbol = new Symbol(id, name, oldType, value);
    		}
    		else if (type == oldType) // the new type is the same as the old type; no worries
    		{
    			symbol = new Symbol(id, name, type, value);
    		}
    		else if (type != oldType) // the new type is different: this is a violation of type checking
    		{
    			//System.out.println("ERROR: New type "+type+" of variable "+name+" does not match type ("+oldType+"). Errors may occur.");
    			symbol = new Symbol(id, name, type, value); //do it anyway but warn the user
    			symbol.setNote("TypeError: New type "+type+" of variable "+name+" does not match type ("+oldType+"). Errors may occur.");
    		}
    		table.put(temp, symbol);
    	}
    	else //add new
    	{
    		//System.out.println("Making new symbol");
	        Symbol symbol = new Symbol(id, name, type, value);
	        table.put(id, symbol);
    	}
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
    
 // Method to retrieve a symbol's name by its ID
    public Symbol getSymbolByName(String name) 
    {
    	for (Map.Entry<Integer, Symbol> i : table.entrySet())
    	{
    		if (i.getValue().getName().equals(name))
    		{
    			return i.getValue();
    		}
    	}
    	return null;
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
            System.out.println("ID\tName\tType\t\tValue\t\tNote");
            System.out.println("----------------------------------------------------");
            for (Symbol symbol : table.values()) {
                System.out.println(symbol.getId() + "\t" +
                                   symbol.getName() + "\t" +
                                   symbol.getType() + "\t\t" +
                                   symbol.getValue() + "\t\t" +
                                   symbol.getNote());
            }
        }
    }
}