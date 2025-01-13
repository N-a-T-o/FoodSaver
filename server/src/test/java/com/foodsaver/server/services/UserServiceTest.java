package com.foodsaver.server.services;

import com.foodsaver.server.dtos.UserDTO;
import com.foodsaver.server.exceptions.User.UserNotFoundException;
import com.foodsaver.server.model.User;
import com.foodsaver.server.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void userDTOToUser_ShouldMapUserDTOToUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("johndoe");
        userDTO.setEmail("john@example.com");
        User user = new User();
        user.setUsername("johndoe");
        user.setEmail("john@example.com");

        when(modelMapper.map(userDTO, User.class)).thenReturn(user);

        User result = userService.userDTOToUser(userDTO);

        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void userToUserDTO_ShouldMapUserToUserDTO() {
        User user = new User();
        user.setUsername("johndoe");
        user.setEmail("john@example.com");
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("johndoe");
        userDTO.setEmail("john@example.com");

        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.userToUserDTO(user);

        assertEquals(userDTO.getUsername(), result.getUsername());
        assertEquals(userDTO.getEmail(), result.getEmail());
    }

    @Test
    void findUserByUsername_ShouldReturnUserDTO() {
        String username = "johndoe";
        User user = new User();
        user.setUsername(username);
        user.setEmail("john@example.com");
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        userDTO.setEmail("john@example.com");

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.findUserByUsername(username);

        assertEquals(userDTO.getUsername(), result.getUsername());
        assertEquals(userDTO.getEmail(), result.getEmail());
    }

    @Test
    void findUserByUsername_ShouldThrowUserNotFoundExceptionWhenUserNotFound() {
        String username = "nonexistent";

        when(userRepository.findByUsername(username)).thenReturn(null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.findUserByUsername(username));
        assertEquals("User not found with username " + username, exception.getMessage());
    }
}

