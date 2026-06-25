package lexico;

public enum TipoToken {

    // Keywords (Prioridad 1)
    KW_SCAN,
    KW_AS,
    KW_FOR,
    KW_EACH,
    KW_IN,
    KW_IF,
    KW_ELSE,
    KW_LET,
    KW_DONE,

    // Funciones integradas (Prioridad 2)
    FUNC_MOVE,
    FUNC_COPY,
    FUNC_DELETE,
    FUNC_RENAME,
    FUNC_LOG,

    // Atributos (Prioridad 2)
    ATTR_EXT,
    ATTR_SIZE,
    ATTR_NAME,
    ATTR_DATE,

    // Literales (Prioridad 3)
    LIT_RUTA,
    LIT_EXT,
    LIT_TAMANIO,
    LIT_ENTERO,
    LIT_CADENA,

    // Operadores (Prioridad 4)
    OP_IGUAL,
    OP_DISTINTO,
    OP_MAYOR_EQ,
    OP_MENOR_EQ,
    OP_MAYOR,
    OP_MENOR,
    OP_AND,
    OP_OR,
    OP_NOT,
    OP_ASIG,
    OP_SUMA,
    OP_PUNTO,

    // Delimitadores (Prioridad 4)
    LLAVE_ABRE,
    LLAVE_CIERRA,
    PAR_ABRE,
    PAR_CIERRA,
    COMA,

    // Especiales
    IDENTIFICADOR,
    EOF,
    ERROR,
    DESCONOCIDO
}
