package com.semali.sosbackend.service;

import com.semali.sosbackend.dto.TrustedContactRequest;
import com.semali.sosbackend.dto.TrustedContactResponse;
import com.semali.sosbackend.entity.TrustedContact;
import com.semali.sosbackend.entity.User;
import com.semali.sosbackend.exception.ResourceNotFoundException;
import com.semali.sosbackend.repository.TrustedContactRepository;
import com.semali.sosbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrustedContactServiceTest {

    @Mock private TrustedContactRepository trustedContactRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private TrustedContactService trustedContactService;

    // ---------- CREATE ----------

    @Test
    void create_shouldSucceed_whenUserExists() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        TrustedContactRequest request = new TrustedContactRequest();
        request.setName("Kamala Perera");
        request.setContactNo("0771112233");
        request.setEmail("kamala@example.com");
        request.setRelation("Mother");
        request.setPriorityOrder(1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(trustedContactRepository.save(any(TrustedContact.class)))
                .thenAnswer(invocation -> {
                    TrustedContact saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    return saved;
                });

        TrustedContactResponse response = trustedContactService.create(userId, request);

        assertEquals("Kamala Perera", response.getName());
        assertEquals(1, response.getPriorityOrder());
        verify(trustedContactRepository).save(any(TrustedContact.class));
    }

    @Test
    void create_shouldThrow_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        TrustedContactRequest request = new TrustedContactRequest();
        request.setName("Kamala Perera");
        request.setContactNo("0771112233");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> trustedContactService.create(userId, request));

        verify(trustedContactRepository, never()).save(any());
    }

    // ---------- GET ALL ----------

    @Test
    void getAllForUser_shouldReturnContacts_orderedByPriority() {
        UUID userId = UUID.randomUUID();

        TrustedContact contact1 = TrustedContact.builder()
                .id(UUID.randomUUID()).name("Kamala Perera").priorityOrder(1).build();
        TrustedContact contact2 = TrustedContact.builder()
                .id(UUID.randomUUID()).name("Nuwan Silva").priorityOrder(2).build();

        when(trustedContactRepository.findByUserIdOrderByPriorityOrder(userId))
                .thenReturn(List.of(contact1, contact2));

        List<TrustedContactResponse> results = trustedContactService.getAllForUser(userId);

        assertEquals(2, results.size());
        assertEquals("Kamala Perera", results.get(0).getName());
        assertEquals("Nuwan Silva", results.get(1).getName());
    }

    @Test
    void getAllForUser_shouldReturnEmptyList_whenNoContacts() {
        UUID userId = UUID.randomUUID();

        when(trustedContactRepository.findByUserIdOrderByPriorityOrder(userId))
                .thenReturn(List.of());

        List<TrustedContactResponse> results = trustedContactService.getAllForUser(userId);

        assertTrue(results.isEmpty());
    }

    // ---------- GET BY ID ----------

    @Test
    void getById_shouldReturnContact_whenOwnedByUser() {
        UUID userId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();

        TrustedContact contact = TrustedContact.builder()
                .id(contactId).name("Kamala Perera").build();

        when(trustedContactRepository.findByIdAndUserId(contactId, userId))
                .thenReturn(Optional.of(contact));

        TrustedContactResponse response = trustedContactService.getById(contactId, userId);

        assertEquals("Kamala Perera", response.getName());
    }

    @Test
    void getById_shouldThrow_whenNotOwnedByUser() {
        UUID userId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();

        // Simulates the ownership boundary: contact exists, but not for this user,
        // so the repository returns empty rather than exposing someone else's data.
        when(trustedContactRepository.findByIdAndUserId(contactId, userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> trustedContactService.getById(contactId, userId));
    }

    // ---------- UPDATE ----------

    @Test
    void update_shouldModifyContact_whenOwnedByUser() {
        UUID userId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();

        TrustedContact existing = TrustedContact.builder()
                .id(contactId).name("Old Name").contactNo("0770000000").build();

        TrustedContactRequest request = new TrustedContactRequest();
        request.setName("Updated Name");
        request.setContactNo("0771234567");
        request.setPriorityOrder(2);

        when(trustedContactRepository.findByIdAndUserId(contactId, userId))
                .thenReturn(Optional.of(existing));
        when(trustedContactRepository.save(any(TrustedContact.class)))
                .thenReturn(existing);

        TrustedContactResponse response = trustedContactService.update(contactId, userId, request);

        assertEquals("Updated Name", response.getName());
        assertEquals("0771234567", response.getContactNo());
        assertEquals(2, response.getPriorityOrder());
    }

    @Test
    void update_shouldThrow_whenNotOwnedByUser() {
        UUID userId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();
        TrustedContactRequest request = new TrustedContactRequest();
        request.setName("Doesn't matter");
        request.setContactNo("0770000000");

        when(trustedContactRepository.findByIdAndUserId(contactId, userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> trustedContactService.update(contactId, userId, request));

        verify(trustedContactRepository, never()).save(any());
    }

    // ---------- DELETE ----------

    @Test
    void delete_shouldRemoveContact_whenOwnedByUser() {
        UUID userId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();

        TrustedContact contact = TrustedContact.builder().id(contactId).build();

        when(trustedContactRepository.findByIdAndUserId(contactId, userId))
                .thenReturn(Optional.of(contact));

        trustedContactService.delete(contactId, userId);

        verify(trustedContactRepository).delete(contact);
    }

    @Test
    void delete_shouldThrow_whenNotOwnedByUser() {
        UUID userId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();

        when(trustedContactRepository.findByIdAndUserId(contactId, userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> trustedContactService.delete(contactId, userId));

        verify(trustedContactRepository, never()).delete(any());
    }
}