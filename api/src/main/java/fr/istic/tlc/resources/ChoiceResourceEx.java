package fr.istic.tlc.resources;

import fr.istic.tlc.dao.ChoiceRepository;
import fr.istic.tlc.dao.PollRepository;
import fr.istic.tlc.dao.UserRepository;
import fr.istic.tlc.domain.Choice;
import fr.istic.tlc.domain.Poll;
import fr.istic.tlc.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChoiceResourceEx {

    @Autowired
    ChoiceRepository choiceRepository;
    @Autowired
    PollRepository pollRepository;
    @Autowired
    UserRepository userRepository;

    @GetMapping("/polls/{slug}/choices")
    public ResponseEntity<List<Choice>> retrieveAllChoicesFromPoll(@PathVariable final String slug) {
        // On vérifie que le choix existe
        final Poll poll = pollRepository.findBySlug(slug);
        if (poll == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        final List<Choice> choices = poll.getPollChoices();
        return new ResponseEntity<>(choices, HttpStatus.OK);
    }

    @GetMapping("/users/{idUser}/choices")
    public ResponseEntity<List<Choice>> retrieveAllChoicesFromUser(@PathVariable final long idUser) {
        // On vérifie que l'utilisateur existe
        final User user = userRepository.findById(idUser);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user.getUserChoices(), HttpStatus.OK);
    }

    @GetMapping("/polls/{slug}/choices/{idChoice}")
    public ResponseEntity<Choice> retrieveChoiceFromPoll(@PathVariable final String slug, @PathVariable final long idChoice) {
        // On vérifie que le choix et le poll existent
        final Poll poll = pollRepository.findBySlug(slug);
        final Choice choice = choiceRepository.findById(idChoice);
        if (poll == null || choice == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // On vérifie que le choix appartienne bien au poll
        if (!poll.getPollChoices().contains(choice)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(choice, HttpStatus.OK);
    }

    @GetMapping("/users/{idUser}/choices/{idChoice}")
    public ResponseEntity<Choice> retrieveChoiceFromUser(@PathVariable final long idUser, @PathVariable final long idChoice) {
        // On vérifie que le choix et l'utilisateur existent
        final User user = userRepository.findById(idUser);
        final Choice choice = choiceRepository.findById(idChoice);
        if (user == null || choice == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // On vérifie que le choix appartienne bien à l'utilisateur
        if (!user.getUserChoices().contains(choice)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(choice, HttpStatus.OK);
    }

    @DeleteMapping("/polls/{slug}/choices")
    public ResponseEntity<?> deleteChoiceFromPoll(@RequestBody final Map<String, List<Long>> choices, @PathVariable final String slug, @RequestParam final String token) {
        // On vérifie que le poll existe
        final List<Long> idchoices = choices.get("choices");
        final Poll poll = pollRepository.findBySlug(slug);
        if (poll == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // On vérifie que le token soit bon
        if (!poll.getSlugAdmin().equals(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // On enlève les choix du poll
        for (Long id : idchoices) {
            // On vérifie que le choice existe
            final Choice choice = choiceRepository.findById(id);
            if (choice != null) {
                // On remove le choice du poll
                poll.removeChoice(choice);
                pollRepository.getEntityManager().merge(poll);
                // On remove le choices des utilisateurs
                for (User user : userRepository.findAll().list()) {
                    if (user.getUserChoices().contains(choice)) {
                        user.getUserChoices().remove(choice);
                        userRepository.getEntityManager().merge(user);
                    }
                }
                // On supprime le choice
                choiceRepository.deleteById(id);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/polls/{slug}/choices")
    public ResponseEntity<List<Choice>> createChoices(@RequestBody final List<Choice> choices, @PathVariable final String slug, @RequestParam final String token) {
        // On vérifie que le poll existe
        final Poll poll = pollRepository.findBySlug(slug);
        if (poll == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // On vérifie que le token soit bon
        if (!poll.getSlugAdmin().equals(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // On ajoute chaque choix au poll et vice versa
        for (Choice choice : choices) {
            this.choiceRepository.persist(choice);
            poll.addChoice(choice);
            pollRepository.getEntityManager().merge(poll);
        }
        return new ResponseEntity<>(choices, HttpStatus.CREATED);
    }

    @PutMapping("/polls/{slug}/choices/{idChoice}")
    public ResponseEntity<Choice> updateChoice(@Valid @RequestBody final Choice choice1, @PathVariable final String slug, @PathVariable final long idChoice, @RequestParam final String token) {
        // On vérifie que le poll et le choix existent
        final Poll poll = pollRepository.findBySlug(slug);
        final Choice choice = choiceRepository.findById(idChoice);
        if (poll == null || choice == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // On vérifie que le choix appartienne bien au poll
        if (!poll.getPollChoices().contains(choice)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // On vérifie que le token soit bon
        if (!poll.getSlugAdmin().equals(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // On met à jour l'ancien choix
        final Choice ancientChoice = choice;
        if (choice1.getstartDate() != null) {
            ancientChoice.setstartDate(choice1.getstartDate());
        }
        if (choice1.getendDate() != null) {
            ancientChoice.setendDate(choice1.getendDate());
        }
        // On update la bdd
        final Choice updatedChoice = choiceRepository.getEntityManager().merge(ancientChoice);
        return new ResponseEntity<>(updatedChoice, HttpStatus.OK);
    }

    @PostMapping("/polls/{slug}/vote/{idUser}")
    public ResponseEntity<Object> vote(@RequestBody final Map<String, List<Long>> choices, @PathVariable final String slug, @PathVariable final long idUser) {
        // On vérifie que le poll et l'utilisateur existent
        final List<Long> idchoices = choices.get("choices");
        final Poll poll = pollRepository.findBySlug(slug);
        final User user = userRepository.findById(idUser);
        if (poll == null || user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        for (Long choice : idchoices) {
            // On vérifie que le choice existe
            final Choice optchoice = choiceRepository.findById(choice);
            if (optchoice == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            // On vérifie que le choix appartienne bien au poll
            if (!poll.getPollChoices().contains(optchoice)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            // On vérifie que le user n'ai pas déjà voté pour ce choix
            if (user.getUserChoices().contains(optchoice)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            // On ajoute le choix à la liste de l'utilisateur et vice versa
            optchoice.addUser(user);
            choiceRepository.getEntityManager().merge(optchoice);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/polls/{slug}/choices/{idChoice}/removevote/{idUser}")
    public ResponseEntity<Object> removeVote(@PathVariable final String slug, @PathVariable final long idChoice, @PathVariable final long idUser) {
        // On vérifie que le poll, le choix et l'utilisateur existent
        final Poll poll = pollRepository.findBySlug(slug);
        final Choice choice = choiceRepository.findById(idChoice);
        final User user = userRepository.findById(idUser);
        if (poll == null || choice == null || user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // On vérifie que le choix appartienne bien au poll
        if (!poll.getPollChoices().contains(choice)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // On vérifie que le user ait bien voté pour ce choix
        if (!user.getUserChoices().contains(choice)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // On retire le choix à la liste de l'utilisateur et vice versa
        choice.removeUser(user);
        choiceRepository.getEntityManager().merge(choice);
        user.removeChoice(choice);
        userRepository.getEntityManager().merge(user);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/polls/{slug}/choices/{idChoice}/count")
    public ResponseEntity<Object> numberOfVoteForChoice(@PathVariable final String slug, @PathVariable final long idChoice) {
        // On vérifie que le poll et choix existent
        final Poll poll = pollRepository.findBySlug(slug);
        final Choice choice = choiceRepository.findById(idChoice);
        if (poll == null || choice == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // On vérifie que le choix appartienne bien au poll
        if (!poll.getPollChoices().contains(choice)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // On compte le nombre de vote pour le choix
        return new ResponseEntity<>(choice.getUsers().size(), HttpStatus.OK);
    }
}
