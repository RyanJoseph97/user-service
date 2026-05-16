package com.eventmaster.service;

import com.eventmaster.exception.UserNotFoundException;
import com.eventmaster.model.Follow;
import com.eventmaster.model.FollowerSummary;
import com.eventmaster.model.User;
import com.eventmaster.repository.FollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private FollowService followService;

    private User alice;
    private User bob;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        alice = new User("alice", "hashedpw", "alice@example.com", "Alice", "Austin, TX");
        bob   = new User("bob",   "hashedpw", "bob@example.com",   "Bob",   "Dallas, TX");
    }

    // --- follow ---

    @Test
    public void follow_selfFollow_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> followService.follow("alice", "alice"));

        verifyNoInteractions(userService, followRepository);
    }

    @Test
    public void follow_followerNotFound_throwsUserNotFound() {
        when(userService.findByUsername("unknown"))
                .thenThrow(UserNotFoundException.byUsername("unknown"));

        assertThrows(UserNotFoundException.class,
                () -> followService.follow("unknown", "bob"));
    }

    @Test
    public void follow_followeeNotFound_throwsUserNotFound() {
        when(userService.findByUsername("alice")).thenReturn(alice);
        when(userService.findByUsername("unknown"))
                .thenThrow(UserNotFoundException.byUsername("unknown"));

        assertThrows(UserNotFoundException.class,
                () -> followService.follow("alice", "unknown"));
    }

    @Test
    public void follow_alreadyFollowing_throwsIllegalState() {
        when(userService.findByUsername("alice")).thenReturn(alice);
        when(userService.findByUsername("bob")).thenReturn(bob);
        when(followRepository.existsByFollowerAndFollowee(alice, bob)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> followService.follow("alice", "bob"));

        verify(followRepository, never()).save(any());
    }

    @Test
    public void follow_success_savesFollow() {
        when(userService.findByUsername("alice")).thenReturn(alice);
        when(userService.findByUsername("bob")).thenReturn(bob);
        when(followRepository.existsByFollowerAndFollowee(alice, bob)).thenReturn(false);

        followService.follow("alice", "bob");

        verify(followRepository).save(any(Follow.class));
    }

    // --- unfollow ---

    @Test
    public void unfollow_notFollowing_throwsIllegalState() {
        when(userService.findByUsername("alice")).thenReturn(alice);
        when(userService.findByUsername("bob")).thenReturn(bob);
        when(followRepository.existsByFollowerAndFollowee(alice, bob)).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> followService.unfollow("alice", "bob"));

        verify(followRepository, never()).deleteByFollowerAndFollowee(any(), any());
    }

    @Test
    public void unfollow_success_deletesFollow() {
        when(userService.findByUsername("alice")).thenReturn(alice);
        when(userService.findByUsername("bob")).thenReturn(bob);
        when(followRepository.existsByFollowerAndFollowee(alice, bob)).thenReturn(true);

        followService.unfollow("alice", "bob");

        verify(followRepository).deleteByFollowerAndFollowee(alice, bob);
    }

    // --- getFollowers ---

    @Test
    public void getFollowers_returnsFollowerSummaries() {
        alice.setDateJoined(LocalDate.of(2024, 1, 1));
        Follow follow = new Follow(alice, bob);

        when(userService.findByUsername("bob")).thenReturn(bob);
        when(followRepository.findByFollowee(bob, Pageable.unpaged())).thenReturn(new PageImpl<>(List.of(follow)));

        Page<FollowerSummary> result = followService.getFollowers("bob", Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("alice", result.getContent().get(0).getUsername());
        assertEquals("Alice", result.getContent().get(0).getName());
        assertEquals(LocalDate.of(2024, 1, 1), result.getContent().get(0).getDateJoined());
    }

    @Test
    public void getFollowers_noFollowers_returnsEmptyList() {
        when(userService.findByUsername("bob")).thenReturn(bob);
        when(followRepository.findByFollowee(bob, Pageable.unpaged())).thenReturn(Page.empty());

        Page<FollowerSummary> result = followService.getFollowers("bob", Pageable.unpaged());

        assertTrue(result.isEmpty());
    }

    // --- getFollowing ---

    @Test
    public void getFollowing_returnsFolloweeSummaries() {
        bob.setDateJoined(LocalDate.of(2024, 6, 15));
        Follow follow = new Follow(alice, bob);

        when(userService.findByUsername("alice")).thenReturn(alice);
        when(followRepository.findByFollower(alice, Pageable.unpaged())).thenReturn(new PageImpl<>(List.of(follow)));

        Page<FollowerSummary> result = followService.getFollowing("alice", Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("bob", result.getContent().get(0).getUsername());
        assertEquals("Bob", result.getContent().get(0).getName());
        assertEquals(LocalDate.of(2024, 6, 15), result.getContent().get(0).getDateJoined());
    }

    @Test
    public void getFollowing_notFollowingAnyone_returnsEmptyList() {
        when(userService.findByUsername("alice")).thenReturn(alice);
        when(followRepository.findByFollower(alice, Pageable.unpaged())).thenReturn(Page.empty());

        Page<FollowerSummary> result = followService.getFollowing("alice", Pageable.unpaged());

        assertTrue(result.isEmpty());
    }
}
