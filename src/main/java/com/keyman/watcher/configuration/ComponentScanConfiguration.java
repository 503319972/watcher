package com.keyman.watcher.configuration;

import com.keyman.watcher.Main;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
        basePackageClasses = {Main.class})
public class ComponentScanConfiguration {
}
