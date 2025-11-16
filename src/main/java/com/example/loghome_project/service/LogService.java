package com.example.loghome_project.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 로그 적재를 담당하는 Service
 */
@Service
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    /**
     * 로그를 파일에 저장
     * @param message 로그 메시지
     * @param profile 환경 (dev/prod)
     * @return 저장된 파일 경로
     */
    public String saveLog(String message, String profile) {
        try {
            // Profile에 따른 로그 디렉토리 설정
            String logDirectory = profile.equals("prod") ? "logs/batch-prod" : "logs/batch-dev";
            String logFileName = profile.equals("prod") ? "batch-prod" : "batch-dev";
            
            // 로그 디렉토리 생성
            Path logDir = Paths.get(logDirectory);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                logger.info("로그 디렉토리 생성: {}", logDirectory);
            }
            
            // 파일명 생성 (시간 포함)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_%s_%s.log", logFileName, timestamp, profile);
            Path logFilePath = logDir.resolve(fileName);
            
            // 로그 내용 작성
            StringBuilder fileContent = new StringBuilder();
            fileContent.append("================================================\n");
            fileContent.append("배치 로그 저장\n");
            fileContent.append("Profile: ").append(profile).append("\n");
            fileContent.append("저장 시간: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            fileContent.append("================================================\n\n");
            fileContent.append(message).append("\n");
            
            // 파일로 저장
            Files.write(logFilePath, fileContent.toString().getBytes(StandardCharsets.UTF_8), 
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            
            logger.info("로그가 파일로 저장되었습니다: {}", logFilePath.toString());
            
            return logFilePath.toString();
            
        } catch (Exception e) {
            logger.error("로그 파일 저장 실패: {}", e.getMessage(), e);
            return "ERROR: " + e.getMessage();
        }
    }
}






