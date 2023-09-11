package pers.clare.hisql;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import pers.clare.hisql.service.SQLService;

import java.util.UUID;

public abstract class BasicTest {

    protected final String table = "T" + UUID.randomUUID().toString().replaceAll("-", "");

    protected final String column1 = "ID";
    protected final String column2 = "NAME";

    protected final String findAll = "select * from " + table;
    protected final String findAllWhereColumn1 = findAll + " where " + column1 + ">?";

    protected final String descColumn1 = column1 + " desc";

    @Autowired
    private SQLService sqlService;

    abstract protected int getMax();

    @BeforeEach
    protected void create() {
        sqlService.update("create table " + table + " (id int auto_increment, name varchar(255),primary key(id))");
        for (int i = 1; i <= getMax(); i++) {
            sqlService.update("insert into " + table + " values(?,?)", i, i);
        }
    }

    @AfterEach
    protected void drop() {
        sqlService.update("drop table " + table);
    }

}
