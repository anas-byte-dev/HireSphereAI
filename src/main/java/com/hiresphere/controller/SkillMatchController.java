package com.hiresphere.controller;

import com.hiresphere.dto.SkillMatchRequest;
import com.hiresphere.dto.SkillMatchResponse;
import com.hiresphere.service.SkillMatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SkillMatchController - REST API for rule-based skill matching.
 */
@Tag(name = "Skill Matching", description = "Automated skill-matching algorithms between candidate skills and job requirements")
@RestController
@RequestMapping("/api/skill-match")
public class SkillMatchController {

    private final SkillMatchService skillMatchService;

    public SkillMatchController(SkillMatchService skillMatchService) {
        this.skillMatchService = skillMatchService;
    }

    /**
     * Match a stored candidate's skills with a stored job's required skills.
     */
    @GetMapping
    public ResponseEntity<?> matchCandidateWithJob(@RequestParam int candidateId,
                                                   @RequestParam int jobId) {
        StringBuilder errorMsg = new StringBuilder();
        SkillMatchResponse response = skillMatchService.matchCandidateWithJob(candidateId, jobId, errorMsg);

        if (response == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", true);
            error.put("message", errorMsg.toString());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Path variable alternative for candidate and job match.
     */
    @GetMapping("/candidate/{candidateId}/job/{jobId}")
    public ResponseEntity<?> matchCandidateWithJobPath(@PathVariable int candidateId,
                                                       @PathVariable int jobId) {
        return matchCandidateWithJob(candidateId, jobId);
    }

    /**
     * Compare arbitrary skill lists directly via JSON payload.
     * Useful for client-side forms and quick testing.
     */
    @PostMapping("/compare")
    public ResponseEntity<SkillMatchResponse> compareSkills(@RequestBody SkillMatchRequest request) {
        if (request == null) {
            request = new SkillMatchRequest();
        }
        SkillMatchResponse response = skillMatchService.matchSkills(
                request.getCandidateSkills(),
                request.getJobSkills()
        );
        return ResponseEntity.ok(response);
    }
}
