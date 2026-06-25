// SortScript - Organizar descargas
scan "C:/Users/Ana/Downloads" as downloads

for each file in downloads {
    if file.ext == .pdf {
        move(file, "C:/Users/Ana/Documents")
    }
    if file.size > 100MB {
        log("Archivo grande: " + file.name)
    }
}

log("Proceso finalizado")
done()
