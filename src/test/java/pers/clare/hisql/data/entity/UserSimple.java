package pers.clare.hisql.data.entity;

import lombok.Getter;

import java.util.Objects;

@Getter
public class UserSimple {
    private Long id;

    private String account;

    private String name;

    private String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSimple that = (UserSimple) o;
        return Objects.equals(account, that.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account);
    }
}
