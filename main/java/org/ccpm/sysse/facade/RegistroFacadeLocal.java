package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.Registro;

@Local
public interface RegistroFacadeLocal {

    void create(Registro registro);

    void edit(Registro registro);

    void remove(Registro registro);

    Registro find(Object id);

    List<Registro> findAll();

    List<Registro> findRange(int[] range);

    int count();
    
    public void create(java.lang.String nombre, java.lang.String organsimo, java.lang.String telefono, java.lang.String email) throws java.lang.Exception;
    
}
