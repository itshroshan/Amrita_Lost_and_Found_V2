package com.amrita.lostandfound.controller;

import com.amrita.lostandfound.model.AuthResponse;
import com.amrita.lostandfound.model.MessageResponse;
import com.amrita.lostandfound.model.User;
import com.amrita.lostandfound.repository.UserRepository;
import com.amrita.lostandfound.security.JwtTokenProvider;
import com.amrita.lostandfound.service.EmailService;
import com.amrita.lostandfound.service.OtpRateLimiterService;
import io.github.bucket4j.Bucket;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRateLimiterService rateLimiterService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // In-memory store for OTPs during registration/reset (In production, use Redis)
    private final Map<String, Map<String, String>> otpStorage = new HashMap<>();

    private boolean validStudentEmail(String email) {
        return email != null && email.startsWith("bl.") && email.endsWith("@bl.students.amrita.edu");
    }

    private boolean validFacultyEmail(String email) {
        return email != null && email.endsWith("@blr.amrita.edu");
    }

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");

        io.github.bucket4j.Bucket bucket = rateLimiterService.resolveBucket(email);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new MessageResponse("Too many login attempts. Try again later.", false));
        }

        String password = loginRequest.get("password");

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty() || !BCrypt.checkpw(password, userOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid credentials", false));
        }

        User user = userOpt.get();

        if (!"admin".equals(user.getRole()) && user.getIsVerified() == 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Please verify your email before logging in.", false));
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole());
        
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .sameSite("Lax")
                .path("/")
                .maxAge(24 * 60 * 60) // 1 day
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(token, user.getName(), user.getEmail(), user.getRole()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0) // Instantly deletes the cookie
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("Logged out successfully.", true));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> regRequest) {
        String name = regRequest.get("name");
        String email = regRequest.get("email");
        String password = regRequest.get("password");

        if (!validStudentEmail(email) && !validFacultyEmail(email)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Please use your official Amrita email ID.", false));
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email is already registered.", false));
        }

        Bucket bucket = rateLimiterService.resolveBucket(email);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new MessageResponse("Too many requests. Try again later.", false));
        }

        String otp = generateOtp();
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Map<String, String> regData = new HashMap<>();
        regData.put("name", name);
        regData.put("email", email);
        regData.put("password", hashedPassword);
        regData.put("otp", otp);
        
        otpStorage.put(email, regData);

        emailService.sendOtp(email, otp);
        return ResponseEntity.ok(new MessageResponse("OTP sent to email.", true));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyRegistrationOtp(@RequestBody Map<String, String> verifyRequest) {
        String email = verifyRequest.get("email");
        String otp = verifyRequest.get("otp");

        Map<String, String> regData = otpStorage.get(email);
        if (regData == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Session expired or invalid.", false));
        }

        if (otp.equals(regData.get("otp"))) {
            User user = new User();
            user.setName(regData.get("name"));
            user.setEmail(email);
            user.setPassword(regData.get("password"));
            user.setRole(email.endsWith("@bl.students.amrita.edu") ? "student" : "faculty");
            user.setIsVerified(1);

            userRepository.save(user);
            otpStorage.remove(email);
            
            return ResponseEntity.ok(new MessageResponse("Registration successful.", true));
        }

        return ResponseEntity.badRequest().body(new MessageResponse("Invalid OTP.", false));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email is not registered.", false));
        }

        Bucket bucket = rateLimiterService.resolveBucket(email);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new MessageResponse("Too many requests. Try again later.", false));
        }

        String otp = generateOtp();
        
        Map<String, String> resetData = new HashMap<>();
        resetData.put("otp", otp);
        otpStorage.put(email, resetData);

        emailService.sendOtp(email, otp);
        return ResponseEntity.ok(new MessageResponse("OTP sent to your email.", true));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        Map<String, String> sessionData = otpStorage.get(email);
        if (sessionData == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Session expired or invalid.", false));
        }

        if (otp.equals(sessionData.get("otp"))) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
                userRepository.save(user);
                
                otpStorage.remove(email);
                return ResponseEntity.ok(new MessageResponse("Password reset successfully.", true));
            }
        }

        return ResponseEntity.badRequest().body(new MessageResponse("Invalid OTP.", false));
    }
}