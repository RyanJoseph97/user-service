package com.eventmaster.controller;

import com.eventmaster.exception.GlobalExceptionHandler;
import com.eventmaster.exception.UserNotFoundException;
import com.eventmaster.model.ChangePasswordRequest;
import com.eventmaster.model.UpdateUserRequest;
import com.eventmaster.model.User;
import com.eventmaster.service.PasswordService;
import com.eventmaster.service.UserService;
import com.eventmaster.jwt.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userController, "adminUsername", "admin");
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Sets the request principal so Spring MVC's PrincipalMethodArgumentResolver
     * can inject Authentication into controller method parameters in standalone MockMvc.
     */
    private RequestPostProcessor auth(String username) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        return (MockHttpServletRequest request) -> {
            request.setUserPrincipal(token);
            return request;
        };
    }

    // --- PATCH /{username}/verify ---

    @Test
    public void verifyUser_asAdmin_returns200() throws Exception {
        User user = new User("someuser", "pw", "s@example.com", "Some User", "");
        user.setAccountStatus(com.eventmaster.model.AccountStatus.VERIFIED);
        when(userService.verifyUser("someuser")).thenReturn(user);

        mockMvc.perform(patch("/users/someuser/verify").with(auth("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("someuser"))
                .andExpect(jsonPath("$.accountStatus").value("VERIFIED"));
    }

    @Test
    public void verifyUser_asNonAdmin_returns403() throws Exception {
        mockMvc.perform(patch("/users/someuser/verify").with(auth("regularuser")))
                .andExpect(status().isForbidden());

        verify(userService, never()).verifyUser(anyString());
    }

    // --- PATCH /{username} ---

    @Test
    public void updateUser_asOwner_returns200() throws Exception {
        User updated = new User("alice", "pw", "new@example.com", "Alice New", "NYC");
        when(userService.updateUser(eq("alice"), any(UpdateUserRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/users/alice")
                        .with(auth("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@example.com\",\"name\":\"Alice New\",\"location\":\"NYC\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    public void updateUser_asOtherUser_returns403() throws Exception {
        mockMvc.perform(patch("/users/alice")
                        .with(auth("bob"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hacked\"}"))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateUser(anyString(), any());
    }

    // --- PATCH /{username}/password ---

    @Test
    public void changePassword_asOwner_returns204() throws Exception {
        doNothing().when(userService).changePassword(eq("alice"), any(ChangePasswordRequest.class));

        mockMvc.perform(patch("/users/alice/password")
                        .with(auth("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldpass1\",\"newPassword\":\"newpass12\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void changePassword_asOtherUser_returns403() throws Exception {
        mockMvc.perform(patch("/users/alice/password")
                        .with(auth("bob"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldpass1\",\"newPassword\":\"newpass12\"}"))
                .andExpect(status().isForbidden());

        verify(userService, never()).changePassword(anyString(), any());
    }

    // --- DELETE /{username} ---

    @Test
    public void deleteUser_asOwner_returns204() throws Exception {
        doNothing().when(userService).deleteUser("alice");

        mockMvc.perform(delete("/users/alice").with(auth("alice")))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser("alice");
    }

    @Test
    public void deleteUser_asOtherUser_returns403() throws Exception {
        mockMvc.perform(delete("/users/alice").with(auth("bob")))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(anyString());
    }

    // --- GET /{id} ---

    @Test
    public void getUserById_found_returns200() throws Exception {
        User user = new User("alice", "pw", "alice@example.com", "Alice", "Austin");
        when(userService.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/1").with(auth("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    public void getUserById_notFound_returns404() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/99").with(auth("alice")))
                .andExpect(status().isNotFound());
    }

    // --- GET /by-username/{username} ---

    @Test
    public void getUserByUsername_found_returns200() throws Exception {
        User user = new User("alice", "pw", "alice@example.com", "Alice", "Austin");
        when(userService.findByUsername("alice")).thenReturn(user);

        mockMvc.perform(get("/users/by-username/alice").with(auth("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    public void getUserByUsername_notFound_returns404() throws Exception {
        when(userService.findByUsername("ghost"))
                .thenThrow(UserNotFoundException.byUsername("ghost"));

        mockMvc.perform(get("/users/by-username/ghost").with(auth("alice")))
                .andExpect(status().isNotFound());
    }
}
