package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.LogAccion;

@Local
public interface LogAccionFacadeLocal {

    void create(LogAccion logAccion);

    void edit(LogAccion logAccion);

    void remove(LogAccion logAccion);

    LogAccion find(Object id);

    List<LogAccion> findAll();

    List<LogAccion> findRange(int[] range);

    int count();
    
}
