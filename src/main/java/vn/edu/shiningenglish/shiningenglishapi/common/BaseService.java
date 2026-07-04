package vn.edu.shiningenglish.shiningenglishapi.common;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.shiningenglish.shiningenglishapi.valueobject.QueryOption;

public abstract class BaseService<T, ID> {

    protected final JpaRepository<T, ID> repository;

    protected BaseService(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    public Optional<T> getById(ID id) {
        return repository.findById(id);
    }

    public T create(T entity) {
        return repository.save(entity);
    }

    public T update(T entity) {
        return repository.save(entity);
    }

    public boolean deleteById(ID id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public long count() {
        return repository.count();
    }

    protected Pageable buildPageable(QueryOption options, Sort defaultSort) {
        if (options == null) {
            return PageRequest.of(0, 15, defaultSort);
        }
        var page = options.getPage() != null ? options.getPage() - 1 : 0;
        var perPage = options.getPerPage();
        var sort = Sort.by(
            options.getOrderDirection().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
            options.getOrderBy()
        );
        return PageRequest.of(page, perPage, sort);
    }

    protected Pageable buildPageable(QueryOption options) {
        return buildPageable(options, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
