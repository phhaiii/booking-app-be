package com.myapp.booking.configurations;

import com.myapp.booking.security.CurrentUserMethodArgumentResolver;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentUserMethodArgumentResolver currentUserMethodArgumentResolver;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserMethodArgumentResolver);
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
//        String uploadLocation = uploadPath.toUri().toString(); // e.g. "file:///C:/.../uploads/"
//
//        registry.addResourceHandler("/uploads/**")
//                .addResourceLocations(uploadLocation)
//                .setCachePeriod(3600)
//                .resourceChain(true);
//
//        System.out.println("✅ Static resources configured:");
//        System.out.println("   URL Pattern: /uploads/**");
//        System.out.println("   File Location: " + uploadLocation);
//    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        // Ensure the path ends with a separator for proper resource resolution
        String uploadLocationUri = uploadPath.toUri().toString();
        if (!uploadLocationUri.endsWith("/")) {
            uploadLocationUri += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocationUri)
                .setCachePeriod(3600);

        System.out.println("✅ Static resources configured:");
        System.out.println("   URL Pattern: /uploads/**");
        System.out.println("   File Location: " + uploadLocationUri);
        System.out.println("   Absolute Path: " + uploadPath.toString());
    }


    @PostConstruct
    public void init() {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        System.out.println("═══════════════════════════════════════");
        System.out.println("📁 UPLOAD DIRECTORY CHECK");
        System.out.println("═══════════════════════════════════════");
        System.out.println("Upload dir: " + uploadPath);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("✅ Created upload directory: " + uploadPath);
            } else {
                System.out.println("✅ Upload directory exists");
            }

            System.out.println("Is Directory: " + Files.isDirectory(uploadPath));
            System.out.println("Is Readable: " + Files.isReadable(uploadPath));
            System.out.println("Is Writable: " + Files.isWritable(uploadPath));

            long fileCount = Files.list(uploadPath).count();
            System.out.println("Files in directory: " + fileCount);

            if (fileCount > 0) {
                System.out.println("\n📄 All files in uploads:");
                Files.list(uploadPath)
                        .forEach(file -> System.out.println("  ✓ " + file.getFileName()));
            }

        } catch (IOException e) {
            System.err.println("❌ Error with upload directory: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("═══════════════════════════════════════\n");
    }

    // ✅ THÊM: Rest Controller để debug
    @RestController
    @RequestMapping("/api/debug")
    public static class DebugController {

        @Value("${file.upload-dir:uploads}")
        private String uploadDir;

//        @GetMapping("/uploads")
//        public ResponseEntity<?> listUploads() {
//            try {
//                Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
//
//                List<String> files = Files.list(uploadPath)
//                        .map(path -> path.getFileName().toString())
//                        .collect(Collectors.toList());
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("uploadDir", uploadPath.toString());
//                result.put("fileCount", files.size());
//                result.put("files", files);
//
//                return ResponseEntity.ok(result);
//            } catch (IOException e) {
//                return ResponseEntity.internalServerError()
//                        .body(Map.of("error", e.getMessage()));
//            }
//        }
@GetMapping("/uploads")
public ResponseEntity<?> listUploads() {
    try {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath) || !Files.isDirectory(uploadPath)) {
            return ResponseEntity.ok(Map.of(
                    "uploadDir", uploadPath.toString(),
                    "fileCount", 0,
                    "files", List.of()
            ));
        }

        List<String> files = Files.list(uploadPath)
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("uploadDir", uploadPath.toString());
        result.put("fileCount", files.size());
        result.put("files", files);

        return ResponseEntity.ok(result);
    } catch (IOException e) {
        return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
    }
}

        @GetMapping("/uploads/check/{filename:.+}")
        public ResponseEntity<?> checkFile(@PathVariable String filename) {
            try {
                Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                Path filePath = uploadPath.resolve(filename);

                Map<String, Object> result = new HashMap<>();
                result.put("filename", filename);
                result.put("fullPath", filePath.toString());
                result.put("exists", Files.exists(filePath));
                result.put("isFile", Files.isRegularFile(filePath));
                result.put("readable", Files.isReadable(filePath));

                if (Files.exists(filePath)) {
                    result.put("size", Files.size(filePath));
                }

                return ResponseEntity.ok(result);
            } catch (IOException e) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", e.getMessage()));
            }
        }
    }
}