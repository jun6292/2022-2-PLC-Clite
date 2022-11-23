import java.util.*;

public class State extends HashMap<Variable, Value> { 
    // Defines the set of variables and their associated values 
    // that are active during interpretation
    
    public State( ) { }
    
    public State(Variable key, Value val) {
        put(key, val);
    }
    
    public State onion(Variable key, Value val) {
        // student exercise
        for (Variable key : )
        return this;
    }

    void display() {
        // student exercise
        System.out.print("{ ");
        Iterator<Variable> it = this.keyset().iterator();
        while (it.hasNext()) {
            Variable key = (Variable) it.next();
            System.out.print("<" + key + ", " + this.get(key) + ">");
            if (it.hasNext())
                System.out.print(", ");
        }
        System.out.print("}\n");
   	}
}