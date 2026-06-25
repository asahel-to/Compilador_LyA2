package parser;

import java.util.HashMap;
import java.util.Map;

public class TablaErrores {
    // --- LEXICO (101-109) ---
    public static final int ERROR_SIMBOLO_ALFABETO = 101;
    public static final int ERROR_TOKEN_NO_RECONOCIDO = 102;
    public static final int ERROR_KW_INCOMPLETA = 103;
    public static final int ERROR_FN_INCOMPLETA = 104;
    public static final int ERROR_ATTR_INCOMPLETO = 105;
    public static final int ERROR_KW_MALFORMADA = 106;
    public static final int ERROR_FN_MALFORMADA = 107;
    public static final int ERROR_ATTR_MALFORMADO = 108;
    public static final int ERROR_LITERAL_MALFORMADO = 109;

    // --- SINTACTICO (110-115) ---
    public static final int ERROR_SCAN_INICIO = 110;
    public static final int ERROR_INSTRUCCION_INVALIDA = 111;
    public static final int ERROR_SINTACTICO_GENERAL = 112;
    public static final int ERROR_EXPRESION_INVALIDA = 113;
    public static final int ERROR_ATRIBUTO_INVALIDO = 114;
    public static final int ERROR_FUNCION_INVALIDA_SIN = 115;

    // --- SEMANTICO (116-121) ---
    public static final int ERROR_NO_DECLARADO = 116;
    public static final int ERROR_YA_DECLARADO = 117;
    public static final int ERROR_ARGUMENTOS_CANTIDAD = 118;
    public static final int ERROR_ARGUMENTOS_TIPO = 119;
    public static final int ERROR_ATRIBUTO_TIPO = 120;
    public static final int ERROR_FUERA_ALCANCE = 121;

    private static final Map<Integer, String> mapaErrores = new HashMap<>();

    static {
        mapaErrores.put(ERROR_SCAN_INICIO, "Se espera 'scan' al inicio del programa.");
        mapaErrores.put(ERROR_INSTRUCCION_INVALIDA, "Instrucci\u00f3n no v\u00e1lida.");
        mapaErrores.put(ERROR_SINTACTICO_GENERAL, "Error sint\u00e1ctico.");
        mapaErrores.put(ERROR_EXPRESION_INVALIDA, "Error en expresi\u00f3n.");
        mapaErrores.put(ERROR_ATRIBUTO_INVALIDO, "Atributo inv\u00e1lido.");
        mapaErrores.put(ERROR_FUNCION_INVALIDA_SIN, "Funci\u00f3n no reconocida.");
        mapaErrores.put(ERROR_SIMBOLO_ALFABETO, "S\u00edmbolo no definido en el alfabeto.");
        mapaErrores.put(ERROR_TOKEN_NO_RECONOCIDO, "Token no reconocido.");
        mapaErrores.put(ERROR_NO_DECLARADO, "Variable no declarada.");
        mapaErrores.put(ERROR_YA_DECLARADO, "Variable ya declarada.");
        mapaErrores.put(ERROR_ARGUMENTOS_CANTIDAD, "N\u00famero incorrecto de argumentos.");
        mapaErrores.put(ERROR_ARGUMENTOS_TIPO, "Tipo de argumento incorrecto.");
        mapaErrores.put(ERROR_ATRIBUTO_TIPO, "Atributo no v\u00e1lido para el tipo.");
        mapaErrores.put(ERROR_FUERA_ALCANCE, "Variable fuera de su alcance.");
        mapaErrores.put(ERROR_KW_INCOMPLETA, "Palabra clave incompleta.");
        mapaErrores.put(ERROR_FN_INCOMPLETA, "Nombre de funci\u00f3n incompleto.");
        mapaErrores.put(ERROR_ATTR_INCOMPLETO, "Nombre de atributo incompleto.");
        mapaErrores.put(ERROR_KW_MALFORMADA, "Palabra clave con caracteres extra.");
        mapaErrores.put(ERROR_FN_MALFORMADA, "Nombre de funci\u00f3n con caracteres extra.");
        mapaErrores.put(ERROR_ATTR_MALFORMADO, "Nombre de atributo con caracteres extra.");
        mapaErrores.put(ERROR_LITERAL_MALFORMADO, "Literal mal formado.");
    }

    public static String getMensaje(int codigo) {
        return mapaErrores.getOrDefault(codigo, "Error desconocido");
    }

    public static Map<Integer, String> getMapaErrores() {
        return mapaErrores;
    }
}
