package com.example.lang.controller;

import com.example.lang.entity.Card;
import com.example.lang.entity.User;
import com.example.lang.repository.CardRepository;
import com.example.lang.repository.UserRepository;
import com.example.lang.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/study/all")
public class GlobalStudyController {

    @Autowired
    private StudyService studyService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    // Ключи сессии для глобальной тренировки (отличаются от обычных)
    private static final String SESSION_QUEUE = "globalStudyQueue";
    private static final String SESSION_INDEX = "globalStudyIndex";
    private static final String SESSION_CORRECT = "globalStudyCorrect";
    private static final String SESSION_WRONG = "globalStudyWrong";
    private static final String SESSION_WRONG_CARDS = "globalStudyWrongCards";
    private static final String SESSION_MODE = "globalStudyMode";

    @GetMapping
    public String startGlobalStudy(
            @RequestParam(defaultValue = "classic") String mode,
            Model model,
            HttpSession session) {

        User currentUser = getCurrentUser();
        List<Card> cards = studyService.getAllStudyCards(currentUser.getId());

        if (cards.isEmpty()) {
            model.addAttribute("errorMessage", "У вас нет карточек для изучения.");
            return "redirect:/";
        }

        List<Long> cardIds = new ArrayList<>();
        for (Card card : cards) {
            cardIds.add(card.getId());
        }

        // Перемешиваем для режима random
        if ("random".equals(mode)) {
            Collections.shuffle(cardIds);
        }

        session.setAttribute(SESSION_QUEUE, cardIds);
        session.setAttribute(SESSION_INDEX, 0);
        session.setAttribute(SESSION_CORRECT, 0);
        session.setAttribute(SESSION_WRONG, 0);
        session.setAttribute(SESSION_WRONG_CARDS, new ArrayList<>());
        session.setAttribute(SESSION_MODE, mode);

        return showCurrentCard(model, session);
    }

    @PostMapping("/answer")
    public String processAnswer(
            @RequestParam Long cardId,
            @RequestParam(required = false) String isCorrect,
            @RequestParam(required = false) String typedAnswer,
            Model model,
            HttpSession session) {

        @SuppressWarnings("unchecked")
        List<Long> queue = (List<Long>) session.getAttribute(SESSION_QUEUE);

        if (queue == null || !queue.contains(cardId)) {
            throw new RuntimeException("Недопустимая карточка");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена"));

        String mode = (String) session.getAttribute(SESSION_MODE);
        boolean correct;

        if ("typing".equals(mode)) {
            String correctAnswer = card.getBackText();
            if ("reverse".equals(mode)) {
                correctAnswer = card.getFrontText();
            }
            correct = typedAnswer != null &&
                    typedAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
        } else {
            correct = "true".equals(isCorrect);
        }

        studyService.processAnswer(card, correct);

        int correctCount = (int) session.getAttribute(SESSION_CORRECT);
        int wrongCount = (int) session.getAttribute(SESSION_WRONG);

        if (correct) {
            session.setAttribute(SESSION_CORRECT, correctCount + 1);
        } else {
            session.setAttribute(SESSION_WRONG, wrongCount + 1);

            @SuppressWarnings("unchecked")
            List<Long> wrongCards = (List<Long>) session.getAttribute(SESSION_WRONG_CARDS);
            if (wrongCards == null) {
                wrongCards = new ArrayList<>();
            }
            wrongCards.add(cardId);
            session.setAttribute(SESSION_WRONG_CARDS, wrongCards);
        }

        int currentIndex = (int) session.getAttribute(SESSION_INDEX);
        session.setAttribute(SESSION_INDEX, currentIndex + 1);

        if (currentIndex + 1 >= queue.size()) {
            return "redirect:/study/all/results";
        }

        return showCurrentCard(model, session);
    }

    @GetMapping("/results")
    public String showResults(Model model, HttpSession session) {
        Object correctObj = session.getAttribute(SESSION_CORRECT);
        Object wrongObj = session.getAttribute(SESSION_WRONG);
        int correct = correctObj != null ? (int) correctObj : 0;
        int wrong = wrongObj != null ? (int) wrongObj : 0;
        int total = correct + wrong;

        double accuracy = total > 0 ? (double) correct / total * 100 : 0;

        @SuppressWarnings("unchecked")
        List<Long> wrongCardIds = (List<Long>) session.getAttribute(SESSION_WRONG_CARDS);
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
        model.addAttribute("wrongCards", wrongCards);
        model.addAttribute("isGlobal", true);  // Флаг для шаблона

        // Очищаем сессию
        session.removeAttribute(SESSION_QUEUE);
        session.removeAttribute(SESSION_INDEX);
        session.removeAttribute(SESSION_CORRECT);
        session.removeAttribute(SESSION_WRONG);
        session.removeAttribute(SESSION_WRONG_CARDS);
        session.removeAttribute(SESSION_MODE);

        return "study/results";
    }

    private String showCurrentCard(Model model, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Long> queue = (List<Long>) session.getAttribute(SESSION_QUEUE);
        int currentIndex = (int) session.getAttribute(SESSION_INDEX);

        Long cardId = queue.get(currentIndex);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена"));

        String mode = (String) session.getAttribute(SESSION_MODE);
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
        model.addAttribute("currentIndex", currentIndex + 1);
        model.addAttribute("totalCards", queue.size());
        model.addAttribute("mode", mode);
        model.addAttribute("question", question);
        model.addAttribute("answer", answer);
        model.addAttribute("example", card.getExampleSentence());
        model.addAttribute("isGlobal", true);  // Флаг для шаблона

        return "study/study";
    }
}