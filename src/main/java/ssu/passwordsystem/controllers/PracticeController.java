package ssu.passwordsystem.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ssu.passwordsystem.hashfunctions.NHash;
import ssu.passwordsystem.objects.User;
import ssu.passwordsystem.repo.UserRepository;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/practice")
public class PracticeController {

    private static final String TECHNICAL_ERROR = "Произошла техническая ошибка при обработке вашего запроса. Повторите запрос позже.";
    private static final String WRONG_LOGIN_DATA = "Неверный логин или пароль. Повторите попытку.";
    private static final String LOGIN_TAKEN_ERROR = "Данный логин уже занят. Придумайте новый логин и повторите попытку.";
    private static final String WRONG_PASSWORD = "Неверный пароль администратора. Повторите попытку.";
    private static final String ADMIN_PASSWORD = "ae38e5284fef91c8ef04d6f9b8795873";
    @Autowired
    private UserRepository userRepository;
    private Map<String, User> userSession = new HashMap<>();

    @GetMapping("/")
    String home(Model model, HttpSession session) {
        if (!userSession.containsKey(session.getId()))
            return "HomePage";
        User user = userSession.get(session.getId());
        model.addAttribute("user", user);
        return "HomeUserPage";
    }

    @GetMapping("/admin")
    String adminGet() {
        return "AdminAuthPage";
    }

    @PostMapping("/admin")
    String admin(@RequestParam("password") String password, Model model) throws IOException {
        if (NHash.hash(password).equals(ADMIN_PASSWORD)) {
            List<User> users = userRepository.findAll();
            model.addAttribute("users", users);
            return "AdminPage";
        }
        else {
            model.addAttribute("errorMessage", WRONG_PASSWORD);
            return "AdminAuthPage";
        }
    }

    @GetMapping("/admin/delete")
    String adminGetDelete() {
        return "redirect:/practice/admin";
    }

    @PostMapping("/admin/delete")
    String adminDelete(@RequestParam("login") String login, Model model) {
        if (userRepository.existsById(login))
            userRepository.deleteById(login);
        userSession.entrySet().removeIf(entry -> entry.getValue().getLogin().equals(login));
        model.addAttribute("users", userRepository.findAll());
        return "AdminPage";
    }

    @GetMapping("/auth")
    String authGet(HttpSession session) {
        if (userSession.containsKey(session.getId()))
            return "redirect:/practice/";
        return "AuthPage";
    }


    @PostMapping(value = "/auth")
    String authPost(@RequestParam("login") String login, @RequestParam("password") String password, Model model, HttpSession session) {
        if (userSession.containsKey(session.getId()))
            return "redirect:/practice/";
        Optional<User> userOpt = userRepository.findById(login);
        if (!userOpt.isPresent()) {
            model.addAttribute("errorMessage", WRONG_LOGIN_DATA);
            return "AuthPage";
        }
        User user = userOpt.get();
        try {
            if (!user.getPassword().equals(NHash.hash(password))) {
                model.addAttribute("errorMessage", WRONG_LOGIN_DATA);
                return "AuthPage";
            }
            String name = user.getName();
            StringBuilder successMessage = new StringBuilder();
            successMessage.append("Пользователь ")
                    .append(name == null || name.isEmpty() ? login : name)
                    .append(" вошел в систему.");

            userSession.put(session.getId(), user);
            model.addAttribute("successMessage", successMessage.toString());
            return "SuccessPage";
        } catch (IOException e) {
            model.addAttribute("errorMessage", TECHNICAL_ERROR);
            return "AuthPage";
        }
    }

    @GetMapping("/register")
    String registerGet(HttpSession session) {
        if (userSession.containsKey(session.getId()))
            return "redirect:/practice/";
        return "RegistrationPage";
    }

    @PostMapping("/register")
    String registerPost(
            @RequestParam("login") String login,
            @RequestParam("name") String name,
            @RequestParam("password") String password,
            Model model,
            HttpSession session
    ) {
        if (userSession.containsKey(session.getId()))
            return "redirect:/practice/";
        if (userRepository.existsById(login)) {
            model.addAttribute("errorMessage", LOGIN_TAKEN_ERROR);
            return "RegistrationPage";
        }
        try {
            userRepository.save(new User(login, name, NHash.hash(password)));

            StringBuilder successMessage = new StringBuilder();
            successMessage.append("Пользователь ")
                    .append(login)
                    .append(" зарегистрирован.");
            model.addAttribute("successMessage", successMessage.toString());
            return "SuccessPage";
        } catch (IOException e) {
            model.addAttribute("errorMessage", TECHNICAL_ERROR);
            return "RegistrationPage";
        }
    }

    @PostMapping("/out")
    String signOutPost(HttpSession session) {
        userSession.remove(session.getId());
        return "redirect:/practice/";
    }
}
