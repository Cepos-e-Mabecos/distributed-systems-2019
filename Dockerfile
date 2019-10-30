FROM maven:3-jdk-11
COPY . /usr/src/
WORKDIR /usr/src/

ENV PORT 5000
EXPOSE $PORT

RUN mvn clean install
RUN mvn dependency:resolve
RUN mvn verify

CMD java -jar ./target/RMIServer.jar $PORT

# How to use?
# docker build --tag {nameImage} .
# docker run --name {nameContainer} --detach --env-file env.list {nameImage}