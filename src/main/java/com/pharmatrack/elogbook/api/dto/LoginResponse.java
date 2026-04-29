package com.pharmatrack.elogbook.api.dto;

public record LoginResponse(UserDto user, String token) {}
