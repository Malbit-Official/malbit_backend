package com.example.demo.service;

import com.example.demo.entity.JobType;
import com.example.demo.entity.User;
import com.example.demo.users.dto.UserJoinRequest;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest // 스프링 컨테이너를 띄워서 테스트 (DB 연동 포함)
@Transactional   // 테스트가 끝나면 데이터를 자동으로 롤백(삭제)해줌! DB가 깨끗하게 유지돼요.
class UserServiceTest {

    @Autowired
    UserService userService;
    @Autowired UserRepository userRepository;

    @Test
    @DisplayName("회원가입이 성공적으로 완료되어야 한다")
    void join_success() {
        // 1. Given (준비: 이런 데이터가 있을 때)
        UserJoinRequest request = new UserJoinRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");
        request.setNickname("세오나");
        request.setJobType(JobType.OFFICE);

        // 2. When (실행: 가입을 시도하면)
        Long savedId = userService.join(request);

        // 3. Then (검증: 결과가 이래야 한다)
        User findUser = userRepository.findById(savedId).get();
        assertThat(findUser.getEmail()).isEqualTo("test@test.com");
        assertThat(findUser.getName()).isEqualTo("세오나");
    }
}