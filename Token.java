
public class Token {

    final TokenType tipo;
    final String lexema;
    final Object literal;
    final int linea;


    Token(TokenType tipo, String lexema, Object literal, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.literal = literal;
        this.linea = linea;
    }

    public String toString() {
        return tipo + " " + lexema + " " + literal;
    }
}