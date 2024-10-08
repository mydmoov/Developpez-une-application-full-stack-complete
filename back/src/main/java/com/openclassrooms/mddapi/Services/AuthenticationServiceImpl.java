package com.openclassrooms.mddapi.Services;

import com.openclassrooms.mddapi.Dtos.LoginUserDto;
import com.openclassrooms.mddapi.Dtos.RegisterUserDto;
import com.openclassrooms.mddapi.Models.User;
import com.openclassrooms.mddapi.Repositorys.UserRepository;
import com.openclassrooms.mddapi.Services.Interfaces.AuthenticationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * Service pour l'authentification des utilisateurs.
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationServiceImpl(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Inscrit un nouvel utilisateur.
     * @param input Les informations de l'utilisateur à inscrire.
     * @return
     */
    @Override
    public User signup(RegisterUserDto input) {
        try {
            var user = new User()
                    .setName(input.getName())
                    .setEmail(input.getEmail())
                    .setPassword(passwordEncoder.encode(input.getPassword()));

            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Name or Email already taken", e);
        }
    }

    /**
     * Authentifie un utilisateur.
     * @param input Les informations de l'utilisateur à authentifier.
     * @return
     */
    @Override
    public User authenticate(LoginUserDto input) {
        try {
            User user = userRepository.findByNameOrEmail(input.getNameOrEmail(), input.getNameOrEmail())
                    .orElseThrow(() ->
                            new BadCredentialsException("Invalid name, email or password")
                    );

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user,
                            input.getPassword()
                    )
            );

            return user;
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid name, email or password");
        }
    }

    /**
     * Récupère la liste de tous les utilisateurs.
     * @return
     */
    @Override
    public List<User> allUsers() {
        List<User> users = new ArrayList<>();

        userRepository.findAll().forEach(users::add);

        return users;
    }
}

