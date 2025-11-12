package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public UserService(CardRepository cardRepository, UserRepository userRepository){
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    public Page<CardDTO> getUserCards(String username, CardStatus status, Pageable pageable) {
        com.example.bankcards.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + username));

        Page<Card> cardsPage = cardRepository.findByClientAndStatus(user.getId(), status, pageable);

        return cardsPage.map(card -> {
            CardDTO dto = new CardDTO();
            dto.setId(card.getId());
            dto.setClientId(card.getClient().getId());
            dto.setExpiryDate(card.getExpiryDate());
            dto.setCardStatus(card.getCardStatus());
            dto.setBalance(card.getBalance());
            String last4 = card.getLast4() != null ? card.getLast4() : "0000";
            dto.setLast4("**** **** **** " + last4);
            return dto;
        });
    }

    @Transactional
    public void transferBetweenCard(Long fromCardId, Long toCardId, BigDecimal amount, String username){

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }

        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));

        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));

        if (!fromCard.getClient().getUsername().equals(username) || !toCard.getClient().getUsername().equals(username)) {
            throw new IllegalStateException("Нельзя переводить между чужими картами");
        }

        if (fromCard.getCardStatus() != CardStatus.ACTIVE || toCard.getCardStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Перевод возможен только между активными картами");
        }

        if (fromCard.getBalance() == null) fromCard.setBalance(BigDecimal.ZERO);
        if (toCard.getBalance() == null) toCard.setBalance(BigDecimal.ZERO);

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Недостаточно средств для перевода");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

    }

    @Transactional(readOnly = true)
    public BigDecimal getCardBalance(Long cardId, String username){
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));

        if (!card.getClient().getUsername().equals(username)) {
            throw new IllegalStateException("Нельзя смотреть баланс чужой карты");
        }

        return card.getBalance() != null ? card.getBalance() : BigDecimal.ZERO;
    }

    @Transactional
    public void requestCardBlock(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));

        if (!card.getClient().getUsername().equals(username)) {
            throw new IllegalStateException("Нельзя блокировать чужую карту");
        }

        if (card.getCardStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Можно блокировать только активную карту");
        }

        card.setCardStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

    }
}
