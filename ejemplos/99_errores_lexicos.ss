// SortScript - Prueba de errores lexicos
// ========================================
// Este codigo contiene errores a proposito
// para verificar la deteccion lexica.

// Error 1: Caracter @ no esta en el alfabeto
scan "C:/test" as t

let f = 0
for each f in t {
    if f.ext == @pdf {
        move(f, "C:/destino")
    }
}

// Error 2: Signo $ no esta en el alfabeto
let x = valor$mal

// Error 3: Caracter # no esta en el alfabeto
let y = #123

// Error 4: Palabra totalmente desconocida
zzzzt abcdefg

// Codigo correcto abajo
let mensaje = "Esto si funciona"
log(mensaje)
done()
