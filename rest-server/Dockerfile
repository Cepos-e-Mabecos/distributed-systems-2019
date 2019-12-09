FROM maven:3-jdk-11
COPY . /usr/src/
WORKDIR /usr/src/

RUN mvn clean install
RUN mvn dependency:resolve
RUN mvn verify

CMD java -jar ./target/RESTServer.jar $MULTICASTADDRESS $MULTICASTPORT $FRONTENDADDRESS $FRONTENDPORT