FROM openjdk:8-jdk-alpine
COPY DiscordFunBot.jar /home/DiscordFunBot/DiscordFunBot.jar
WORKDIR /home/DiscordFunBot
CMD ["java -jar", "DiscordFunBot.jar"]