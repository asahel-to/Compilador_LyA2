// ============================================
// SortScript - Ejemplo 1: Introduccion
// ============================================
// Este programa escanea una carpeta y
// clasifica archivos por tipo.

scan "C:/Users/Ana/Descargas" as carpeta

for each archivo in carpeta {
    if archivo.ext == .pdf {
        move(archivo, "C:/Users/Ana/Documentos")
    }

    if archivo.ext == .jpg {
        copy(archivo, "C:/Users/Ana/Imagenes")
    }

    if archivo.size > 50MB {
        log("Archivo grande: " + archivo.name)
    }
}

log("Proceso terminado")
done()
