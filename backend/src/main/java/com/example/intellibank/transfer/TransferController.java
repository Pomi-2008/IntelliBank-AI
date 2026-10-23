package com.example.intellibank.transfer;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final TransferService service;
    public TransferController(TransferService service) { this.service = service; }
    @PostMapping public Map<String,Object> transfer(@Valid @RequestBody TransferRequest request) { return service.transfer(request); }
}
