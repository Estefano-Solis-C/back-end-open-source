package com.codexateam.platform.iam.infrastructure.persistence.jpa.repositories;

import com.codexateam.platform.iam.domain.model.aggregates.User;
import com.codexateam.platform.iam.domain.model.valueobjects.EmailAddress; // <--- IMPORT
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(EmailAddress email);
    boolean existsByEmail(EmailAddress email);
}