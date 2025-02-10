package com.foodsaver.server.services;

import com.foodsaver.server.dtos.UserDTO;
import com.foodsaver.server.exceptions.User.UserNotFoundException;
import com.foodsaver.server.model.User;
import com.foodsaver.server.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public User userDTOToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    public UserDTO userToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public UserDTO findUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new UserNotFoundException("User not found with username " + username);
        return userToUserDTO(user);
    }
}
