# Build stage
FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Créer l'utilisateur AVANT de créer les dossiers
RUN addgroup -S spring && adduser -S spring -G spring

# MODIFIÉ : Créer le dossier /tmp/uploads avec permissions
RUN mkdir -p /tmp/uploads && chmod 777 /tmp/uploads

# Changer d'utilisateur
USER spring:spring

# Copier le JAR depuis le stage de build
COPY --from=build /app/target/*.jar app.jar

# Variables d'environnement
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"
# NOUVEAU : Définir UPLOAD_DIR par défaut
ENV UPLOAD_DIR=/tmp/uploads

# Exposition du port
EXPOSE 8080

# Commande de démarrage
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]