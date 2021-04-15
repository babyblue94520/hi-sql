package pers.clare.hisql.page;

public interface PageMode {

    String buildTotalSQL(String sql);

    String buildPaginationSQL(Pagination pagination, String sql);

    String buildSortSQL(Sort sort, String sql);

    void appendPaginationSQL(StringBuilder sql, Pagination pagination);

    void appendSortSQL(StringBuilder sql, String[] sorts);
}
