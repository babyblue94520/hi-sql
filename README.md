# Hi SQL

## Overview

![](images/orm.png)
![](images/write_sql.png)

## Requirement

* Spring Framework 5+
* Java 11+

## Default

* **Lower case naming**

    ```
    HelloWorld > hello_world
    ```

* **MySQL Pagination**

    ```sql
    select * from table limit 0,20
    ```

## QuickStart


### Config

[HiSqlConfig.java](src/test/java/pers/clare/hisql/data/HiSqlConfig.java)
```java
@EnableHiSql
public class HiSqlConfig {
    
}
```

### Usage


* **SQLCrudRepository**

    Create an interface for extents SQLCrudRepository.

    **Example**

    * [User.java](src/test/java/pers/clare/hisql/data/entity/User.java)

    * [UserRepository.java](src/test/java/pers/clare/hisql/data/repository/UserRepository.java)

    * [SQLCrudRepositoryTest.java](src/test/java/pers/clare/hisql/repository/SQLCrudRepositoryTest.java)


* **SQLRepository**

  **SQL expression**

  * **:value**
    * Indicates parameter name.

    * **{sql}**
      * Replace {sql} with the string value of the same-named parameter.
      * **SqlReplace**
        * If the value is null, a blank sql is set.
        ```
        @Repository
        public interface TestRepository extends SQLRepository {
        
          @HiSql("select * from test where 1=1 {value}")
          void test(SqlReplace<Object> value)
        }
        
        testRepository.test(SqlReplace.of(value," and column = :value"))
        
        ```

  **Example**

  * [CustomRepository.java](src/test/java/pers/clare/hisql/data/repository/CustomRepository.java)

      ```java
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

      @Repository
      public interface CustomRepository extends SQLRepository {
          @HiSql("insert into user (account)values(:account)")
          Long insert(String account);

          @HiSql("update user set name =:name where id=:id")
          int update(long id, String name);

          @HiSql("update user set name =:user.name where id=:user.id")
          int update(User user);

          @HiSql("delete from user where id=:user.id")
          int delete(User user);

          @HiSql("delete from user")
          int delete();

          @HiSql("select count(*) from user")
          long count();

          @HiSql("select * from user where account=:account")
          User findByAccount(String account);

          @HiSql("select * from user where id=:id")
          User findById(Long id);

          @HiSql("select * from user where id=:id")
          UserSimple findSimpleById(Long id);

          @HiSql("select * from user where id=:id")
          Map<String, Object> findMapById(Long id);

          @HiSql("select * from user where account=:account")
          List<User> findAllByAccount(String account);

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

          @HiSql("select * from user where 1=1 {id} and :id is not null")
          List<User> findAll(SqlReplace<Object> id);
      }

      ```

      
    * [SQLRepositoryTest.java](src/test/java/pers/clare/hisql/repository/SQLRepositoryTest.java)



