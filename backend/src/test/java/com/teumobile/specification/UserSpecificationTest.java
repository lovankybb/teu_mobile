package com.teumobile.specification;

import com.teumobile.entity.User;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserSpecificationTest {

    @Mock
    private Root<User> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> path;

    @Mock
    private Expression<String> expression;

    @Mock
    private Predicate predicate;

    @Test
    @DisplayName("Kiểm thử UserSpecification với từ khóa tìm kiếm và trạng thái isActive")
    void testFilterUsersWithSearchAndActive() {
        Specification<User> spec = UserSpecification.filterUsers("john", true);

        when(root.get(anyString())).thenReturn(path);
        when(cb.lower(any())).thenReturn(expression);
        when(cb.like(any(), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        doReturn(predicate).when(cb).equal(any(Expression.class), any());
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb, times(1)).or(any(Predicate[].class));
        verify(cb, times(1)).equal(any(Expression.class), eq(true));
    }

    @Test
    @DisplayName("Kiểm thử UserSpecification với tham số null hoặc rỗng")
    void testFilterUsersWithNullParameters() {
        Specification<User> spec = UserSpecification.filterUsers(null, null);

        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb, never()).or(any(Predicate[].class));
    }
}
