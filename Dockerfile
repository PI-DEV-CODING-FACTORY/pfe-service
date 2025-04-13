# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Create Maven settings with multiple mirrors
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' >> /root/.m2/settings.xml && \
    echo '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' >> /root/.m2/settings.xml && \
    echo '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">' >> /root/.m2/settings.xml && \
    echo '    <mirrors>' >> /root/.m2/settings.xml && \
    echo '        <mirror>' >> /root/.m2/settings.xml && \
    echo '            <id>central</id>' >> /root/.m2/settings.xml && \
    echo '            <name>Central Repository</name>' >> /root/.m2/settings.xml && \
    echo '            <url>https://repo.maven.apache.org/maven2</url>' >> /root/.m2/settings.xml && \
    echo '            <mirrorOf>central</mirrorOf>' >> /root/.m2/settings.xml && \
    echo '        </mirror>' >> /root/.m2/settings.xml && \
    echo '        <mirror>' >> /root/.m2/settings.xml && \
    echo '            <id>aliyun</id>' >> /root/.m2/settings.xml && \
    echo '            <name>Aliyun Maven Repository</name>' >> /root/.m2/settings.xml && \
    echo '            <url>https://maven.aliyun.com/repository/public</url>' >> /root/.m2/settings.xml && \
    echo '            <mirrorOf>central</mirrorOf>' >> /root/.m2/settings.xml && \
    echo '        </mirror>' >> /root/.m2/settings.xml && \
    echo '    </mirrors>' >> /root/.m2/settings.xml && \
    echo '</settings>' >> /root/.m2/settings.xml

# Copy project files
COPY pom.xml .
COPY src ./src

# Build with retry logic
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:go-offline && \
    mvn clean package -DskipTests || \
    (echo "First build attempt failed, retrying..." && \
     mvn clean package -DskipTests) || \
    (echo "Second build attempt failed, retrying one last time..." && \
     mvn clean package -DskipTests)

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=prod

# Expose the port the app runs on
EXPOSE 8080

# Create a non-root user
RUN addgroup --system --gid 1001 spring && \
    adduser --system --uid 1001 --gid 1001 spring
USER spring

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 