// StaticTypeCheck.java

import java.util.*;

// Static type checking for Clite is defined by the functions 
// V and the auxiliary functions typing and typeOf.  These
// functions use the classes in the Abstract Syntax of Clite.

public class StaticTypeCheck {
    private static boolean hasReturn;   // 현재 함수에서 리턴을 했는지 판단
    private static boolean hasTypeError;    // 타입 에러가 한 번이라도 발생했는지 판단
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
        hasTypeError = true;
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

    // 전체 프로그램의 타당성 검사
    public static void V (Program p) {
        // main 함수가 있는지 검사, 없으면 에러메시지 출력
        check(p.functions.getFunctionId("main") != null, "Main function not found...");
        // 전역 변수 및 함수 이름이 unique한지 검사
        Declarations functionsOrGlobals = new Declarations();
        functionsOrGlobals.addAll(p.globals);
        functionsOrGlobals.addAll(p.functions.getAllFunctionNames());
        V(functionsOrGlobals);
        // 함수별로 검사
        V(p.functions, typing(p.globals));

        // 에러가 없다면 출력, .output 파일 참고
        if (!hasTypeError)
            System.out.println("\nNo type errors\n");
        // V (p.decpart);
        // V (p.body, typing (p.decpart));
    }

    // 함수들이 타당한지 검사
    public static void V (Functions functions, TypeMap tm) {
        for (Function function : functions) {
            // 함수의 파라미터, 로컬 변수의 TypeMap 생성
            TypeMap fMap = new TypeMap();
            fMap.putAll(tm);
            fMap.putAll(typing(function.params));
            fMap.putAll(typing(function.locals));

            // 파라미터와 로컬변수를 선언부에 추가하고 중복 검사
            Declarations localsAndParams = new Declarations();
            localsAndParams.addAll(function.locals);
            localsAndParams.addAll(function.params);
            V(localsAndParams);

            // 함수 내부의 타당성 검사
            V(function, fMap, functions);
        }
    }

    // 어떤 하나의 함수가 타당한지 검사
    public static void V(Function function, TypeMap tm, Functions functions) {
        // TypeMap 출력
        System.out.println("Function " + function.variable + " = ");
        tm.display(functions);
        hasReturn = false;  // 함수가 리턴을 했는지 판단
        // 함수 body
        Iterator<Statement> it = function.body.members.iterator();
        while (it.hasNext()) {
            Statement s = it.next();
            // Return 문장 발생, return은 단 한번만 가능
            if (s instanceof Return) {
                check(!hasReturn, "Function " + function.variable + " has multiple return statements");
                hasReturn = true;
                V(s, tm, functions);
            }
            else {
                // 리턴 뒤에 다른 문장이 있으면 에러
                check(!hasReturn, "Return must be last statement in function block");
                V(s, tm, functions);
            }
        }
        // non-void function일 경우 return문 필수
        if (!function.type.equals(Type.VOID) && !function.variable.equals("main")) {
            check(hasReturn, "Non-void function " + function.variable + " missing return statement!");
        }
        else if (function.type.equals(Type.VOID)) {
            check(!hasReturn, "Void function " + function.variable + " has return statement when it shouldn't!");
        }   // void 함수는 리턴이 있으면 안됨
    }

    // 함수의 Call
    public static void V(Call call, TypeMap tm, Functions functions)
    {
        // 호출된 함수 가져옴.
        Function function = functions.getFunctionId(call.name);

        // 호출된 함수의 파라미터와 호출 시의 매개변수 가져옴.
        Iterator<Declaration> funcIt = function.params.iterator();
        Iterator<Expression> callIt = call.args.iterator();
        // 함수의 파라미터 돌면서 체크.
        while (funcIt.hasNext()) {
            Declaration dec = funcIt.next();

            // 파라미터 개수와 매개변수 개수 체크
            check(callIt.hasNext(), "Incorrect number of arguments for function call " + call.name);
            // 없으면 종료
            if (!callIt.hasNext())
                break;

            Expression exp = callIt.next();

            // 파라미터 타입과 비교
            Type expType = typeOf(exp, tm, functions);
            check(dec.t == expType, "Wrong type in parameter for " + dec.v + " of function " + call.name + " (got a " + expType + ", expected a " + dec.t + ")");
        }

        // 파라미터 개수와 매개변수 개수 체크
        check(!callIt.hasNext(), "Incorrect number of arguments for function call " + call.name);
    }

    // Expression의 type을 TypeMap에서 가져온다.
    public static Type typeOf (Expression e, TypeMap tm, Functions functions) {
        if (e instanceof Value) return ((Value)e).type;
        if (e instanceof Variable) {
            Variable v = (Variable)e;
            check (tm.containsKey(v), "undefined variable: " + v);
            return (Type) tm.get(v);
        }
        // Call
        if (e instanceof Call)
        {
            Call c = (Call) e;
            Function f = functions.getFunctionId(c.name);
            // 선언되지 않은 함수는 에러.
            if(f == null)
            {
                check(f != null, "undefined function: " + c.name);
                return Type.VOID;
            }
            return f.type;
        }
        if (e instanceof Binary) {
            Binary b = (Binary)e;
            if (b.op.ArithmeticOp( ))
                if (typeOf(b.term1,tm, functions)== Type.FLOAT)
                    return (Type.FLOAT);
                else return (Type.INT);
            if (b.op.RelationalOp( ) || b.op.BooleanOp( )) 
                return (Type.BOOL);
        }
        if (e instanceof Unary) {
            Unary u = (Unary)e;
            if (u.op.NotOp( ))        return (Type.BOOL);
            else if (u.op.NegateOp( )) return typeOf(u.term,tm, functions);
            else if (u.op.intOp( ))    return (Type.INT);
            else if (u.op.floatOp( )) return (Type.FLOAT);
            else if (u.op.charOp( ))  return (Type.CHAR);
        }
        throw new IllegalArgumentException("should never reach here");
    } 

