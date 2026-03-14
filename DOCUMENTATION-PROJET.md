# Documentation Complète du Projet - Gestion de Produits

## Vue d'ensemble

Ce projet est une application de gestion de produits composée de deux modules Java/Spring Boot qui communiquent avec une base de données MySQL.

| Projet | Rôle |
|---|---|
| **product-core** | Coeur métier : modèle, accès BDD, logique métier |
| **product-api** | API REST : expose les fonctionnalités via internet |

Architecture :

```
[Client / Navigateur / Postman]
        |
        v
  product-api (port 8086)        --> API REST (4 endpoints)
        |
        v
  product-core                    --> Logique métier (ProductService)
        |
        v
  MySQL (port 3306)              --> Base de données (table products)
```

---

## 1. Création du projet product-core

**product-core** est le coeur de l'application. Il contient toute la logique de gestion des produits et communique directement avec MySQL.

### Structure du projet

```
product-core/
├── pom.xml
├── init-db.sql
└── src/main/
    ├── java/com/product/core/
    │   ├── App.java
    │   ├── model/
    │   │   └── Product.java
    │   ├── repository/
    │   │   └── ProductRepository.java
    │   └── service/
    │       └── ProductService.java
    └── resources/
        └── application.properties
```

### Technologies utilisées

- Java 17
- Spring Boot 3.2.5
- Spring Data JPA
- MySQL Connector
- Maven

---

## 2. Le modèle Product

La classe `Product` représente un produit dans la base de données. Elle est annotée `@Entity` pour JPA.

### Attributs

| Attribut | Type | Description |
|---|---|---|
| `id` | Long | Identifiant unique (auto-incrémenté) |
| `name` | String | Nom du produit |
| `description` | String | Description du produit |
| `price` | double | Prix du produit |
| `quantity` | int | Quantité en stock |

### Annotations JPA

- `@Entity` : déclare la classe comme entité persistante
- `@Table(name = "products")` : nom de la table en base
- `@Id` + `@GeneratedValue(strategy = IDENTITY)` : clé primaire auto-incrémentée

---

## 3. Accès à la base de données (JPA Repository)

L'interface `ProductRepository` étend `JpaRepository<Product, Long>`. Spring Data JPA fournit automatiquement les opérations CRUD.

### Méthodes disponibles (fournies par Spring)

| Méthode | Action |
|---|---|
| `save(product)` | Enregistrer un produit |
| `findById(id)` | Rechercher par ID |
| `findAll()` | Récupérer tous les produits |
| `deleteById(id)` | Supprimer un produit |
| `count()` | Compter les produits |

### Méthodes personnalisées

| Méthode | Action |
|---|---|
| `findByName(name)` | Recherche par nom |
| `findByPriceLessThan(price)` | Recherche par prix maximum |

---

## 4. Configuration de la connexion MySQL

Fichier `application.properties` :

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/productdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

Points importants :
- `createDatabaseIfNotExist=true` : la base `productdb` est créée automatiquement
- `ddl-auto=update` : la table `products` est créée/mise à jour par Hibernate
- Les valeurs sont configurables par variables d'environnement (utile pour Docker)

---

## 5. La logique métier (ProductService)

La classe `ProductService` annotée `@Service` contient les 4 actions obligatoires.

### Action 1 — Ajouter un produit

```java
public Product addProduct(Product product) {
    return productRepository.save(product);
}
```

Enregistre un produit dans la base et retourne le produit avec son ID généré.

### Action 2 — Lister tous les produits

```java
public List<Product> listAllProducts() {
    return productRepository.findAll();
}
```

Retourne la liste complète de l'inventaire.

### Action 3 — Modifier la quantité

```java
public Product updateQuantity(Long id, int newQuantity) {
    Optional<Product> optional = productRepository.findById(id);
    if (optional.isPresent()) {
        Product product = optional.get();
        product.setQuantity(newQuantity);
        return productRepository.save(product);
    }
    throw new RuntimeException("Produit introuvable avec l'id : " + id);
}
```

Recherche le produit par ID, met à jour la quantité, sauvegarde en base.

### Action 4 — Compter les produits en stock faible

```java
public long countLowStockProducts() {
    return productRepository.findAll().stream()
            .filter(p -> p.getQuantity() <= 5)
            .count();
}
```

Un produit est en stock faible si sa quantité est inférieure ou égale à 5.

---

## 6. Gestion des versions

Chaque fonctionnalité correspond à une version publiée :

| Version | Fonctionnalité | Type |
|---|---|---|
| 1.0-SNAPSHOT | Ajout de produit | Développement |
| 2.0-SNAPSHOT | Liste des produits | Développement |
| 3.0-SNAPSHOT | Modification du stock | Développement |
| 4.0-SNAPSHOT | Calcul du stock faible | Développement |
| 1.0.0 | Version finale stable | Release |

Commande pour changer la version :

```bash
mvn versions:set "-DnewVersion=X.X-SNAPSHOT"
```

---

## 7. Publication dans Nexus

Nexus stocke les bibliothèques Maven et rend product-core disponible comme dépendance.

### Configuration pom.xml

