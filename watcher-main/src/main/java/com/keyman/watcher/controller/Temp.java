package com.keyman.watcher.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("temp")
public class Temp {

    @GetMapping
    @RequestMapping("")
    public String temp(){
        return "success";
    }
}
