package pers.clare.hisql.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "test")
public class TestTable {
    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestTable testTable = (TestTable) o;
        return id.equals(testTable.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
