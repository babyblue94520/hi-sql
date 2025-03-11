# Hi SQL

[中文](README.zh-TW.md)

## Overview

![](images/orm.png)
![](images/write_sql.png)

A simple and highly flexible pure __SQL__ package.

There are many __ORM Frameworks__ available, and different projects may use different __ORMs__. When using them, you need to design __SQL__ based on requirements, then convert it into the corresponding __ORM__ syntax. Additionally, you must ensure that the __ORM__ translates into the expected __SQL__. Special __SQL__ cases may require extra effort to convert into __ORM__ syntax, and some adjustments might be necessary depending on the database used. We won’t go into more details about the pros and cons here.

## Requirements

* Spring Framework 5+
* Java 11+

## Features

* Primarily __Native SQL__
* High performance
* Supports basic parameterized queries and dynamic __SQL__ replacement
* Maps query results to any __Java__ object
* Supports __Spring @Transactional__

## Quick Start

### Configuration

By default, it scans all __Interfaces__ annotated with __@Repository__ in the same package.

[HiSqlConfig.java](src/test/java/pers/clare/hisql/data/HiSqlConfig.java)

```java
@EnableHiSql
public class HiSqlConfig {
    
}
```

### Creating Entity and Interface

* __SQLCrudRepository__

  Extend __SQLCrudRepository__ to enable basic functionalities such as INSERT, UPDATE, DELETE, and SELECT for an __Entity__.

  [User.java](src/test/java/pers/clare/hisql/data/entity/User.java)

  [UserRepository.java](src/test/java/pers/clare/hisql/data/repository/UserRepository.java)

  ```java
  @Repository
  public interface UserRepository extends SQLCrudRepository<User, Long> {
  
  }
  ```

* __SQLRepository__

  Extend __SQLRepository__ to create a lightweight __Repository__.

### Usage

```java
@Service
public class UserService {
    
  private UserRepository userRepository;
  
  public User find(Long id){
      if(id == null) return null;
      return userRepository.findById(id);
  }
    
}
```

## Features

* Parameterization

    * Simple parameter

      ```java
      @Repository
      public interface DemoRepository extends SQLRepository {
      
        @HiSql("select * from test where value=:value")
        List<Object> findAll(String value);
      }
      ```

    * Object parameters

      Parses an object into parameters like __id__, __obj.id__, __name__, __obj.name__. If there are duplicate parameter names, they are overridden based on order.

      ```java
      import lombok.Getter;
      import lombok.Setter;     
      
      @Getter
      @Setter
      public class ValueObject{
          private Integer id;
      
          private String name;
      }
      
      @Repository
      public interface DemoRepository extends SQLRepository {
      
        @HiSql("""
              select *
              from test
              where name=:obj.name
              limit 1
          """)
        ValueObject findAll(ValueObject obj);
      
        @HiSql("select * from test where name=:obj.name")
        List<ValueObject> findAll(ValueObject obj);
      
        @HiSql("select * from test where name=:name")
        List<ValueObject> findAll2(ValueObject obj);
      }
      ```

* Dynamic __SQL__ Replacement

  Use this method when you need to dynamically add conditions or replace __SQL__ strings.

  Avoid concatenating __SQL__ externally to prevent __SQL Injection__. Follow the example below.

  ```java
  @Repository
  public interface DemoRepository extends SQLRepository {
    
      @HiSql("select * from test where 1=1 {valueCondition}")
      List<Map<String,Object>> findAll(String valueCondition, String value);
      
      @HiSql("select * from test where 1=1 {value}")
      List<Map<String,Object>> findAll(SqlReplace<Object> value);
  }
    
  @Service
  public class DemoService {
      private DemoRepository demoRepository;
        
      public void findAll(String value){
          // Simple string replacement operation
          demoRepository.findAll("and column = :value", value);
          // Using SqlReplace to replace null with blank or the specified string
          demoRepository.findAll(SqlReplace.of(value," and column = :value"));
      }
  }
  ```

* Read __SQL__ from __XML__

    * The default root directory is __resources/hisql/__, where an __XML__ file with the same name as the __Class Name__ should be created.

      Example: `resources/hisql/CustomRepository.xml`

    * Create a __Tag__ with the same name as the **Method Name** or specify a __Tag__ with __@HiSql(name=...)__

        ```xml
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE SQL>
        <SQL>
            <findAll><![CDATA[
                select * from user where 1=1 {id} and :id is not null
            ]]></findAll>
        </SQL>
        ```

* Check tests for more examples

  [CustomRepository.java](src/test/java/pers/clare/hisql/data/repository/CustomRepository.java)

## Advanced

* ### Pagination Optimization

  Use initially calculated total count or an estimated value to avoid recalculating total count when paginating. Not recommended if precision is required.

    ```java
    Pagination pagination = Pagination.of(0,20);
    Page page = repository.page(pagination);

    pagination = Pagination.of(page.getPage() + 1, page.getSize(), page.getTotal());
    repository.page(pagination);
    ```

* ### Virtual Total Count

  Using __select count(*)__ in pagination can be slow. Instead, use estimated totals to avoid scanning the entire result set.

    ```java
    pagination.setVirtualTotal(true);
    ```

    * MySQL implementation

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

