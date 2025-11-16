package com.example.loghome_project;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 배치 파일에서 호출하는 독립 실행 가능한 로그 기록 프로그램
 */
public class LogWriter {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java LogWriter <message> <profile>");
            System.out.println("Example: java LogWriter \"Hello World\" dev");
            System.exit(1);
        }

        String message = args[0];
        String profile = args.length > 1 ? args[1] : "dev";

        try {
            // 서버 IP 주소 확인
            String serverIp = getServerIp();
            
            // Profile에 따른 로그 디렉토리 설정
            String logDirectory;
            String logFileName;
            
            // prod 환경에서 IP 기반 추가 분기
            if (profile.equals("prod")) {
                if (serverIp.startsWith("192.168.1.")) {
                    // 내부 네트워크 (개발 서버)
                    logDirectory = "logs/batch-prod-internal";
                    logFileName = "batch-prod-internal";
                    profile = "prod-internal";
                } else {
                    // 외부 네트워크 (운영 서버)
                    logDirectory = "logs/batch-prod-external";
                    logFileName = "batch-prod-external";
                    profile = "prod-external";
                }
            } else {
                // dev 환경
                logDirectory = "logs/batch-dev";
                logFileName = "batch-dev";
            }
            
            // 로그 디렉토리 생성
            Path logDir = Paths.get(logDirectory);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                System.out.println("로그 디렉토리 생성: " + logDirectory);
            }
            
            // 파일명 생성 (시간 포함)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_%s_%s.log", logFileName, timestamp, profile);
            Path logFilePath = logDir.resolve(fileName);
            
            // 로그 내용 작성
            StringBuilder fileContent = new StringBuilder();
            fileContent.append("================================================\n");
            fileContent.append("배치에서 생성한 로그\n");
            fileContent.append("Profile: ").append(profile).append("\n");
            fileContent.append("서버 IP: ").append(serverIp).append("\n");
            fileContent.append("저장 시간: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            fileContent.append("================================================\n\n");
            fileContent.append(message).append("\n");
            
            // 파일로 저장
            Files.write(logFilePath, fileContent.toString().getBytes(StandardCharsets.UTF_8), 
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            
            System.out.println("로그가 파일로 저장되었습니다: " + logFilePath.toString());
            System.out.println("저장된 메시지: " + message);
            
        } catch (Exception e) {
            System.err.println("로그 파일 저장 실패: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 서버의 로컬 IP 주소를 가져온다
     * @return IP 주소
     */
    private static String getServerIp() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            Inet4Address ipv4 = (Inet4Address) Inet4Address.getByName(localhost.getHostAddress());
            return ipv4.getHostAddress();
        } catch (Exception e) {
            System.err.println("IP 주소 확인 실패: " + e.getMessage());
            return "UNKNOWN";
        }
    }
}
