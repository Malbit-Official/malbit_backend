// 이메일 인증번호 임시 저장과 검증 로직을 담당하는 비즈니스 서비스 클래스
package com.example.demo.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    // 임시 저장소 (이메일 : 인증번호)
    private final Map<String, String> authCodeMap = new HashMap<>();

    // 인증 완료된 이메일 저장소 (이메일 : 인증여부)
    private final Map<String, Boolean> verifiedEmailMap = new HashMap<>();

    // 비밀번호 재설정 완료 여부를 저장
    private final Map<String,Boolean> passwordResetReadyMap = new HashMap<>();

    // 6자리 랜덤 인증번호 설정
    private String createCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    // 메일 발송
    public void sendEmail (String toEmail) {
        String authCode = createCode();

        MimeMessage message = mailSender.createMimeMessage();

        try{
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            message.setSubject("[말빛] 회원가입 인증번호입니다.");

            helper.setFrom("baesuna2930@gmail.com", "말빛(Malbit)");

            helper.setText("인증번호는 <b>[" + authCode + "]</b> 입니다.", true);

            mailSender.send(message);

            authCodeMap.put(toEmail, authCode);
        } catch (Exception e) {
            throw new RuntimeException("메일 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 인증번호 확인
    public boolean verifyCode(String email, String code) {
        String savedCode = authCodeMap.get(email);

        if (savedCode != null && savedCode.equals(code)) {
            // 인증 성공 시 완료 명단에 추가
            verifiedEmailMap.put(email, true);
            passwordResetReadyMap.put(email, true);
            // 사용한 인증번호는 삭제 (재사용 방지)
            authCodeMap.remove(email);
            return true;
        }
        return false;
    }

    // 비밀번호 재설정 가능한 상태인지 확인
    public boolean isReadyToReset(String email) {
        return passwordResetReadyMap.getOrDefault(email, false);
    }

    // 회원가입 시 호출할 인증 완료 여부 확인
    public boolean isVerified(String email) {
        return verifiedEmailMap.getOrDefault(email, false);
    }

    // 회원가입 완료 후 기록 삭제
    public void removeVerification(String email) {
        verifiedEmailMap.remove(email);
    }
}
