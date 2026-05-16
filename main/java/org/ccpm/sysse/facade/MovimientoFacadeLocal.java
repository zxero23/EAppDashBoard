package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.Movimiento;

@Local
public interface MovimientoFacadeLocal {

    void create(Movimiento movimiento);

    void edit(Movimiento movimiento);

    void remove(Movimiento movimiento);

    Movimiento find(Object id);

    List<Movimiento> findAll();

    List<Movimiento> findRange(int[] range);

    int count();
    
}
