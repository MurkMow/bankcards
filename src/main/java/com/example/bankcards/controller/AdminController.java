package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/admin/card")
public class AdminController {

    private final AdminService cardService;

    public AdminController(AdminService cardService){
        this.cardService = cardService;

    }

    @PostMapping("/create")
    public void createCard (@RequestParam Long userId, YearMonth expiryDate){
        cardService.createCard(userId,expiryDate);
    }

    @PostMapping("/block")
    public void blockCard (@RequestParam Long cardId){
        cardService.blockCard(cardId);
    }

    @PostMapping("/activate")
    public void activateCard (@RequestParam Long cardId){
        cardService.activateCard(cardId);
    }

    @DeleteMapping("/delete")
    public void deleteCard (@RequestParam Long cardId){
        cardService.deleteCard(cardId);
    }

    @GetMapping("/cards")
    public List<CardDTO> getAllCards(){
        return cardService.getAllCards();
    }
}
