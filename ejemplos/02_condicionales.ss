// ============================================
// SortScript - Ejemplo 2: Condicionales
// ============================================
// Demuestra if, else y operadores logicos.

scan "C:/Users/Ana/Temp" as tmp

let f = 0
for each f in tmp {
    // Clasificar por extension
    if f.ext == .txt || f.ext == .docx {
        move(f, "C:/Users/Ana/Documentos/Textos")
    }

    // Clasificar por tamano
    if f.size > 100MB {
        log("ALERTA: " + f.name + " excede 100MB")
    }

    // Filtro inverso
    if f.ext != .exe {
        copy(f, "C:/Users/Ana/Seguros")
    } else {
        delete(f)
    }
}

done()
