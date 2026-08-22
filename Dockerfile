FROM maven:3.9-eclipse-temurin-17

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

EXPOSE 10000

CMD ["sh", "-c", "java -cp target/MiniLangTranslator-1.0.0.jar com.minilang.web.MiniLangServer"]
