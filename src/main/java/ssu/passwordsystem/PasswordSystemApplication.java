package ssu.passwordsystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ssu.passwordsystem.hashfunctions.NHash;
import ssu.passwordsystem.objects.User;
import ssu.passwordsystem.repo.UserRepository;

import java.io.IOException;
import java.util.Optional;

@Controller
@SpringBootApplication
@RequestMapping("/practice")
public class PasswordSystemApplication {

    private static final String TECHNICAL_ERROR = "There was an error while processing your request. Please try again later";
    private static final String WRONG_LOGIN_DATA = "Wrong login or password. Please, try again.";
    private static final String LOGIN_TAKEN_ERROR = "This login already exists. Please, try another one.";
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
	String home() {
		return "HomePage";
	}

	@GetMapping("/auth")
    String authGet() {
	    return "AuthPage";
    }

	@PostMapping(value = "/auth")
	String authPost(@RequestParam("login") String login, @RequestParam("password") String password, Model model) {
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
            successMessage.append("User ");
            if (name == null || name.isEmpty())
                name = login;
            successMessage.append(name)
                    .append(" is logged in.");
            model.addAttribute("successMessage", successMessage.toString());
            return "SuccessPage";
        } catch (IOException e) {
            model.addAttribute("errorMessage", TECHNICAL_ERROR);
            return "AuthPage";
        }
    }

    @GetMapping("/register")
    String registerGet() {
	    return "RegistrationPage";
    }

    @PostMapping("/register")
    String registerPost(
            @RequestParam("login") String login,
            @RequestParam("name") String name,
            @RequestParam("password") String password,
            Model model
    ) {
	    if (userRepository.existsById(login)) {
	        model.addAttribute("errorMessage", LOGIN_TAKEN_ERROR);
	        return "RegistrationPage";
        }
        try {
            userRepository.save(new User(login, name, NHash.hash(password)));

            StringBuilder successMessage = new StringBuilder();
	        successMessage.append("User ");
	        if (name == null || name.isEmpty())
	            name = login;
	         successMessage.append(name)
                       .append(" is registered.");
            model.addAttribute("successMessage", successMessage.toString());
            return "SuccessPage";
        } catch (IOException e) {
            model.addAttribute("errorMessage", TECHNICAL_ERROR);
            return "RegistrationPage";
        }
    }

	public static void main(String[] args) {
		SpringApplication.run(PasswordSystemApplication.class, args);
	}

}
