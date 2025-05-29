### Java Base Image ###
FROM eclipse-temurin:21

### Update Packages ###
RUN apt-get update

### Set DB environment variables ###
ENV DB_URL="jdbc:mysql://database:3306/hyperagents"
ENV DB_USER="db_username"
ENV DB_PASS="db_password"

### Setup Working Directory ###
WORKDIR /run

### Copy jar into the container ###
COPY hyper-agents.jar /run

### Run the experiment ###
CMD ["java", "-jar", "hyper-agents.jar", "--eval-hyper-agents"]