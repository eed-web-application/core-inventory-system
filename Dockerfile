FROM eclipse-temurin:21-jammy as builder

ARG USERNAME
ARG TOKEN
ENV GH_USERNAME=$USERNAME
ENV GH_TOKEN=$TOKEN
COPY . /opt/app
RUN /opt/app/gradlew -p /opt/app/ assemble

FROM eclipse-temurin:21-jammy
RUN useradd -rm -d /home/app -s /bin/bash -g root -G sudo -u 1001 app
USER app
WORKDIR /home/app
COPY --from=builder /opt/app/tools/run.sh /home/app
COPY --from=builder /opt/app/build/libs/*.jar /home/app
EXPOSE 8080
ENTRYPOINT ["/home/app/run.sh"]
