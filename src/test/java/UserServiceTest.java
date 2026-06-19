import com.eventmaster.exception.DuplicateUserException;
import com.eventmaster.model.ChangePasswordRequest;
import com.eventmaster.model.UpdateUserRequest;
import com.eventmaster.model.User;
import com.eventmaster.repository.FollowRepository;
import com.eventmaster.repository.FollowRequestRepository;
import com.eventmaster.repository.UserRepository;
import com.eventmaster.service.UserService;
import com.eventmaster.service.PasswordService;
import com.eventmaster.exception.UserNotFoundException;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;


public class UserServiceTest {
    private static final String username = "testUser";
    private static final String testemail = "testemail";
    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private FollowRequestRepository followRequestRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindByUsername(){
        User user = new User(username, "password123", "testUser@example.com", username, "Austin, TX");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User found = userService.findByUsername(username);

        assertNotNull(found);
        assertEquals(username, found.getUsername());
    }

    @Test
    public void testUserNotFoundByUsername(){
        try{
            userService.findByUsername(username);
        } catch (UserNotFoundException e){
            assert(e.getMessage().equals("User not found with username: " + username));
        }
    }

    @Test
    public void testUserNotFoundById(){
        long id = 0;

        Optional<User> user = userService.findById(id);

        assertFalse(user.isPresent());
    }

    @Test
    public void testFindByIdSuccess(){
        long id = 1;
        User user = new User(username, "password123", "testUser@example.com", username, "Austin, TX");
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        Optional<User> found = userService.findById(id);

        assertTrue(found.isPresent());
        assertEquals(username, found.get().getUsername());
    }

    @Test
    public void testUserNotFoundByEmail(){
        try {
            User user = userService.findByEmail(testemail);
        } catch(UserNotFoundException e){
            assert(e.getMessage().equals("User not found with email: " + testemail));
        }
    }

    @Test
    public void testSaveUser(){
        User user = new User("testuser", "password", "email@example.com", "Test Name", "Location");
        when(userRepository.save(user)).thenReturn(user);

        User savedUser = userService.saveUser(user);

        assertNotNull(savedUser);
        assertEquals(user.getUsername(), savedUser.getUsername());
        assertEquals(user.getEmail(), savedUser.getEmail());
    }

    @Test
    public void testSaveUserDuplicateUsername(){
        User user = new User("testuser", "password", "email@example.com", "Test Name", "Location");

        // Simulate H2 throwing a DataIntegrityViolationException with the username in the message
        when(userRepository.save(user))
                .thenThrow(new DataIntegrityViolationException(
                        "constraint [PUBLIC.CONSTRAINT_INDEX_4 ON PUBLIC.USERS(USERNAME NULLS FIRST)" +
                        " VALUES ( /* 4 */ 'testuser' )"));

        DuplicateUserException ex = assertThrows(DuplicateUserException.class,
                () -> userService.saveUser(user));

        assertTrue(ex.isUsernameDuplicate());
        assertFalse(ex.isEmailDuplicate());
        assertEquals("username 'testuser' is already in use", ex.getMessage());
    }

    @Test
    public void testSaveUserDuplicateEmail(){
        User user = new User("testuser", "password", "email@example.com", "Test Name", "Location");

        // Simulate H2 throwing a DataIntegrityViolationException with the email in the message
        when(userRepository.save(user))
                .thenThrow(new DataIntegrityViolationException(
                        "constraint [PUBLIC.CONSTRAINT_INDEX_4D ON PUBLIC.USERS(EMAIL NULLS FIRST)" +
                        " VALUES ( /* 4 */ 'email@example.com' )"));

        DuplicateUserException ex = assertThrows(DuplicateUserException.class,
                () -> userService.saveUser(user));

        assertTrue(ex.isEmailDuplicate());
        assertFalse(ex.isUsernameDuplicate());
        assertEquals("email 'email@example.com' is already in use", ex.getMessage());
    }

    @Test
    public void testUserFindByEmail(){
        String email = "test@test";
        User user = new User("ryanjo", "test", email, "ryan", "Austin,Tx");

        // Mock the repository to return the user when findByEmail is called
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User user_ret = userService.findByEmail(email);

        assertNotNull(user_ret);
        assertEquals(email, user_ret.getEmail());
        assertEquals(user.getUsername(), user_ret.getUsername());
    }

    // Password hashing tests
    @Mock
    private PasswordService passwordService;

