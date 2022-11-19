// StaticTypeCheck.java

import java.util.*;

// Static type checking for Clite is defined by the functions 
// V and the auxiliary functions typing and typeOf.  These
// functions use the classes in the Abstract Syntax of Clite.


public class StaticTypeCheck {
    public static int typeError = 0;    // typeError의 개수 리턴
    public static TypeMap typing (Declarations d) {
        TypeMap map = new TypeMap();
        for (Declaration di : d) 
            map.put (di.v, di.t);
        return map;
    }

    public static void check(boolean test, String msg) {
        if (test)  return;
        System.err.println(msg);
        typeError++;
        // 프로그램 내의 모든 타입에러를 검사할 수 있게 하기 위함
        // System.exit(1);
    }

    public static void V (Declarations d) {
        for (int i=0; i<d.size() - 1; i++)
            for (int j=i+1; j<d.size(); j++) {
                Declaration di = d.get(i);
                Declaration dj = d.get(j);
                check( ! (di.v.equals(dj.v)),
                       "duplicate declaration: " + dj.v);
            }
    } 

    public static void V (Program p) {
        V (p.decpart);
        V (p.body, typing (p.decpart));
    } 

    public static Type typeOf (Expression e, TypeMap tm) {
        if (e instanceof Value) return ((Value)e).type;
        if (e instanceof Variable) {
            Variable v = (Variable)e;
            check (tm.containsKey(v), "undefined variable: " + v);
            return (Type) tm.get(v);
        }
        if (e instanceof Binary) {
            Binary b = (Binary)e;
            if (b.op.ArithmeticOp( ))
                if (typeOf(b.term1,tm)== Type.FLOAT)
                    return (Type.FLOAT);
                else return (Type.INT);
            if (b.op.RelationalOp( ) || b.op.BooleanOp( )) 
                return (Type.BOOL);
        }
        if (e instanceof Unary) {
            Unary u = (Unary)e;
            if (u.op.NotOp( ))        return (Type.BOOL);
            else if (u.op.NegateOp( )) return typeOf(u.term,tm);
            else if (u.op.intOp( ))    return (Type.INT);
            else if (u.op.floatOp( )) return (Type.FLOAT);
            else if (u.op.charOp( ))  return (Type.CHAR);
        }
        throw new IllegalArgumentException("should never reach here");
    } 

    public static void V (Expression e, TypeMap tm) {
        if (e instanceof Value) 
            return;
        if (e instanceof Variable) { 
            Variable v = (Variable)e;
            check( tm.containsKey(v)
                   , "undeclared variable: " + v);
            return;
        }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Type typ1 = typeOf(b.term1, tm);
            Type typ2 = typeOf(b.term2, tm);
            V (b.term1, tm);
            V (b.term2, tm);
            if (b.op.ArithmeticOp( ))  
                check( typ1 == typ2 &&
                       (typ1 == Type.INT || typ1 == Type.FLOAT)
                       , "type error for " + b.op);
            else if (b.op.RelationalOp( )) 
                check( typ1 == typ2 , "type error for " + b.op);
            else if (b.op.BooleanOp( )) 
                check( typ1 == Type.BOOL && typ2 == Type.BOOL,
                       b.op + ": non-bool operand");
            else
                throw new IllegalArgumentException("should never reach here");
            return;
        }
        // student exercise
        // Unary 구현
        if (e instanceof Unary) {   // 객체의 타입을 확인, 형변환 가능 여부를 반환
            Unary u = (Unary) e;
            Type typ = typeOf(u.term, tm);  // unary term의 type을 저장
            V(u.term, tm);  // unary term의 타당성을 체크, term에 해당하는 variable이 type map에 있는지 확인
            if (u.op.NotOp())   // ! 연산자
                check((typ == Type.BOOL), "type error for " + u.op);    // term이 bool type인지 체크, 아니면 에러 메시지 출력
            else if (u.op.NegateOp())   // - 연산자
                check((typ == Type.INT || typ == Type.FLOAT), "type error for " + u.op);   // term이 int 또는 float type인지 체크, 아니면 에러 메시지 출력
            else if (u.op.intOp())  // (int) 형변환 연산자
                check((typ == Type.CHAR || typ== Type.FLOAT),"type error for " + u.op);    // term이 char 또는 float type인지 체크, 아니면 에러 메시지 출력
            else if (u.op.floatOp())    // (float) 형변환 연산자
                check((typ == Type.INT),"type error for " + u.op);    // term이 int type인지 확인, 아니면 에러 메시지 출력
            else if (u.op.charOp())     // (char) 형변환 연산자
                check((typ == Type.INT),"type error for " + u.op);     // term이 int type인지 확인, 아니면 에러 메시지 출력
            else
                throw new IllegalArgumentException("should never reach here");
            return;
        }
    }

    public static void V (Statement s, TypeMap tm) {
        if ( s == null )
            throw new IllegalArgumentException( "AST error: null statement");
        if (s instanceof Skip) return;
        if (s instanceof Assignment) {
            Assignment a = (Assignment)s;
            check( tm.containsKey(a.target)
                   , " undefined target in assignment: " + a.target);
            V(a.source, tm);
            Type ttype = (Type)tm.get(a.target);
            Type srctype = typeOf(a.source, tm);
            if (ttype != srctype) {
                if (ttype == Type.FLOAT)
                    check( srctype == Type.INT
                           , "mixed mode assignment to " + a.target);
                else if (ttype == Type.INT)
                    check( srctype == Type.CHAR
                           , "mixed mode assignment to " + a.target);
                else
                    check( false
                           , "mixed mode assignment to " + a.target);
            }
            return;
        } 
        // student exercise
        // statement가 Block일 때 구현
        if (s instanceof Block) {
            Block b = (Block) s;
            for (int i = 0; i < b.members.size(); i++) {
                V((Statement)(b.members.get(i)), tm);
            }
            return;
        }
        // statement가 Conditional일 때 구현
        if (s instanceof Conditional) {
            Conditional c = (Conditional) s;
            V(c.test, tm);
            Type testType = typeOf(c.test, tm);
            if (testType == Type.BOOL) {
                V(c.thenbranch, tm);
                V(c.elsebranch, tm);
            }
            else
                check(false, "non-bool type in conditional test: " + c.test);
            return;
        }
        // statement가 Loop일 때 구현
        if (s instanceof Loop) {
            Loop l = (Loop) s;
            V(l.test, tm);
            Type testType = typeOf(l.test, tm);
            if (testType == Type.BOOL) {
                V(l.body, tm);
            } else
                check(false, "non-bool type in loop test: " + l.test);
            return;
        }
        throw new IllegalArgumentException("should never reach here");
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display(0);           // student exercise, Program(abstract syntax) 출력
        System.out.println("\nBegin type checking...");
        System.out.println("Type map:");
        TypeMap map = typing(prog.decpart);
        map.display();   // student exercise, type map 출력
        V(prog);
        if (typeError > 0) {    // typeError 발생 횟수를 출력하고 프로그램 종료
            System.out.println(typeError + " errors occurred");
            System.exit(1);
        }
    } //main

} // class StaticTypeCheck

