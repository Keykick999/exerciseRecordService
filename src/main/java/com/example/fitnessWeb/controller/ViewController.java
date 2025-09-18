package com.example.fitnessWeb.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }

    @GetMapping("exercise-records")
    public String exerciseRecords() {
        return "exercise-records";
    }

    @GetMapping("/exercise-record-detail/{exerciseRecordId}")
    public String exerciseRecordDetail(Model model, @PathVariable Long exerciseRecordId) {
        model.addAttribute("exerciseRecordId", exerciseRecordId);
        return "exercise-record-detail";
    }

    @GetMapping("/routineChat")
    public String routineChat() {
        return "routine-chat";
    }

    @GetMapping("/speech")
    public String speech() {
        return "speech";
    }

    @GetMapping("/workoutVideos")
    public String workoutVideos() {
        return "workout-videos";
    }
}