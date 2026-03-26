package pers.clare.hisql.page.impl;

import org.junit.jupiter.api.Test;
import pers.clare.hisql.page.Pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginationModeTest {

    @Test
    void testH2PaginationMode() {
        H2PaginationMode mode = new H2PaginationMode();
        StringBuilder sql = new StringBuilder("SELECT * FROM table");

        Pagination pagination = Pagination.of(2, 10);
        mode.appendPaginationSQL(sql, pagination);
        assertEquals("SELECT * FROM table LIMIT 20,10", sql.toString());

        sql = new StringBuilder("SELECT * FROM table");
        pagination.setCursor(true);
        mode.appendPaginationSQL(sql, pagination);
        assertEquals("SELECT * FROM table LIMIT 0,10", sql.toString());
    }

    @Test
    void testMSSQLPaginationMode() {
        MSSQLPaginationMode mode = new MSSQLPaginationMode();
        StringBuilder sql = new StringBuilder("SELECT * FROM table");

        Pagination pagination = Pagination.of(2, 10);
        mode.appendPaginationSQL(sql, pagination);
        assertEquals("SELECT * FROM table OFFSET 20 ROWS FETCH NEXT 10 ROWS ONLY", sql.toString());

        sql = new StringBuilder("SELECT * FROM table");
        pagination.setCursor(true);
        mode.appendPaginationSQL(sql, pagination);
        assertEquals("SELECT * FROM table OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY", sql.toString());
    }

    @Test
    void testMySQLPaginationMode() {
        MySQLPaginationMode mode = new MySQLPaginationMode();
        StringBuilder sql = new StringBuilder("SELECT * FROM table");

        Pagination pagination = Pagination.of(2, 10);
        mode.appendPaginationSQL(sql, pagination);
        assertEquals("SELECT * FROM table LIMIT 20,10", sql.toString());

        sql = new StringBuilder("SELECT * FROM table");
        pagination.setCursor(true);
        mode.appendPaginationSQL(sql, pagination);
        assertEquals("SELECT * FROM table LIMIT 0,10", sql.toString());
    }
}
