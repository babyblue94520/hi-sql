# Hi SQL

[English](README.md)

## 概述

![](images/orm.png)
![](images/write_sql.png)

Hi SQL 是一個簡單且具備高彈性的純 __SQL__ 套件。

### 為什麼選擇 Hi SQL？
市面上雖然有許多 __ORM 框架 (Framework)__，但在不同專案中可能面臨以下挑戰：
- 需要學習特定的 ORM 語法。
- 難以確保 ORM 產生的 SQL 是否符合預期性能。
- 處理複雜或特殊的 SQL 時，轉換成 ORM 語法的成本極高。
- 部分 ORM 與特定資料庫耦合度過高。

Hi SQL 直接使用原生 SQL，讓開發者能完全掌握查詢邏輯，同時保有開發效率。

## 系統需求

* Spring Framework 5+
* Java 11+

## 特色

* **原生 SQL 為主**：直接編寫 SQL，掌握最高控制權。
* **高效能**：極輕量化的封裝，減少額外開銷。
* **參數化與動態替換**：提供直覺的參數化查詢與語法替換功能。
* **彈性物件映射**：輕易將查詢結果映射至任何 Java 物件。
* **Spring 整合**：完整支持 `@Transactional` 事務管理。

## 快速開始

### 配置 (Configuration)

預設會掃描指定套件 (Package) 下所有標記 `@Repository` 的介面 (Interface)。

[HiSqlConfig.java](src/test/java/pers/clare/hisql/data/HiSqlConfig.java)

```java
@EnableHiSql
public class HiSqlConfig {
}
```

### 建立實體 (Entity) 與介面 (Interface)

* **SQLCrudRepository**

  繼承 `SQLCrudRepository` 即可獲得實體 (Entity) 的基本增刪改查 (INSERT, UPDATE, DELETE, SELECT) 功能。

  [User.java](src/test/java/pers/clare/hisql/data/entity/User.java) | [UserRepository.java](src/test/java/pers/clare/hisql/data/repository/UserRepository.java)

  ```java
  @Repository
  public interface UserRepository extends SQLCrudRepository<User, Long> {
  }
  ```

* **SQLRepository**

  繼承 `SQLRepository` 則可建立更輕量、自定義程度更高的 Repository。

### 基本使用

```java
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User find(Long id) {
        if (id == null) return null;
        return userRepository.findById(id);
    }
}
```

## 主要功能

### 1. 參數化查詢

* **一般參數**

  ```java
  @Repository
  public interface DemoRepository extends SQLRepository {
      @HiSql("SELECT * FROM test WHERE value = :value")
      List<Object> findAll(String value);
  }
  ```

* **物件參數**

  自動將物件屬性解析為參數名稱（如 `id`、`obj.id`、`name`、`obj.name`）。若參數名稱衝突，將依順序覆蓋。

  ```java
  @Getter
  @Setter
  public class ValueObject {
      private Integer id;
      private String name;
  }

  @Repository
  public interface DemoRepository extends SQLRepository {
      @HiSql("SELECT * FROM test WHERE name = :obj.name LIMIT 1")
      ValueObject findOne(ValueObject obj);

      @HiSql("SELECT * FROM test WHERE name = :name")
      List<ValueObject> findAll(ValueObject obj);
  }
  ```

### 2. 動態 SQL 替換

根據程式邏輯動態增減內容。**請遵循範例做法，切勿自行拼接字串，以避免 SQL 注入 (SQL Injection) 攻擊。**

#### 使用 SqlReplace

```java
@Repository
public interface DemoRepository extends SQLRepository {
    @HiSql("SELECT * FROM test WHERE 1=1 {valueCondition}")
    List<Map<String, Object>> findByString(String valueCondition, String value);
    
    @HiSql("SELECT * FROM test WHERE 1=1 {value}")
    List<Map<String, Object>> findByReplace(SqlReplace<Object> value);
}

@Service
public class DemoService {
    private DemoRepository demoRepository;
      
    public void demo(String value) {
        // 簡單字串替換
        demoRepository.findByString("AND column = :value", value);
        
        // 使用 SqlReplace：若 value 為 null 則替換為空字串，否則帶入指定語法
        demoRepository.findByReplace(SqlReplace.of(value, " AND column = :value"));
    }
}
```

