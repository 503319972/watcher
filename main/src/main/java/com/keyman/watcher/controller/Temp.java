package com.keyman.watcher.controller;

import com.keyman.watcher.configuration.FileMapConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("temp")
public class Temp {

    @Autowired
    FileMapConfiguration fileMapConfiguration;

    @GetMapping
    @RequestMapping("")
    public String temp(){
        return "success";
    }
}
