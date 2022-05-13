package pers.clare.hisql.repository;

import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;

import java.util.Collection;
import java.util.List;

public interface SQLCrudRepository<Entity, Key> extends SQLRepository {

    long count();

    long count(Entity entity);


    List<Entity> findAll();

    List<Entity> findAll(Sort sort);

    Page<Entity> page(Pagination pagination);

    Next<Entity> next(Pagination pagination);

    Entity find(Entity entity);


    Entity insert(Entity entity);

    int update(Entity entity);

    int delete(Entity entity);


    Collection<Entity> insertAll(Collection<Entity> entities);

    Entity[] insertAll(Entity[] entities);

    int[] updateAll(Collection<Entity> entities);

    int[] updateAll(Entity[] entities);

    int deleteAll();

    long countById(Key key);

    Entity findById(Key key);

    int deleteById(Key key);
}
