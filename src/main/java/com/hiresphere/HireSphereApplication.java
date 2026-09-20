package com.hiresphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * HireSphereApplication - Entry point for the HireSphere AI Spring Boot application.
 * Real-Time Talent & Placement Platform with Autonomous Agentic AI.
 */
@SpringBootApplication(scanBasePackages = "com.hiresphere")
public class HireSphereApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(HireSphereApplication.class, args);
        System.out.println("==========================================================");
        System.out.println("  HireSphere AI Backend is UP and RUNNING!");
        System.out.println("  Server Port:      http://localhost:8085");
        System.out.println("  API Base:         http://localhost:8085/api");
        System.out.println("  Swagger UI:       http://localhost:8085/swagger-ui.html");
        System.out.println("  H2 Console:       http://localhost:8085/h2-console");
        System.out.println("  Real-time Stream: http://localhost:8085/api/realtime/stream");
        System.out.println("==========================================================");
    }

    private static void loadDotEnv() {
        File[] possibleFiles = new File[] {
                new File(".env"),
                new File("../.env"),
                new File("backend/hiresphere-backend/.env")
        };
        for (File f : possibleFiles) {
            if (f.exists() && f.isFile()) {
                try {
                    List<String> lines = Files.readAllLines(f.toPath());
                    for (String line : lines) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;
                        int eqIdx = line.indexOf('=');
                        if (eqIdx > 0) {
                            String key = line.substring(0, eqIdx).trim();
                            String val = line.substring(eqIdx + 1).trim();
                            if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                                val = val.substring(1, val.length() - 1);
                            }
                            if (!key.isEmpty() && !val.isEmpty() && System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, val);
                            }
                        }
                    }
                    System.out.println("[HireSphere] Loaded environment configuration from: " + f.getAbsolutePath());
                    break;
                } catch (Exception ignored) {
                }
            }
        }
    }
}
