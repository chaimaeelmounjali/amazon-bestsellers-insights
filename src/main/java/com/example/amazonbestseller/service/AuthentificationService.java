// File: AuthentificationService.java
package com.example.amazonbestseller.service;

import com.example.amazonbestseller.dto.LoginDTO;
import com.example.amazonbestseller.dto.RegisterDTO;
import com.example.amazonbestseller.entity.*;
import com.example.amazonbestseller.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthentificationService {

    private final UtilisateurRepository utilisateurRepository;
    private final AcheteurRepository acheteurRepository;
    private final VendeurRepository vendeurRepository;
    private final AdminRepository adminRepository;
    private final InvestisseurRepository investisseurRepository;
    private final MagasinRepository magasinRepository;
    private final PasswordEncoder passwordEncoder;

    // ========== INSCRIPTION ==========

    @Transactional
    public Utilisateur inscrireUtilisateur(RegisterDTO registerDTO) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }

        // Vérifier si le nom d'utilisateur existe déjà
        if (utilisateurRepository.findByNomUtilisateur(registerDTO.getNomUtilisateur()).isPresent()) {
            throw new RuntimeException("Ce nom d'utilisateur est déjà pris");
        }

        // Créer l'utilisateur selon le rôle
        Utilisateur utilisateur;

        switch (registerDTO.getRole()) {
            case ACHETEUR:
                utilisateur = creerAcheteur(registerDTO);
                break;
            case VENDEUR:
                utilisateur = creerVendeur(registerDTO);
                break;
            case ADMIN:
                utilisateur = creerAdmin(registerDTO);
                break;
            case INVESTISSEUR:
                utilisateur = creerInvestisseur(registerDTO);
                break;
            default:
                throw new RuntimeException("Rôle invalide");
        }

        return utilisateur;
    }

    private Acheteur creerAcheteur(RegisterDTO dto) {
        Acheteur acheteur = new Acheteur();
        acheteur.setNomUtilisateur(dto.getNomUtilisateur());
        acheteur.setEmail(dto.getEmail());
        acheteur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        acheteur.setRole(Utilisateur.Role.ACHETEUR);
        acheteur.setEstActif(true);
        acheteur.setDateInscription(LocalDateTime.now());
        acheteur.setAdresseLivraison(dto.getAdresse());
        acheteur.setNumeroTelephone(dto.getTelephone());

        return acheteurRepository.save(acheteur);
    }

    private Vendeur creerVendeur(RegisterDTO dto) {
        Vendeur vendeur = new Vendeur();
        vendeur.setNomUtilisateur(dto.getNomUtilisateur());
        vendeur.setEmail(dto.getEmail());
        vendeur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        vendeur.setRole(Utilisateur.Role.VENDEUR);
        vendeur.setEstActif(true);
        vendeur.setDateInscription(LocalDateTime.now());
        vendeur.setVentesTotales(BigDecimal.ZERO);
        vendeur.setCommission(BigDecimal.ZERO);
        vendeur.setObjectifVentes(
                dto.getObjectifVentes() != null ? dto.getObjectifVentes() : BigDecimal.valueOf(10000));

        Vendeur savedVendeur = vendeurRepository.save(vendeur);

        // Création automatique du magasin pour le vendeur
        com.example.amazonbestseller.entity.Magasin magasin = new com.example.amazonbestseller.entity.Magasin();
        magasin.setNom("Boutique de " + dto.getNomUtilisateur());
        magasin.setIdVendeur(savedVendeur.getId());
        magasin.setAdresse("Adresse à définir");
        magasin.setEmail(dto.getEmail());
        magasin.setNote(BigDecimal.valueOf(5.0)); // Note de départ encourageante

        magasinRepository.save(magasin);

        return savedVendeur;
    }

    private Admin creerAdmin(RegisterDTO dto) {
        Admin admin = new Admin();
        admin.setNomUtilisateur(dto.getNomUtilisateur());
        admin.setEmail(dto.getEmail());
        admin.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        admin.setRole(Utilisateur.Role.ADMIN);
        admin.setEstActif(true);
        admin.setDateInscription(LocalDateTime.now());
        admin.setPermissions("READ,WRITE,DELETE");

        return adminRepository.save(admin);
    }

    private Investisseur creerInvestisseur(RegisterDTO dto) {
        Investisseur investisseur = new Investisseur();
        investisseur.setNomUtilisateur(dto.getNomUtilisateur());
        investisseur.setEmail(dto.getEmail());
        investisseur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        investisseur.setRole(Utilisateur.Role.INVESTISSEUR);
        investisseur.setEstActif(true);
        investisseur.setDateInscription(LocalDateTime.now());
        investisseur.setMontantInvestissement(
                dto.getMontantInvestissement() != null ? dto.getMontantInvestissement() : BigDecimal.ZERO);
        investisseur.setRoi(BigDecimal.ZERO);

        return investisseurRepository.save(investisseur);
    }

    // ========== CONNEXION ==========

    @Transactional
    public Utilisateur connecter(LoginDTO loginDTO, HttpSession session) {
        // Rechercher l'utilisateur par email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(loginDTO.getMotDePasse(), utilisateur.getMotDePasse())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        // Vérifier si le compte est actif
        if (!utilisateur.getEstActif()) {
            throw new RuntimeException("Ce compte a été désactivé");
        }

        // Mettre à jour la dernière connexion pour les admins
        if (utilisateur instanceof Admin) {
            Admin admin = (Admin) utilisateur;
            admin.setDerniereConnexion(LocalDateTime.now());
            adminRepository.save(admin);
        }

        // --- SPRING SECURITY INTEGRATION ---
        // Créer l'autorité (ex: ROLE_ADMIN)
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name());

        // Créer l'objet Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                utilisateur,
                null,
                Collections.singletonList(authority));

        // Définir le contexte de sécurité
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Sauvegarder dans la session pour la persistance entre les requêtes
        session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

        // Créer la session (Legacy attributes)
        session.setAttribute("utilisateurId", utilisateur.getId());
        session.setAttribute("utilisateurRole", utilisateur.getRole());
        session.setAttribute("utilisateurNom", utilisateur.getNomUtilisateur());
        session.setMaxInactiveInterval(3600); // 1 heure

        return utilisateur;
    }

    // ========== DÉCONNEXION ==========

    public void deconnecter(HttpSession session) {
        session.invalidate();
    }

    // ========== VÉRIFICATION DE SESSION ==========

    public boolean estConnecte(HttpSession session) {
        return session.getAttribute("utilisateurId") != null;
    }

    public Utilisateur.Role getRoleUtilisateur(HttpSession session) {
        return (Utilisateur.Role) session.getAttribute("utilisateurRole");
    }

    public Long getUtilisateurIdFromSession(HttpSession session) {
        return (Long) session.getAttribute("utilisateurId");
    }

    public Utilisateur getUtilisateurConnecte(HttpSession session) {
        Long utilisateurId = getUtilisateurIdFromSession(session);
        if (utilisateurId == null) {
            throw new RuntimeException("Aucun utilisateur connecté");
        }

        return utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    // ========== GESTION DU PROFIL ==========

    @Transactional
    public Utilisateur mettreAJourProfil(Long utilisateurId, RegisterDTO updateDTO) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Mise à jour des informations communes
        if (updateDTO.getNomUtilisateur() != null) {
            // Vérifier que le nouveau nom n'est pas déjà pris
            Optional<Utilisateur> existant = utilisateurRepository.findByNomUtilisateur(updateDTO.getNomUtilisateur());
            if (existant.isPresent() && !existant.get().getId().equals(utilisateurId)) {
                throw new RuntimeException("Ce nom d'utilisateur est déjà pris");
            }
            utilisateur.setNomUtilisateur(updateDTO.getNomUtilisateur());
        }

        if (updateDTO.getEmail() != null) {
            // Vérifier que le nouvel email n'est pas déjà pris
            Optional<Utilisateur> existant = utilisateurRepository.findByEmail(updateDTO.getEmail());
            if (existant.isPresent() && !existant.get().getId().equals(utilisateurId)) {
                throw new RuntimeException("Cet email est déjà utilisé");
            }
            utilisateur.setEmail(updateDTO.getEmail());
        }

        // Mise à jour spécifique selon le type
        if (utilisateur instanceof Acheteur) {
            Acheteur acheteur = (Acheteur) utilisateur;
            if (updateDTO.getAdresse() != null)
                acheteur.setAdresseLivraison(updateDTO.getAdresse());
            if (updateDTO.getTelephone() != null)
                acheteur.setNumeroTelephone(updateDTO.getTelephone());
            return acheteurRepository.save(acheteur);
        } else if (utilisateur instanceof Vendeur) {
            Vendeur vendeur = (Vendeur) utilisateur;
            if (updateDTO.getObjectifVentes() != null)
                vendeur.setObjectifVentes(updateDTO.getObjectifVentes());
            return vendeurRepository.save(vendeur);
        } else if (utilisateur instanceof Admin) {
            return adminRepository.save((Admin) utilisateur);
        } else if (utilisateur instanceof Investisseur) {
            return investisseurRepository.save((Investisseur) utilisateur);
        }

        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void changerMotDePasse(Long utilisateurId, String ancienMotDePasse, String nouveauMotDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(ancienMotDePasse, utilisateur.getMotDePasse())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        // Mettre à jour avec le nouveau mot de passe
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepository.save(utilisateur);
    }

    // ========== GESTION DES COMPTES ==========

    @Transactional
    public void desactiverCompte(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        utilisateur.setEstActif(false);
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void activerCompte(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        utilisateur.setEstActif(true);
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void supprimerCompte(Long utilisateurId) {
        utilisateurRepository.deleteById(utilisateurId);
    }

    // ========== UTILITAIRES ==========

    public boolean verifierPermission(HttpSession session, Utilisateur.Role roleRequis) {
        Utilisateur.Role roleUtilisateur = getRoleUtilisateur(session);

        if (roleUtilisateur == null) {
            return false;
        }

        // L'admin a tous les droits
        if (roleUtilisateur == Utilisateur.Role.ADMIN) {
            return true;
        }

        return roleUtilisateur == roleRequis;
    }

    public void verifierAcces(HttpSession session, Utilisateur.Role roleRequis) {
        if (!verifierPermission(session, roleRequis)) {
            throw new RuntimeException("Accès refusé. Rôle requis: " + roleRequis);
        }
    }
}