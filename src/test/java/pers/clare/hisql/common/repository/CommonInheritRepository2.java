package pers.clare.hisql.common.repository;

import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.common.data.CommonUser;
import pers.clare.hisql.function.ConnectionCallback;
import org.springframework.stereotype.Repository;

@Repository
public interface CommonInheritRepository2<K> extends CommonInheritRepository1<String, CommonUser, K, Object> {
    @HiSql("update user set name = :name where id=:id")
    CommonUser update(Long id, String name, ConnectionCallback<CommonUser> callback);

    @HiSql("update user set name =1 where id=:id")
    int update(long id, String name);
}
