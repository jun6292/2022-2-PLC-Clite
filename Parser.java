import java.util.*;

public class Parser {
    // Recursive descent parser that inputs a C++Lite program and
    // generates its abstract syntax. Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.

    Token token; // current token from the input stream
    Lexer lexer;
    Variable functionId;    // 함수명

    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts; // as a token stream, and
        token = lexer.next(); // retrieve its first Token
    }

    private String match(TokenType t) {
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }

    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok + "; saw: " + token);
        System.exit(1);
    }

    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok + "; saw: " + token);
        System.exit(1);
    }

    public Program program() {
        // Program-->{ Type Identifier FunctionOrGlobal }
        //              MainFunction
        Declarations globals = new Declarations();
        Functions functions = new Functions();

        while (isType()) {
            FunctionOrGlobal(globals, functions);
        }
        // main() {
        TokenType[] header = {TokenType.Main, TokenType.LeftParen, TokenType.RightParen, TokenType.LeftBrace };
        for (int i = 0; i < header.length; i++)
            match(header[i]);

        Declarations locals = declarations();   // 메인함수의 로컬변수
        Block body = new Block();   // 메인함수의 body

        while (isStatement()) { // Statement이면 block에 추가
            body.members.add(statement());
        }

        match(TokenType.RightBrace);    // '}'
        // main 함수 정보를 functions에 추가
        functions.add(new Function(Type.INT, "main", new Declarations(), locals, body));
        return new Program(globals, functions);
    }

    private boolean isStatement() { // statement가 될 수 있는 토큰임을 판단
        return token.type().equals(TokenType.Semicolon) // Skip
                || token.type().equals(TokenType.LeftBrace) // Block
                || token.type().equals(TokenType.If)    // IfStatement
                || token.type().equals(TokenType.While) // WhileStatement
                || token.type().equals(TokenType.Identifier);   // Assignment
    }

    // FunctionOrGlobal --> ( Parameters ) { Declarations Statements } |
    //                                      Global
    private void FunctionOrGlobal(Declarations globals, Functions functions) {
        Type t = type();
        if (t.equals(Type.INT) && token.type().equals(TokenType.Main)) {
            return;
        }
        Variable var = new Variable(match(TokenType.Identifier));   // 식별자
        // main 함수가 아닌 함수일 때: functionId ( parameters ) { Declarations Statements }
        if (token.type().equals(TokenType.LeftParen)) { // (
            functionId = var;
            token = lexer.next();
            // Parameters
            Declarations params;
            if (token.type().equals(TokenType.RightParen)) {
                params = new Declarations();
            } else {
                params = parameters();
            }
            match(TokenType.RightParen);    // )
            match(TokenType.LeftBrace); // {
            Declarations locals = declarations();   // Declarations
            Block b = new Block();

            while (token.type().equals(TokenType.Semicolon) || token.type().equals(TokenType.LeftBrace)
                    || token.type().equals(TokenType.Identifier) || token.type().equals(TokenType.If)
                    || token.type().equals(TokenType.While) || token.type().equals(TokenType.Return)) {
                b.members.add(statement());
            }   // Statements

            match(TokenType.RightBrace);    // }
            functions.add(new Function(t, var.toString(), params, locals, b)); // functions에 추가
        }
        else {    // globals 일 때
            globals.add(new Declaration(var, t));
            while (token.type().equals(TokenType.Comma)) {  // , identifer, identifier, ...
                token = lexer.next();
                var = new Variable(match(TokenType.Identifier));
                globals.add(new Declaration(var, t)); // globals에 추가
            }
            match(TokenType.Semicolon); // ;
        }
    }

    private Declarations declarations() {
        // Declarations --> { Declaration }
        // Declaration이 0번 이상 존재
        Declarations decs = new Declarations();
        while (isType())
            declaration(decs);
        return decs;  // student exercise

    }

    private void declaration(Declarations ds) {
        // Declaration --> Type Identifier { , Identifier } ;
        // student exercise
        Type t = type();
        Variable var;
        Declaration dec;

        while (true) {
            var = new Variable(match(TokenType.Identifier)); // 변수 생성
            dec = new Declaration(var, t); // 선언문 생성
            ds.add(dec); // 선언부에 추가
            if (!token.type().equals(TokenType.Comma)) // ',' 가 아니면 반복 종료
                break;
            token = lexer.next();   // 다음 토큰을 읽어온다.
        }
        match(TokenType.Semicolon); // 선언문의 마지막을 세미콜론과 match
    }

    private Declarations parameters() { // 함수의 매개변수들
        Declarations parameters = new Declarations();
        Type t = type();
        Variable var = new Variable(match(TokenType.Identifier));

        parameters.add(new Declaration(var, t));

        while (token.type().equals(TokenType.Comma)) {
            token = lexer.next();
            t = type();
            var = new Variable(match(TokenType.Identifier));
            parameters.add(new Declaration(var, t));
        }
        return parameters;
    }

    private Type type() {
        // Type --> int | bool | float | char
        Type t = null;
        // student exercise
        // 토큰이 int, float, char, bool이면 반환, 아니면 syntax error
        if (token.type().equals(TokenType.Int))
            t = Type.INT;
        else if (token.type().equals(TokenType.Float))
            t = Type.FLOAT;
        else if (token.type().equals(TokenType.Char))
            t = Type.CHAR;
        else if (token.type().equals(TokenType.Bool))
            t = Type.BOOL;
        else if (token.type().equals(TokenType.Void))
            t = Type.VOID;
        else
            error("Error in type"); // syntax error
        token = lexer.next();
        return t;
    }

    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        // 각 Statement에 맞게 반환해준다. Statement가 아니라면 Error호출
        Statement s = null;

        // student exercise
        if (token.type().equals(TokenType.Semicolon))
            s = new Skip();    // 토큰이 ';'이면 Skip을 반환
        else if (token.type().equals(TokenType.LeftBrace))
            s = statements();    // 토큰이 '{'이면 statement를 반환
        else if (token.type().equals(TokenType.If))
            s = ifStatement();    // 토큰이 If이면 ifStatement를 반환
        else if (token.type().equals(TokenType.While))
            s = whileStatement();    // 토큰이 While이면 whileStatement를 반환
        else if (token.type().equals(TokenType.Identifier)) {
            Variable var = new Variable(match(TokenType.Identifier));
            if (token.type().equals(TokenType.LeftParen)) { // 토큰이 ( 일 때, 함수 호출이므로
                s = callStatement(var); // callStatement 반환
            }
            else {
                s = assignment(var);   // 토큰이 ( 가 아니면 assignment 반환
            }
        }
        else if (token.type().equals(TokenType.Return)) // 토큰이 Return이면 returnStatement 반환
            s = returnStatement();
        else
            error("Error in statement");   // syntax error
        return s;
    }

    private Block statements() {
        // Block --> '{' Statements '}'
        // Blcok 을 구성
        Block b = new Block();
        // student exercise
        match(TokenType.LeftBrace); // '{'와 match
        while (isStatement()) {     // Statement임을 판단
            b.members.add(statement()); // statement를 블록의 멤버에 추가
        }
        match(TokenType.RightBrace); // '}'와 match
        return b; // block 반환
    }

    private Call callStatement(Variable var){  // callStatement 추가
        // CallStatement -> Call;
        // Call -> Identifier ( Arguments )
        match(TokenType.LeftParen); // (
        Expressions args = new Expressions();
        // ) 괄호 닫는 것 까지 돌면서 Arguments 추가
        while(!(token.type().equals(TokenType.RightParen))){
            args.add(expression());
            if (!token.type().equals(TokenType.RightParen))
                match(TokenType.Comma);
        }
        match(TokenType.RightParen);    // )
        match(TokenType.Semicolon);     // ;
        return new Call(var.toString(), args);   // 함수명(인자, 인자, ...);
    }

    private Return returnStatement(){   // returnStatement 추가
        match(TokenType.Return);    // return
        Expression returning = expression();    // return 다음의 expression
        match(TokenType.Semicolon); // ;
        return new Return(functionId, returning);
    }

    private Assignment assignment(Variable target) {
        // Assignment --> Identifier = Expression ;
        // assigment 구성
        // Variable과 Expression 객체를 생성하고 Assignment를 만들어 반환
        match(TokenType.Assign);    // Assign과 match
        Expression source = expression();
        match(TokenType.Semicolon); // ';'과 match

        return new Assignment(target, source);  // student exercise
    }

    private Conditional ifStatement() {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
        // ifStatement 구성
        match(TokenType.If);    // if 와 match
        match(TokenType.LeftParen); // '('와 match
        Expression e = expression();    // test 문장
        match(TokenType.RightParen);    // ')'와 match
        Statement thenbr = statement(); // then 문장
        // else 토큰이 존재할 경우 처리
        if (token.type().equals(TokenType.Else)) {
            match(TokenType.Else);  // else와 match
            return new Conditional(e, thenbr, statement()); // if-else 구문
        }
        return new Conditional(e, thenbr);  // student exercise
    }

    private Loop whileStatement() {
        // WhileStatement --> while ( Expression ) Statement
        // while statement를 구성
        Statement body;
        Expression test;

        match(TokenType.While); // while과 match
        match(TokenType.LeftParen); // '('와 match
        test = expression();    // test 문장
        match(TokenType.RightParen);    // ')'와 match
        body = statement();     // while문의 body
        return new Loop(test, body);    // student exercise
    }

    private Expression expression() {
        // Expression --> Conjunction { || Conjunction }
        // || 연산
        Expression conj = conjunction();
        while (token.type().equals(TokenType.Or)) { // || 인지 확인
            Operator op = new Operator(match(token.type()));    // Or과 match
            Expression e = expression();
            conj = new Binary(op, conj, e); // or 연산 생성
        }
        return conj;  // student exercise
    }

    private Expression conjunction() {
        // Conjunction --> Equality { && Equality }
        // && 연산
        Expression equal = equality();
        while (token.type().equals(TokenType.And)) {    // && 인지 확인
            Operator op = new Operator(match(token.type()));    // And와 match
            Expression conj = conjunction();
            equal = new Binary(op, equal, conj);    // and 연산 생성
        }
        return equal;  // student exercise
    }

    private Expression equality() {
        // Equality --> Relation [ EquOp Relation ]
        // != == 연산
        Expression rel = relation();
        while (isEqualityOp()) {    // !=, == 인지 확인
            Operator op = new Operator(match(token.type()));
            Expression rel2 = relation();
            rel = new Binary(op, rel, rel2);    // !=, == 연산 생성
        }
        return rel;  // student exercise
    }

    private Expression relation() {
        // Relation --> Addition [RelOp Addition]
        // <, >, <=, >= 연산
        Expression e = addition();
        while (isRelationalOp()) {  // <, >, <=, >= 인지 확인
            Operator op = new Operator(match(token.type()));
            Expression e2 = addition();
            e = new Binary(op, e, e2);  // <, >, <=, >= 연산 생성
        }
        return e;  // student exercise
    }

    private Expression addition() {
        // Addition --> Term { AddOp Term }
        // + - 연산
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);   // +, - 연산 생성
        }
        return e;   // student exercise
    }

    private Expression term() {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }

    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(op, term);
        } else
            return primary();
    }

    private Expression primary() {
        // Primary --> Identifier | Literal | ( Expression )
        // | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            e = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();
            match(TokenType.RightParen);
        } else if (isType()) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else
            error("Identifier | Literal | ( | Type");
        return e;
    }

    private Value literal() {
        // student exercise
        // literal value를 반환
        Value v = null;
        if(token.type().equals(TokenType.IntLiteral))
            v = new IntValue(Integer.parseInt(match(TokenType.IntLiteral)));
        else if(token.type().equals(TokenType.CharLiteral))
            v = new CharValue(match(TokenType.CharLiteral).charAt(0));
        else if(token.type().equals(TokenType.FloatLiteral))
            v = new FloatValue(Float.parseFloat(match(TokenType.FloatLiteral)));
        else if(token.type().equals(TokenType.True))
            v = new BoolValue(Boolean.parseBoolean(match(TokenType.True)));
        else if(token.type().equals(TokenType.False))
            v = new BoolValue(Boolean.parseBoolean(match(TokenType.False)));
        else
            error("Error in literal value");
        return v;  // student exercise
    }

    private boolean isAddOp() {
        return token.type().equals(TokenType.Plus) || token.type().equals(TokenType.Minus);
    }

    private boolean isMultiplyOp() {
        return token.type().equals(TokenType.Multiply) || token.type().equals(TokenType.Divide);
    }

    private boolean isUnaryOp() {
        return token.type().equals(TokenType.Not) || token.type().equals(TokenType.Minus);
    }

    private boolean isEqualityOp() {
        return token.type().equals(TokenType.Equals) || token.type().equals(TokenType.NotEqual);
    }

    private boolean isRelationalOp()  {
        return token.type().equals(TokenType.Less) || token.type().equals(TokenType.LessEqual)
                || token.type().equals(TokenType.Greater) || token.type().equals(TokenType.GreaterEqual);
    }

    private boolean isType() {
        return token.type().equals(TokenType.Int) || token.type().equals(TokenType.Bool)
                || token.type().equals(TokenType.Float) || token.type().equals(TokenType.Char) || token.type().equals(TokenType.Void);
    }

    private boolean isLiteral() {
        return token.type().equals(TokenType.IntLiteral) || isBooleanLiteral()
                || token.type().equals(TokenType.FloatLiteral) || token.type().equals(TokenType.CharLiteral);
    }

    private boolean isBooleanLiteral() {
        return token.type().equals(TokenType.True) || token.type().equals(TokenType.False);
    }

    public static void main(String args[]) {
        Parser parser = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.printDisplay(args[0]); // 프로그램의 도입부 출력, 파일 이름 출력
        prog.display(); // display abstract syntax tree
    } // main
} // Parser
