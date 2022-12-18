import java.util.*;

public class TypeTransformer {

    public static Program T (Program p, TypeMap tm) {
        // 전역 변수 TypeMap
        TypeMap globalMap = StaticTypeCheck.typing(p.globals);
        Functions functions = p.functions;

        // Transform 후의 Functions
        Functions transformedFunctions = new Functions();
        // 모든 함수에 대해 transform 진행.
        for (Function f : p.functions) {
            // 매개변수, 로컬변수 파라미터
            TypeMap fMap = new TypeMap();
            fMap.putAll(globalMap);
            fMap.putAll(StaticTypeCheck.typing(f.params));
            fMap.putAll(StaticTypeCheck.typing(f.locals));

            // 함수의 body Transform
            Block transformedBody = (Block) T(f.body, fMap, functions);

            // Function Transform
            transformedFunctions.add(new Function(f.type, f.variable, f.params, f.locals, transformedBody));
        }
        return new Program(p.globals, transformedFunctions);
        // Block body = (Block)T(p.body, tm);
        // return new Program(p.decpart, body);
    } 

    public static Expression T (Expression e, TypeMap tm, Functions functions) {
        if (e instanceof Value) 
            return e;
        if (e instanceof Variable)
            return e;
        if (e instanceof Call) {
            Call c = (Call) e;
            ArrayList<Expression> transformedExpression = new ArrayList<>();
            // args 모두 순회하며 Transform
            for (Expression expression : c.args) {
                transformedExpression.add(T(expression, tm, functions));
            }
            return new Call(c.name, transformedExpression);
        }
        if (e instanceof Binary) {
            Binary b = (Binary)e; 
            Type typ1 = StaticTypeCheck.typeOf(b.term1, tm, functions);
            Type typ2 = StaticTypeCheck.typeOf(b.term2, tm, functions);
            Expression t1 = T (b.term1, tm, functions);
            Expression t2 = T (b.term2, tm, functions);
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
            Type typ = StaticTypeCheck.typeOf(u.term, tm, functions);  // Unary에 대한 타입을 저장
            Expression t = T(u.term, tm, functions);   // type map에 있는 unary term을 선언

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

    public static Statement T (Statement s, TypeMap tm, Functions functions) {
        if (s instanceof Skip) return s;
        if (s instanceof Assignment) {
            Assignment a = (Assignment)s;
            Variable target = a.target;
            Expression src = T (a.source, tm, functions);
            Type ttype = (Type)tm.get(a.target);
            Type srctype = StaticTypeCheck.typeOf(a.source, tm, functions);
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
            Expression test = T (c.test, tm, functions);
            Statement tbr = T (c.thenbranch, tm, functions);
            Statement ebr = T (c.elsebranch, tm, functions);
            return new Conditional(test,  tbr, ebr);
        }
        if (s instanceof Loop) {
            Loop l = (Loop)s;
            Expression test = T (l.test, tm, functions);
            Statement body = T (l.body, tm, functions);
            return new Loop(test, body);
        }
        if (s instanceof Block) {
            Block b = (Block)s;
            Block out = new Block();
            for (Statement stmt : b.members)
                out.members.add(T(stmt, tm, functions));
            return out;
        }
        // Call 일 경우
        if (s instanceof Call) {
            Call c = (Call) s;
            ArrayList<Expression> transformedExpression = new ArrayList<>();
            // args 모두 순회하며 Transform
            for (Expression expression : c.args)
                transformedExpression.add(T(expression, tm, functions));
            return new Call(c.name, transformedExpression);
        }
        // Return 일 경우
        if (s instanceof Return) {
            Return r = (Return) s;
            return new Return(r.target, T(r.result, tm, functions));
        }
        throw new IllegalArgumentException("should never reach here");
    }
    

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        System.out.println("Begin parsing... programs/" + args[0]);
        System.out.println();
        prog.display();    // student exercise
        System.out.println("\nBegin type checking...");
        System.out.println("Globals = ");
        TypeMap map = StaticTypeCheck.typing(prog.globals);
        map.display();    // student exercise
        StaticTypeCheck.V(prog);
        Program out = T(prog, map);
        System.out.println("\nTransformed Abstract Syntax Tree\n");
        out.display();    // student exercise
        // 타입 에러가 발생해도 모든 타입 에러를 체크하고 몇 개의 타입 에러가 발생했는지 출력하고 프로그램 종료
        if (StaticTypeCheck.typeError > 0) {
            System.out.println(StaticTypeCheck.typeError + " error occurred");
            System.exit(1);
        }
        else
            System.out.println("No type erros\n");
    } //main

    } // class TypeTransformer

    
