package org.ccpm.tablero.modelo;

import java.util.List;

/** Ids de "situacion" que usa el tablero. Estan fijos en la base desde la v1; aca es el unico lugar donde se nombran. */
public final class Semaforo {
    public static final long EN_TERMINO = 7, EN_ALERTA = 8, FUERA_DE_TERMINO = 10, TERMINADO = 11;
    public static final List<Long> IDS = List.of(EN_TERMINO, EN_ALERTA, FUERA_DE_TERMINO, TERMINADO);

    private Semaforo() {}
}
