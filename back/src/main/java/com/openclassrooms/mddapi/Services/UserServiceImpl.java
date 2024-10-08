package com.openclassrooms.mddapi.Services;

import com.openclassrooms.mddapi.Dtos.TopicDTO.TopicDto;
import com.openclassrooms.mddapi.Dtos.UserDto.UserDto;
import com.openclassrooms.mddapi.Dtos.UserDto.UserUpdateDto;
import com.openclassrooms.mddapi.Models.User;
import com.openclassrooms.mddapi.Repositorys.TopicRepository;
import com.openclassrooms.mddapi.Repositorys.UserRepository;
import com.openclassrooms.mddapi.Services.Interfaces.AuthenticationService;
import com.openclassrooms.mddapi.Services.Interfaces.JwtService;
import com.openclassrooms.mddapi.Services.Interfaces.UserService;
import com.openclassrooms.mddapi.exeptions.ForbiddenExeption;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service pour les utilisateurs.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TopicRepository topicRepository;

    /**
     * Constructeur.
     *
     * @param userRepository         Le repository des utilisateurs.
     * @param passwordEncoder       L'encodeur de mot de passe.
     * @param jwtService            Le service JWT.
     * @param topicRepository       Le repository des topics.
     */
    public UserServiceImpl(AuthenticationService authenticationService, UserRepository userRepository, TopicRepository topicRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.topicRepository = topicRepository;
    }

/**
     * Méthode pour récupérer tous les utilisateurs.
     * Retourne une liste de toutes les entités User.
     */
    @Override
    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);    }

    /**
     * Méthode pour récupérer un utilisateur actuel
     * @param user
     * @return
     */
    @Override
    public UserDto getCurrentUser(User user) {
        List<TopicDto> topics = topicRepository.findByUsers_Id(user.getId())
                .stream()
                .map(topic -> new TopicDto(topic.getId(), topic.getTitle(), topic.getDescription(), topic.getCreated_at(), topic.getUpdated_at()))
                .collect(Collectors.toList());

        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt(), user.getUpdatedAt(), topics);
    }

    /**
     * Méthode pour récupérer un utilisateur par son ID.
     * @param id
     * @return
     */
    @Override
    public UserDto getUserById(Integer id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return getCurrentUser(user);
        } else {
            throw new NoSuchElementException("User not found with id: " + id);
        }
    }

    /**
     * Méthode pour mettre à jour un utilisateur.
     * @param updateDto
     * @return
     */
    @Override
    public UserDto updateUser( UserUpdateDto updateDto) {
        User authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userOptional = userRepository.findByEmail(updateDto.getEmail());
        if (updateDto.getName() != null) {
            authenticatedUser.setName(updateDto.getName());
        }
        if (updateDto.getEmail() != null) {
            authenticatedUser.setEmail(updateDto.getEmail());
        }
        if (updateDto.getPassword() != null) {
            authenticatedUser.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }
        userRepository.save(authenticatedUser);
        // Mettre à jour le contexte de sécurité avec le nouvel utilisateur
        Authentication newAuth = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        // Générer un nouveau jeton JWT
        String newJwtToken = jwtService.generateToken(authenticatedUser);

        UserDto userDto = getCurrentUser(authenticatedUser);
        userDto.setJwtToken(newJwtToken);

        return userDto;

    }


}
