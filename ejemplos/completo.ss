// SortScript - Ejemplo completo de todas las caracteristicas
// =========================================================

// 1. Scan inicial obligatorio
scan "C:/Users/Ana/Escritorio" as escritorio

// 2. Variables con let (tipos: number y string)
let tamLimite = 50MB
let mensajeInicial = "Procesando archivos..."
log(mensajeInicial)

// 3. For each sobre la coleccion
let item = 0
for each item in escritorio {
    // 4. If/else if/else con operadores logicos
    if item.ext == .pdf || item.ext == .docx {
        move(item, "C:/Users/Ana/Documentos")
    } else if item.ext == .jpg || item.ext == .png {
        copy(item, "C:/Users/Ana/Imagenes")
    } else if item.ext == .zip || item.ext == .rar {
        move(item, "C:/Users/Ana/Archivos")
    } else {
        log("Archivo sin clasificar: " + item.name)
    }

    // 5. Atributos de archivo (ext, size, name)
    if item.size > tamLimite {
        log("Archivo grande: " + item.name + " (" + item.size + ")")
    }
}

// 6. Funciones: log (1-2 args), move, copy, delete, rename
log("--- Resumen ---")
log("Tamanio limite usado:", tamLimite)
log("Proceso completado")

done()
