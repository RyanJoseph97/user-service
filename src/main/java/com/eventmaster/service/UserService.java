package com.eventmaster.service;

import com.eventmaster.exception.UserNotFoundException;
import com.eventmaster.model.User;
import com.eventmaster.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User findByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.byUsername(username));
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.byEmail(email));
    }

    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
