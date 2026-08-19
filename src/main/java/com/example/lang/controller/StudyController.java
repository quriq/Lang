package com.example.lang.controller;

import com.example.lang.entity.Card;
import com.example.lang.entity.Deck;
import com.example.lang.entity.User;
import com.example.lang.repository.CardRepository;
import com.example.lang.repository.DeckRepository;
import com.example.lang.repository.UserRepository;
import com.example.lang.service.FsrsService;
import com.example.lang.service.StudyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
    @Autowired
    private FsrsService fsrsService;

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
    public String startStudy(
            @PathVariable Long deckId,
            @RequestParam(defaultValue = "classic") String mode,
            Model model,
            HttpSession session) {

        User currentUser = getCurrentUser();

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Колода не найдена"));

        if (!deck.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Нельзя учить чужую колоду");
        }

        List<Card> cards;

        if ("spaced".equals(mode)) {
            cards = studyService.getSpacedRepetitionCards(deckId);
        } else {
            cards = studyService.getStudyCards(deckId);
        }

        if (cards.isEmpty()) {
            if ("spaced".equals(mode)) {
                model.addAttribute("errorMessage", "Нет карточек для повторения. Возвращайтесь позже!");
            } else {
                model.addAttribute("errorMessage", "В этой колоде нет карточек для изучения.");
            }
            return "redirect:/decks/" + deckId;
        }

        List<Long> cardIds = new ArrayList<>();
        for (Card card : cards) {
            cardIds.add(card.getId());
        }

        if ("random".equals(mode)) {
            Collections.shuffle(cardIds);
        }

        session.setAttribute(SESSION_QUEUE + deckId, cardIds);
        session.setAttribute(SESSION_INDEX + deckId, 0);
        session.setAttribute(SESSION_CORRECT + deckId, 0);
        session.setAttribute(SESSION_WRONG + deckId, 0);
        session.setAttribute("studyWrongCards_" + deckId, new ArrayList<>());
        session.setAttribute("studyMode_" + deckId, mode);

        return showCurrentCard(deckId, model, session);
    }

    @PostMapping("/answer")
    public String processAnswer(
            @PathVariable Long deckId,
            @RequestParam Long cardId,
            @RequestParam(required = false) Boolean isCorrect,
            @RequestParam(required = false) String typedAnswer,
            HttpServletRequest request,
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

        String mode = (String) session.getAttribute("studyMode_" + deckId);
        boolean correct = false;

        if ("spaced".equals(mode)) {
            int rating = 3;
            String fsrsRatingStr = request.getParameter("fsrsRating");

            if (fsrsRatingStr != null) {
                try {
                    rating = Integer.parseInt(fsrsRatingStr);
                    rating = Math.max(1, Math.min(4, rating));
                } catch (NumberFormatException e) {
                    rating = 3;
                }
            }

            correct = (rating > 1);

            studyService.processSpacedRepetitionAnswer(card, rating);
        }

        else if ("typing".equals(mode)) {
            String correctAnswer = "reverse".equals(mode) ? card.getBackText() : card.getFrontText();
            correct = typedAnswer != null && typedAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
            studyService.processAnswer(card, correct);
        }

        else {
            correct = Boolean.TRUE.equals(isCorrect);
            studyService.processAnswer(card, correct);
        }

        int correctCount = (int) session.getAttribute(SESSION_CORRECT + deckId);
        int wrongCount = (int) session.getAttribute(SESSION_WRONG + deckId);

        if (correct) {
            session.setAttribute(SESSION_CORRECT + deckId, correctCount + 1);
        } else {
            session.setAttribute(SESSION_WRONG + deckId, wrongCount + 1);

            @SuppressWarnings("unchecked")
            List<Long> wrongCards = (List<Long>) session.getAttribute("studyWrongCards_" + deckId);
            if (wrongCards == null) {
                wrongCards = new ArrayList<>();
            }
            wrongCards.add(cardId);
            session.setAttribute("studyWrongCards_" + deckId, wrongCards);
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

        @SuppressWarnings("unchecked")
        List<Long> wrongCardIds = (List<Long>) session.getAttribute("studyWrongCards_" + deckId);
        List<Card> wrongCards = new ArrayList<>();

        if (wrongCardIds != null && !wrongCardIds.isEmpty()) {
            for (Long cardId : wrongCardIds) {
                cardRepository.findById(cardId).ifPresent(wrongCards::add);
            }
        }

        model.addAttribute("correct", correct);
        model.addAttribute("wrong", wrong);
        model.addAttribute("total", total);
        model.addAttribute("accuracy", String.format("%.1f", accuracy));
        model.addAttribute("deckId", deckId);
        model.addAttribute("wrongCards", wrongCards);

        session.removeAttribute(SESSION_QUEUE + deckId);
        session.removeAttribute(SESSION_INDEX + deckId);
        session.removeAttribute(SESSION_CORRECT + deckId);
        session.removeAttribute(SESSION_WRONG + deckId);
        session.removeAttribute("studyWrongCards_" + deckId);
        session.removeAttribute("studyMode_" + deckId);

        return "study/results";
    }

    private String showCurrentCard(Long deckId, Model model, HttpSession session) {

        @SuppressWarnings("unchecked")
        List<Long> queue = (List<Long>) session.getAttribute(SESSION_QUEUE + deckId);
        int currentIndex = (int) session.getAttribute(SESSION_INDEX + deckId);

        if (currentIndex >= queue.size()) {
            System.out.println("ERROR: currentIndex >= queue.size()!");
            return "redirect:/decks/" + deckId + "/study/results";
        }

        Long cardId = queue.get(currentIndex);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена"));

        String mode = (String) session.getAttribute("studyMode_" + deckId);
        if (mode == null) mode = "classic";

        String question;
        String answer;

        if ("reverse".equals(mode)) {
            question = card.getBackText();
            answer = card.getFrontText();
        } else {
            question = card.getFrontText();
            answer = card.getBackText();
        }

        model.addAttribute("card", card);
        model.addAttribute("deckId", deckId);
        model.addAttribute("currentIndex", currentIndex + 1);
        model.addAttribute("totalCards", queue.size());
        model.addAttribute("mode", mode);
        model.addAttribute("question", question);
        model.addAttribute("answer", answer);
        model.addAttribute("example", card.getExampleSentence());

        if ("spaced".equals(mode)) {
            LocalDateTime now = LocalDateTime.now();
            int elapsedDays = card.getFsrsLastReview() != null
                    ? (int) Duration.between(card.getFsrsLastReview(), now).toDays()
                    : 0;

            model.addAttribute("fsrsAgainLabel", "10m");

            FsrsService.FsrsResult hardResult = fsrsService.processAnswer(
                    card.getFsrsDifficulty(), card.getFsrsStability(), elapsedDays, 2);
            FsrsService.FsrsResult goodResult = fsrsService.processAnswer(
                    card.getFsrsDifficulty(), card.getFsrsStability(), elapsedDays, 3);
            FsrsService.FsrsResult easyResult = fsrsService.processAnswer(
                    card.getFsrsDifficulty(), card.getFsrsStability(), elapsedDays, 4);

            model.addAttribute("fsrsHardLabel", formatInterval(hardResult.interval()));
            model.addAttribute("fsrsGoodLabel", formatInterval(goodResult.interval()));
            model.addAttribute("fsrsEasyLabel", formatInterval(easyResult.interval()));
        }

        return "study/study";
    }
    private String formatInterval(int days) {
        if (days <= 0) return "10m";
        if (days == 1) return "1d";
        if (days < 30) return days + "d";
        if (days < 365) return (days / 30) + "mo";
        return (days / 365) + "y";
    }

}