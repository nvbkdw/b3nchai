FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/bench-ai.jar /bench-ai/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/bench-ai/app.jar"]
