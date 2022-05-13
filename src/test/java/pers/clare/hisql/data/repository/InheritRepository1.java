package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.common.data.CommonUser;
import pers.clare.hisql.common.repository.CommonInheritRepository1;
import pers.clare.hisql.function.ConnectionCallback;

@Repository
public interface InheritRepository1 extends CommonInheritRepository1<String, CommonUser, Long, Object> {
    @HiSql("update user set name = :name where id=:id")
    CommonUser update(Long id, String name, ConnectionCallback<CommonUser> callback);

    @HiSql("update user set name =1 where id=:id")
    int update(long id, String name);
}
