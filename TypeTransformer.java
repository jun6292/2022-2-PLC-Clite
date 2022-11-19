import java.util.*;

public class TypeTransformer {

    public static Program T (Program p, TypeMap tm) {
        Block body = (Block)T(p.body, tm);
        return new Program(p.decpart, body);
    } 

    public static Expression T (Expression e, TypeMap tm) {
        if (e instanceof Value) 
            return e;
        if (e instanceof Variable)
            return e;
        if (e instanceof Binary) {
            Binary b = (Binary)e; 
            Type typ1 = StaticTypeCheck.typeOf(b.term1, tm);
            Type typ2 = StaticTypeCheck.typeOf(b.term2, tm);
            Expression t1 = T (b.term1, tm);
            Expression t2 = T (b.term2, tm);
            if (typ1 == Type.INT) 
                return new Binary(b.op.intMap(b.op.val), t1,t2);
            else if (typ1 == Type.FLOAT) 
                return new Binary(b.op.floatMap(b.op.val), t1,t2);
            else if (typ1 == Type.CHAR) 
                return new Binary(b.op.charMap(b.op.val), t1,t2);
            else if (typ1 == Type.BOOL) 
                return new Binary(b.op.boolMap(b.op.val), t1,t2);
            throw new IllegalArgumentException("should never reach here");
        }
        // student exercise
        // Unary에 대한 형변환
        if (e instanceof Unary) {
            Unary u = (Unary)e;
            Type typ = StaticTypeCheck.typeOf(u.term, tm);  // Unary에 대한 타입을 저장
            Expression t = T(u.term, tm);   // type map에 있는 unary term을 선언

            if ((typ == Type.BOOL) && (u.op.NotOp()))   // unary의 타입이 bool이고 연산자가 ! 일 때
                return new Unary(u.op.boolMap(u.op.val), t);  // !
            else if ((typ == Type.FLOAT) && (u.op.NegateOp())) // 타입이 float이고 연산자가 - 일 때
                return new Unary(u.op.floatMap(u.op.val), t); // FLOAT_NEG -
            else if ((typ == Type.INT) && (u.op.NegateOp())) // 타입이 int이고 연산자가 - 일 때
                return new Unary(u.op.intMap(u.op.val), t); // INT_NEG -
            else if ((typ == Type.FLOAT) && (u.op.intOp())) // 타입이 float이고 연산자가 int()일 때
                return new Unary(u.op.floatMap(u.op.val), t); // F2I
            else if ((typ == Type.CHAR) && (u.op.intOp())) // 타입이 char이고 연산자가 int()일 때
                return new Unary(u.op.charMap(u.op.val), t); // C2I
            else if ((typ == Type.INT) && (u.op.floatOp())) // 타입이 int이고 연산자가 float()일 때
                return new Unary(u.op.intMap(u.op.val), t); // I2F
            else if ((typ == Type.INT) && (u.op.charOp())) // 타입이 int이고 연산자가 char()일 때
                return new Unary(u.op.intMap(u.op.val), t); // I2C
            throw new IllegalArgumentException("should never reach here");
        }
        throw new IllegalArgumentException("should never reach here");
    }

    public static Statement T (Statement s, TypeMap tm) {
        if (s instanceof Skip) return s;
        if (s instanceof Assignment) {
            Assignment a = (Assignment)s;
            Variable target = a.target;
            Expression src = T (a.source, tm);
            Type ttype = (Type)tm.get(a.target);
            Type srctype = StaticTypeCheck.typeOf(a.source, tm);
            if (ttype == Type.FLOAT) {
                if (srctype == Type.INT) {
                    src = new Unary(new Operator(Operator.I2F), src);
                    srctype = Type.FLOAT;
                }
            }
            else if (ttype == Type.INT) {
                if (srctype == Type.CHAR) {
                    src = new Unary(new Operator(Operator.C2I), src);
                    srctype = Type.INT;
                }
            }
            StaticTypeCheck.check( ttype == srctype,
                      "bug in assignment to " + target);
            return new Assignment(target, src);
        } 
        if (s instanceof Conditional) {
            Conditional c = (Conditional)s;
            Expression test = T (c.test, tm);
            Statement tbr = T (c.thenbranch, tm);
            Statement ebr = T (c.elsebranch, tm);
            return new Conditional(test,  tbr, ebr);
        }
        if (s instanceof Loop) {
            Loop l = (Loop)s;
            Expression test = T (l.test, tm);
            Statement body = T (l.body, tm);
            return new Loop(test, body);
        }
        if (s instanceof Block) {
            Block b = (Block)s;
            Block out = new Block();
            for (Statement stmt : b.members)
                out.members.add(T(stmt, tm));
            return out;
        }
        throw new IllegalArgumentException("should never reach here");
    }
    

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        System.out.println("Begin parsing... programs/" + args[0]);
        System.out.println();
        prog.display(0);    // student exercise
        System.out.println("\nBegin type checking...");
        System.out.println("Type map:");
        TypeMap map = StaticTypeCheck.typing(prog.decpart);
        map.display();    // student exercise
        StaticTypeCheck.V(prog);
        Program out = T(prog, map);
        System.out.println("\nTransformed Abstract Syntax Tree\n");
        out.display(0);    // student exercise
        // 타입 에러가 발생해도 모든 타입 에러를 체크하고 몇 개의 타입 에러가 발생했는지 출력하고 프로그램 종료
        if (StaticTypeCheck.typeError >0) {
            System.out.println(StaticTypeCheck.typeError + " error occurred");
            System.exit(1);
        }
    } //main

    } // class TypeTransformer

    
