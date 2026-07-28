package com.utubehub.mcp.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpResource {
    String uri() default "";
    String name() default "";
    String description() default "";
    String mimeType() default "application/json";
}
