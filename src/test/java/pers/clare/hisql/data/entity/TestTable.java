package pers.clare.hisql.data.entity;

import lombok.*;

import javax.persistence.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(TestTableKey.class)
public class TestTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Id
    @Column(nullable = false, columnDefinition = "varchar(100) default ''")
    private String account;

    @Column(nullable = false, columnDefinition = "varchar(100) default ''")
    private String name;

}
