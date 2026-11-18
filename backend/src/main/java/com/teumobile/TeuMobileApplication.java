package com.teumobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
@EnableFeignClients
public class TeuMobileApplication {

    public static void main(String[] args) {
        loadDotEnvIfPresent();
        SpringApplication.run(TeuMobileApplication.class, args);
    }

    /**
     * Tự động nạp các biến môi trường từ file .env (ở thư mục gốc dự án hoặc thư mục hiện tại)
     * vào System Properties để ứng dụng luôn có cấu hình (Cloudinary, DB, Keycloak)
     * ngay cả khi chạy trực tiếp từ IDE mà không cần plugin bên ngoài.
     */
    private static void loadDotEnvIfPresent() {
        File[] candidateFiles = new File[]{
                new File(".env"),
                new File("../.env"),
                new File("backend/.env")
        };

        for (File file : candidateFiles) {
            if (file.exists() && file.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        if (eqIdx > 0) {
                            String key = line.substring(0, eqIdx).trim();
                            String value = line.substring(eqIdx + 1).trim();
                            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'"))) {
                                value = value.substring(1, value.length() - 1);
                            }
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, value);
                            }
                        }
                    }
                    System.out.println("[TeuMobile] Đã tự động nạp biến môi trường từ " + file.getAbsolutePath());
                    break;
                } catch (Exception e) {
                    System.err.println("[TeuMobile] Không thể đọc file .env: " + e.getMessage());
                }
            }
        }
    }
}
