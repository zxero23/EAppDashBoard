package org.ccpm.tablero.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Rutas del front (React Router) que no son archivos: se sirven con index.html. */
@Controller
public class SpaController {

    @GetMapping({"/", "/{ruta:[^.]*}"})
    public String index() {
        return "forward:/index.html";
    }
}
