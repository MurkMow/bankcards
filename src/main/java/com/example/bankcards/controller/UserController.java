package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@RequestMapping("/api/user/card")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("")
    public Page<CardDTO> getMyCards(Principal principal, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) CardStatus status){

        return userService.getUserCards(principal.getName(), status, PageRequest.of(page, size));

    }

    @PostMapping("/transfer")
    public void transferBetweenCards(Principal principal, @RequestParam Long fromCardId, @RequestParam Long toCardId, @RequestParam BigDecimal amount){

        userService.transferBetweenCard(fromCardId, toCardId, amount, principal.getName());

    }

    @GetMapping("/balance")
    public BigDecimal getCardBalance(Principal principal, @RequestParam Long cardId){

        return userService.getCardBalance(cardId, principal.getName());

    }

    @PostMapping("/block")
    public void blockCard(Principal principal, @RequestParam Long cardId){

        userService.requestCardBlock(cardId, principal.getName());

    }

}
