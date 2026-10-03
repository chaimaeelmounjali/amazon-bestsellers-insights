# Amazon Bestsellers Insights

## 1) Objectif produit
Application full-stack pour **explorer, gérer et analyser un catalogue de produits Amazon Best Sellers**.
Le dépôt combine :
- une API Spring Boot (authentification, produits, ventes, dashboards, export, prédiction) ;
- un frontend React (exploration, fiches produit, dashboards par rôle) ;
- un script Python d’enrichissement CSV ;
- une base MySQL initialisée via scripts SQL + chargement CSV.

## 2) Architecture du projet
### Backend (Spring Boot)
- Dossier : `/home/runner/work/amazon-bestsellers-insights/amazon-bestsellers-insights/src/main/java/com/example/amazonbestseller`
- Couches : `controller`, `service`, `repository`, `entity`, `dto`, `config`, `security`, `mapper`, `exception`.
- API REST sous préfixe `/api/*`.
- Sécurité Spring Security en session + HTTP Basic (rôles `ADMIN`, `VENDEUR`, `INVESTISSEUR`, `ACHETEUR`).

### Frontend (React)
- Dossier : `/home/runner/work/amazon-bestsellers-insights/amazon-bestsellers-insights/frontend`
- Routage React Router avec routes publiques et routes protégées (`ProtectedRoute`).
- Tableaux de bord dédiés selon le rôle utilisateur.

### Scripts d’enrichissement
- Dossier : `/home/runner/work/amazon-bestsellers-insights/amazon-bestsellers-insights/scripts`
- Script `enrich_csv_data.py` : enrichit un CSV (nom produit + image) à partir des pages Amazon.

### Service de prédiction
- Backend :
  - `AnalysePredictiveService` : prédiction de rang futur, identification de futurs best-sellers, recommandation de prix.
  - `UserPredictionService` : projection de croissance utilisateurs (régression linéaire simple).

## 3) Stack confirmée (`pom.xml` et `frontend/package.json`)
### Backend
- Java 17, Spring Boot 3.2.0
- Spring Web, Spring Data JPA, Spring Security, Spring Validation, Spring Mail
- MySQL (`mysql-connector-j`)
- Lombok, MapStruct
- OpenCSV, Apache Commons CSV, Commons IO, Commons Lang3
- Jsoup
- Apache POI (export Excel)
- Commons Math3
- Springdoc OpenAPI UI
- Tests : Spring Boot Test, Spring Security Test

### Frontend
- React 19 + React DOM 19
- React Router DOM 7
- Axios
- Chart.js + react-chartjs-2
- lucide-react
- react-scripts (Create React App)
- Tests : Testing Library + jest-dom + user-event
- Style/tooling : Tailwind CSS, PostCSS, Autoprefixer

## 4) Schéma SQL et chargement des données
### Schéma
- Fichier : `/home/runner/work/amazon-bestsellers-insights/amazon-bestsellers-insights/schema.sql`
- Base : `amazon_bestsellers`
- Tables principales : `utilisateur`, `admin`, `investisseur`, `vendeur`, `acheteur`, `magasin`, `produit`, `stock`, `commande`, `ligne_commande`, `vente`, `avis`, `alerte`, `statistique`.

### Données d’initialisation
- Fichier : `/home/runner/work/amazon-bestsellers-insights/amazon-bestsellers-insights/insert_data.sql`
- Insère des utilisateurs de test, magasins, produits, stocks, commandes, ventes, avis, alertes, statistiques.

### Chargement CSV
- Service : `CSVLoaderService` (appelé au démarrage via `CSVLoaderRunner` si `app.load-csv-on-startup=true` ou manuellement via API).
- Le service peut nettoyer la base (`app.clean-before-load`) puis :
  1. lire le CSV,
  2. créer/mettre à jour les produits,
  3. créer/mettre à jour les stocks,
  4. exécuter `insert_data.sql`.

## 5) Organisation des dossiers
```text
amazon-bestsellers-insights/
├── src/main/java/com/example/amazonbestseller/
│   ├── config/ controller/ dto/ entity/ exception/
│   ├── mapper/ repository/ security/ service/
├── src/main/resources/
│   ├── application.properties
│   ├── application.yml
│   └── uploads/
├── src/test/java/com/example/amazonbestseller/
├── frontend/
│   ├── src/
│   │   ├── api/ components/ context/ hooks/ pages/ services/
├── scripts/
│   └── enrich_csv_data.py
├── data/
│   ├── amazon_bestsellers.csv
│   ├── amazon_bestsellers_full.csv
│   └── amazon_bestsellers_enriched.csv
├── schema.sql
└── insert_data.sql
```

## 6) Flux de données (vue d’ensemble)
1. **Collecte/enrichissement** : CSV brut (`data/...`) enrichi par `scripts/enrich_csv_data.py`.
2. **Ingestion backend** : API `/api/csv/charger` ou auto-chargement au démarrage.
3. **Stockage** : persistance dans MySQL (`produit`, `stock`, etc.) + seed SQL métier.
4. **Exposition API** : endpoints REST pour produits, recherche, dashboards, ventes, exports, prédictions.
5. **Consommation frontend** : React appelle `http://localhost:8080/api` via Axios (`withCredentials: true`).

