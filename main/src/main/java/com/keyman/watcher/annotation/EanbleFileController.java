package com.keyman.watcher.annotation;

import com.keyman.watcher.Main;
import org.springframework.context.annotation.ComponentScan;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ComponentScan(
        basePackageClasses = {Main.class})
public @interface EanbleFileController {
    boolean enabled() default true;
}
