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
            assertDoesNotThrow(() -> parse(query.value()),
                    () -> method.getName() + " has invalid HQL");
            if (!query.countQuery().isBlank()) {
                assertDoesNotThrow(() -> parse(query.countQuery()),
                        () -> method.getName() + " has invalid count HQL");
            }
        }
    }

    private void parse(String hql) {
        HqlParseTreeBuilder builder = HqlParseTreeBuilder.INSTANCE;
        builder.buildHqlParser(hql, builder.buildHqlLexer(hql)).statement();
    }
}
