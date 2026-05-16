package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.HistorialExpediente;

@Local
public interface HistorialExpedienteFacadeLocal {

    void create(HistorialExpediente historialExpediente);

    void edit(HistorialExpediente historialExpediente);

    void remove(HistorialExpediente historialExpediente);

    HistorialExpediente find(Object id);

    List<HistorialExpediente> findAll();

    List<HistorialExpediente> findRange(int[] range);

    int count();
    
}
