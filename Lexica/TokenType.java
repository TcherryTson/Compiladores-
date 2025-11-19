/**
 * Propósito: Define a enumeração de todos os tipos de tokens que a linguagem suporta.
 * Detalhes Chave: Inclui tokens para símbolos de pontuação, operadores, literais e palavras-chave
 * (ex: MAS, IGUAL_IGUAL, NUMERO, SI, MIENTRAS, ENTERO, VAR).
 */
public enum TokenType {

    PARENTESIS_IZQUIERDO, PARENTESIS_DERECHO, LLAVE_IZQUIERDA, LLAVE_DERECHA,
    COMA, PUNTO, MENOS, MAS, PUNTO_Y_COMA, BARRA, ASTERISCO,

    EXCLAMACION, EXCLAMACION_IGUAL,
    IGUAL, IGUAL_IGUAL,
    MAYOR, MAYOR_IGUAL,
    MENOR, MENOR_IGUAL,

    IDENTIFICADOR, CADENA, NUMERO,

    Y, CLASE, SINO, FALSO, PARA, FUN, SI, NULO, O,
    IMPRIMIR, RETORNAR, SUPER, ESTE, VERDADERO, VAR, MIENTRAS, ENTERO, FLOTANTE, BOOLEANO,

    FIN_DE_ARCHIVO
}
