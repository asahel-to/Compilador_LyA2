// ============================================
// SortScript - Ejemplo 4: Script Completo
// ============================================
// Organizacion completa del Escritorio.

scan "C:/Users/Ana/Desktop" as escritorio

let docs = "C:/Users/Ana/Documentos"
let media = "C:/Users/Ana/Multimedia"
let backups = "C:/Users/Ana/Backups"
let tamLimite = 10MB

let item = 0
log("Iniciando limpieza del escritorio...")

for each item in escritorio {
    // Documentos
    if item.ext == .pdf || item.ext == .docx || item.ext == .xlsx {
        move(item, docs)
    }

    // Multimedia
    if item.ext == .jpg || item.ext == .mp3 || item.ext == .mp4 {
        move(item, media)
    }

    // Respaldar archivos importantes
    if item.ext == .zip || item.ext == .rar {
        copy(item, backups)
    }

    // Reportar archivos grandes
    if item.size > tamLimite && item.ext != .zip {
        log("Archivo grande en escritorio: " + item.name)
    }
}

log("Limpieza completada")
done()
