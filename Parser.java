import java.util.*;

public class Parser {
    // Recursive descent parser that inputs a C++Lite program and 
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.
  
    Token token;          // current token from the input stream
    Lexer lexer;
  
    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }
  
    private String match (TokenType t) {
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }
  
    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    public Program program() {
        // Program --> void main ( ) '{' Declarations Statements '}'
        TokenType[ ] header = {TokenType.Int, TokenType.Main,
                          TokenType.LeftParen, TokenType.RightParen};
        for (int i=0; i<header.length; i++)   // bypass "int main ( )"
            match(header[i]);
        match(TokenType.LeftBrace);

        // student exercise
        Declarations decs = declarations();
        Block blk = new Block();
        while (true) {
            blk.members.add(statement());
        }

        match(TokenType.RightBrace);

        return Program(decs, blk);  // student exercise
    }
  
    private Declarations declarations () {
        // Declarations --> { Declaration }
        // Declarations 객체를 생성하고 반환
        Declarations decs = new Declarations();
        while (isType())
            declaration(decs);

        return decs;  // student exercise
    }
  
    private void declaration (Declarations ds) {
        // Declaration  --> Type Identifier { , Identifier } ;
        // student exercise
        Type t = type();
        Variable var = new Variable(match(TokenType.Identifier));
        Declaration dec = new Declaration(var, t);
        decs.add(dec);

        // 토큰이 Comma일 경우에만 반복
        while (token.type().equals(TokenType.Comma)) {
            token = lexer.next();
            var = new Variable(match(TokenType.Identifier));
            dec = new Declaration(var, t);
            decs.add(dec);
        }
        match(TokenType.Semicolon); // declaration의 마지막을 세미콜론과 match, 다음 토큰을 읽어온다.
    }
  
    private Type type () {
        // Type  -->  int | bool | float | char 
        Type t = null;
        // student exercise
        // 토큰이 int, float, char, bool이면 반환, 아니면 에러
        if (token.type().equals(TokenType.Int))
            t = Type.INT;
        else if (token.type().equals(TokenType.Float))
            t = Type.FLOAT;
        else if (token.type().equals(TokenType.Char))
            t = Type.CHAR;
        else if (token.type().equals(TokenType.Bool))
            t = Type.BOOL;
        else
            error("Error in type Construction");
        token = lexer.next();
        return t;          
    }
  
    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        Statement s = null;

        // student exercise
        if (token.type().equals(TokenType.Semicolon))
            s = new Skip();    // 토큰이 ;이면 Skip을 반환
        else if (token.type().equals(TokenType.LeftBrace))
            s = statement();    // 토큰이 {이면 statement를 반환
        else if (token.type().equals(TokenType.Identifier))
            s = assignment();   // 토큰이 Identifier이면 assignment를 반환
        else if (token.type().equals(TokenType.If))
            s = ifStatement();    // 토큰이 If이면 ifStatement를 반환
        else if (token.type().equals(TokenType.While))
            s = whileStatement();    // 토큰이 While이면 whileStatement를 반환
        else
            error("Error in Statement construction");   // 올바르지 않은 토큰에 대한 처리
        return s;
    }
  
    private Block statements () {
        // Block --> '{' Statements '}'
        Block b = new Block();

        // student exercise
        Statement s;
        match(TokenType.LeftBrace); // '{'와 match
        // Statements 처리
        while (token.type().equals(TokenType.Semicolon)
                || token.type().equals(TokenType.LeftBrace)
                || token.type().equals(TokenType.Indentifier)
                || token.type().equals(TokenType.If)
                || token.type().equals(TokenType.While)) {
            s = statement();
            b.members.add(s);
        }
        match(TokenType.LeftBrace); // '}'와 match
        return b;
    }
  
    private Assignment assignment () {
        // Assignment --> Identifier = Expression ;
        // Variable과 Expression 객체를 생성하고 Assignment 만들어 반환
        Variable var = new Variable(match(TokenType.Identifier));
        match(TokenType.Assign);    // 토큰타입을 Assign과 match
        Expression e = expression();
        match(TokenType.Semicolon); // 토큰타입을 ;과 match

        Assignment assign = new Assignment(var, e);
        return assign;  // student exercise
    }
  
    private Conditional ifStatement () {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
        // Conditional ifStatement 객체를 생성하고 반환
        Conditional condi;
        match(TokenType.If);    // if 와 match
        match(TokenType.LeftParen); // '('와 match
        Expresison e = expression();    // test 문장
        match(TokenType.RightParen);    // ')'와 match
        Statement thenbr = statement(); // then 문장
        condi = new Conditional(e, thenbr); // if 구문
        // else가 존재할 경우 처리
        if (token.type().equals(TokenType.Else)) {
            match(TokenType.Else);  // else와 match
            Statement elsebr = statement(); // else 문장
            return new Conditional(e, thenbr, elsebr); // if-else 구문
        }
        return condi;  // student exercise
    }
  
    private Loop whileStatement () {
        // WhileStatement --> while ( Expression ) Statement
        //
        match(TokenType.While); // while과 match
        match(TokenType.LeftParen); // '('와 match
        Expression e = expression();    // test 문장
        match(TokenTyep.rightParen);    // ')'와 match
        Statement body = statement();   // while문의 body
        Loop loop = new Loop(e, body);  // while 구문
        return loop;  // student exercise
    }

    private Expression expression () {
        // Expression --> Conjunction { || Conjunction }
        Expression conj = conjunction();
        // ||
        while (token.type().equals(TokenType.Or)); {
            Operator op = new Operator(match(token.type()));
            Expression e = expression();
            conj = new Binary(op, conj, e);
        }
        return conj;  // student exercise
    }
  
    private Expression conjunction () {
        // Conjunction --> Equality { && Equality }
        Expression equal = equality();
        // &&
        while (token.type().equals(TokenType.And)) {
            Operator op = new Operator(match(token.type()));
            Expression conj = conjunction();
            equal = new Binary(op, equal, conj);
        }
        return equal;  // student exercise
    }
  
    private Expression equality () {
        // Equality --> Relation [ EquOp Relation ]
        Expression rel = relation();
        // ==, !=
        while (isEqualityOp()) {
            Operator op = new Operator(match(token.type()));
            Expression rel2 = relation();
            rel = new Binary(op, rel, rel2);
        }
        // 연산자 우선순위의 영향, relation() 반환
        return rel;  // student exercise
    }

    private Expression relation (){
        // Relation --> Addition [RelOp Addition]
        Expression e = addition();
        // <, >, <=, >=
        while (isRelationalOp()) {
            Operator op = new Operator(match(token.type()));
            Expression e2 = addition();
            e = new Binary(op, e, e2);
        }
        // 연산자 우선순위의 영향, addition() 반환
        return e;  // student exercise
    }
  
    private Expression addition () {
        // Addition --> Term { AddOp Term }
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression term () {
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
        }
        else return primary();
    }
  
    private Expression primary () {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            e = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();       
            match(TokenType.RightParen);
        } else if (isType( )) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }

    private Value literal( ) {
        // int, float, char, bool형 literal value를 반환
        Value val = null;
        if (token.type().equals(TokenType.IntLiteral))
            val = new IntValue(Integer.parseInt(match(TokenType.IntLiteral)));
        else if (token.type().equals(TokenType.FloatLiteral))
            val = new FloatValue(Float.parseFloat(match(TokenType.FloatLiteral)));
        else if (token.type().equals(TokenType.CharLiteral))
            val = new CharValue(match(TokenType.CharLiteral).charAt(0));
        else if (token.type().equals(TokenType.True))
            val = new BoolValue(Boolean.parseBoolean(match(TokenType.True)));
        else if (token.type().equals(TokenType.False))
            val = new BoolValue(Boolean.parseBoolean(match(TokenType.False)));
        return val;  // student exercise
    }
  

    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
               token.type().equals(TokenType.Divide);
    }
    
    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
            token.type().equals(TokenType.NotEqual);
    }
    
    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
               token.type().equals(TokenType.LessEqual) || 
               token.type().equals(TokenType.Greater) ||
               token.type().equals(TokenType.GreaterEqual);
    }
    
    private boolean isType( ) {
        return token.type().equals(TokenType.Int)
            || token.type().equals(TokenType.Bool) 
            || token.type().equals(TokenType.Float)
            || token.type().equals(TokenType.Char);
    }
    
    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
            isBooleanLiteral() ||
            token.type().equals(TokenType.FloatLiteral) ||
            token.type().equals(TokenType.CharLiteral);
    }
    
    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
            token.type().equals(TokenType.False);
    }
    
    public static void main(String args[]) {
        Parser parser = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.printDisplay(args[0]);           // display abstract syntax tree
        prog.display(0);
    } //main

} // Parser
