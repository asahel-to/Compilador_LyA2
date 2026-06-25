package lexico; // Paquete del analizador léxico

// ─────────────────────────────────────────────────────────────
// Representa la unidad mínima del lenguaje SortScript.
// Cada palabra, símbolo u operador encontrado en el código
// fuente se convierte en un objeto Token.
// ─────────────────────────────────────────────────────────────
public class Token {

    private String lexema;        // Texto tal como aparece en el código. Ej: "sort", "==", "512MB"
    private int linea;            // Línea donde se encontró el token (para reportar errores)
    private TipoToken tipo;       // Categoría asignada por el AFD. Ej: KW_SORT, LIT_ENTERO
    private String estadoFinal;   // Estado del AFD al terminar de leer el token (para diagnóstico)
    private boolean existeSimbolo;// true = token válido y reconocido / false = token con errores

    // ─────────────────────────────────────────────────────────
    // CONSTRUCTOR BÁSICO
    // Se usa cuando el tokenizador apenas corta el texto en piezas.
    // Solo guarda el lexema y la línea; el tipo lo asigna el AFD después.
    // ─────────────────────────────────────────────────────────
    public Token(String lexema, int linea) {
        this.lexema = lexema;
        this.linea  = linea;
    }

    // ─────────────────────────────────────────────────────────
    // CONSTRUCTOR COMPLETO
    // Se usa cuando el AFD ya clasificó el token y se conocen
    // todos sus datos: tipo, estado final y si es válido.
    // ─────────────────────────────────────────────────────────
    public Token(String lexema, int linea, TipoToken tipo, String estadoFinal, boolean existeSimbolo) {
        this.lexema        = lexema;
        this.linea         = linea;
        this.tipo          = tipo;
        this.estadoFinal   = estadoFinal;
        this.existeSimbolo = existeSimbolo;
    }

    // ── Getters y Setters ─────────────────────────────────────

    public String getLexema()               { return lexema; }        // Devuelve el texto original del token
    public int getLinea()                   { return linea; }         // Devuelve la línea donde apareció
    public TipoToken getTipo()              { return tipo; }          // Devuelve la categoría del token
    public void setTipo(TipoToken tipo)     { this.tipo = tipo; }     // Permite reclasificar el token
    public String getEstadoFinal()          { return estadoFinal; }   // Devuelve el estado final del AFD
    public boolean existeSimbolo()          { return existeSimbolo; } // true si el token es válido
    public void setExisteSimbolo(boolean b) { this.existeSimbolo = b;}// Actualiza si el token es válido
}