```xml
<distributionManagement>
    <repository>
        <id>nexus-releases</id>
        <url>https://URL-NEXUS/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>nexus-snapshots</id>
        <url>https://URL-NEXUS/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

### Configuration settings.xml (~/.m2/settings.xml)

```xml
<servers>
    <server>
        <id>nexus-releases</id>
        <username>Admin</username>
        <password>Devops12EX</password>
    </server>
    <server>
        <id>nexus-snapshots</id>
        <username>Admin</username>
        <password>Devops12EX</password>
    </server>
</servers>
```

### Commande de publication

```bash
mvn clean deploy -DskipTests
```

---

## 8. Création du projet product-api

**product-api** est l'API REST. Il ne contient aucune logique métier, il utilise uniquement les méthodes de product-core.

### Structure du projet

```
product-api/
├── pom.xml
└── src/main/
    ├── java/com/product/api/
    │   ├── ApiApp.java
    │   └── controller/
    │       └── ProductController.java
    └── resources/
        └── application.properties
```

---

## 9. product-core comme dépendance

Dans le `pom.xml` de product-api :

```xml
<dependency>
    <groupId>com.product</groupId>
    <artifactId>product-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

product-core fait le travail, product-api expose les fonctionnalités via internet.

---

## 10. Les 4 endpoints REST

| # | Méthode HTTP | URL | Action |
|---|---|---|---|
| 1 | POST | `/api/products` | Ajouter un produit |
| 2 | GET | `/api/products` | Lister tous les produits |
| 3 | PUT | `/api/products/{id}/quantity?quantity=X` | Modifier la quantité |
| 4 | GET | `/api/products/low-stock` | Compter les produits en stock faible |

### Exemple d'ajout (POST)

Corps JSON :
```json
{
  "name": "Ordinateur Portable",
  "description": "PC portable 15 pouces",
  "price": 899.99,
  "quantity": 10
}
```

---

## 11. Configuration du port

L'application product-api fonctionne sur le port **8086**.

```properties
server.port=8086
```

L'API est accessible sur : `http://localhost:8086/api/products`

---

## 12. Tests de l'API

### Via navigateur

- `http://localhost:8086/api/products` → Liste des produits
- `http://localhost:8086/api/products/low-stock` → Nombre de produits en stock faible

### Via curl (PowerShell)

```powershell
curl.exe -X POST http://localhost:8086/api/products -H "Content-Type: application/json" -d '{"name":"Telephone","description":"Smartphone","price":599.99,"quantity":20}'

curl.exe http://localhost:8086/api/products

curl.exe -X PUT "http://localhost:8086/api/products/1/quantity?quantity=5"

curl.exe http://localhost:8086/api/products/low-stock
```

### Résultats obtenus

Les tests ont confirmé que :
- Les produits s'ajoutent correctement dans MySQL
- La liste retourne tous les produits
- La modification de quantité fonctionne
- Le compteur de stock faible est opérationnel

---

## 13. Gestion Git & GitHub

### Repositories créés

| Projet | Repository GitHub |
|---|---|
| product-core | `https://github.com/kaba476/product-core` |
| product-api | `https://github.com/kaba476/product_api` |

### Branches par fonctionnalité (product-core)

```
main
├── feature/product-model       → Modèle Product + JPA Repository
├── feature/add-product         → ProductService.addProduct()
├── feature/list-products       → ProductService.listAllProducts()
├── feature/update-quantity     → ProductService.updateQuantity()
└── feature/low-stock           → ProductService.countLowStockProducts()
```

### Branches (product-api)

```
main
└── feature/rest-endpoints      → ProductController avec 4 endpoints
```

Toutes les branches ont été fusionnées dans `main`.

---

## 14. Génération du fichier JAR

### Commandes de compilation

```bash
cd product-core
mvn clean install -DskipTests

cd product-api
mvn clean package -DskipTests
```

### Fichiers JAR générés

| Fichier | Emplacement | Utilisation |
|---|---|---|
| `product-core-1.0-SNAPSHOT.jar` | product-core/target/ | Bibliothèque (dépendance Maven) |
| `product-core-1.0-SNAPSHOT-exec.jar` | product-core/target/ | Exécutable Spring Boot |
| `product-api-1.0-SNAPSHOT.jar` | product-api/target/ | Exécutable Spring Boot (API REST) |

Le fichier **product-api-1.0-SNAPSHOT.jar** est celui qui sera :
1. Mis dans **Docker** (via un Dockerfile)
2. Construit en **image Docker**
3. Déployé sur la **VM avec Ansible**

Exécution directe :

```bash
java -jar product-api-1.0-SNAPSHOT.jar
```

---

## Résumé final

| Étape | Description | Statut |
|---|---|---|
| 1 | Création de product-core | Terminé |
| 2 | Modèle Product (5 attributs) | Terminé |
| 3 | Accès BDD (JPA Repository) | Terminé |
| 4 | Configuration MySQL | Terminé |
| 5 | ProductService (4 actions) | Terminé |
| 6 | Gestion des versions | Terminé |
| 7 | Publication Nexus | En cours (côté binôme) |
| 8 | Création de product-api | Terminé |
| 9 | product-core comme dépendance | Terminé |
| 10 | 4 endpoints REST | Terminé |
| 11 | Port 8086 | Terminé |
| 12 | Tests API | Terminé |
| 13 | Git & GitHub (branches + merge) | Terminé |
| 14 | Génération du JAR | Terminé |
