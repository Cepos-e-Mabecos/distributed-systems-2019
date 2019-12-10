FROM maven:3-jdk-11
COPY ./placemanager-server/ /usr/src/placemanager-server/
COPY ./rest-server/ /usr/src/rest-server/

WORKDIR /usr/src/placemanager-server/
RUN mvn clean install
RUN mvn dependency:resolve
RUN mvn verify

WORKDIR /usr/src/rest-server/
RUN mvn clean install
RUN mvn dependency:resolve
RUN mvn verify
CMD java -jar ./target/RESTServer.jar $MULTICASTADDRESS $MULTICASTPORT $FRONTENDADDRESS $FRONTENDPORT