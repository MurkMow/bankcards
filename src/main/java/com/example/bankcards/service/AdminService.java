package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumber;
import com.example.bankcards.util.EncryptionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Service
public class AdminService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    public AdminService(CardRepository cardRepository, UserRepository userRepository, EncryptionService encryptionService){
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    @Transactional
    public void createCard (Long userId, YearMonth expiryDate){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + userId));

        Card card = new Card();
        card.setClient(user);
        card.setExpiryDate(expiryDate);
        card.setCardStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        String plainNumber = CardNumber.generate16DigitNumber();
        String encrypted = encryptionService.encrypt(plainNumber);
        card.setEncryptedNumber(encrypted);
        card.setLast4(plainNumber.substring(plainNumber.length() - 4));

        cardRepository.save(card);

    }

    @Transactional
    public void blockCard (Long cardId){

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена: " + cardId));

        card.setCardStatus(CardStatus.BLOCKED);

    }

    @Transactional
    public void activateCard (Long cardId){

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена: " + cardId));

        if (card.getCardStatus() == CardStatus.EXPIRED){
            throw new IllegalStateException("Карта просрочена");
        }

        card.setCardStatus(CardStatus.ACTIVE);

    }

    @Transactional
    public void deleteCard (Long cardId){
        
        if (!cardRepository.existsById(cardId)) {
            throw new EntityNotFoundException("Карта не найдена: " + cardId);
        }

        cardRepository.deleteById(cardId);

    }

    public List<CardDTO> getAllCards(){

        return cardRepository.findAll().stream()
                .map(this::toDto)
                .toList();

    }

    private CardDTO toDto(Card c) {

        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(c.getId());
        cardDTO.setClientId(c.getClient() != null ? c.getClient().getId() : null);
        cardDTO.setExpiryDate(c.getExpiryDate());
        cardDTO.setCardStatus(c.getCardStatus());
        cardDTO.setBalance(c.getBalance());

        String last4 = c.getLast4() != null ? c.getLast4() : "0000";
        cardDTO.setLast4("**** **** **** " + last4);

        return cardDTO;

    }



}
