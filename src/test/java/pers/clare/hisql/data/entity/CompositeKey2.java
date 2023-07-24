package pers.clare.hisql.data.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.annotation.Order;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
public class CompositeKey2 implements Serializable {

    @Order(2)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Order(1)
    @Id
    private String account;

    public CompositeKey2() {
    }

    public CompositeKey2(Long id, String account) {
        this.id = id;
        this.account = account;
    }
}
