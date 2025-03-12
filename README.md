# Hi SQL

[中文](README.zh-TW.md)

## Overview

![](images/orm.png)
![](images/write_sql.png)

A simple and highly flexible pure __SQL__ library.

There are many __ORM Frameworks__ available, and different projects may use different __ORMs__. When using them, you need to design __SQL__ based on requirements, convert it into the corresponding __ORM__ syntax, and ensure that the __ORM__ generates the expected __SQL__. Special __SQL__ cases require extra effort to convert to __ORM__ syntax, and some need adjustments based on the database being used. The pros and cons of __ORMs__ are well known, so they won't be discussed further here.

## Requirements

* Spring Framework 5+
* Java 11+

## Features

* Primarily __Native SQL__
* High performance
* Provides basic parameterized queries and dynamic __SQL__ substitution
* Maps query results to any __Java__ object
* Supports __Spring @Transactional__

## Quick Start

### Configuration

By default, it scans all __Interfaces__ annotated with __@Repository__ within the same __Package__.

[HiSqlConfig.java](src/test/java/pers/clare/hisql/data/HiSqlConfig.java)

```java
@EnableHiSql
public class HiSqlConfig {
    
}
```

### Create Entity and Interface

* __SQLCrudRepository__

  Extend __SQLCrudRepository__ to get basic INSERT, UPDATE, DELETE, and SELECT functionalities for an __Entity__.

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

    * Standard parameters

      ```java
      @Repository
      public interface DemoRepository extends SQLRepository {
      
        @HiSql("select * from test where value=:value")
        List<Object> findAll(String value);
      }
      ```

    * Object parameters

      Converts an object into parameters like __id__, __obj.id__, __name__, __obj.name__. If duplicate parameter names exist, they are overwritten in order.

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

* Dynamic __SQL__ Substitution

  Use this method when dynamically adding conditions or replacing __SQL__ strings.

  Avoid manual __SQL__ concatenation externally to prevent __SQL Injection__. Follow the example below.

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
          // Simple string substitution
          demoRepository.findAll("and column = :value", value);
          // Using SqlReplace, if value is null, it is replaced with blank, otherwise, it is substituted along with the value parameter
          demoRepository.findAll(SqlReplace.of(value," and column = :value"));
      }
  }
  ```

* Read __SQL__ from __XML__

    * The default root directory is __resources/hisql/__, where an __XML__ file with the same name as the __Class__ should be created.

      Example: `resources/hisql/CustomRepository.xml`

    * Create a __Tag__ with the same name as the **Method Name** or use __@HiSql(name=...)__ to specify a __Tag__.

        ```xml
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE SQL>
        <SQL>
            <findAll><![CDATA[
                select * from user where 1=1 {id} and :id is not null
            ]]></findAll>
        </SQL>
        ```

* More examples in tests

  [CustomRepository.java](src/test/java/pers/clare/hisql/data/repository/CustomRepository.java)

## Advanced

* ### Pagination Optimization

  Use an initially calculated total or an estimated value to avoid repeatedly calculating the total when paginating. If precise totals are required, this method is not recommended.

    ```java
    Pagination pagination = Pagination.of(0,20);
    Page page = repository.page(pagination);

    pagination = Pagination.of(page.getPage() + 1, page.getSize(), page.getTotal());
    repository.page(pagination);
    ```

* ### Virtual Total Count

  In pagination mode, using __select count(*)__ to query the total count can be very slow. Estimating the total count avoids scanning the entire result set.

  ### Usage

    ```java
    pagination.setVirtualTotal(true);
    ```

  ### Implementation

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

* ### Modifying __PaginationMode__

  ### Configuration

    ```java
    @EnableHiSql(
        paginationMode = MySQLPaginationMode.class
    )
    public class Demo2HiSqlConfig {
    }
    ```

  ### Custom Implementation

    ```java
    public class CustomPaginationMode implements PaginationMode {

        public void appendPaginationSQL(
                StringBuilder sql
                , Pagination pagination
        ) {
            appendSortSQL(sql, pagination.getSorts());
            sql.append(" limit ")
                    .append(pagination.getSize() * pagination.getPage())
                    .append(',')
                    .append(pagination.getSize());
        }

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
    ```

* ### Modifying NamingStrategy

  ### Configuration

    ```java
    @EnableHiSql(
        naming = UpperCaseNamingStrategy.class
    )
    public class Demo2HiSqlConfig {
    }
    ```

  ### Custom Implementation

    ```java
    public class CustomNamingStrategy implements NamingStrategy{
    }

    @EnableHiSql(
        naming = CustomNamingStrategy.class
    )
    public class Demo2HiSqlConfig {
    }
    ```

* ### Custom ResultSetConverter

  Converts ResultSet Value to the target special type.

  ### Custom Implementation

    ```java
    public class CustomResultSetConverter extends ResultSetConverter {
        {
            register(Pattern.class, (rs, i) -> Pattern.compile(rs.getString(i)));
        }
    }
    ```

  ### Configuration

    ```java
    @EnableHiSql(
            resultSetConverter = CustomResultSetConverter.class
    )
    public class HiSqlConfig {
    }
    ```


