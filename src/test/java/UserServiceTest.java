import com.eventmaster.exception.DuplicateUserException;
import com.eventmaster.model.User;
import com.eventmaster.repository.UserRepository;
import com.eventmaster.service.UserService;
import com.eventmaster.exception.UserNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;


public class UserServiceTest {
    private static final String username = "testUser";
    private static final String testemail = "testemail";
    @Mock
    private UserRepository userRepository;

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

}
