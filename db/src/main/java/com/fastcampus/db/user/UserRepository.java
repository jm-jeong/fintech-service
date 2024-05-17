package com.fastcampus.db.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
	Optional<UserAccount> findByName(String name);

	Optional<UserAccount> findByEmail(String email);
}
