// Following is the semantics class:
// The meaning M of a Statement is a State
// The meaning M of a Expression is a Value

public class Semantics {

    State M (Program p) { 
        return M (p.body, initialState(p.decpart)); 
    }
  
    State initialState (Declarations d) {
        State state = new State();
        Value intUndef = new IntValue();
        for (Declaration decl : d)
            state.put(decl.v, Value.mkValue(decl.t));
        return state;
    }

    State M (Statement s, State state) {
        if (s instanceof Skip) return M((Skip)s, state);
        if (s instanceof Assignment)  return M((Assignment)s, state);
        if (s instanceof Conditional)  return M((Conditional)s, state);
        if (s instanceof Loop)  return M((Loop)s, state);
        if (s instanceof Block)  return M((Block)s, state);
        throw new IllegalArgumentException("should never reach here");
    }
  
    State M (Skip s, State state) {
        return state;
    }
  
    State M (Assignment a, State state) {
        return state.onion(a.target, M (a.source, state));
    }
  
    State M (Block b, State state) {
        // student exercise
        for (Statement s : b.members)   // Block 내부의 모든 Statement에 대해
            state = M(s, state);    // Statement의 해당하는 하위 클래스로 캐스팅한 의미를 state에 저장
        return state;   // 갱신된 state를 반환
    }
  
    State M (Conditional c, State state) {
        // student exercise
        // The meaning of conditional is:
        if (M(c.test, state).boolValue())   // Expression test의 의미가 true라면,
            return M(c.thenbranch, state);  // Conditional의 meaning은 thenbranch이다.
        else    // Expression test가 false라면,
            return M(c.elsebranch, state);  // Conditinal의 meaning은 elsebranch이다.
    }
  
    State M (Loop l, State state) {
        // student exercise
        // The meaning of loop is:
        if (M(l.test, state).boolValue())   // Expression test의 의미가 true라면,
            return M(l, M(l.body, state));  // loop의 meaning은 loop body인데, state가 재귀적으로 입력으로 들어간다.
        else    // Expression test의 의미가 false라면,
            return state;   // 입력으로 받은 state 반환한다.
    }

