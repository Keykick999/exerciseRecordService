package com.example.fitnessWeb.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
public class ViewController {

    @GetMapping("/home")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/routine")
    public String addRoutine() {
        return "routine";
    }

    @GetMapping("/routines")
    public String showRoutineList() {
        return "routines";
    }

    @GetMapping("chat")
    public String chat() {
        return "chat";
    }
}