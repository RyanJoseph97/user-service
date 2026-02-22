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
        try{
            userRepository.save(user);
        } catch (Exception e){
            System.out.println("Exception encountered");
            fail();
        }
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
