package com.amrita.lostandfound.controller;

import com.amrita.lostandfound.model.User;
import com.amrita.lostandfound.repository.UserRepository;
import com.amrita.lostandfound.service.EmailService;
import com.amrita.lostandfound.service.OtpRateLimiterService;
import jakarta.servlet.http.HttpSession;
import io.github.bucket4j.Bucket;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRateLimiterService rateLimiterService;

    @Autowired
    private EmailService emailService;

    private boolean validStudentEmail(String email) {
        return email != null && email.startsWith("bl.") && email.endsWith("@bl.students.amrita.edu");
    }

    private boolean validFacultyEmail(String email){
        return email != null && email.endsWith("@blr.amrita.edu");
    }

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    // --- HOME ROUTE ---
    @GetMapping("/")
    public String home() {
        return "index"; // index.html
    }

    // --- LOGIN ROUTES ---
    @GetMapping("/login")
    public String login() {
        return "login"; // login.html
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty() || !BCrypt.checkpw(password, userOpt.get().getPassword())) {
            model.addAttribute("title", "Login Error");
            model.addAttribute("message", "Invalid password. Please try again.");
            model.addAttribute("redirect_url", "/login");
            model.addAttribute("show_forgot", true);
            return "error";
        }

        User user = userOpt.get();

        if (!"admin".equals(user.getRole()) && user.getIsVerified() == 0) {
            model.addAttribute("title", "Email Not Verified");
            model.addAttribute("message", "Please verify your email before logging in.");
            model.addAttribute("redirect_url", "/register");
            return "error";
        }

        session.setAttribute("name", user.getName());
        session.setAttribute("email", user.getEmail());
        session.setAttribute("role", user.getRole());

        return "admin".equals(user.getRole()) ? "redirect:/admin" : "redirect:/student";
    }

    // --- REGISTER ROUTES ---
    @GetMapping("/register")
    public String register() {
        return "register"; // register.html
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        if (!validStudentEmail(email) && !validFacultyEmail(email)) {
            model.addAttribute("title", "Invalid Email");
            model.addAttribute("message", "Please use your official Amrita student email ID.");
            model.addAttribute("redirect_url", "/register");
            return "error";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("title", "Already Registered");
            model.addAttribute("message", "This email is already registered. Try logging in.");
            model.addAttribute("redirect_url", "/login");
            return "error";
        }

        // --- BUCKET4J RATE LIMITER CHECK ---
        Bucket bucket = rateLimiterService.resolveBucket(email);
        if (!bucket.tryConsume(1)) {
            model.addAttribute("title", "Rate Limit Exceeded");
            model.addAttribute("message", "Too many OTP requests. Please wait 15 minutes before trying again.");
            model.addAttribute("redirect_url", "/register");
            return "error";
        }

        String otp = generateOtp();
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Map<String, String> regData = new HashMap<>();
        regData.put("name", name);
        regData.put("email", email);
        regData.put("password", hashedPassword);
        regData.put("otp", otp);
        session.setAttribute("reg_data", regData);

        emailService.sendOtp(email, otp);
        return "redirect:/verify-otp";
    }

    // --- VERIFY REGISTRATION OTP ---
    @GetMapping("/verify-otp")
    public String verifyRegistrationOtpPage(HttpSession session) {
        if (session.getAttribute("reg_data") == null) return "redirect:/register";
        return "verify_otp"; // verify_otp.html
    }

    @PostMapping("/verify-otp")
    public String verifyRegistrationOtp(@RequestParam String otp, HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Map<String, String> regData = (Map<String, String>) session.getAttribute("reg_data");
        if (regData == null) return "redirect:/register";

        if (otp.equals(regData.get("otp"))) {
            String email = regData.get("email");
            Optional<User> existingUserOpt = userRepository.findByEmail(email);

            User user = existingUserOpt.orElseGet(User::new);
            user.setName(regData.get("name"));
            user.setEmail(email);
            user.setPassword(regData.get("password"));
            if(email.endsWith("@bl.students.amrita.edu")){
                user.setRole("student");
            }
            else {
                user.setRole("faculty");
            }
            user.setIsVerified(1);

            userRepository.save(user);
            session.removeAttribute("reg_data");
            return "redirect:/login";
        } else {
            model.addAttribute("title", "Verification Failed");
            model.addAttribute("message", "Invalid OTP. Please try again.");
            model.addAttribute("redirect_url", "/register");
            return "error_otp";
        }
    }

    // --- CHANGE PASSWORD ROUTES ---
    @GetMapping("/change-password")
    public String changePasswordPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/login";

        model.addAttribute("email", email);
        return "change_password"; // change_password.html
    }

    @PostMapping("/change-password")
    public String changePasswordPost(@RequestParam String current_password,
                                     @RequestParam String new_password,
                                     @RequestParam String confirm_password,
                                     HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/login";
        model.addAttribute("email", email);

        if (!new_password.equals(confirm_password)) {
            model.addAttribute("error", "New passwords do not match");
            return "change_password";
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !BCrypt.checkpw(current_password, user.getPassword())) {
            model.addAttribute("error", "Current password is incorrect");
            return "change_password";
        }

        // --- BUCKET4J RATE LIMITER CHECK ---
        Bucket bucket = rateLimiterService.resolveBucket(email);
        if (!bucket.tryConsume(1)) {
            model.addAttribute("error", "Too many OTP requests. Please wait 15 minutes before trying again.");
            return "change_password";
        }

        String otp = generateOtp();
        Map<String, String> pwdChange = new HashMap<>();
        pwdChange.put("email", email);
        pwdChange.put("new_password", new_password);
        pwdChange.put("otp", otp);
        session.setAttribute("pwd_change", pwdChange);

        emailService.sendOtp(email, otp);
        return "redirect:/verify-change-password";
    }

    @GetMapping("/verify-change-password")
    public String verifyChangePasswordPage(HttpSession session) {
        if (session.getAttribute("pwd_change") == null) return "redirect:/change-password";
        return "verify_change_password"; // verify_change_password.html
    }

    @PostMapping("/verify-change-password")
    public String verifyChangePasswordPost(@RequestParam String otp, HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) session.getAttribute("pwd_change");
        if (data == null) return "redirect:/change-password";

        if (!otp.equals(data.get("otp"))) {
            model.addAttribute("error", "Invalid OTP");
            return "verify_change_password";
        }

        User user = userRepository.findByEmail(data.get("email")).orElse(null);
        if (user != null) {
            user.setPassword(BCrypt.hashpw(data.get("new_password"), BCrypt.gensalt()));
            userRepository.save(user);
        }

        session.removeAttribute("pwd_change");
        model.addAttribute("message", "Password changed successfully");
        return "success";
    }

    // --- FORGOT PASSWORD ROUTES ---
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot_password"; // forgot_password.html
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordPost(@RequestParam String email, HttpSession session, Model model) {
        if (userRepository.findByEmail(email).isEmpty()) {
            model.addAttribute("error", "Email not registered");
            return "forgot_password";
        }
        // --- BUCKET4J RATE LIMITER CHECK ---
        Bucket bucket = rateLimiterService.resolveBucket(email);
        if (!bucket.tryConsume(1)) {
            model.addAttribute("error", "Too many OTP requests. Please wait 15 minutes before trying again.");
            return "forgot_password";
        }

        String otp = generateOtp();
        Map<String, String> forgotPwd = new HashMap<>();
        forgotPwd.put("email", email);
        forgotPwd.put("otp", otp);
        session.setAttribute("forgot_pwd", forgotPwd);

        emailService.sendOtp(email, otp);
        return "redirect:/verify-forgot-otp";
    }

    @GetMapping("/verify-forgot-otp")
    public String verifyForgotOtpPage(HttpSession session) {
        if (session.getAttribute("forgot_pwd") == null) return "redirect:/forgot-password";
        return "verify_forgot_otp"; // verify_forgot_otp.html
    }

    @PostMapping("/verify-forgot-otp")
    public String verifyForgotOtpPost(@RequestParam String otp, HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) session.getAttribute("forgot_pwd");
        if (data == null) return "redirect:/forgot-password";

        if (!otp.equals(data.get("otp"))) {
            model.addAttribute("error", "Invalid OTP");
            return "verify_forgot_otp";
        }

        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(HttpSession session) {
        if (session.getAttribute("forgot_pwd") == null) return "redirect:/forgot-password";
        return "reset_password"; // reset_password.html
    }

    @PostMapping("/reset-password")
    public String resetPasswordPost(@RequestParam String new_password,
                                    @RequestParam String confirm_password,
                                    HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) session.getAttribute("forgot_pwd");
        if (data == null) return "redirect:/forgot-password";

        if (!new_password.equals(confirm_password)) {
            model.addAttribute("error", "Passwords do not match");
            return "reset_password";
        }

        User user = userRepository.findByEmail(data.get("email")).orElse(null);
        if (user != null) {
            user.setPassword(BCrypt.hashpw(new_password, BCrypt.gensalt()));
            userRepository.save(user);
        }

        session.removeAttribute("forgot_pwd");
        model.addAttribute("message", "Password reset successfully. Please login again.");
        return "success";
    }

    // --- LOGOUT ROUTE ---
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}