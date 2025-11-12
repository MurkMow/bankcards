package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;

import java.util.List;

public record UserDTO(
        Long id,
        String username,
        String password,
        String email,
        Role role,
        List<Card> cards
)
{
}
