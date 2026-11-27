package com.codeforge.backend.testcase.controller;

import com.codeforge.backend.common.pagination.PaginationValidator;
import com.codeforge.backend.testcase.dto.TestCaseRequest;
import com.codeforge.backend.testcase.dto.TestCaseBatchRequest;
import com.codeforge.backend.testcase.dto.TestCaseResponse;
import com.codeforge.backend.testcase.service.TestCaseService;
import com.codeforge.backend.testcase.service.TestCaseSyncService;
import com.codeforge.backend.testcase.service.TestCaseImportService;
import com.codeforge.backend.testcase.dto.TestCaseSummary;
import com.codeforge.backend.testcase.dto.TestCaseSelectionRequest;
import com.codeforge.backend.testcase.dto.TestCaseBatchUpdateRequest;
import com.codeforge.backend.testcase.dto.TestCaseImportRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static com.codeforge.backend.common.error.ErrorCode.TEST_CASE_SYNC_FAILED;

@RestController
@RequestMapping("/api/problems/{problemId}/test-cases")
@PreAuthorize("hasAnyRole('ADMIN', 'PROBLEM_MANAGER')")
public class TestCaseController {

    private final TestCaseService testCaseService;
    private final TestCaseSyncService syncService;
    private final TestCaseImportService importService;

    public TestCaseController(TestCaseService testCaseService,
            TestCaseSyncService syncService, TestCaseImportService importService) {
        this.testCaseService = testCaseService;
        this.syncService = syncService;
        this.importService = importService;
    }

    @PostMapping
    public ResponseEntity<TestCaseResponse> create(
            @PathVariable Long problemId,
            @Valid @RequestBody TestCaseRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(testCaseService.create(problemId, request));
    }

    @GetMapping
    public Page<TestCaseResponse> findAll(
            @PathVariable Long problemId,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size,
            @PageableDefault(size = 100, sort = { "orderIndex",
                    "id" }, direction = Sort.Direction.ASC) Pageable pageable) {
        PaginationValidator.validate(page, size, 500);

        return testCaseService.findAll(problemId, pageable);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<TestCaseResponse> createBatch(
            @PathVariable Long problemId,
            @Valid @RequestBody TestCaseBatchRequest request) {
        return testCaseService.createBatch(problemId, request.testCases());
    }

    @PutMapping("/{testCaseId}")
    public TestCaseResponse update(
            @PathVariable Long problemId,
            @PathVariable Long testCaseId,
            @Valid @RequestBody TestCaseRequest request) {

        return testCaseService.update(problemId, testCaseId, request);
    }

    @DeleteMapping("/{testCaseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long problemId,
            @PathVariable Long testCaseId) {

        testCaseService.delete(problemId, testCaseId);
    }

    @GetMapping("/status")
    public TestCaseService.Status status(@PathVariable Long problemId) { return testCaseService.status(problemId); }

    @GetMapping("/summaries")
    public Page<TestCaseSummary> summaries(@PathVariable Long problemId,
            @RequestParam(required = false) String page, @RequestParam(required = false) String size,
            @PageableDefault(size = 20, sort = {"orderIndex", "id"}) Pageable pageable) {
        PaginationValidator.validate(page, size, 100);
        return testCaseService.summaries(problemId, pageable);
    }

    @GetMapping("/{testCaseId}")
    public TestCaseResponse getOne(@PathVariable Long problemId, @PathVariable Long testCaseId) {
        return testCaseService.getOne(problemId, testCaseId);
    }

    @PatchMapping("/batch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateBatch(@PathVariable Long problemId, @Valid @RequestBody TestCaseBatchUpdateRequest request) {
        testCaseService.updateSampleBatch(problemId, request.ids(), request.isSample());
    }

    @PostMapping("/batch-delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBatch(@PathVariable Long problemId, @Valid @RequestBody TestCaseSelectionRequest request) {
        testCaseService.deleteBatch(problemId, request.ids());
    }

    @PostMapping(value = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TestCaseImportService.Preview preview(@PathVariable Long problemId, @RequestPart("files") List<MultipartFile> files) {
        return importService.preview(problemId, files);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TestCaseImportService.Imported importFiles(@PathVariable Long problemId,
            @RequestPart("files") List<MultipartFile> files, @Valid @RequestPart("manifest") TestCaseImportRequest manifest) {
        return importService.importFiles(problemId, files, manifest);
    }

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sync(@PathVariable Long problemId) {
        try {
            syncService.syncProblem(problemId);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    TEST_CASE_SYNC_FAILED,
                    exception);
        }
    }
}
