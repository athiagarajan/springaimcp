package com.springaimcp.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a photograph or visual media associated with a temple")
public record TempleImage(
    @Schema(description = "Direct URL of the image")
    String url,

    @Schema(description = "Title or caption of the image")
    String title,

    @Schema(description = "Description or historical context")
    String description,

    @Schema(description = "Source platform or archive (e.g., Wikipedia, Wikimedia Commons)")
    String source
) {}
