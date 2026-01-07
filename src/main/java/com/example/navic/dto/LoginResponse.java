package com.example.navic.dto;

public record LoginResponse(String token, long expiresInSeconds) {}