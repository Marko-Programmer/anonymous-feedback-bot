package com.marko.anonymous_feedback_bot.model;

import java.util.HashMap;
import java.util.Map;

public class UserFilters {

    public Map<String, Boolean> roleFilters;
    public Map<String, Boolean> branchFilters;
    public Map<String, Boolean> severityFilters;

    public UserFilters() {
        roleFilters = new HashMap<>();
        roleFilters.put("Механік", true);
        roleFilters.put("Менеджер", true);
        roleFilters.put("Електрик", true);

        branchFilters = new HashMap<>();
        branchFilters.put("Філія 1", true);
        branchFilters.put("Філія 2", true);
        branchFilters.put("Філія 3", true);
        branchFilters.put("Філія 4", true);
        branchFilters.put("Філія 5", true);

        severityFilters = new HashMap<>();
        severityFilters.put("1", true);
        severityFilters.put("2", true);
        severityFilters.put("3", true);
        severityFilters.put("4", true);
        severityFilters.put("5", true);
    }

}
