/**
 * Propósito: Define a enumeração dos tipos de dados primitivos da linguagem,
 * usados para a verificação de tipos na análise semântica.
 * Detalhes Chave: Tipos incluídos: ENTERO, FLOTANTE, BOOLEANO, CADENA, NULO, e INDEFINIDO.
 * O método estático desdeToken(Token tokenTipo) mapeia o token do parser para um tipo semântico.
 */

public enum Tipo {
    ENTERO,
    FLOTANTE,
    BOOLEANO,
    CADENA,
    NULO,
    INDEFINIDO;

    public static Tipo desdeToken(Token tokenTipo) {
        switch (tokenTipo.tipo) {
            case ENTERO: return ENTERO;
            case FLOTANTE: return FLOTANTE;
            case BOOLEANO: return BOOLEANO;
            case CADENA: return CADENA;
            case VAR: return INDEFINIDO;
            default: return null;
        }
    }

}
