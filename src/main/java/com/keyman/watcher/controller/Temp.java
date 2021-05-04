package com.keyman.watcher.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * just for test
 */

@RestController
@RequestMapping("66d75efc-b959-4d99-bde9-d740ece38d34")
public class Temp {

    @GetMapping
    @RequestMapping("")
    public String temp(){
        return "success";
    }
}
