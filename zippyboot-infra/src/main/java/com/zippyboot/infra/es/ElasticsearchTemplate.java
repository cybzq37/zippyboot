package com.zippyboot.infra.es;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.MultiGetItem;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ByQueryResponse;
import org.springframework.data.elasticsearch.core.query.BulkOptions;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(ElasticsearchOperations.class)
public class ElasticsearchTemplate {

    private final ElasticsearchOperations operations;

    public <T> T save(T document) {
        return operations.save(document);
    }

    public <T> T save(T document, IndexCoordinates indexCoordinates) {
        return operations.save(document, indexCoordinates);
    }

    public <T> Iterable<T> saveAll(Iterable<T> documents) {
        return operations.save(documents);
    }

    public <T> Iterable<T> saveAll(Iterable<T> documents, IndexCoordinates indexCoordinates) {
        return operations.save(documents, indexCoordinates);
    }

    public <T> Optional<T> get(String id, Class<T> documentClass) {
        return Optional.ofNullable(operations.get(id, documentClass));
    }

    public <T> Optional<T> get(String id, Class<T> documentClass, IndexCoordinates indexCoordinates) {
        return Optional.ofNullable(operations.get(id, documentClass, indexCoordinates));
    }

    public <T> List<MultiGetItem<T>> multiGet(Query query, Class<T> documentClass) {
        return operations.multiGet(query, documentClass);
    }

    public <T> List<MultiGetItem<T>> multiGet(Query query, Class<T> documentClass, IndexCoordinates indexCoordinates) {
        return operations.multiGet(query, documentClass, indexCoordinates);
    }

    public <T> List<MultiGetItem<T>> multiGet(Collection<String> ids, Class<T> documentClass) {
        return operations.multiGet(operations.idsQuery(List.copyOf(ids)), documentClass);
    }

    public <T> List<MultiGetItem<T>> multiGet(Collection<String> ids, Class<T> documentClass, IndexCoordinates indexCoordinates) {
        return operations.multiGet(operations.idsQuery(List.copyOf(ids)), documentClass, indexCoordinates);
    }

    public boolean exists(String id, Class<?> documentClass) {
        return operations.exists(id, documentClass);
    }

    public boolean exists(String id, IndexCoordinates indexCoordinates) {
        return operations.exists(id, indexCoordinates);
    }

    public <T> UpdateResponse update(T document) {
        return operations.update(document);
    }

    public <T> UpdateResponse update(T document, IndexCoordinates indexCoordinates) {
        return operations.update(document, indexCoordinates);
    }

    public void bulkUpdate(List<UpdateQuery> updateQueries, Class<?> documentClass) {
        operations.bulkUpdate(updateQueries, documentClass);
    }

    public void bulkUpdate(List<UpdateQuery> updateQueries, IndexCoordinates indexCoordinates) {
        operations.bulkUpdate(updateQueries, indexCoordinates);
    }

    public void bulkUpdate(List<UpdateQuery> updateQueries, BulkOptions bulkOptions, IndexCoordinates indexCoordinates) {
        operations.bulkUpdate(updateQueries, bulkOptions, indexCoordinates);
    }

    public ByQueryResponse updateByQuery(UpdateQuery updateQuery, Class<?> documentClass) {
        return operations.updateByQuery(updateQuery, operations.getIndexCoordinatesFor(documentClass));
    }

    public ByQueryResponse updateByQuery(UpdateQuery updateQuery, IndexCoordinates indexCoordinates) {
        return operations.updateByQuery(updateQuery, indexCoordinates);
    }

    public String deleteById(String id, Class<?> documentClass) {
        return operations.delete(id, documentClass);
    }

    public String deleteById(String id, IndexCoordinates indexCoordinates) {
        return operations.delete(id, indexCoordinates);
    }

    public String delete(Object document) {
        return operations.delete(document);
    }

    public String delete(Object document, IndexCoordinates indexCoordinates) {
        return operations.delete(document, indexCoordinates);
    }

    public ByQueryResponse delete(DeleteQuery deleteQuery, Class<?> documentClass) {
        return operations.delete(deleteQuery, documentClass);
    }

    public ByQueryResponse delete(DeleteQuery deleteQuery, Class<?> documentClass, IndexCoordinates indexCoordinates) {
        return operations.delete(deleteQuery, documentClass, indexCoordinates);
    }

    public ByQueryResponse delete(Query query, Class<?> documentClass) {
        return operations.delete(DeleteQuery.builder(query).build(), documentClass);
    }

    public ByQueryResponse delete(Query query, Class<?> documentClass, IndexCoordinates indexCoordinates) {
        return operations.delete(DeleteQuery.builder(query).build(), documentClass, indexCoordinates);
    }

    public long count(Query query, Class<?> documentClass) {
        return operations.count(query, documentClass);
    }

    public long count(Query query, Class<?> documentClass, IndexCoordinates indexCoordinates) {
        return operations.count(query, documentClass, indexCoordinates);
    }

    public <T> Optional<SearchHit<T>> searchOne(Query query, Class<T> documentClass) {
        return Optional.ofNullable(operations.searchOne(query, documentClass));
    }

    public <T> Optional<SearchHit<T>> searchOne(Query query, Class<T> documentClass, IndexCoordinates indexCoordinates) {
        return Optional.ofNullable(operations.searchOne(query, documentClass, indexCoordinates));
    }

    public <T> SearchHits<T> search(Query query, Class<T> documentClass) {
        return operations.search(query, documentClass);
    }

    public <T> SearchHits<T> search(Query query, Class<T> documentClass, IndexCoordinates indexCoordinates) {
        return operations.search(query, documentClass, indexCoordinates);
    }

    public <T> SearchPage<T> searchPage(Query query, Class<T> documentClass) {
        return toSearchPage(operations.search(query, documentClass), query.getPageable());
    }

    public <T> SearchPage<T> searchPage(Query query, Class<T> documentClass, IndexCoordinates indexCoordinates) {
        return toSearchPage(operations.search(query, documentClass, indexCoordinates), query.getPageable());
    }

    public <T> Page<T> page(Query query, Class<T> documentClass) {
        return toPage(operations.search(query, documentClass), query.getPageable());
    }

    public <T> Page<T> page(Query query, Class<T> documentClass, IndexCoordinates indexCoordinates) {
        return toPage(operations.search(query, documentClass, indexCoordinates), query.getPageable());
    }

    public <T> SearchHitsIterator<T> searchForStream(Query query, Class<T> documentClass) {
        return operations.searchForStream(query, documentClass);
    }

    public <T> SearchHitsIterator<T> searchForStream(Query query, Class<T> documentClass, IndexCoordinates indexCoordinates) {
        return operations.searchForStream(query, documentClass, indexCoordinates);
    }

    public IndexOperations indexOps(Class<?> documentClass) {
        return operations.indexOps(documentClass);
    }

    public IndexOperations indexOps(IndexCoordinates indexCoordinates) {
        return operations.indexOps(indexCoordinates);
    }

    private <T> SearchPage<T> toSearchPage(SearchHits<T> hits, Pageable pageable) {
        return SearchHitSupport.searchPageFor(hits, pageable);
    }

    private <T> Page<T> toPage(SearchHits<T> hits, Pageable pageable) {
        List<T> content = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
        return new PageImpl<>(content, pageable, hits.getTotalHits());
    }
}
