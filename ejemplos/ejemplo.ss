// SortScript - Ejemplo de organizaci�n de archivos
// =================================================

// Escanear la carpeta de Descargas
scan "C:/Users/Ana/Downloads" as downloads

// Iterar sobre cada archivo en la carpeta
let file = 0
for each file in downloads {
    // Si es PDF, mover a Documentos
    if file.ext == .pdf {
        move(file, "C:/Users/Ana/Documents")
    }

    // Si es imagen, mover a Imagenes
    if file.ext == .jpg || file.ext == .png {
        copy(file, "C:/Users/Ana/Imagenes")
    }

    // Si el archivo es muy grande, registrar alerta
    if file.size > 100MB {
        log("Archivo grande detectado: " + file.name)
    }
}

// Mostrar mensaje de finalizaci�n
log("Organizaci�n completada exitosamente")
done()
