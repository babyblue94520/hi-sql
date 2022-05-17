package pers.clare.hisql.data.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
public class CompositeKey implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Id
    private String account;

    public CompositeKey() {
    }

    public CompositeKey(Long id, String account) {
        this.id = id;
        this.account = account;
    }
}
