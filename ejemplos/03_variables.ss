// ============================================
// SortScript - Ejemplo 3: Variables
// ============================================
// Demuestra declaracion con let y
// reutilizacion de valores.

scan "C:/Users/Ana/Fotos" as fotos

let rutaImagenes = "C:/Users/Ana/Imagenes"
let rutaVideos = "C:/Users/Ana/Videos"
let maxTamano = 200MB

for each foto in fotos {
    if foto.ext == .jpg || foto.ext == .png {
        copy(foto, rutaImagenes)
    }

    if foto.ext == .mp4 {
        move(foto, rutaVideos)
    }

    if foto.size > maxTamano {
        log("Redimensionar: " + foto.name)
    }
}

done()
