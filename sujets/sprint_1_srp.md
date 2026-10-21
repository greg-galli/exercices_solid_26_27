# Sprint 1 : Single Responsibility Principle (SRP)

> **Principe de Responsabilité Unique (SRP)**  
> *« Une classe ne devrait avoir qu'une seule et unique raison de changer. »*  
> — Robert C. Martin

---

## Contexte Métier & Constat de l'Existant

Inspectez le fichier `OrderCheckoutService.java` dans le package `service`.

Actuellement, ce service réalise l'intégralité du processus lors du passage de commande :
1. **Validation métier du panier** : intégrité du client, quantité positive et plafonnée à 20 articles, prix unitaire positif.
2. **Calcul des taxes (TVA)** : 20% pour le standard, 5.5% pour les livres et l'alimentation.
3. **Calcul des promotions** : réductions codées en dur avec des embranchements `if/else`.
4. **Paiement** : exécution de la transaction bancaire.
5. **Persistance** : enregistrement de la commande dans le système de stockage.
6. **Notification** : composition et envoi du courriel au client.

### Pourquoi ce code est toxique en équipe ?
Cette classe cumule **6 raisons distinctes de changer** :
* Si la réglementation fiscale change sur la TVA : on modifie `OrderCheckoutService`.
* Si les règles de validation du panier évoluent : on modifie `OrderCheckoutService`.
* Si le service commercial modifie un code promo : on modifie `OrderCheckoutService`.
* Si le prestataire d'envoi d'emails ou de passerelle bancaire change : on modifie `OrderCheckoutService`.

Chaque développeur travaillant sur une fonctionnalité différente risque d'entrer en conflit de merge avec ses collègues, et chaque modification présente un risque de faire régresser une autre responsabilité !

---

## Votre Mission d'Ingénierie

Refactorer `OrderCheckoutService` pour qu'il devienne un **chef d'orchestre haut niveau**. Il ne doit plus contenir de calculs fiscaux, ni de règles de validation, ni d'appels directs de persistance ou de notification.

Il doit déléguer chaque sous-tâche à un composant dédié ayant **une responsabilité unique et bien identifiée** :
* Un composant responsable de la **validation** (lève une `OrderValidationException` si une règle est enfreinte).
* Un composant responsable du **calcul des taxes (TVA)**.
* Un composant responsable du **paiement**.
* Un composant responsable de la **persistance / référentiel des commandes**.
* Un composant responsable de l'**envoi des notifications**.

---

## Conseils de Conception & Indices

1. **Rôle d'orchestrateur** : `OrderCheckoutService` reçoit désormais ses collaborateurs via son constructeur (Injection de Dépendances). Sa méthode `checkout` se contente de coordonner les étapes dans l'ordre logique.
2. **Nommage explicite** : Nommez vos nouveaux composants en fonction de leur rôle unique (ex. `OrderValidator`, `TaxCalculator`, `PaymentProcessor`, `OrderRepository`, `InvoiceNotificationSender`).
3. **Composants Spring** : Annotez vos composants avec `@Component` ou `@Service` selon leur nature.

---

## Critères d'Acceptation & Validation

1. **Lisibilité** : `OrderCheckoutService` devient concis (moins de 50 lignes de code) et se lit comme un plan d'action séquentiel.
2. **Modularité & Testabilité** :
   * Chaque nouveau composant (`OrderValidator`, `TaxCalculator`, etc.) possède sa propre classe de tests unitaires dédiée et isolée.
   * Rédigez des tests exhaustifs sur `OrderValidatorTest` (cas limites, quantités, prix négatifs, client null).
   * Rédigez des tests exhaustifs sur `TaxCalculatorTest` (taux normal 20%, réduit 5.5%, mixte, panier vide).
3. **Non-Régression Absolue** :
   * Lancez `./gradlew test` (ou `.\gradlew.bat test` sous Windows).
   * **100% des tests de `OrderCheckoutServiceTest` doivent rester verts** sans avoir modifié la logique des assertions métier !
