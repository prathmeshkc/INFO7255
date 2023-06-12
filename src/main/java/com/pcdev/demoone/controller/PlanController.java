package com.pcdev.demoone.controller;

import com.pcdev.demoone.service.PlanService;
import com.pcdev.demoone.util.JsonValidator;
import lombok.RequiredArgsConstructor;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("v1/plan")
@RequiredArgsConstructor
public class PlanController {

    private final JsonValidator jsonValidator;
    private final PlanService planService;

    @PostMapping(value = "/", produces = "application/json")
    public ResponseEntity<Object> createPlan(@RequestBody String planString) throws URISyntaxException {
        if (planString == null || planString.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONObject().put("error", "empty request body").toString());
        }

        JSONObject planJson = new JSONObject(planString);
        try {
            jsonValidator.validateJson(planJson);
        } catch (ValidationException | IOException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONObject().put("error", exception.getMessage()).toString());
        }

        String planKey = planJson.get("objectType").toString() + "_" + planJson.get("objectId").toString();
        if (planService.checkIfKeyExists(planKey)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new JSONObject().put("error", "plan already exists!").toString());
        }

        String eTag = planService.savePlan(planKey, planJson.toString());
        return ResponseEntity.created(new URI("/plan/" + planJson.get("objectId").toString())).eTag(eTag).body(new JSONObject().put("message", "plan created successfully!")
                .put("planId", planJson.get("objectId").toString()).toString());

    }

    @GetMapping(value = "/{planId}", produces = "application/json")
    public ResponseEntity<Object> getPlan(
            @RequestHeader HttpHeaders headers,
            @PathVariable String planId
    ) {
        String key = "plan_" + planId;
        if (!planService.checkIfKeyExists(key)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JSONObject().put("error", "plan not found!").toString());
        }

        String oldETag = planService.getETag(key);
        String receivedETag = headers.getFirst("If-None-Match");
        if (receivedETag != null && receivedETag.equals(oldETag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(oldETag)
                    .body(new JSONObject().put("message", "plan not modified!").toString());
        }

        String plan = planService.getPlan(key).toString();
        return ResponseEntity.ok().eTag(oldETag).body(plan);
    }

    @DeleteMapping(value = "/{planId}", produces = "application/json")
    public ResponseEntity<Object> deletePlan(@PathVariable String planId) {

        String keyToDelete = "plan_" + planId;
        if (!planService.checkIfKeyExists(keyToDelete)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JSONObject().put("error", "plan not found!").toString());
        }

        if (planService.deletePlan(keyToDelete)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.internalServerError().body(new JSONObject().put("error", "internal server error!").toString());
    }


}