    Value applyBinary (Operator op, Value v1, Value v2) {
        StaticTypeCheck.check( ! v1.isUndef( ) && ! v2.isUndef( ),
               "reference to undef value");
        if (op.val.equals(Operator.INT_PLUS)) 
            return new IntValue(v1.intValue( ) + v2.intValue( ));
        if (op.val.equals(Operator.INT_MINUS)) 
            return new IntValue(v1.intValue( ) - v2.intValue( ));
        if (op.val.equals(Operator.INT_TIMES)) 
            return new IntValue(v1.intValue( ) * v2.intValue( ));
        if (op.val.equals(Operator.INT_DIV)) 
            return new IntValue(v1.intValue( ) / v2.intValue( ));
        

        if (op.val.equals(Operator.INT_LT)) 
            return new BoolValue(v1.intValue( ) <  v2.intValue( ));
        if (op.val.equals(Operator.INT_LE)) 
            return new BoolValue(v1.intValue( ) <= v2.intValue( ));
        if (op.val.equals(Operator.INT_EQ)) 
            return new BoolValue(v1.intValue( ) == v2.intValue( ));
        if (op.val.equals(Operator.INT_LE)) 
            return new BoolValue(v1.intValue( ) != v2.intValue( ));
        if (op.val.equals(Operator.INT_GT)) 
            return new BoolValue(v1.intValue( ) >  v2.intValue( ));
        if (op.val.equals(Operator.INT_GE)) 
            return new BoolValue(v1.intValue( ) >= v2.intValue( ));

        if (op.val.equals(Operator.FLOAT_PLUS)) 
            return new FloatValue(v1.floatValue( ) + v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_MINUS)) 
            return new FloatValue(v1.floatValue( ) - v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_TIMES)) 
            return new FloatValue(v1.floatValue( ) * v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_DIV)) 
            return new FloatValue(v1.floatValue( ) / v2.floatValue( ));

        if (op.val.equals(Operator.FLOAT_LT)) 
            return new BoolValue(v1.floatValue( ) <  v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_LE)) 
            return new BoolValue(v1.floatValue( ) <= v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_EQ)) 
            return new BoolValue(v1.floatValue( ) == v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_LE)) 
            return new BoolValue(v1.floatValue( ) != v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_GT)) 
            return new BoolValue(v1.floatValue( ) >  v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_GE)) 
            return new BoolValue(v1.floatValue( ) >= v2.floatValue( ));

        if (op.val.equals(Operator.CHAR_LT)) 
            return new BoolValue(v1.charValue( ) <  v2.charValue( ));
        if (op.val.equals(Operator.CHAR_LE)) 
            return new BoolValue(v1.charValue( ) <= v2.charValue( ));
        if (op.val.equals(Operator.CHAR_EQ)) 
            return new BoolValue(v1.charValue( ) == v2.charValue( ));
        if (op.val.equals(Operator.CHAR_LE)) 
            return new BoolValue(v1.charValue( ) != v2.charValue( ));
        if (op.val.equals(Operator.CHAR_GT)) 
            return new BoolValue(v1.charValue( ) >  v2.charValue( ));
        if (op.val.equals(Operator.CHAR_GE)) 
            return new BoolValue(v1.charValue( ) >= v2.charValue( ));

        if (op.val.equals(Operator.BOOL_EQ)) 
            return new BoolValue(v1.boolValue( ) == v2.boolValue( ));
        if (op.val.equals(Operator.BOOL_NE))
            return new BoolValue(v1.boolValue( ) != v2.boolValue( ));
        /*if (op.val.equals(Operator.BOOL_LT)) 
            return new BoolValue(v1.boolValue( ) <  v2.boolValue( ));
        if (op.val.equals(Operator.BOOL_LE)) 
            return new BoolValue(v1.boolValue( ) <= v2.boolValue( ));
        if (op.val.equals(Operator.BOOL_GT)) 
            return new BoolValue(v1.boolValue( ) >  v2.boolValue( ));
        if (op.val.equals(Operator.BOOL_GE)) 
            return new BoolValue(v1.boolValue( ) >= v2.boolValue( ));*/
        
        throw new IllegalArgumentException("should never reach here");
    } 
    
    Value applyUnary (Operator op, Value v) {
        StaticTypeCheck.check( ! v.isUndef( ),
               "reference to undef value");
        if (op.val.equals(Operator.NOT))
            return new BoolValue(!v.boolValue( ));
        else if (op.val.equals(Operator.INT_NEG))
            return new IntValue(-v.intValue( ));
        else if (op.val.equals(Operator.FLOAT_NEG))
            return new FloatValue(-v.floatValue( ));
        else if (op.val.equals(Operator.I2F))
            return new FloatValue((float)(v.intValue( ))); 
        else if (op.val.equals(Operator.F2I))
            return new IntValue((int)(v.floatValue( )));
        else if (op.val.equals(Operator.C2I))
            return new IntValue((int)(v.charValue( )));
        else if (op.val.equals(Operator.I2C))
            return new CharValue((char)(v.intValue( )));
        throw new IllegalArgumentException("should never reach here");
    } 

    Value M (Expression e, State state) {   // Expression x State --> Value
        if (e instanceof Value) 
            return (Value)e;
        if (e instanceof Variable) 
            return (Value)(state.get(e));
        // student exercise
        //... for Binary
        if (e instanceof Binary) {  // 이항 연산자라면 applyBinary를 통해 얻은 값을 반환한다.
            Binary b = (Binary) e;
            return applyBinary(b.op, M(b.term1, state), M(b.term2, state)); //
        }
        //... for Unary
        if (e instanceof Unary) {   // 단항 연산자라면 applyUnary를 통해 얻은 값을 반환한다.
            Unary u = (Unary) e;
            return applyUnary(u.op, M(u.term, state));  //
        }
        throw new IllegalArgumentException("should never reach here");
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display(0);
        System.out.println("\nBegin type checking...programs/"+args[0]+"\n");
        System.out.println("Type map:");
        TypeMap map = StaticTypeCheck.typing(prog.decpart);
        map.display();    
        StaticTypeCheck.V(prog);
        Program out = TypeTransformer.T(prog, map);
        System.out.println("Output AST");
        out.display(0);
        Semantics semantics = new Semantics( );
        State state = semantics.M(out);
        System.out.println("\nBegin interpreting...programs/"+args[0]+"\n");
        System.out.println("Final State");
        state.display();  // student exercise
    }
}