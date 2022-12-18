import java.util.*;

public class TypeMap extends HashMap<Variable, Type> {
// TypeMap is implemented as a Java HashMap.  
// Plus a 'display' method to facilitate experimentation.
    public void display() { // Type map을 출력하는 함수, 변수의 타입 정보 유지
        System.out.print("{ ");
        String sep = "";
        for (Variable key : keySet()) { // <identifier, type> 형태로 출력
            System.out.print(sep + "<" + key + ", " + get(key) + ">");
            sep = ", ";
        }
        System.out.println(" }");
    }

    // 함수와 같이 출력
    public void display(Functions functions)
    {
        System.out.print("{");

        // 출력
        for (Map.Entry<Variable, Type> entry : entrySet())
            System.out.println("   <" + entry.getKey() + ", " + entry.getValue() + ">,");

        // 함수
        for (Function function : functions)
            System.out.println("   <" + function.variable + ", " + function.type + ", " + function.params + ">");

        System.out.print("}\n");
    }
}