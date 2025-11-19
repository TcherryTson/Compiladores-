/**
 * Propósito: Define a enumeração de todas as instruções (códigos de operação) que a Máquina Virtual executa.
 * Detalhes Chave: Inclui instruções para constantes (CONSTANTE), operações aritméticas/lógicas (SUMAR, NEGATIVO, IGUAL),
 * controle de fluxo (SALTAR, SALTAR_SI_FALSO, LOOP) e manipulação de variáveis (DEFINIR_GLOBAL, LEER_LOCAL).
 */
public enum OpCode {
    CONSTANTE,
    SUMAR, RESTAR, MULTIPLICAR, DIVIDIR,
    NEGATIVO, NOT,
    IGUAL, MAYOR, MENOR,
    IMPRIMIR,
    SALTAR, SALTAR_SI_FALSO,
    LOOP,
    DEFINIR_GLOBAL, LEER_GLOBAL, ASIGNAR_GLOBAL,
    LEER_LOCAL, ASIGNAR_LOCAL,
    POP,
    RETORNAR

}
