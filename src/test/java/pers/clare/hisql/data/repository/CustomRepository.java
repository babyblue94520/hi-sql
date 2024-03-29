package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.data.entity.UserSimple;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.repository.SQLRepository;
import pers.clare.hisql.support.SqlReplace;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Repository
public interface CustomRepository extends SQLRepository {
    @HiSql(value = "insert into user (account)values(:account)", returnIncrementKey = true)
    Long insert(String account);

    @HiSql(value = "insert into user (account)values(:account)", returnIncrementKey = true)
    void insertVoid(String account);

    @HiSql("update user set name =:name where id=:id")
    int update(long id, String name);

    @HiSql("update user set name =:name where id=:id")
    void updateVoid(long id, String name);

    @HiSql("update user set name =:user.name where id=:user.id")
    int update(User user);

    @HiSql("update user set name =:name where id=:id")
    void updateVoid(User user);

    @HiSql("delete from user where id=:user.id")
    int delete(User user);

    @HiSql("delete from user where id=:id")
    void deleteVoid(User user);

    @HiSql("delete from user")
    int delete();

    @HiSql("select count(*) from user")
    long count();

    @HiSql("from user where account=:account")
    User findByAccount(String account);

    @HiSql("select * from user where id=:id")
    User findById(Long id);

    @HiSql("from user where id=:id")
    UserSimple findSimpleById(Long id);

    @HiSql("select * from user where id=:id")
    Map<String, Object> findMapById(Long id);

    @HiSql("from user where id=:id")
    Map<String, Object> findMapById2(Long id);

    @HiSql("select * from user where account=:account")
    List<User> findAllByAccount(String account);

    @HiSql(" from user where account=:account")
    List<User> findAllByAccount2(String account);

    @HiSql("select * from user")
    List<UserSimple> findAll();

    @HiSql("select * from user")
    Set<UserSimple> findAllSet();

    @HiSql("select * from user")
    Set<Map<String, Object>> findAllMapSet();

    @HiSql("select * from user where account=:account")
    Page<User> pageByAccount(String account);

    @HiSql("select * from user where account=:account")
    Page<User> pageByAccount(Pagination pagination, String account);

    @HiSql("select * from user where account=:account")
    Next<User> nextByAccount(String account);

    @HiSql("select * from user where account=:account")
    Next<User> nextByAccount(Pagination pagination, String account);

    @HiSql("select * from user where account=:account")
    List<User> findAllByAccount(Sort sort, String account);

    @HiSql("select * from user where id in :ids and account=:account")
    List<User> findAll(Long[] ids, String account);

    @HiSql("select * from user where id in :ids and account=:account")
    List<User> findAll(Collection<Long> ids, String account);

    @HiSql("select * from user where (id,account) in :values")
    List<User> findAll(Object[][] values);

    @HiSql("select * from user where (id,account) in :values")
    List<User> findAll(Collection<Object[]> values);

    @HiSql("select * from user where 1=1 {idSql}")
    List<User> findAll(String idSql, Long id);

    List<User> findAll(SqlReplace<Object> id);

    @HiSql("select '[0-9]'")
    Pattern findPattern();
}
