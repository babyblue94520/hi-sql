package pers.clare.hisql.data.entity;

import lombok.Getter;
import pers.clare.hisql.vo.IdName;

import java.util.Objects;

@Getter
public class UserSimple extends IdName {

    private String account;

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
