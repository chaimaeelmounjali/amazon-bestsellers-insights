# 📊 Amazon Bestsellers Insights & Analytics Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.x-blue.svg?logo=react)](https://reactjs.org/)
[![Java](https://img.shields.io/badge/Java-17-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![Chart.js](https://img.shields.io/badge/Chart.js-4.x-FF6384.svg?logo=chartdotjs)](https://www.chartjs.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

*Bilingual README: [Français](#-version-française) | [English](#-english-version)*

---

## 🇫🇷 Version Française

### 🎯 Objectif
**Amazon Bestsellers Insights** est une plateforme analytique et prédictive complète permettant d'explorer, visualiser et anticiper les tendances des produits les plus vendus sur Amazon. L'application résout la problématique de la prise de décision commerciale pour les vendeurs et analystes e-commerce en centralisant l'ingestion de données de ventes volumineuses, le calcul d'indicateurs clés de performance (KPIs) en temps réel, et la prédiction de rentabilité et de dynamique de stock.

### 🛠️ Stack Technologique
- **Backend** : Java 17, Spring Boot 3.2.0 (Spring Web, Spring Data JPA, Spring Security / JWT, Bean Validation).
- **Frontend** : React 19, React Router v7, Axios, Lucide React, Chart.js & react-chartjs-2, CSS3 moderne & responsive.
- **Base de Données & Persistance** : Hibernate ORM, H2 Database / MySQL, pagination et requêtes JPA optimisées.
- **Data Engineering / Scripts** : Python (Pandas) pour le nettoyage et l'enrichissement automatisé des jeux de données CSV.
- **Architecture** : RESTful API découplée, authentification basée sur les rôles (Admin, Seller, User), architecture en couches (Controller, Service, Repository, DTO).

### 👩‍💻 Mon Rôle & Contributions
- **Conception & Architecture globale** : Design de l'architecture logicielle fullstack et modélisation de la base de données relationnelle.
- **Développement Backend Spring Boot** :
  - Implémentation des endpoints REST sécurisés avec gestion fine des autorisations et JWT.
  - Développement des services métier pour l'analyse des ventes, le suivi des stocks et les alertes de réapprovisionnement.
  - Résolution des problématiques de scan JPA, de gestion des entités et d'initialisation de données de démonstration fiables (`DataInitializer`).
- **Développement Frontend React** :
  - Conception d'un tableau de bord interactif avec visualisations dynamiques (graphiques en barres, courbes d'évolution des ventes, camemberts de répartition).
  - Gestion d'état fluide avec hooks React personnalisés (`useAuth`, `useDashboard`, `useInventory`, `useOrders`, `useProducts`).
  - Implémentation des espaces dédiés : catalogue produits, portail Vendeur (Seller) et panneau d'administration (Admin).
- **Pipelines de données** : Élaboration de scripts Python pour normaliser, filtrer et enrichir les catalogues produits avant importation.

### 📊 Résultats & Métriques Clés
- **Plateforme End-to-End opérationnelle** : Déploiement d'un écosystème prêt à l'emploi avec authentification sécurisée et 3 rôles utilisateurs distincts.
- **Visualisation temps réel** : Restitution instantanée de dizaines de métriques de vente (top catégories, marge bénéficiaire, vitesse d'écoulement).
- **Fiabilité des données** : 100% des erreurs d'initialisation et de dashboard vides résolues grâce à un pipeline robuste de génération et validation de données de test.

---

## 🇬🇧 English Version

### 🎯 Objective
**Amazon Bestsellers Insights** is an end-to-end analytical and predictive web platform designed to explore, monitor, and forecast trends among top-performing Amazon products. The platform empowers e-commerce sellers and retail analysts to make data-driven inventory and pricing decisions through automated CSV data ingestion, real-time KPI calculations, and trend forecasting dashboards.

### 🛠️ Tech Stack
- **Backend**: Java 17, Spring Boot 3.2.0 (Spring MVC, Spring Data JPA, Spring Security, JWT authentication, Lombok).
- **Frontend**: React 19, React Router v7, Axios, Lucide React, Chart.js & react-chartjs-2, modern responsive CSS.
- **Database & Persistence**: Hibernate ORM, H2 / MySQL, dynamic filtering, pagination, and JPA query optimization.
- **Data Engineering**: Python scripts for CSV normalization, cleansing, and metadata enrichment.
- **Architecture**: Decoupled client-server REST architecture, role-based access control (Admin, Seller, User), clean layered pattern (Controller, Service, Repository, DTO).

### 👩‍💻 My Role & Key Contributions
- **System Architecture & Data Modeling**: Architected the end-to-end full-stack solution and designed the relational schema.
- **Spring Boot Backend Engineering**:
  - Implemented secure REST APIs with token-based authentication and granular permission checks.
  - Engineered core domain services: sales analytics, stock velocity calculations, inventory threshold alerts.
  - Hardened JPA repository queries, entity lifecycle management, and resolved edge-case test data generation issues.
- **React Frontend Engineering**:
  - Crafted an intuitive, responsive analytics dashboard featuring interactive Chart.js visualizations.
  - Developed custom modular React hooks (`useDashboard`, `useInventory`, `useOrders`, `useProducts`, `useAuth`) for clean state synchronization.
  - Built dedicated portals: Product Catalog explorer, Seller management suite, and System Admin back-office.
- **Data Preprocessing**: Wrote Python ingestion scripts to clean, structure, and populate marketplace data.

### 📊 Key Results & Impact
- **Production-Ready Full-Stack MVP**: Successfully delivered a complete, role-governed platform connecting data ingestion to real-time interactive insights.
- **Actionable Business Intelligence**: Live rendering of top-selling categories, price-elasticity insights, and automated low-stock warnings.
- **High Data Integrity**: Robust database seeding and fail-safe dashboard state handling with zero unhandled null states.

---

### 🚀 Quick Start / Démarrage Rapide

#### Backend (Spring Boot)
```bash
# Build & Run Backend
mvn clean install
mvn spring-boot:run
# Server runs on http://localhost:8080
```

#### Frontend (React)
```bash
cd frontend
npm install
npm start
# Client runs on http://localhost:3000
```
