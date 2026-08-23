package com.semali.sosbackend.service;

import com.semali.sosbackend.dto.TrustedContactRequest;
import com.semali.sosbackend.dto.TrustedContactResponse;
import com.semali.sosbackend.entity.TrustedContact;
import com.semali.sosbackend.entity.User;
import com.semali.sosbackend.exception.ResourceNotFoundException;
import com.semali.sosbackend.repository.TrustedContactRepository;
import com.semali.sosbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrustedContactService {

    private final TrustedContactRepository trustedContactRepository;
    private final UserRepository userRepository;

    @Transactional
    public TrustedContactResponse create(UUID userId, TrustedContactRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TrustedContact contact = TrustedContact.builder()
                .user(user)
                .name(request.getName())
                .contactNo(request.getContactNo())
                .email(request.getEmail())
                .relation(request.getRelation())
                .priorityOrder(request.getPriorityOrder())
                .build();

        trustedContactRepository.save(contact);

        return mapToResponse(contact);
    }

    @Transactional
    public List<TrustedContactResponse> getAllForUser(UUID userId) {
        return trustedContactRepository.findByUserIdOrderByPriorityOrder(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public TrustedContactResponse getById(UUID id, UUID userId) {
        TrustedContact contact = trustedContactRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trusted contact not found"));

        return mapToResponse(contact);
    }

    @Transactional
    public TrustedContactResponse update(UUID id, UUID userId, TrustedContactRequest request) {
        TrustedContact contact = trustedContactRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trusted contact not found"));

        contact.setName(request.getName());
        contact.setContactNo(request.getContactNo());
        contact.setEmail(request.getEmail());
        contact.setRelation(request.getRelation());
        contact.setPriorityOrder(request.getPriorityOrder());

        trustedContactRepository.save(contact);

        return mapToResponse(contact);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        TrustedContact contact = trustedContactRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trusted contact not found"));

        trustedContactRepository.delete(contact);
    }

    private TrustedContactResponse mapToResponse(TrustedContact contact) {
        return TrustedContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .contactNo(contact.getContactNo())
                .email(contact.getEmail())
                .relation(contact.getRelation())
                .priorityOrder(contact.getPriorityOrder())
                .build();
    }
}