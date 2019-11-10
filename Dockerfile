FROM maven:3-jdk-11
COPY . /usr/src/
WORKDIR /usr/src/

ENV MULTICASTPORT 5000
EXPOSE $MULTICASTPORT

ENV MULTICASTADDRESS 230.1.1.1

ENV RMISERVERPORT 6000
EXPOSE $RMISERVERPORT

RUN mvn clean install
RUN mvn dependency:resolve
RUN mvn verify

CMD java -jar ./target/RMIServer.jar $MULTICASTPORT $MULTICASTADDRESS $RMISERVERPORT

# How to use?
# docker build --tag {nameImage} .
# docker run --name {nameContainer} --detach --env-file env.list {nameImage}