package com.example.intellibank.ai;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final AssistantService service;
    public AssistantController(AssistantService service) { this.service = service; }
    @PostMapping public Map<String,Object> ask(@Valid @RequestBody AssistantRequest request) { return service.answer(request); }
}
