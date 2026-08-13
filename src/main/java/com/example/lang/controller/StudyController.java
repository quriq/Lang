package com.example.lang.controller;

import com.example.lang.entity.Card;
import com.example.lang.entity.Deck;
import com.example.lang.entity.User;
import com.example.lang.repository.CardRepository;
import com.example.lang.repository.DeckRepository;
import com.example.lang.repository.UserRepository;
import com.example.lang.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/decks/{deckId}/study")
public class StudyController {

    @Autowired
    private StudyService studyService;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    private static final String SESSION_QUEUE = "studyQueue_";
    private static final String SESSION_INDEX = "studyIndex_";
    private static final String SESSION_CORRECT = "studyCorrect_";
    private static final String SESSION_WRONG = "studyWrong_";

    @GetMapping
    public String startStudy(@PathVariable Long deckId, Model model, HttpSession session) {
        User currentUser = getCurrentUser();

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Колода не найдена"));

        if (!deck.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Нельзя учить чужую колоду");
        }

        List<Card> cards = studyService.getStudyCards(deckId);

        if (cards.isEmpty()) {
            model.addAttribute("errorMessage", "В этой колоде нет карточек для изучения.");
            return "redirect:/decks/" + deckId;
        }

        List<Long> cardIds = new ArrayList<>();
        for (Card card : cards) {
            cardIds.add(card.getId());
        }

        session.setAttribute(SESSION_QUEUE + deckId, cardIds);
        session.setAttribute(SESSION_INDEX + deckId, 0);
        session.setAttribute(SESSION_CORRECT + deckId, 0);
        session.setAttribute(SESSION_WRONG + deckId, 0);

        return showCurrentCard(deckId, model, session);
    }

    @PostMapping("/answer")
    public String processAnswer(
            @PathVariable Long deckId,
            @RequestParam Long cardId,
            @RequestParam boolean isCorrect,
            Model model,
            HttpSession session) {

        User currentUser = getCurrentUser();

        @SuppressWarnings("unchecked")
        List<Long> queue = (List<Long>) session.getAttribute(SESSION_QUEUE + deckId);

        if (queue == null || !queue.contains(cardId)) {
            throw new RuntimeException("Недопустимая карточка");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена"));

        if (!card.getDeck().getId().equals(deckId)) {
            throw new RuntimeException("Карточка не принадлежит этой колоде");
        }

        studyService.processAnswer(card, isCorrect);

        int correct = (int) session.getAttribute(SESSION_CORRECT + deckId);
        int wrong = (int) session.getAttribute(SESSION_WRONG + deckId);

        if (isCorrect) {
            session.setAttribute(SESSION_CORRECT + deckId, correct + 1);
        } else {
            session.setAttribute(SESSION_WRONG + deckId, wrong + 1);
        }

        int currentIndex = (int) session.getAttribute(SESSION_INDEX + deckId);
        session.setAttribute(SESSION_INDEX + deckId, currentIndex + 1);

        if (currentIndex + 1 >= queue.size()) {
            return "redirect:/decks/" + deckId + "/study/results";
        }

        return showCurrentCard(deckId, model, session);
    }

    @GetMapping("/results")
    public String showResults(@PathVariable Long deckId, Model model, HttpSession session) {
        int correct = (int) session.getAttribute(SESSION_CORRECT + deckId);
        int wrong = (int) session.getAttribute(SESSION_WRONG + deckId);
        int total = correct + wrong;

        double accuracy = total > 0 ? (double) correct / total * 100 : 0;

        model.addAttribute("correct", correct);
        model.addAttribute("wrong", wrong);
        model.addAttribute("total", total);
        model.addAttribute("accuracy", String.format("%.1f", accuracy));
        model.addAttribute("deckId", deckId);

        session.removeAttribute(SESSION_QUEUE + deckId);
        session.removeAttribute(SESSION_INDEX + deckId);
        session.removeAttribute(SESSION_CORRECT + deckId);
        session.removeAttribute(SESSION_WRONG + deckId);

        return "study/results";
    }

    private String showCurrentCard(Long deckId, Model model, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Long> queue = (List<Long>) session.getAttribute(SESSION_QUEUE + deckId);
        int currentIndex = (int) session.getAttribute(SESSION_INDEX + deckId);

        Long cardId = queue.get(currentIndex);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена"));

        model.addAttribute("card", card);
        model.addAttribute("deckId", deckId);
        model.addAttribute("currentIndex", currentIndex + 1);
        model.addAttribute("totalCards", queue.size());

        return "study/study";
    }
}