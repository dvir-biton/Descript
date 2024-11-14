package parser.variables;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, VariableInfo> table = new HashMap<>();

    public void addVariable(String name, String type, int index) {
        table.put(name, new VariableInfo(type, index));
    }

    public VariableInfo getVariable(String name) {
        return table.get(name);
    }
}