    // Expression을 검사
    public static void V (Expression e, TypeMap tm, Functions functions) {
        if (e instanceof Value) 
            return;
        if (e instanceof Variable) { 
            Variable v = (Variable)e;
            check( tm.containsKey(v)
                   , "undeclared variable: " + v);
            return;
        }
        // Expression 형태로 함수 call
        if (e instanceof Call)
        {
            // Expression 형태의 Call 은 non-void 만 가능
            Call call = (Call) e;
            Function function = functions.getFunctionId(call.name);
            // 함수가 선언 되었는지 확인
            if(function == null) {
                check(function != null, "undeclared function: " + call.name);
            }
            else {
                check(!function.type.equals(Type.VOID), "Call Expression must be a non-void type function!");
                V((Call) e, tm, functions);
            }
            return;
        }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Type typ1 = typeOf(b.term1, tm, functions);
            Type typ2 = typeOf(b.term2, tm, functions);
            V (b.term1, tm, functions);
            V (b.term2, tm, functions);
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
            Type typ = typeOf(u.term, tm, functions);  // unary term의 type을 저장
            V(u.term, tm, functions);  // unary term의 타당성을 체크, term에 해당하는 variable이 type map에 있는지 확인
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

    public static void V (Statement s, TypeMap tm, Functions functions) {
        if ( s == null )
            throw new IllegalArgumentException( "AST error: null statement");
        if (s instanceof Skip) return;
        if (s instanceof Assignment) {
            Assignment a = (Assignment)s;
            check( tm.containsKey(a.target)
                   , " undefined target in assignment: " + a.target);
            V(a.source, tm, functions);
            Type ttype = (Type)tm.get(a.target);
            Type srctype = typeOf(a.source, tm, functions);
            // 함수의 리턴형과 연산 타입이 같아야 함
            // target, source 타입이 서로 다를 경우
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
            // Block 내의 모든 Statement 검사
            for (Statement statement : b.members)
                V(statement, tm, functions);
            return;
        }
        // statement가 Conditional일 때 구현
        if (s instanceof Conditional) { // 조건문, then, else 각각을 검사
            Conditional c = (Conditional) s;
            boolean ifReturn = false, elseReturn = false;

            V(c.test, tm, functions);  // if 문의 test 문장이 타당한지 검사
            Type testType = typeOf(c.test, tm, functions);
            if (testType == Type.BOOL) {    // test 문장이 bool type일 때 (bool type이면 에러 X)
                V(c.thenbranch, tm, functions);    // then이 타당한지 검사
                ifReturn = hasReturn;
                hasReturn = false;

                V(c.elsebranch, tm, functions);    // else가 타당한지 검사
                elseReturn = hasReturn;

                // if와 else 둘 다에서 Return 하면 Return 한 것.
                // 아니면 다음에 Return 이 나와야 함.
                hasReturn = ifReturn && elseReturn;
                return;
            }
            else
                check(false, "non-bool type in conditional test: " + c.test);   // test 문장이 bool이 아니면 type error
            return;
        }
        // statement가 Loop일 때 구현
        if (s instanceof Loop) {
            Loop l = (Loop) s;
            V(l.test, tm, functions);  // while 문의 test 문장이 타당한지 검사
            Type testType = typeOf(l.test, tm, functions);
            if (testType == Type.BOOL) {    // test 문장이 bool type일 때 (bool type이면 에러 X)
                V(l.body, tm, functions);  // while 문의 body가 타당한지 검사
                hasReturn = false;
                return;
            } else
                check(false, "non-bool type in loop test: " + l.test);  // test 문장이 bool이 아니면 type error
            return;
        }
        // Statement 형태로 함수 call
        // Call 만 있는 경우
        if (s instanceof Call)  // Statement 형태의 Call 은 void 만 가능.
        {
            Call call = (Call) s;
            Function function = functions.getFunctionId(call.name);
            check(function.type.equals(Type.VOID), "Call Statement must be a void type function!");
            V((Call) s, tm, functions);
            return;
        }
        // Return
        if (s instanceof Return)
        {
            // Return
            hasReturn = true;

            Return r = (Return) s;
            Function f = functions.getFunctionId(r.target.toString());
            Type t = typeOf(r.result, tm, functions);
            check(t.equals(f.type), "Return expression doesn't match function's return type! (got a " + t + ", expected a " + f.type + ")");

            return;
        }
        throw new IllegalArgumentException("should never reach here");
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        System.out.println("Begin parsing... programs/" + args[0]);
        System.out.println();
        prog.display();           // student exercise, Program(abstract syntax) 출력
        System.out.println("\nBegin type checking...");
        System.out.print("Globals = ");
        TypeMap map = typing(prog.globals);
        map.display();   // student exercise, type map 출력
        V(prog);
        // 타입 에러가 발생해도 모든 타입 에러를 체크하고 몇 개의 타입 에러가 발생했는지 출력하고 프로그램 종료
        if (typeError > 0) {
            System.out.println(typeError + " errors occurred");
            System.exit(1);
        }
    } //main

} // class StaticTypeCheck

