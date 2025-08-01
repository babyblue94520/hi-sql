package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.data.entity.UserSimple;
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
    @HiSql(value = "INSERT INTO user (account)VALUES(:account)", returnIncrementKey = true)
    Long insert(String account);

    @HiSql(value = "INSERT INTO user (account)VALUES(:account)", returnIncrementKey = true)
    void insertVoid(String account);

    @HiSql("UPDATE user SET name =:name WHERE id=:id")
    int update(long id, String name);

    @HiSql("UPDATE user SET name =:name WHERE id=:id")
    void updateVoid(long id, String name);

    @HiSql("UPDATE user SET name =:user.name WHERE id=:user.id")
    int update(User user);

    @HiSql("UPDATE user SET name =:name WHERE id=:id")
    void updateVoid(User user);

    @HiSql("DELETE FROM user WHERE id=:user.id")
    int delete(User user);

    @HiSql("DELETE FROM user WHERE id=:id")
    void deleteVoid(User user);

    @HiSql("DELETE FROM user")
    int delete();

    @HiSql("SELECT COUNT(*) FROM user")
    long count();

    @HiSql("from user WHERE account=:account")
    User findByAccount(String account);

    @HiSql("SELECT * FROM user WHERE id=:id")
    User findById(Long id);

    @HiSql("from user WHERE id=:id")
    UserSimple findSimpleById(Long id);

    @HiSql("SELECT * FROM user WHERE id=:id")
    Map<String, Object> findMapById(Long id);

    @HiSql("from user WHERE id=:id")
    Map<String, Object> findMapById2(Long id);

    @HiSql("SELECT * FROM user WHERE account=:account")
    List<User> findAllByAccount(String account);

    @HiSql(" FROM user WHERE account=:account")
    List<User> findAllByAccount2(String account);

    @HiSql("SELECT * FROM user")
    List<UserSimple> findAll();

    @HiSql("SELECT * FROM user")
    Set<UserSimple> findAllSet();

    @HiSql("SELECT * FROM user")
    Set<Map<String, Object>> findAllMapSet();

    @HiSql("SELECT * FROM user WHERE account=:account")
    Page<User> pageByAccount(String account);

    @HiSql("SELECT * FROM user WHERE account=:account")
    Page<User> pageByAccount(Pagination pagination, String account);

    @HiSql("SELECT * FROM user WHERE account=:account")
    List<User> findAllByAccount(Sort sort, String account);

    @HiSql("SELECT * FROM user WHERE id IN :ids AND account=:account")
    List<User> findAll(Long[] ids, String account);

    @HiSql("SELECT * FROM user WHERE id IN :ids AND account=:account")
    List<User> findAll(Collection<Long> ids, String account);

    @HiSql("SELECT * FROM user WHERE (id,account) IN :values")
    List<User> findAll(Object[][] values);

    @HiSql("SELECT * FROM user WHERE (id,account) IN :values")
    List<User> findAll(Collection<Object[]> values);

    @HiSql("SELECT * FROM user WHERE 1=1 {idSql}")
    List<User> findAll(String idSql, Long id);

    List<User> findAll(SqlReplace<Object> id);

    @HiSql("SELECT '[0-9]'")
    Pattern findPattern();
}
