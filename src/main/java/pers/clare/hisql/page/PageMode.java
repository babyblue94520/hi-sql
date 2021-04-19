package pers.clare.hisql.page;

public interface PageMode {

    String buildTotalSQL(String sql);

    String buildSortSQL(Sort sort, String sql);

    String buildPaginationSQL(Pagination pagination, String sql);

    void appendSortSQL(StringBuilder sql, String[] sorts);

    void appendPaginationSQL(StringBuilder sql, Pagination pagination);
}
