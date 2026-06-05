// 파일 저장과 관련된 로직을 담당하는 클래스
package com.example.demo.global.infrastructure;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

    // 프로젝트 루트 경로 아래에 저장 (테스트용)
    private final String rootPath = System.getProperty("user.dir");

    public String saveFile(MultipartFile file, String subDir) throws IOException {
        if (file.isEmpty()) return null;

        // 저장 디렉토리 설정 (ex: /uploads/profiles/)
        String uploadPath = rootPath + File.separator + "uploads" + File.separator + subDir + File.separator;
        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();

        // 파일명 중복 방지 (UUID + 원본파일명)
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        File dest = new File(uploadPath + fileName);

        // 실제 파일 저장
        file.transferTo(dest);

        // DB에 저장할 상대 경로 반환
        return "/uploads/" + subDir + "/" + fileName;
    }
}
