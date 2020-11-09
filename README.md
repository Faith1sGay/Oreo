# Oreo
A discord bot for a server to help learn java

### Running using `docker-compose`
Fill out `src/main/resources/config.example.json` and rename it to `config.json`.
```
$ docker-compose build
$ docker-compose up
```

### Running without `docker`
Fill out `src/main/resources/config.example.json` and rename it to `config.json`.
```
$ mvn package
$ java -jar target/oreo-*.jar
```
