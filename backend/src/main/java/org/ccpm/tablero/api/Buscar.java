package org.ccpm.tablero.api;

import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class Buscar {
    private Buscar() {}

    static <T> T buscar(CrudRepository<T, Long> repo, Long id) {
        return repo.findById(id).orElseThrow(Buscar::noEncontrado);
    }

    static ResponseStatusException noEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe");
    }
}
