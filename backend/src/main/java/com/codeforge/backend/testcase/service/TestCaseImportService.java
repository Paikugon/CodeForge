package com.codeforge.backend.testcase.service;

import com.codeforge.backend.testcase.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class TestCaseImportService {
    public record PreviewCase(String name, int inputBytes, int outputBytes, String inputPreview, String outputPreview) {}
    public record Preview(List<PreviewCase> testCases) {}
    public record Imported(int createdCount) {}
    private record Part(String text, int bytes) {}
    private static class Pair { Part input; Part output; }
    private final TestCaseService testCases;

    public TestCaseImportService(TestCaseService testCases) { this.testCases = testCases; }

    public Preview preview(Long problemId, List<MultipartFile> files) {
        testCases.status(problemId);
        var parsed = parse(files);
        return new Preview(parsed.entrySet().stream().map(entry -> new PreviewCase(entry.getKey(),
                entry.getValue().input.bytes(), entry.getValue().output.bytes(),
                previewText(entry.getValue().input.text()), previewText(entry.getValue().output.text()))).toList());
    }

    public Imported importFiles(Long problemId, List<MultipartFile> files, TestCaseImportRequest manifest) {
        if (manifest == null || manifest.testCases() == null || manifest.testCases().isEmpty()
                || manifest.testCases().size() > TestCaseLimits.MAX_CASES)
            fail("CODE_TEST_CASE_IMPORT_MANIFEST", "");
        var parsed = parse(files);
        var seen = new HashSet<String>();
        var requests = new ArrayList<TestCaseRequest>();
        for (var entry : manifest.testCases()) {
            if (entry == null || entry.name() == null || (entry.orderIndex() != null && entry.orderIndex() < 0))
                fail("CODE_TEST_CASE_IMPORT_MANIFEST", "");
            Pair pair = parsed.get(entry.name());
            if (pair == null || !seen.add(entry.name())) fail("CODE_TEST_CASE_IMPORT_MANIFEST", entry.name());
            requests.add(new TestCaseRequest(pair.input.text(), pair.output.text(), entry.isSample(), entry.orderIndex()));
        }
        if (seen.size() != parsed.size()) fail("CODE_TEST_CASE_IMPORT_MANIFEST", "");
        // The proxied service owns one DB transaction and publishes one sync event.
        return new Imported(testCases.createBatch(problemId, requests).size());
    }

    private Map<String, Pair> parse(List<MultipartFile> files) {
        if (files == null || files.isEmpty() || files.size() > 200) fail("CODE_TEST_CASE_IMPORT_COUNT", "");
        var pairs = new LinkedHashMap<String, Pair>();
        long[] total = {0};
        try {
            boolean zip = files.size() == 1 && filename(files.getFirst()).toLowerCase(Locale.ROOT).endsWith(".zip");
            if (zip) {
                MultipartFile file = files.getFirst();
                if (file.getSize() > TestCaseLimits.MAX_BATCH_BYTES) fail("CODE_TEST_CASE_IMPORT_TOO_LARGE", filename(file));
                int entries = 0;
                try (var input = new ZipInputStream(file.getInputStream(), StandardCharsets.UTF_8)) {
                    ZipEntry entry;
                    while ((entry = input.getNextEntry()) != null) {
                        if (++entries > 400) fail("CODE_TEST_CASE_IMPORT_COUNT", "");
                        if (entry.isDirectory()) {
                            if (input.read() != -1) fail("CODE_TEST_CASE_IMPORT_FORMAT", entry.getName());
                            continue;
                        }
                        add(pairs, entry.getName(), read(input, entry.getName()), total);
                    }
                    if (entries == 0) fail("CODE_TEST_CASE_IMPORT_FORMAT", filename(file));
                }
            } else {
                for (MultipartFile file : files) {
                    String name = filename(file);
                    if (file.getSize() > TestCaseLimits.MAX_FILE_BYTES) fail("CODE_TEST_CASE_IMPORT_TOO_LARGE", name);
                    try (var input = file.getInputStream()) { add(pairs, name, read(input, name), total); }
                }
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw new TestCaseImportException("CODE_TEST_CASE_IMPORT_FORMAT", "");
        }
        if (pairs.isEmpty() || pairs.size() > TestCaseLimits.MAX_CASES) fail("CODE_TEST_CASE_IMPORT_COUNT", "");
        for (var entry : pairs.entrySet()) {
            if (entry.getValue().input == null || entry.getValue().output == null) fail("CODE_TEST_CASE_IMPORT_MISSING_PAIR", entry.getKey());
        }
        return pairs;
    }

    private void add(Map<String, Pair> pairs, String filename, byte[] bytes, long[] total) {
        if (filename == null || filename.length() > 255 || filename.startsWith("/") || filename.contains("\\")
                || filename.chars().anyMatch(Character::isISOControl)
                || Arrays.stream(filename.split("/", -1)).anyMatch(part -> part.isEmpty() || part.equals(".") || part.equals(".."))) {
            fail("CODE_TEST_CASE_IMPORT_FORMAT", "");
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        boolean in = lower.endsWith(".in");
        if (!in && !lower.endsWith(".out")) fail("CODE_TEST_CASE_IMPORT_FORMAT", filename);
        String name = filename.substring(0, filename.length() - (in ? 3 : 4));
        if (name.isBlank()) fail("CODE_TEST_CASE_IMPORT_FORMAT", filename);
        total[0] += bytes.length;
        if (total[0] > TestCaseLimits.MAX_BATCH_BYTES) fail("CODE_TEST_CASE_IMPORT_TOO_LARGE", filename);
        Pair pair = pairs.computeIfAbsent(name, ignored -> new Pair());
        if (pairs.size() > TestCaseLimits.MAX_CASES) fail("CODE_TEST_CASE_IMPORT_COUNT", filename);
        if ((in && pair.input != null) || (!in && pair.output != null)) fail("CODE_TEST_CASE_IMPORT_DUPLICATE", filename);
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException exception) { throw new TestCaseImportException("CODE_TEST_CASE_IMPORT_ENCODING", filename); }
        if (text.startsWith("\uFEFF")) text = text.substring(1);
        if (text.indexOf('\0') >= 0) fail("CODE_TEST_CASE_IMPORT_FORMAT", filename);
        Part part = new Part(text, bytes.length);
        if (in) pair.input = part; else pair.output = part;
    }

    private byte[] read(InputStream input, String name) throws IOException {
        byte[] bytes = input.readNBytes(TestCaseLimits.MAX_FILE_BYTES + 1);
        if (bytes.length > TestCaseLimits.MAX_FILE_BYTES) fail("CODE_TEST_CASE_IMPORT_TOO_LARGE", name);
        return bytes;
    }

    private String previewText(String text) { return text.substring(0, Math.min(text.length(), 1000)); }
    private String filename(MultipartFile file) { return file.getOriginalFilename() == null ? "" : file.getOriginalFilename(); }
    private void fail(String code, String file) { throw new TestCaseImportException(code, file); }
}
