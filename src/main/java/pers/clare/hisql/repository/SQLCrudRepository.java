package pers.clare.hisql.repository;

import org.springframework.lang.NonNull;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;

import java.util.Collection;
import java.util.List;

public interface SQLCrudRepository<Entity, Key> extends SQLRepository {

    @NonNull
    long count();

    @NonNull
    long count(Entity entity);

    @NonNull
    List<Entity> findAll();

    @NonNull
    List<Entity> findAll(Sort sort);

    @NonNull
    Page<Entity> page(Pagination pagination);

    @NonNull
    Next<Entity> next(Pagination pagination);

    @NonNull
    Entity insert(@NonNull Entity entity);

    @NonNull
    int update(Entity entity);

    @NonNull
    int delete(Entity entity);

    @NonNull
    Collection<Entity> insertAll(@NonNull Collection<Entity> entities);

    @NonNull
    Entity[] insertAll(@NonNull Entity[] entities);

    @NonNull
    int[] updateAll(@NonNull Collection<Entity> entities);

    @NonNull
    int[] updateAll(@NonNull Entity[] entities);

    @NonNull
    int deleteAll();

    @NonNull
    int[] deleteAll(@NonNull Collection<Entity> entities);

    @NonNull
    int[] deleteAll(@NonNull Entity[] entities);

    @NonNull
    long countById(Key key);

    @NonNull
    int deleteById(Key key);

    Entity find(Entity entity);

    Entity findById(Key key);

    <T> T findByObject(T object);

    <T> T insertByObject(T object);

    <T> int updateByObject(T object);

    <T> int deleteByObject(T object);
}
