import com.eventmaster.model.User;
import com.eventmaster.repository.UserRepository;
import com.eventmaster.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;


public class UserServiceTest {
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
        String username = "testuser";
        User user = new User(username, "password123", "testUser@example.com", "testUser", "Austin, TX");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User found = userService.findByUsername(username);

        assertNotNull(found);
        assertEquals(username, found.getUsername());

    }

    @Test
    public void testUserNotFoundByUsername(){
        try{
            userService.findByUsername("username");
        } catch (RuntimeException e){
            assert(e.getMessage().equals("User not found"));
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
        String email = "test";

        Optional<User> user = userService.findByEmail(email);

        assertFalse(user.isPresent());
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

//    @Test
//    public void testUserFindByEmail(){
//        String email = "test@test";
//        User user = new User("ryanjo", "test", email, "ryan", "Austin,Tx");
//
//        userService.saveUser(user);
//
//        Optional<User> user_ret = userService.findByEmail(email);
//
//        assert(userService.findByEmail(email).get().equals(user));
//
//
//
//    }

}
