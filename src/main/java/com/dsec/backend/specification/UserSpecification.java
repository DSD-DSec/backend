package com.dsec.backend.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import com.dsec.backend.entity.UserEntity;

@Component
public class UserSpecification {

    public Specification<UserEntity> geSpecification(String firstName, String lastName,
            String email, String generalSearch) {
        return firstNameLike(firstName)
                .and(lastNameLike(lastName))
                .and(emailLike(email))
                .and(createGeneralSpecs(generalSearch));
    }

    private Specification<UserEntity> createGeneralSpecs(String str) {
        Specification<UserEntity> spec = firstNameLike(null); // this one is always true;
        if (str == null)
            return spec; // don't filter if null

        String[] parts = str.split("\\s+");

        for (String part : parts) {
            spec = spec.and(firstNameLike(part).or(lastNameLike(part)).or(emailLike(part)));
        }
        return spec;
    }

    private Specification<UserEntity> firstNameLike(String firstName) {
        return (root, query, builder) -> {
            if (firstName == null)
                return builder.conjunction();// don't filter if null

            return builder.like(builder.lower(root.<String>get("firstName")),
                    "%" + firstName.toLowerCase() + "%");
        };
    }

    private Specification<UserEntity> lastNameLike(String lastName) {
        return (root, query, builder) -> {
            if (lastName == null)
                return builder.conjunction();// don't filter if null

            return builder.like(builder.lower(root.<String>get("lastName")),
                    "%" + lastName.toLowerCase() + "%");
        };
    }

    private Specification<UserEntity> emailLike(String email) {
        return (root, query, builder) -> {
            if (email == null)
                return builder.conjunction();// don't filter if null

            return builder.like(builder.lower(root.<String>get("email")),
                    "%" + email.toLowerCase() + "%");
        };
    }

}
