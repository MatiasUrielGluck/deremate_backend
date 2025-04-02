package com.matiasugluck.deremate_backend.repository;

import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);
    VerificationToken findByUser(User user);
}