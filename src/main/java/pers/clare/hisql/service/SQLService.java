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
        long total = pagination.getTotal();

        if (total == 0) {
            if (pagination.isVirtualTotal()) {
                total = getPaginationMode().getVirtualTotal(this, sql, parameters);
            } else {
                total = getPaginationMode().getTotal(this, sql, parameters);
            }
        } else {
            int listSize = list.size();
            long currentTotal = (long) page * size + listSize;
            if (total > currentTotal && listSize < size) {
                total = currentTotal;
            } else if (total < currentTotal && listSize > 0) {
                if (pagination.isVirtualTotal()) {
                    total = currentTotal;
                    if (listSize == size) {
                        total += size;
                    }
                } else {
                    total = getPaginationMode().getTotal(this, sql, parameters);
                }
            }
        }
        return Page.of(page, size, list, total);
    }
}