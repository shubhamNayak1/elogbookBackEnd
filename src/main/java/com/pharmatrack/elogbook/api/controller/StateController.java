package com.pharmatrack.elogbook.api.controller;

import com.pharmatrack.elogbook.api.dto.AppStateDto;
import com.pharmatrack.elogbook.security.CurrentUser;
import com.pharmatrack.elogbook.service.StateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/state")
public class StateController {

    private final StateService stateService;
    private final CurrentUser currentUser;

    public StateController(StateService stateService, CurrentUser currentUser) {
        this.stateService = stateService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public AppStateDto state() {
        return stateService.bootstrap(currentUser.require());
    }
}
