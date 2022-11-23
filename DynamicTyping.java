// DynamicTyping.java

// Following is the semantics class
// for a dynamically typed language.
// The meaning M of a Statement is a State.
// The meaning M of a Expression is a Value.

public class DynamicTyping extends Semantics {
    
    State M (Program p) { 
        return M (p.body, new State( )); 
    }
  
    Value applyBinary (Operator op, Value v1, Value v2) {
        StaticTypeCheck.check( v1.type( ) == v2.type( ),
                               "mismatched types");
        if (op.ArithmeticOp( )) {
            if (v1.type( ) == Type.INT) {
                if (op.val.equals(Operator.PLUS)) 
                    return new IntValue(v1.intValue( ) + v2.intValue( ));
                if (op.val.equals(Operator.MINUS)) 
                    return new IntValue(v1.intValue( ) - v2.intValue( ));
                if (op.val.equals(Operator.TIMES)) 
                    return new IntValue(v1.intValue( ) * v2.intValue( ));
                if (op.val.equals(Operator.DIV)) 
                    return new IntValue(v1.intValue( ) / v2.intValue( ));
            }
            else if (v1.type( ) == Type.FLOAT) {
                // student exercise
            }
        }
        else if (op.RelationalOp())
        {
            if (v1.type( ) == Type.INT) {
                if (op.val.equals(Operator.LT)) 
                    return new BoolValue(v1.intValue( ) < v2.intValue( ));
                if (op.val.equals(Operator.LE)) 
                    return new BoolValue(v1.intValue( ) <= v2.intValue( ));
                if (op.val.equals(Operator.EQ)) 
                    return new BoolValue(v1.intValue( ) == v2.intValue( ));
                if (op.val.equals(Operator.NE)) 
                    return new BoolValue(v1.intValue( ) != v2.intValue( ));
                if (op.val.equals(Operator.GT)) 
                    return new BoolValue(v1.intValue( ) > v2.intValue( ));
                if (op.val.equals(Operator.GE)) 
                    return new BoolValue(v1.intValue( ) >= v2.intValue( ));
            }
            else if (v1.type( ) == Type.FLOAT) {
                // student exercise
            }
        }
        else if (op.BooleanOp())
        {
            if(v1.type() == Type.BOOL)
            {
                if(op.val.equals(Operator.AND))
                    return new BoolValue(v1.boolValue( ) && v2.boolValue ( ));
                if(op.val.equals(Operator.OR))
                    return new BoolValue(v1.boolValue( ) || v2.boolValue ( ));
            }
        }
        throw new IllegalArgumentException("should never reach here");
    } 
    
    Value applyUnary (Operator op, Value v) {
        if (op.val.equals(Operator.NOT) && v.type == Type.BOOL)
            return new BoolValue(!v.boolValue( ));
        else if (op.val.equals(Operator.NEG) && v.type == Type.INT)
            return new IntValue(-v.intValue( ));
        else if (op.val.equals(Operator.NEG) && v.type == Type.FLOAT)
            return new FloatValue(-v.floatValue( ));
        else if (op.val.equals(Operator.FLOAT) && v.type == Type.INT)
            return new FloatValue((float)(v.intValue( ))); 
        else if (op.val.equals(Operator.INT) && v.type == Type.FLOAT)
            return new IntValue((int)(v.floatValue( )));
        else if (op.val.equals(Operator.INT) && v.type == Type.CHAR)
            return new IntValue((int)(v.charValue( )));
        else if (op.val.equals(Operator.CHAR) && v.type == Type.INT)
            return new CharValue((char)(v.intValue( )));
        throw new IllegalArgumentException("should never reach here");
    } 

    Value M (Expression e, State sigma) {
        if (e instanceof Value) 
            return (Value)e;
        if (e instanceof Variable) {
            StaticTypeCheck.check( sigma.containsKey(e),
                "reference to undefined variable");
            return (Value)(sigma.get(e));
        }
        // student exercise
        //... for Binary
        //... for Unary
        
        throw new IllegalArgumentException("should never reach here");
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();    
        DynamicTyping dynamic = new DynamicTyping( );
        State state = dynamic.M(prog);
        System.out.println("Final State");
        state.display( );   // student exercise
    }
}
