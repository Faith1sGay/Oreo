# Oreo
A discord bot for a server to help learn java

### Running using `docker-compose`
Edit your token into `src/main/java/io/github/faith1sgay/oreo/Oreo.java`.
```
$ docker-compose build
$ docker-compose up
```

### Running without `docker`
Edit your token into `src/main/java/io/github/faith1sgay/oreo/Oreo.java`.
```
$ mvn package
$ java -jar target/oreo-*.jar
```
