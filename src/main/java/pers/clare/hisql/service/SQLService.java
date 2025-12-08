package pers.clare.hisql.service;

import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;

import java.util.List;

public interface SQLService extends SQLStoreService, SQLTypeService, SQLBasicService {

    default <T> Page<T> toPage(
            Pagination pagination
            , List<T> list
            , String sql
            , Object[] parameters
    ) {
        pagination = getPagination(pagination);
        int size = pagination.getSize();
        int page = pagination.getPage();
        int listSize = list.size();
        long total = pagination.getTotal();
        long currentTotal = (long) page * size + listSize;
        if (listSize < size) {
            // Is last page.
            total = currentTotal;
        } else if (total == 0) {
            if (pagination.isVirtualTotal()) {
                total = currentTotal;
                long virtualTotal = getPaginationMode()
                        .getVirtualTotal(this, sql, parameters);
                if (total < virtualTotal) {
                    total = virtualTotal;
                } else {
                    total += size;
                }
            } else {
                total = getPaginationMode().getTotal(this, sql, parameters);
            }
        } else if (total < currentTotal) {
            if (pagination.isVirtualTotal()) {
                total += size;
            } else {
                total = getPaginationMode().getTotal(this, sql, parameters);
            }
        }

        return Page.of(page, size, list, total);
    }
}
