# 2022-2 Programming Language Concept

`JAVA`
- #1 Parser
- #2 StaticTypeCheck (추가구현: Detect all type errors ✅)
- #3 Semantics (추가구현 : DynamicTyping ✅)
- ✨ #4 Function Implementation ✨

with test programs (*.cpp)

## Interpreter

| files | summary |
|---|---|
| Token | Define tokens |
| TokenType | Define the token type of the Clite language |
| Lexer | Morphological analysis, read file and generate tokens |
| AbstractSyntax | Define abstract syntax |
| Parser | Syntactic analysis, generate Abstract Syntax Tree |
| StaticTypeCheck | Type System, detect type errors |
| TypeTransformer | Explicit type conversion and implicit type conversion |
| TypeMap | Save and display identifier with its type |
| Semantics | Perform semantic analysis of the program |
| State | Save and display state information of variables with its value |
| DynamicTyping | Dynamic typing not used in Clite |
