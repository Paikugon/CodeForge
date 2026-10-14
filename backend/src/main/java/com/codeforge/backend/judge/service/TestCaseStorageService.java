package com.codeforge.backend.judge.service;

import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
public class TestCaseStorageService {

    private final Path storageRoot;
    private final ObjectMapper objectMapper;

    public TestCaseStorageService(
            @Value("${judge.storage.path}") String storagePath,
            ObjectMapper objectMapper) {

        this.storageRoot = Path.of(storagePath).toAbsolutePath().normalize();
        this.objectMapper = objectMapper;
    }

    public String getTestCaseDirectoryId(Long problemId) {
        return "codeforge_" + problemId;
    }

    public void writeTestCase(
            Long problemId,
            Long testCaseId,
            String input,
            String expectedOutput) throws IOException {

        Path directory = getProblemDirectory(problemId);
        Files.createDirectories(directory);

        writeAtomically(
                directory.resolve(testCaseId + ".in"),
                input
        );

        writeAtomically(
                directory.resolve(testCaseId + ".out"),
                expectedOutput
        );
    }

    public void deleteTestCase(Long problemId, Long testCaseId)
            throws IOException {

        Path directory = getProblemDirectory(problemId);

        Files.deleteIfExists(directory.resolve(testCaseId + ".in"));
        Files.deleteIfExists(directory.resolve(testCaseId + ".out"));
    }

    public void deleteTestCasesNotIn(
            Long problemId,
            Set<Long> testCaseIds) throws IOException {

        Path directory = getProblemDirectory(problemId);
        if (!Files.isDirectory(directory)) {
            return;
        }

        try (var paths = Files.list(directory)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".in")
                            || path.getFileName().toString().endsWith(".out"))
                    .forEach(path -> deleteIfNotExpected(path, testCaseIds));
        }
    }

    public void writeInfo(
            Long problemId,
            Map<Long, String> expectedOutputs) throws IOException {

        Path directory = getProblemDirectory(problemId);
        Files.createDirectories(directory);

        Map<String, Object> testCases = new LinkedHashMap<>();

        for (Map.Entry<Long, String> entry : expectedOutputs.entrySet()) {
            Long id = entry.getKey();

            Map<String, String> metadata = new LinkedHashMap<>();
            metadata.put("input_name", id + ".in");
            metadata.put("output_name", id + ".out");
            metadata.put(
                    "stripped_output_md5",
                    md5(entry.getValue().strip())
            );

            testCases.put(id + ".in", metadata);
        }

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("spj", false);
        info.put("test_cases", testCases);

        writeAtomically(
                directory.resolve("info"),
                objectMapper.writeValueAsString(info)
        );
    }

    private void deleteIfNotExpected(Path path, Set<Long> testCaseIds) {
        String fileName = path.getFileName().toString();
        String idPart = fileName.substring(0, fileName.length() - 4);

        try {
            if (!testCaseIds.contains(Long.valueOf(idPart))) {
                Files.deleteIfExists(path);
            }
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Unexpected test case file name: " + fileName,
                    e);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to delete stale test case file: " + path,
                    e);
        }
    }

    private Path getProblemDirectory(Long problemId) {
        if (problemId == null || problemId <= 0) {
            throw new IllegalArgumentException("Invalid problem ID");
        }

        return storageRoot.resolve(getTestCaseDirectoryId(problemId));
    }

    private void writeAtomically(Path target, String content)
            throws IOException {

        Path temporary = Files.createTempFile(
                target.getParent(),
                ".codeforge-",
                ".tmp"
        );

        try {
            Files.writeString(
                    temporary,
                    content,
                    StandardCharsets.UTF_8
            );

            try {
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private String md5(String value) {
        try {
            byte[] digest = MessageDigest
                    .getInstance("MD5")
                    .digest(value.getBytes(StandardCharsets.UTF_8));

            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 unavailable", e);
        }
    }
}