#### 使用 SqlReplacer (自動匹配)

預設替換規則：
- `String`: `null` 或空字串不替換。
- `Collection/Array`: 為空不替換。
- 其他類型: `null` 不替換。

```java
@Repository
public interface DemoRepository extends SQLRepository {
    @HiSql("""
      SELECT * FROM test
      WHERE 1=1
          {value}
          {replace2}
          {value3}
      """)
    List<Map<String, Object>> findAll(SqlReplacer sqlReplacer, DemoQuery query);
}

@Service
public class DemoService {
    private DemoRepository demoRepository;
      
    public void findAll(DemoQuery query) {
        demoRepository.findAll(
            SqlReplacer.create()
                .add("value", " AND column = :value") // 名稱一致時
                .add("replace2", "value2", " AND column2 = :value2") // 名稱不一致時
                .add("value3", " AND column3 = :value3", (v) -> v), // 自訂判斷邏輯
            query
        );
    }
}
```

### 3. 從 XML 讀取 SQL

* **目錄規範**：預設路徑為 `resources/hisql/`，檔案名稱須與類別名稱一致。
* **標籤設定**：標籤名稱須與方法名稱一致，或透過 `@HiSql(name=...)` 指定。

範例：`resources/hisql/CustomRepository.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE SQL>
<SQL>
    <findAll><![CDATA[
        SELECT * FROM user WHERE 1=1 {id} AND :id IS NOT NULL
    ]]></findAll>
</SQL>
```

## 進階應用

### 分頁優化 (Pagination)

透過使用初次計算的總數，避免翻頁時重複執行 `COUNT(*)`。

```java
Pagination pagination = Pagination.of(0, 20);
Page page = repository.page(pagination);

// 下一頁時帶入前次的總數
pagination = Pagination.of(page.getPage() + 1, page.getSize(), page.getTotal());
repository.page(pagination);
```

### 虛擬總數 (Virtual Total)

針對大數據量，使用 `EXPLAIN` 的預估值替代精確的 `COUNT(*)`，大幅提升查詢性能。

```java
pagination.setVirtualTotal(true);
```

### 游標分頁 (Cursor Pagination)

針對連續資料讀取，您可以啟用游標分頁來取代傳統的 offset 分頁。啟用後，offset 將強制為 0，並依賴游標條件 (例如：`id > :lastId`) 來達到高效能的循序查找。

```java
pagination.setCursor(true);
```

#### MySQL 實作範例
```java
@Override
public long getVirtualTotal(Pagination pagination, Connection connection, String sql, Object[] parameters) throws SQLException {
    String totalSql = "EXPLAIN SELECT COUNT(*) FROM(" + sql + ")t";
    ResultSet rs = ConnectionUtil.query(connection, totalSql, parameters);
    if (rs.next()) {
        return rs.getLong("rows");
    }
    throw new HiSqlException("Query total error: " + totalSql);
}
```

### 自定義組件

#### 修改分頁模式 (PaginationMode)
```java
@EnableHiSql(paginationMode = MySQLPaginationMode.class)
public class HiSqlConfig {
}
```

#### 修改命名策略 (NamingStrategy)
```java
@EnableHiSql(naming = UpperCaseNamingStrategy.class)
public class HiSqlConfig {
}
```

#### 自定義結果集轉換 (ResultSetConverter)
```java
public class CustomResultSetConverter extends ResultSetConverter {
    {
        // 註冊特殊類型轉換邏輯，例如 Pattern
        register(Pattern.class, (rs, i) -> Pattern.compile(rs.getString(i)));
    }
}

@EnableHiSql(resultSetConverter = CustomResultSetConverter.class)
public class HiSqlConfig {
}
```

---
*更多詳細範例請參考：[CustomRepository.java](src/test/java/pers/clare/hisql/data/repository/CustomRepository.java)*
