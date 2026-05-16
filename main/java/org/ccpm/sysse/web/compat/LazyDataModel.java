package org.ccpm.sysse.web.compat;

import java.util.List;
import java.util.Map;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * Compatibilidad para migrar de PrimeFaces 8 a 15 sin reescribir
 * inmediatamente todas las implementaciones legacy de LazyDataModel.
 */
public abstract class LazyDataModel<T> extends org.primefaces.model.LazyDataModel<T> {

    /**
     * Firma legacy usada en el proyecto (PrimeFaces <= 8).
     */
    public abstract List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters);

    /**
     * Firma nueva requerida por PrimeFaces 15.
     */
    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        String sortField = null;
        SortOrder sortOrder = SortOrder.UNSORTED;

        if (sortBy != null && !sortBy.isEmpty()) {
            SortMeta firstSort = sortBy.values().iterator().next();
            if (firstSort != null) {
                sortField = firstSort.getField();
                if (firstSort.getOrder() != null) {
                    sortOrder = firstSort.getOrder();
                }
            }
        }
        return load(first, pageSize, sortField, sortOrder, filterBy);
    }

    /**
     * En PF15 es obligatorio. En este proyecto legacy el conteo se setea
     * explícitamente con setRowCount(...), por eso devolvemos ese valor.
     */
    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        return getRowCount();
    }
}
