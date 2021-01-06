import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

enum TokenType {
    INTEGER, EOF, PLUS, MINUS, MUL, DIV, LPAREN, RPAREN,
}

class Token {
    TokenType type;
    Object value;
    Token(TokenType type, Object value) {
        this.type = type;
        this.value = value;
    }
}

class Lexer {
    String text;
    char currentChar;
    int pos;
    final char None = 255;
    Lexer(String text) {
        this.text = text;
        this.pos = 0;
        this.currentChar = this.text.charAt(this.pos);
    }
    void error() {
        System.out.println("Unknown character: " + this.currentChar);
        System.exit(1);
    }
    void advance() {
        this.pos++;
        if (this.pos > this.text.length() - 1) {
            this.currentChar = None;
        } else {
            this.currentChar = this.text.charAt(this.pos);
        }
    }
    void skipWhiteSpace() {
        while (this.currentChar != None && Character.isWhitespace(this.currentChar)) {
            this.advance();
        }
    }
    Integer integer() {
        String result = "";
        while (this.currentChar != None && Character.isDigit(this.currentChar)) {
            result += this.currentChar;
            this.advance();
        }
        return Integer.parseInt(result);
    }
    Token getNextToken() {
        while (this.currentChar != None) {
            if (Character.isWhitespace(this.currentChar)) {
                this.skipWhiteSpace();
            }
            if (Character.isDigit(this.currentChar)) {
                return new Token(TokenType.INTEGER, this.integer());
            }
            if (this.currentChar == '(') {
                this.advance();
                return new Token(TokenType.LPAREN, "(");
            }
            if (this.currentChar == ')') {
                this.advance();
                return new Token(TokenType.RPAREN, ")");
            }
            if (this.currentChar == '+') {
                this.advance();
                return new Token(TokenType.PLUS, "+");
            }
            if (this.currentChar == '-') {
                this.advance();
                return new Token(TokenType.MINUS, "-");
            }
            if (this.currentChar == '*') {
                this.advance();
                return new Token(TokenType.MUL, "*");
            }
            if (this.currentChar == '/') {
                this.advance();
                return new Token(TokenType.DIV, "/");
            }
            this.error();
        }
        return new Token(TokenType.EOF, None);
    }
}

// AST Node
abstract class Node {}

class BinOp extends Node {
    Node left;
    Token op;
    Node right;
    BinOp(Node left, Token op, Node right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}
class Num extends Node {
    Token token;
    int value;
    Num(Token token) {
        this.token = token;
        this.value = Integer.parseInt(this.token.value.toString());
    }
}

// Parser
class Parser {
    Lexer lexer;
    Token currentToken;
    Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = this.lexer.getNextToken();
    }
    void error() {
        System.out.println("Syntax Error.");
        System.exit(1);
    }
    void eat(TokenType type) {
        if (this.currentToken.type == type) {
            this.currentToken = this.lexer.getNextToken();
        } else {
            this.error();
        }
    }
    Node factor() {
        Node node;
        if (this.currentToken.type == TokenType.LPAREN) {
            this.eat(TokenType.LPAREN);
            node = this.expr();
            this.eat(TokenType.RPAREN);
        } else {
            node = new Num(this.currentToken);
            this.eat(TokenType.INTEGER);
        }
        return node;
    }
    Node term() {
        Node node = this.factor();
        while (this.currentToken.type == TokenType.MUL || this.currentToken.type == TokenType.DIV) {
            Token token = this.currentToken;
            this.eat(token.type);
            node = new BinOp(node, token, this.factor());
        }
        return node;
    }
    Node expr() {
        Node node = this.term();
        while (this.currentToken.type == TokenType.PLUS || this.currentToken.type == TokenType.MINUS) {
            Token token = this.currentToken;
            this.eat(token.type);
            node = new BinOp(node, token, this.term());
        }
        return node;
    }
    Node parse() {
        return this.expr();
    }
}

abstract class Visitor {
    abstract Object visit(Node node);
}

class Interpreter extends Visitor {
    Parser parser;
    Interpreter(Parser parser) {
        this.parser = parser;
    }
    Object visit(Node node) {
        if (node instanceof BinOp) {
            return this.visitBinOp((BinOp)node);
        } else if (node instanceof Num) {
            return this.visitNum((Num)node);
        } else {
            System.out.println("Unknown Node.");
            System.exit(1);
        }
        return "";
    }
    Object visitBinOp(BinOp node) {
        return "(" + node.op.value.toString() + " " + 
        this.visit(node.left) + " " +
        this.visit(node.right) + ")";
    }
    Object visitNum(Num node) {
        return String.valueOf(node.value);
    }
    String interpretet() {
        Node tree = this.parser.parse();
        return this.visit(tree).toString();
    }
}

class Calculator {
    public static void main(String[] args) throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print("JCalc> ");
            String text = reader.readLine();
            if (text == null) continue;
            Lexer lexer = new Lexer(text);
            Parser parser = new Parser(lexer);
            Interpreter interpreter = new Interpreter(parser);
            String result = interpreter.interpretet();
            System.out.println(result);
        }
    }
}