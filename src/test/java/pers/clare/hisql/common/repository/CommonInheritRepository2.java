package pers.clare.hisql.common.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.common.data.CommonUser;
import pers.clare.hisql.function.ConnectionCallback;

@Repository
public interface CommonInheritRepository2<K> extends CommonInheritRepository1<String, CommonUser, K, Object> {
    @HiSql("UPDATE user SET name = :name WHERE id=:id")
    CommonUser update(Long id, String name, ConnectionCallback<CommonUser> callback);

    @HiSql("UPDATE user SET name =1 WHERE id=:id")
    int update(long id, String name);
}
