import java.util.*;

public class TypeMap extends HashMap<Variable, Type> {
// TypeMap is implemented as a Java HashMap.  
// Plus a 'display' method to facilitate experimentation.
    public void display() { // Type map을 출력하는 함수, 변수의 타입 정보 유지
        System.out.print("{ ");
        for (Variable v : this.keySet()) {  // 모든 <identifier, type> 출력
            System.out.print("<" + v.toString() + ", " + this.get(v) + "> ");
        }
        System.out.println("}");
    }
}
