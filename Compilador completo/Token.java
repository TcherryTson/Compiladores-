/**
 * Propósito: Representa uma unidade léxica (token) gerada pelo scanner.
 * Detalhes Chave: Armazena quatro atributos essenciais: o tipo (TokenType), o lexema (o texto original),
 * o valor literal (para números e strings), e a linea onde o token foi encontrado.
 */
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
