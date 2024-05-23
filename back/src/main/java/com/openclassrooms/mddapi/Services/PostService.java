package com.openclassrooms.mddapi.Services;

// Classe de service responsable de la gestion des Post.
// Elle inclut des méthodes pour créer, mettre à jour, récupérer et enregistrer des Post.

import com.openclassrooms.mddapi.Dtos.PostDTO.PostDto;
import com.openclassrooms.mddapi.Dtos.PostDTO.PostDtoResponseMessage;
import com.openclassrooms.mddapi.Dtos.PostDTO.PostDtoGetAll;
import com.openclassrooms.mddapi.Models.Post;
import com.openclassrooms.mddapi.Models.Topic;
import com.openclassrooms.mddapi.Models.User;
import com.openclassrooms.mddapi.Repositorys.PostRepository;
import com.openclassrooms.mddapi.Repositorys.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TopicRepository topicRepository;

    /*
     * Méthode pour récupérer tous les thémes.
     * Retourne une liste de toutes les entités Post.
     */
    public List<Post> findAllPost() {
        return postRepository.findAll();
    }

    /*
     * Méthode pour récupérer un post par son ID.
     * Prend en entrée l'ID du théme.
     * Retourne un Optional contenant l'entité Post si elle est trouvée.
     */
    public Optional<Post> findById(Integer id) {
        return postRepository.findById(id);
    }

    /*
     * Méthode pour enregistrer un théme.
     * Prend en entrée une entité Post et la sauvegarde dans la base de données.
     * Retourne l'entité Post enregistrée.
     */
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    /*
     * Méthode pour créer un nouveau Post.
     * Prend en entrée un DTO de création de théme et les informations de l'utilisateur.
     * Retourne une réponse contenant le DTO de la réponse de création de Post.
     */
    public PostDtoResponseMessage createPost(PostDto postDto, Principal principal) {
        User currentUser = (User) ((Authentication) principal).getPrincipal();
        Optional<Topic> optionalTopic = topicRepository.findById(postDto.getTopic_id());
        if (!optionalTopic.isPresent()) {
            throw new RuntimeException("Topic not found");
        }
        Topic topic = optionalTopic.get();
        Post post = new Post(
                postDto.getTitle(),
                postDto.getDescription(),
                topic,
                currentUser
        );
        savePost(post);
        return new PostDtoResponseMessage("Post created !");
    }

    /*
     * Méthode pour récupérer toutes les Posts et les convertir en DTO.
     * Retourne une réponse contenant une liste de DTO PostDto.
     */

    public PostDtoGetAll getAllPosts(){
        List<Post> posts = findAllPost();
        List<PostDto> postDtos = posts.stream()
                .map(this::convertToPostDto)
                .collect(Collectors.toList());
        return (new PostDtoGetAll(postDtos));
    }

    /*
     * Méthode pour convertir une entité Post en DTO.
     * Prend en entrée une entité Post.
     * Retourne un DTO PostDto.
     */
    private PostDto convertToPostDto(Post post) {
        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getDescription(),
                post.getOwner_id().getId(),
                post.getTopic_id().getId(),
                post.getCreated_at(),
                post.getUpdated_at()
        );
    }

//    /*
//     * Méthode pour récupérer  par son ID.
//     * Prend en entrée l'ID du post.
//     * Retourne une réponse contenant le DTO de la location récupérée ou une réponse 404 si non trouvée.
//     */
//    public ResponseEntity<RentalDtoGet> getRentalById(Integer id) {
//        Optional<Rental> rentalOptional = findById(id); // Récupération de la location par ID
//        if (rentalOptional.isPresent()) {
//            Rental rental = rentalOptional.get();
//            RentalDtoGet rentalDto = convertToRentalDto(rental); // Conversion de l'entité Rental en DTO
//            return ResponseEntity.ok(rentalDto); // Retour de la réponse avec le DTO de la location
//        } else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Retour d'une réponse 404 si non trouvée
//        }
//    }
}

