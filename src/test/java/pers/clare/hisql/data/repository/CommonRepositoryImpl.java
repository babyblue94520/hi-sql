package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.common.data.CommonUser;
import pers.clare.hisql.common.repository.CommonUserRepository;
import pers.clare.hisql.function.ConnectionCallback;

@Repository
public interface CommonRepositoryImpl extends CommonUserRepository<Long, CommonUser> {
    @HiSql("update user set name = :name where id=:id")
    CommonUser update(Long id, String name, ConnectionCallback<CommonUser> callback);

    @HiSql("update user set name =1 where id=:id")
    int update(long id, String name);
}
