FROM maven:3-jdk-11
COPY ./placemanager-server/ /usr/src/placemanager-server/

WORKDIR /usr/src/placemanager-server/
RUN mvn clean install
RUN mvn dependency:resolve
RUN mvn verify

CMD java -jar ./target/RMIServer.jar $MULTICASTADDRESS $MULTICASTPORT $RMISERVERADDRESS $RMISERVERPORT

# How to use?
# docker build --tag {nameImage} .
# docker run --name {nameContainer} --detach --env-file env.list {nameImage}