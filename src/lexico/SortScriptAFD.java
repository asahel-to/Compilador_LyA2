package lexico;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SortScriptAFD {

    // ─────────────────────────────────────────────────────────────
    // Método principal y único de esta clase.
    // Construye y devuelve un AFD completamente configurado
    // con todos los estados, transiciones y palabras reservadas
    // del lenguaje SortScript.
    // ─────────────────────────────────────────────────────────────
    public static AFD obtenerAFD() {

        // ── Estructuras del AFD ───────────────────────────────────
        // Se crean vacías y se llenan a lo largo del método
        Set<String> estados = new HashSet<>();                          // Todos los estados del autómata
        Set<Character> alfabeto = new HashSet<>();                      // Caracteres válidos del lenguaje
        Map<String, Map<Character, String>> transiciones = new HashMap<>(); // Tabla de transiciones
        Set<String> estadosAceptacion = new HashSet<>();                // Estados que reconocen un token completo
        Set<String> kwStates   = new HashSet<>();                       // Estados pertenecientes a keywords
        Set<String> funcStates = new HashSet<>();                       // Estados pertenecientes a funciones
        Set<String> attrStates = new HashSet<>();                       // Estados pertenecientes a atributos


        // ── Construcción del Alfabeto ─────────────────────────────
        // Define todos los caracteres que el AFD puede leer.
        // Un carácter fuera de este conjunto genera error léxico.

        // Letras mayúsculas A-Z
        for (char c = 'A'; c <= 'Z'; c++) alfabeto.add(c);

        // Letras minúsculas a-z
        for (char c = 'a'; c <= 'z'; c++) alfabeto.add(c);

        // Dígitos 0-9
        for (char c = '0'; c <= '9'; c++) alfabeto.add(c);

        // Símbolos especiales, operadores y caracteres de control
        // Incluye: operadores, comillas, espacios, saltos de línea, etc.
        String simbolos = "+-=<>!.,(){}[]/*\" _\t\n\r&|\\";
        for (char c : simbolos.toCharArray()) alfabeto.add(c);


        // ── Estados base del autómata ─────────────────────────────
        estados.add("INICIO"); // Punto de partida para analizar cada token
        estados.add("ERROR");  // Estado al que se llega con una entrada inválida
        estados.add("ACEPTA"); // Estado genérico de aceptación (reservado)


        // ─────────────────────────────────────────────────────────
        // CONSTRUCCIÓN DE KEYWORDS
        // Para cada palabra clave, construye una cadena de estados
        // y transiciones letra por letra.
        //
        // Ejemplo con "SCAN":
        //   INICIO --S--> S
        //   S      --C--> SC
        //   SC     --A--> SCA
        //   SCA    --N--> SCAN  ← estado de aceptación
        // ─────────────────────────────────────────────────────────
        String[] keywords = {"SCAN", "AS", "FOR", "EACH", "IN", "IF", "ELSE", "LET", "DONE"};

        for (String kw : keywords) {
            String estado = ""; // Estado acumulado letra por letra (ej: "", "S", "SC", "SCA", "SCAN")

            for (int i = 0; i < kw.length(); i++) {
                String prev  = estado;           // Estado anterior (antes de agregar la letra actual)
                estado += kw.charAt(i);          // Nuevo estado = estado anterior + letra actual

                estados.add(estado);             // Registra el estado en el conjunto
                kwStates.add(estado);            // Marca que este estado pertenece a una keyword

                if (i == 0) {
                    // Primera letra: la transición sale desde INICIO
                    // INICIO --primera_letra--> primer_estado
                    Map<Character, String> t = transiciones.getOrDefault("INICIO", new HashMap<>());
                    t.put(kw.charAt(0), estado);
                    transiciones.put("INICIO", t);
                } else {
                    // Letras siguientes: la transición sale desde el estado anterior
                    // estado_prev --letra_actual--> estado_actual
                    Map<Character, String> t = transiciones.getOrDefault(prev, new HashMap<>());
                    t.put(kw.charAt(i), estado);
                    transiciones.put(prev, t);
                }
            }

            // El último estado construido es de aceptación
            // (significa que se reconoció la keyword completa)
            estadosAceptacion.add(estado);
        }


        // ─────────────────────────────────────────────────────────
        // CONSTRUCCIÓN DE FUNCIONES
        // Mismo proceso que las keywords, pero los estados se marcan
        // como funcStates en lugar de kwStates.
        //
        // Ejemplo con "MOVE":
        //   INICIO --M--> M
        //   M      --O--> MO
        //   MO     --V--> MOV
        //   MOV    --E--> MOVE  ← estado de aceptación
        // ─────────────────────────────────────────────────────────
        String[] funciones = {"MOVE", "COPY", "DELETE", "RENAME", "LOG"};

        for (String fn : funciones) {
            String estado = "";

            for (int i = 0; i < fn.length(); i++) {
                String prev = estado;
                estado += fn.charAt(i);

                estados.add(estado);             // Registra el estado
                funcStates.add(estado);          // Marca que pertenece a una función

                if (i == 0) {
                    // Primera letra: transición desde INICIO
                    Map<Character, String> t = transiciones.getOrDefault("INICIO", new HashMap<>());
                    t.put(fn.charAt(0), estado);
                    transiciones.put("INICIO", t);
                } else {
                    // Letras siguientes: transición desde estado anterior
                    Map<Character, String> t = transiciones.getOrDefault(prev, new HashMap<>());
                    t.put(fn.charAt(i), estado);
                    transiciones.put(prev, t);
                }
            }

            // El último estado es de aceptación
            // (función completamente reconocida)
            estadosAceptacion.add(estado);
        }


        // ─────────────────────────────────────────────────────────
        // CONSTRUCCIÓN DE ATRIBUTOS
        // Mismo proceso, pero los estados se marcan como attrStates.
        //
        // Ejemplo con "SIZE":
        //   INICIO --S--> S  (compartido con "SCAN")
        //   S      --I--> SI
        //   SI     --Z--> SIZ
        //   SIZ    --E--> SIZE  ← estado de aceptación
        // ─────────────────────────────────────────────────────────
        String[] atributos = {"EXT", "SIZE", "NAME", "DATE"};

        for (String at : atributos) {
            String estado = "";

            for (int i = 0; i < at.length(); i++) {
                String prev = estado;
                estado += at.charAt(i);

                estados.add(estado);             // Registra el estado
                attrStates.add(estado);          // Marca que pertenece a un atributo

                if (i == 0) {
                    // Primera letra: transición desde INICIO
                    Map<Character, String> t = transiciones.getOrDefault("INICIO", new HashMap<>());
                    t.put(at.charAt(0), estado);
                    transiciones.put("INICIO", t);
                } else {
                    // Letras siguientes: transición desde estado anterior
                    Map<Character, String> t = transiciones.getOrDefault(prev, new HashMap<>());
                    t.put(at.charAt(i), estado);
                    transiciones.put(prev, t);
                }
            }

            // El último estado es de aceptación
            // (atributo completamente reconocido)
            estadosAceptacion.add(estado);
        }


        // ─────────────────────────────────────────────────────────
        // Devuelve el AFD completamente construido con:
        // - Todos los estados generados
        // - El alfabeto completo del lenguaje
        // - La tabla de transiciones
        // - "INICIO" como estado inicial
        // - Los estados de aceptación finales
        // - Los grupos de estados por categoría (kw, func, attr)
        // ─────────────────────────────────────────────────────────
        return new AFD(estados, alfabeto, transiciones, "INICIO",
                       estadosAceptacion, kwStates, funcStates, attrStates);
    }
}