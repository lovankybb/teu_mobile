package com.teumobile.specification;

import com.teumobile.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    private UserSpecification() {
        // Utility class
    }

    public static Specification<User> filterUsers(String search, Boolean isActive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), pattern);
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), pattern);
                Predicate fullNamePredicate = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate phonePredicate = cb.like(root.get("phoneNumber"), pattern);

                predicates.add(cb.or(usernamePredicate, emailPredicate, fullNamePredicate, phonePredicate));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
