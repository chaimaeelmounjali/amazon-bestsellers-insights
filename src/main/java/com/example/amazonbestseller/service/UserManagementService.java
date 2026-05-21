package com.example.amazonbestseller.service;

import com.example.amazonbestseller.dto.UserDTO;
import com.example.amazonbestseller.entity.*;
import com.example.amazonbestseller.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UtilisateurRepository utilisateurRepository;
    private final AdminRepository adminRepository;
    private final VendeurRepository vendeurRepository;
    private final AcheteurRepository acheteurRepository;
    private final InvestisseurRepository investisseurRepository;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> getAllUsers(int page, int size, String search, String role, Boolean active) {
        List<Utilisateur> allUsers = utilisateurRepository.findAll();
        log.info("DEBUG: Found {} users in database", allUsers.size());
        allUsers.forEach(u -> log.info("DEBUG: User in DB - ID: {}, Username: {}, Role: {}, Active: {}", u.getId(),
                u.getNomUtilisateur(), u.getRole(), u.getEstActif()));

        // Filtrer les utilisateurs
        List<Utilisateur> filteredUsers = allUsers.stream()
                .filter(u -> search == null || search.isEmpty() ||
                        u.getNomUtilisateur().toLowerCase().contains(search.toLowerCase()) ||
                        u.getEmail().toLowerCase().contains(search.toLowerCase()))
                .filter(u -> role == null || role.isEmpty() || u.getRole().name().equals(role))
                .filter(u -> active == null || u.getEstActif().equals(active))
                .collect(Collectors.toList());

        log.info("DEBUG: Users after filtering: {}", filteredUsers.size());

        // Pagination manuelle
        int start = page * size;
        int end = Math.min(start + size, filteredUsers.size());

        if (start > end) {
            start = end;
        }

        List<Utilisateur> paginatedUsers = filteredUsers.subList(start, end);

        List<UserDTO> userDTOs = paginatedUsers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("users", userDTOs);
        response.put("totalElements", filteredUsers.size());
        response.put("totalPages", (int) Math.ceil((double) filteredUsers.size() / size));
        response.put("currentPage", page);

        return response;
    }

    public UserDTO getUserById(Long id) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        Utilisateur user = createUserByRole(userDTO);
        user.setMotDePasse(passwordEncoder.encode(userDTO.getPassword()));
        user.setDateInscription(LocalDateTime.now());
        user.setEstActif(true);

        Utilisateur savedUser = saveUserByRole(user);
        log.info("Nouvel utilisateur créé: {} ({})", savedUser.getEmail(), savedUser.getRole());

        return convertToDTO(savedUser);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        user.setNomUtilisateur(userDTO.getNomUtilisateur());
        user.setEmail(userDTO.getEmail());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setMotDePasse(passwordEncoder.encode(userDTO.getPassword()));
        }

        Utilisateur updatedUser = utilisateurRepository.save(user);
        log.info("Utilisateur mis à jour: {}", updatedUser.getEmail());

        return convertToDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        utilisateurRepository.delete(user);
        log.info("Utilisateur supprimé: {}", user.getEmail());
    }

    @Transactional
    public void toggleUserStatus(Long id) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        user.setEstActif(!user.getEstActif());
        utilisateurRepository.save(user);
        log.info("Statut utilisateur modifié: {} -> {}", user.getEmail(), user.getEstActif());
    }

    public Map<String, Object> getUserStatistics() {
        List<Utilisateur> allUsers = utilisateurRepository.findAll();

        Map<String, Long> usersByRole = allUsers.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getRole().name(),
                        Collectors.counting()));

        long activeUsers = allUsers.stream().filter(Utilisateur::getEstActif).count();
        long inactiveUsers = allUsers.size() - activeUsers;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allUsers.size());
        stats.put("byRole", usersByRole);
        stats.put("active", activeUsers);
        stats.put("inactive", inactiveUsers);

        return stats;
    }

    private UserDTO convertToDTO(Utilisateur user) {
        return UserDTO.builder()
                .id(user.getId())
                .nomUtilisateur(user.getNomUtilisateur())
                .email(user.getEmail())
                .role(user.getRole())
                .estActif(user.getEstActif())
                .dateCreation(user.getDateInscription())
                .build();
    }

    private Utilisateur createUserByRole(UserDTO userDTO) {
        return switch (userDTO.getRole()) {
            case ADMIN -> {
                Admin admin = new Admin();
                admin.setNomUtilisateur(userDTO.getNomUtilisateur());
                admin.setEmail(userDTO.getEmail());
                admin.setRole(Utilisateur.Role.ADMIN);
                yield admin;
            }
            case VENDEUR -> {
                Vendeur vendeur = new Vendeur();
                vendeur.setNomUtilisateur(userDTO.getNomUtilisateur());
                vendeur.setEmail(userDTO.getEmail());
                vendeur.setRole(Utilisateur.Role.VENDEUR);
                yield vendeur;
            }
            case ACHETEUR -> {
                Acheteur acheteur = new Acheteur();
                acheteur.setNomUtilisateur(userDTO.getNomUtilisateur());
                acheteur.setEmail(userDTO.getEmail());
                acheteur.setRole(Utilisateur.Role.ACHETEUR);
                yield acheteur;
            }
            case INVESTISSEUR -> {
                Investisseur investisseur = new Investisseur();
                investisseur.setNomUtilisateur(userDTO.getNomUtilisateur());
                investisseur.setEmail(userDTO.getEmail());
                investisseur.setRole(Utilisateur.Role.INVESTISSEUR);
                yield investisseur;
            }
        };
    }

    private Utilisateur saveUserByRole(Utilisateur user) {
        return switch (user.getRole()) {
            case ADMIN -> adminRepository.save((Admin) user);
            case VENDEUR -> vendeurRepository.save((Vendeur) user);
            case ACHETEUR -> acheteurRepository.save((Acheteur) user);
            case INVESTISSEUR -> investisseurRepository.save((Investisseur) user);
        };
    }
}