* **Write SQL on XML**

    * **root path resources/hisql/**
    * **{class package}/Repository.XML**

        ex: resources\hisql\pers\clare\demo\data\hiSql\UserQueryRepository.xml

    * Get SQL by **Method Name** or **@HiSql(name=...)**

        ```xml
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE SQL>
        <SQL>
            <findAllMapXML><![CDATA[
                select id
                    ,name
                    ,create_time
                from user
            ]]></findAllMapXML>
            <pageMapXML><![CDATA[
                select *
                from user
                where create_time between :startTime and :endTime
                {andId}
                {andName}
            ]]></pageMapXML>
        </SQL>
        ```

* **Support @Transactional**

    ```java
    import org.springframework.transaction.annotation.Propagation;@Repository
    public interface TransactionRepository extends SQLRepository {

        // mysql
        @HiSql("update user set name = if(@name:=name,:name,:name) where id=:id")
        int updateName(Long id, String name);

        @HiSql("select @name")
        String getOldName();
    }

    public class Service{
        @Transactional(propagtion = Propagation.SUPPORTS)
        public String queryDefineValue(Long id, String name) {
            transactionRepository.updateName(id, name);
            return String.format("old name:%s , new name:%s", transactionRepository.getOldName(), name);
        }
    }
    ```

    **Rollback example**

    ```java
    import org.springframework.transaction.annotation.Isolation;public class RollbackExample {
        public String rollback(Long id, String name) {
            StringBuilder sb = new StringBuilder();
            try {
                proxy().updateException(sb, id, name);
            } catch (Exception e) {
                sb.append(e.getMessage()).append('\n');
            }
            sb.append(userRepository.findById(id)).append('\n');
            return sb.toString();
        }
    
        @Transactional(propagtion = Propagation.REQUIRED)
        public void updateException(StringBuilder sb, Long id, String name) {
    
            // first update username
            String result = queryDefineValue(id, name);
            sb.append(result).append('\n');
            sb.append("------some connection------").append('\n');
        
            // select user in same connection
            User user = userRepository.findById(id);
            sb.append(user).append('\n');
    
            // second update username
            result = queryDefineValue(id, name+2);
            sb.append(result).append('\n');
    
            // select uncommitted user in different connection
            sb.append("------uncommitted------").append('\n');
            user = proxy().findByIdUncommitted(id);
            sb.append(user).append('\n');
    
            // select committed user in different connection
            sb.append("------committed------").append('\n');
            user = proxy().findById(id);
            sb.append(user).append('\n');
        
            // rollback
            throw new RuntimeException("rollback");
        }
    
        @Transactional
        public User findById(Long id) {
            return userRepository.findById(id);
        }
    
        @Transactional(isolation = Isolation.READ_UNCOMMITTED)
        public User findByIdUncommitted(Long id) {
            return userRepository.findById(id);
        }
    }
    ```

  ![](images/rollback.png)

## **Virtual Total**

"select count(*)" syntax is very slow, use virtual total instead of real count.

### Implement

* MySQL

        Replace the actual count (*) with the explain result "rows".

        ```java
        @Override
        public long getVirtualTotal(
                Pagination pagination
                , Connection connection
                , String sql
                , Object[] parameters
        ) throws SQLException {
            String totalSql = "explain select count(*) from(" + sql + ")t";
            ResultSet rs = ConnectionUtil.query(connection, totalSql, parameters);
            if (rs.next()) {
                return rs.getLong("rows");
            } else {
                throw new HiSqlException(String.format("query total error.(%s)", totalSql));
            }
        }
        ```

### Usage

    ```java
    pagination.setVirtualTotal(true);
    ```

### Customize

* __CustomMySQLPaginationMode__

        ```java
        public class CustomMySQLPaginationMode extends MySQLPaginationMode {

            @Override
            public long getVirtualTotal(
                    Pagination pagination
                    , Connection connection
                    , String sql
                    , Object[] parameters
            ) throws SQLException {
                // TODO
            }
        }

        @EnableHiSql(
            paginationMode = CustomMySQLPaginationMode.class
        )
        public class Demo2HiSqlConfig {
        }
        ```

## **Advanced**

### Change naming strategy

* **UpperCase**

    ```java
    @EnableHiSql(
        naming = UpperCaseNamingStrategy.class
    )
    public class Demo2HiSqlConfig {
    }
    ```

* **Custom naming strategy**

    ```java
    public class CustomNamingStrategy implements NamingStrategy{
    }

    @EnableHiSql(
        naming = CustomNamingStrategy.class
    )
    public class Demo2HiSqlConfig {
    }
    ```

### Change PaginationMode

    ```java
    public class CustomPaginationMode implements PaginationMode{
    }
  
    @EnableHiSql(
        paginationMode = CustomPaginationMode.class
    )
    public class Demo2HiSqlConfig {
    }
    ```

### Custom ResultSet Value Converter

    ```java
    @Getter
    public class Device{
        private Integer id;
        private String name;
        private Pattern regex;
    }
    
    @SpringBootApplication
    public class ApplicationTest {
        static {
            // register Pattern converter
            HiSqlResultSetConverter.register(Pattern.class, (value) -> {
                if (value == null) return null;
                return Pattern.compile(String.valueOf(value));
            });
        }
    }
    ```

### Optimize pagination performance

Reuse last total, avoid re-executing "select count(*)".

```java
Pagination pagination=Pagination.of(0,20);
        Page page=repository.page(pagination);

        pagination=Pagination.of(page.getPage()+1,20,page.getTotal());
        repository.page(pagination);

```

## More Example in tests.
