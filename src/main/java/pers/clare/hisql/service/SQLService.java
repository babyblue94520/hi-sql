package pers.clare.hisql.service;

import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;

import java.util.List;

public interface SQLService extends SQLStoreService, SQLTypeService {

    default <T> Page<T> toPage(
            Pagination pagination
            , List<T> list
            , String sql
            , Object[] parameters
    ) {
        pagination = getPagination(pagination);
        int size = pagination.getSize();
        int page = pagination.getPage();
        long total = getTotal(pagination, list, sql, parameters);

        return Page.of(page, size, list, total);
    }

    private <T> long getTotal(
            Pagination pagination
            , List<T> list
            , String sql
            , Object[] parameters
    ) {
        int size = pagination.getSize();
        int page = pagination.getPage();
        int listSize = list.size();

        if (listSize == 0 && page == 0) return 0;
        long currentTotal = (long) page * size + listSize;
        if (listSize > 0 && listSize < size) return currentTotal;

        long total = pagination.getTotal();
        if (pagination.isVirtualTotal()) {
            if (total == 0) {
                total = getPaginationMode().getVirtualTotal(this, sql, parameters);
            }
            if (total < currentTotal || (total == currentTotal && listSize == size)) {
                total = currentTotal + size;
            } else if (listSize == 0) {
                total = currentTotal;
            }
        } else {
            if (total == 0 || (total < currentTotal && listSize != 0)) {
                total = getPaginationMode().getTotal(this, sql, parameters);
            }
        }
        return total;
    }
}