    @Test
    public void testSaveUserWithHashedPassword() {
        User user = new User("testuser", "plainpassword", "email@example.com", "Test Name", "Location");
        User savedUser = new User("testuser", "$2a$10$hashedpassword", "email@example.com", "Test Name", "Location");
        
        when(passwordService.isPasswordHashed("plainpassword")).thenReturn(false);
        when(passwordService.hashPassword("plainpassword")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.saveUserWithHashedPassword(user);

        assertNotNull(result);
        assertEquals("$2a$10$hashedpassword", result.getPassword());
        verify(passwordService).hashPassword("plainpassword");
    }

    @Test
    public void testSaveUserAlwaysHashesPassword() {
        // Even a BCrypt-formatted input must be re-hashed to enforce server-side policy
        String inputPassword = "$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC";
        String rehashed = "$2a$10$newHashResultXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
        User user = new User("testuser", inputPassword, "email@example.com", "Test Name", "Location");
        User savedUser = new User("testuser", rehashed, "email@example.com", "Test Name", "Location");

        when(passwordService.hashPassword(inputPassword)).thenReturn(rehashed);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.saveUserWithHashedPassword(user);

        assertNotNull(result);
        verify(passwordService).hashPassword(inputPassword);
    }

    @Test
    public void testSaveUserWithNullPassword() {
        User user = new User("testuser", null, "email@example.com", "Test Name", "Location");

        assertThrows(IllegalArgumentException.class, () -> userService.saveUserWithHashedPassword(user));
        verify(passwordService, never()).hashPassword(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // --- verifyUser ---

    @Test
    public void testVerifyUser_setsVerifiedTrue() {
        User user = new User(username, "hashedpw", "test@example.com", "Test", "Location");
        assertEquals(com.eventmaster.model.AccountStatus.UNVERIFIED, user.getAccountStatus());

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.verifyUser(username);

        assertEquals(com.eventmaster.model.AccountStatus.VERIFIED, result.getAccountStatus());
        verify(userRepository).save(user);
    }

    @Test
    public void testVerifyUser_userNotFound_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.verifyUser("ghost"));
    }

    // --- updateUser ---

    @Test
    public void testUpdateUser_updatesProvidedFields() {
        User user = new User(username, "hashedpw", "old@example.com", "Old Name", "Old City");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest();
        ReflectionTestUtils.setField(request, "email", "new@example.com");
        ReflectionTestUtils.setField(request, "name", "New Name");
        ReflectionTestUtils.setField(request, "location", null);

        User result = userService.updateUser(username, request);

        assertEquals("new@example.com", result.getEmail());
        assertEquals("New Name", result.getName());
        assertEquals("Old City", result.getLocation()); // unchanged
    }

    @Test
    public void testUpdateUser_userNotFound_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser("ghost", new UpdateUserRequest()));
    }

    // --- changePassword ---

    @Test
    public void testChangePassword_success() {
        User user = new User(username, "hashedOld", "test@example.com", "Test", "Location");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword("oldPlain", "hashedOld")).thenReturn(true);
        when(passwordService.hashPassword("newPlain12")).thenReturn("hashedNew");

        ChangePasswordRequest request = new ChangePasswordRequest();
        ReflectionTestUtils.setField(request, "currentPassword", "oldPlain");
        ReflectionTestUtils.setField(request, "newPassword", "newPlain12");

        userService.changePassword(username, request);

        assertEquals("hashedNew", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    public void testChangePassword_wrongCurrentPassword_throws() {
        User user = new User(username, "hashedOld", "test@example.com", "Test", "Location");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword("wrongPlain", "hashedOld")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest();
        ReflectionTestUtils.setField(request, "currentPassword", "wrongPlain");
        ReflectionTestUtils.setField(request, "newPassword", "newPlain12");

        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(username, request));

        verify(userRepository, never()).save(any());
    }

    // --- deleteUser ---

    @Test
    public void testDeleteUser_cascadesFollowsAndDeletesUser() {
        User user = new User(username, "hashedpw", "test@example.com", "Test", "Location");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        userService.deleteUser(username);

        verify(followRepository).deleteByFollower(user);
        verify(followRepository).deleteByFollowee(user);
        verify(followRequestRepository).deleteByRequesterUsername(username);
        verify(followRequestRepository).deleteByTargetUsername(username);
        verify(userRepository).delete(user);
    }

    @Test
    public void testDeleteUser_userNotFound_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser("ghost"));
    }

    // --- getAllUsers ---

    @Test
    public void testGetAllUsers_returnsAll() {
        List<User> users = List.of(
                new User("user1", "pw", "u1@example.com", "User One", ""),
                new User("user2", "pw", "u2@example.com", "User Two", "")
        );
        when(userRepository.findAll(Pageable.unpaged())).thenReturn(new PageImpl<>(users));

        Page<User> result = userService.getAllUsers(Pageable.unpaged());

        assertEquals(2, result.getTotalElements());
    }
}
