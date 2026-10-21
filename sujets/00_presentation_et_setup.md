# TP Fil Rouge SOLID & Spring Boot - ShopFast

## Contexte du Projet

Bienvenue dans l'équipe d'ingénierie logicielle de **ShopFast**, une startup e-commerce en très forte croissance !

L'application a été initialement développée lors d'un hackathon pour valider le marché (MVP). Le code actuel fonctionne et les tests initiaux passent au vert. Cependant, face à la montée en charge et à l'arrivée de nouvelles fonctionnalités, l'architecture commence à montrer de sérieuses faiblesses :
- **God Classes** : des classes monolithiques qui cumulent toutes les responsabilités (validation, calculs financiers, taxes, paiements, notifications et persistance).
- **Risque de régressions permanent** : chaque nouvelle règle marketing nécessite de modifier les mêmes fichiers centraux, générant des conflits de merge et des bugs en cascade.
- **Fragilité** : le code métier est directement couplé à des détails techniques d'infrastructure.

Votre mission au cours de ce projet fil rouge est de **refactorer et faire évoluer ShopFast** en appliquant avec rigueur les **5 principes SOLID**, combinés aux capacités d'injection de dépendances de **Spring Boot**.

---

## Organisation des Sprints

Le travail s'organise en **5 Sprints progressifs**. Chaque sprint correspond à une User Story produit et incarne l'un des principes SOLID :

| Sprint | Principe SOLID | Défi Technique & Métier | Rôle de Spring Boot |
| :--- | :--- | :--- | :--- |
| **Sprint 1** | **SRP** - Single Responsibility | Démanteler la « God Class » de commande en composants hautement cohésifs. | Structuration en composants `@Service` / `@Component`. |
| **Sprint 2** | **OCP** - Open/Closed | Concevoir un moteur de promotions ouvert aux nouvelles règles sans modifier le code existant. | Détection et injection automatique de listes de beans (`List<Strategy>`). |
| **Sprint 3** | **LSP** - Liskov Substitution | Diversifier les modes de paiement sans faire crasher le batch nocturne de remboursement. | Contrats d'interfaces comportementaux et substituabilité stricte. |
| **Sprint 4** | **ISP** - Interface Segregation | Intégrer des transporteurs hétérogènes (express, relais, coursier vélo) sans forcer d'implémentations inutiles. | Interfaces de rôle fines et ciblées pour les services clients. |
| **Sprint 5** | **DIP** - Dependency Inversion | Découpler le cœur métier de l'infrastructure externe (Stripe, SendGrid, Base SQL). | Ports & Adaptateurs, inversion de contrôle IoC, profils `@Profile("dev")` vs `@Profile("prod")`. |

---

## Environnement Technique & Prérequis

* **Langage** : Java 25 LTS
* **Framework** : Spring Boot 4.1.1
* **Build Tool** : Gradle avec wrapper embarqué (`./gradlew` sous Linux/macOS, `gradlew.bat` sous Windows)
* **Tests** : JUnit 5 (Jupiter), AssertJ

### Vérification de votre environnement

Dans un terminal, vérifiez que Java 25 est accessible :
```bash
java -version
```

Placez-vous dans votre projet de départ et lancez la suite de tests de base :
```bash
# Sous Windows
.\gradlew.bat test

# Sous Linux / macOS
./gradlew test
```
Tous les tests initiaux doivent être **verts** avant de commencer !

---

## La Règle d'Or : Non-Régression & TDD

> **À chaque étape, la suite de tests existante doit impérativement rester VERTE.**  
> Un refactoring d'architecture ne doit **jamais** altérer le comportement métier attendu. Si un test existant échoue, c'est que votre refactoring a introduit une régression !
