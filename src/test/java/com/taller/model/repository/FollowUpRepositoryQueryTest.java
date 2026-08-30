package com.taller.model.repository;

import org.hibernate.query.hql.internal.HqlParseTreeBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FollowUpRepositoryQueryTest {

    @Test
    void followUpQueriesUseValidPortableHql() {
        for (Method method : FollowUpRepository.class.getDeclaredMethods()) {
            Query query = method.getAnnotation(Query.class);
            if (query == null) continue;
            assertFalse(query.nativeQuery(), () -> method.getName() + " must use JPQL/HQL, not native SQL");
            assertDoesNotThrow(() -> HqlParseTreeBuilder.INSTANCE.buildHqlParser(query.value()).statement(),
                    () -> method.getName() + " has invalid HQL");
            if (!query.countQuery().isBlank()) {
                assertDoesNotThrow(() -> HqlParseTreeBuilder.INSTANCE.buildHqlParser(query.countQuery()).statement(),
                        () -> method.getName() + " has invalid count HQL");
            }
        }
    }
}
