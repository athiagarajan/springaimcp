package com.utubehub.mcp.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpTool {
    String name() default "";
    String description() default "";
}