## 7) Endpoints et fonctionnalités observables
### Authentification et profil
- `/api/auth/inscription`, `/connexion`, `/deconnexion`, `/utilisateur-connecte`
- `/api/auth/profil/{id}`, `/api/auth/changer-mot-de-passe/{id}`

### Produits / recherche / filtrage
- `/api/produits` (CRUD + stats + top + catégories + stock)
- `/api/recherche/simple`, `/semantique`, `/suggestions`, `/similaires/{produitId}`
- `/api/filtrage/produits`, `/categories`, `/prix-range`, `/recherche-avancee`

### Commerce / opérationnel
- `/api/commandes/*` (création manuelle, statuts, stats)
- `/api/ventes/*` (analyse vendeur, statistiques, top produits, performance vendeurs)
- `/api/stock/*`, `/api/avis/*`, `/api/alertes/*`

### Dashboards
- `/api/dashboard/global`
- `/api/dashboard/acheteur/me`, `/acheteur/{id}`
- `/api/dashboard/vendeur/{id}`
- `/api/dashboard/investisseur/{id}`
- `/api/dashboard/admin`

### Prédiction
- `/api/analyse-predictive/futurs-bestsellers`
- `/api/analyse-predictive/position-future/{id}`
- `/api/analyse-predictive/prix-ideal/{id}`
- `/api/admin/users/predictions` (projection utilisateurs)

### Administration, export, ingestion
- `/api/admin/users/*`
- `/api/export/produits/csv`, `/produits/excel`, `/ventes/csv`, `/rapport-complet`
- `/api/csv/charger`

## 8) Rôle de l’auteur (déduit du dépôt)
Au vu des artefacts versionnés, le rôle couvre une réalisation **full-stack** :
- conception du modèle de données e-commerce/analytics ;
- implémentation de l’API Spring Boot et des règles métier ;
- mise en place de la sécurité par rôles ;
- création de l’interface React (exploration + dashboards multi-profils) ;
- scripts d’enrichissement et pipeline de chargement de données ;
- exports CSV/Excel et premières briques de prédiction.

## 9) Résultats / livrables
- Backend Spring Boot structuré en couches, avec documentation OpenAPI.
- Frontend React avec navigation, login et pages métiers (produits, dashboard, admin, vendeur, investisseur).
- Schéma SQL complet + script d’insertion de données réalistes.
- Script Python d’enrichissement de dataset Amazon.
- Services de prédiction métier (rang/prix produits, projection utilisateurs).

## 10) Installation et démarrage
### Prérequis
- Java 17+
- Maven (ou `./mvnw`)
- Node.js + npm
- MySQL 8+

### A) Base de données
1. Créer la base et les tables :
   ```bash
   mysql -u root -p < /home/runner/work/amazon-bestsellers-insights/amazon-bestsellers-insights/schema.sql
   ```
2. Vérifier la configuration backend :
   - `/home/runner/work/amazon-bestsellers-insights/amazon-bestsellers-insights/src/main/resources/application.properties`
   - URL par défaut : `jdbc:mysql://localhost:3306/amazon_bestsellers`

### B) Backend
```bash
cd /home/runner/work/amazon-bestsellers-insights/amazon-bestsellers-insights
./mvnw spring-boot:run
```
API par défaut : `http://localhost:8080`

### C) Frontend
```bash
cd /home/runner/work/amazon-bestsellers-insights/amazon-bestsellers-insights/frontend
npm install
npm start
```
Frontend par défaut : `http://localhost:3000`

### D) Chargement des données
- Auto au démarrage si `app.load-csv-on-startup=true`.
- Ou manuellement via `POST /api/csv/charger`.

### E) Enrichissement CSV (optionnel)
```bash
cd /home/runner/work/amazon-bestsellers-insights/amazon-bestsellers-insights
python scripts/enrich_csv_data.py
```

## 11) Configuration base de données (points importants)
- `spring.jpa.hibernate.ddl-auto=none` dans `application.properties` : le schéma SQL est attendu côté MySQL.
- `application.yml` contient aussi une configuration datasource (avec `ddl-auto: update`) : garder une seule source de vérité selon l’environnement.
- Le fichier CSV utilisé est piloté par `csv.file.path`.

## 12) Limites observables et pistes d’amélioration
### Limites
- Configuration dupliquée (`application.properties` + `application.yml`) avec paramètres divergents.
- Certaines valeurs sont environnement-dépendantes (ex. chemins Windows dans `application.yml`).
- Prédictions basées sur heuristiques/règles (pas de pipeline ML entraîné versionné ici).
- Historique de recherche en mémoire (`Map` service) non persistant.
- Peu de tests automatisés côté backend/frontend pour couvrir les scénarios métier.

### Pistes d’amélioration
- Unifier et externaliser la configuration (profils Spring, variables d’environnement).
- Renforcer la couverture de tests (services critiques, contrôleurs, parcours frontend).
- Industrialiser la prédiction (jeu de validation, suivi qualité, versionnage modèle).
- Persister l’historique/suggestions dans une couche de stockage dédiée.
- Ajouter un processus CI de vérification systématique backend + frontend.
