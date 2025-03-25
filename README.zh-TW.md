# Hi SQL

[English](README.md)

## 概述

![](images/orm.png)
![](images/write_sql.png)

簡單、彈性高的純 __SQL__ 套件。

市面上有許多 __ORM Framework__，不同專案可能使用不同的 __ORM__，使用時，根據需求設計 __SQL__，再轉換成對應的 __ORM__ 語法，還須確定
__ORM__ 轉換成預期的 __SQL__，遇到特殊的 __SQL__ 時，得花更多的時間轉成 __ORM__ 語法，少部分需要依使用的資料庫做調整，更多的優缺點，就不多說。

## 必要條件

* Spring Framework 5+
* Java 11+

## 特色

* __Native SQL__ 為主
* 高效能
* 提供基本的參數化查詢和動態 __SQL__ 替換
* 將查詢結果映射成任意 __Java__ 物件
* 支持 __Spring @Transactional__

## 快速開始

### 配置

預設掃描同 __Package__ 下的所有有 __@Repository__ 的 __Interface__。

[HiSqlConfig.java](src/test/java/pers/clare/hisql/data/HiSqlConfig.java)

```java

@EnableHiSql
public class HiSqlConfig {

}
```

### 建立 Entity 和 Interface

* __SQLCrudRepository__

  繼承 __SQLCrudRepository__，即有對 __Entity__ INSERT、UPDATE、DELETE 和 SELECT 的基本功能。

  [User.java](src/test/java/pers/clare/hisql/data/entity/User.java)

  [UserRepository.java](src/test/java/pers/clare/hisql/data/repository/UserRepository.java)

  ```java
  @Repository
  public interface UserRepository extends SQLCrudRepository<User, Long> {
  
  }
  ```

* __SQLRepository__

  繼承 __SQLRepository__，建立輕量化的 __Repository__。

### 使用

```java

@Service
public class UserService {

    private UserRepository userRepository;

    public User find(Long id) {
        if (id == null) return null;
        return userRepository.findById(id);
    }

}
```

## 功能

* 參數化

    * 一般參數

      ```java
      @Repository
      public interface DemoRepository extends SQLRepository {
      
        @HiSql("select * from test where value=:value")
        List<Object> findAll(String value);
      }
      ```

    * 物件參數

      將物件解析為 __id__、__obj.id__、__name__、__obj.name__ 參數名稱，如果有相同的參數名稱時，則會根據順序覆蓋。

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

* 動態 __SQL__ 替換

  根據程式邏輯，需要動態增加條件或者替換 __SQL__ 字串時，可以使用該方法。

  另外，請勿自行在外部拼接 __SQL__，應遵循範例中的做法，避免 __SQL Injection__。

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
          // 簡單的字串替換操作
          demoRepository.findAll("and column = :value", value);
          // 使用 SqlReplace，如果 value 是 null，則會替換成空白，反之則替換為指定字串，並且帶入 value 參數
          demoRepository.findAll(SqlReplace.of(value," and column = :value"));
      }
  }
  ```

* 從 __XML__ 讀取 __SQL__

    * 預設根目錄為 __resources/hisql/__，建立和 __Class Name__ 一樣的 __XML__ 檔。

      範例： `resources/hisql/CustomRepository.xml`

    * 建立和 **Method Name** 一樣的 __Tag__ 或者 __@HiSql(name=...)__ 指定 __Tag__

        ```xml
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE SQL>
        <SQL>
            <findAll><![CDATA[
                select * from user where 1=1 {id} and :id is not null
            ]]></findAll>
        </SQL>
        ```

* 參考測試更多範例

  [CustomRepository.java](src/test/java/pers/clare/hisql/data/repository/CustomRepository.java)

## 進階

* ### 分頁優化

  使用初次計算的總數或預估值，避免在翻頁時重複計算總數。如果對總數精度有要求，則不建議使用此方式。

    ```java
    Pagination pagination = Pagination.of(0,20);
    Page page = repository.page(pagination);

    pagination = Pagination.of(page.getPage() + 1, page.getSize(), page.getTotal());
    repository.page(pagination);
    ```

* ### 虛擬總數

  在分頁模式中，使用 __select count(*)__ 查詢總數會非常慢，通過預估總數可以避免對整個結果集進行掃描。

  ### 使用方式

    ```java
    pagination.setVirtualTotal(true);
    ```

  ### 實作

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


* ### 修改 __PaginationMode__

  ### 設定

    ```java
    @EnableHiSql(
        paginationMode = MySQLPaginationMode.class
    )
    public class Demo2HiSqlConfig {

    }
    ```

  ### 自定義

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

* ### 修改 NamingStrategy

  ### 設定

    ```java
    @EnableHiSql(
        naming = UpperCaseNamingStrategy.class
    )
    public class Demo2HiSqlConfig {
    }
    ```

  ### 自定義

    ```java
    public class CustomNamingStrategy implements NamingStrategy{
    }

    @EnableHiSql(
        naming = CustomNamingStrategy.class
    )
    public class Demo2HiSqlConfig {
    }
    ```


* ### 自定義 ResultSetConverter

  將 ResultSet Value 轉為目標的特殊類型。

  ### 自定義

    ```java
    public class CustomResultSetConverter extends ResultSetConverter {
        {
            register(Pattern.class, (rs, i) -> Pattern.compile(rs.getString(i)));
        }
    }
    ```

  ### 設定

    ```java
    @EnableHiSql(
            resultSetConverter = CustomResultSetConverter.class
    )
    public class HiSqlConfig {
    }
    ```
