package com.example.demo.repository;

import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 정보를 찾아오는 기능
    Optional<User> findByEmail(String email);

    // 중복 가입 확인
    boolean existsByEmail(String email);
}