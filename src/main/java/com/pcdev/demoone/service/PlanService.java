package com.pcdev.demoone.service;

import com.pcdev.demoone.dao.PlanDao;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanDao planDao;


    public boolean checkIfKeyExists(String key) {
        return planDao.checkIfExists(key);
    }

    public String getETag(String key) {
        return planDao.hGet(key, "eTag");
    }

    public String savePlan(String key, String planJsonString) {
        String newETag = DigestUtils.md5Hex(planJsonString);
        planDao.hSet(key, key, planJsonString);
        planDao.hSet(key, "eTag", newETag);
        return newETag;
    }

    public JSONObject getPlan(String key) {
        String planString = planDao.hGet(key, key);
        return new JSONObject(planString);
    }

    public boolean deletePlan(String key) {
        return planDao.del(key) == 1;
    }

}
