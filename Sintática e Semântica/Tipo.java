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