package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.Iniciador;
import org.ccpm.sysse.entity.Mesa;

@Local
public interface IniciadorFacadeLocal {

    void create(Iniciador iniciador);

    void edit(Iniciador iniciador);

    void remove(Iniciador iniciador);

    Iniciador find(Object id);

    List<Iniciador> findAll();

    List<Iniciador> findAllByTipo(Long idTipo);    

    List<Iniciador> findRange(int[] range);

    int count();

    public java.util.List<org.ccpm.sysse.entity.Iniciador> findAll(boolean estado);
    
    public Mesa findMesaEntrada (Long codOrganismo);

}
