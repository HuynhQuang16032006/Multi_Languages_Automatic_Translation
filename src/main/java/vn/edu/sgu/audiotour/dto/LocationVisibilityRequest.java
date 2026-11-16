package vn.edu.sgu.audiotour.dto;

import jakarta.validation.constraints.NotNull;

public record LocationVisibilityRequest(@NotNull Boolean visible) { }
