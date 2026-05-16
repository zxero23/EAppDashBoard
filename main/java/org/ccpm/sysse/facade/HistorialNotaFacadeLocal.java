package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.HistorialNota;

@Local
public interface HistorialNotaFacadeLocal {

    void create(HistorialNota historialNota);

    void edit(HistorialNota historialNota);

    void remove(HistorialNota historialNota);

    HistorialNota find(Object id);

    List<HistorialNota> findAll();

    List<HistorialNota> findRange(int[] range);

    int count();
    
}
