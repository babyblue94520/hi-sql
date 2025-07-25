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

    protected final String findAll = "SELECT * FROM " + table;
    protected final String findAllWhereColumn1 = findAll + " WHERE " + column1 + ">?";

    protected final String descColumn1 = column1 + " DESC";

    @Autowired
    private SQLService sqlService;

    abstract protected int getMax();

    @BeforeEach
    protected void create() {
        sqlService.update("CREATE TABLE " + table + " (id INT AUTO_INCREMENT, name VARCHAR(255),PRIMARY KEY(id))");
        for (int i = 1; i <= getMax(); i++) {
            sqlService.update("INSERT INTO " + table + " VALUES(?,?)", i, i);
        }
    }

    @AfterEach
    protected void drop() {
        sqlService.update("DROP TABLE " + table);
    }

}
