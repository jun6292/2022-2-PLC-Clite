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
        put(key, val);  // 변수를 key로 값을 value로 State 해시 맵에 저장한 뒤 자기 자신을 반환
        return this;
    }

    // State를 입력 받아서 State에 있는 모든 Entry를 map에 저장 한 뒤 자기 자신을 반환
    public State onion(State t) {
        for (Variable key : t.keySet())
            put(key, t.get(key));
        return this;
    }

    void display() {
        // student exercise
        // state를 출력
        System.out.print("{ ");
        Iterator<Variable> it = this.keyset().iterator();   // state의 key(변수 이름)를 가져온다.
        while (it.hasNext()) {  // Variable을 key로 value를 값으로
            Variable key = (Variable) it.next();
            System.out.print("<" + key + ", " + this.get(key) + ">");   // state의 <변수 이름, 값> 형태로 출력
            if (it.hasNext())
                System.out.print(", ");
        }
        System.out.print("}\n");
   	}
}