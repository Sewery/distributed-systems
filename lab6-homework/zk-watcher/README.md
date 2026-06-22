# ZooKeeper Watcher - Zadanie domowe SR

## Wymagania
- Java 11+
- ZooKeeper 3.8.4 uruchomiony w trybie Replicated (3 serwery)
- Gradle (lub użyj `./gradlew`)

## Struktura projektu
```
zk-watcher/
├── build.gradle
├── settings.gradle
├── gradlew
└── src/main/java/agh/sr/zookeeper/
    └── ZooWatcher.java
```

## Konfiguracja ZooKeeper (Replicated - 3 serwery)

Utwórz 3 pliki konfiguracyjne (zoo1.cfg, zoo2.cfg, zoo3.cfg):

**zoo1.cfg:**
```
tickTime=2000
dataDir=/tmp/zookeeper/zk1
clientPort=2181
initLimit=5
syncLimit=2
server.1=localhost:2888:3888
server.2=localhost:2889:3889
server.3=localhost:2890:3890
```

**zoo2.cfg:** (dataDir=/tmp/zookeeper/zk2, clientPort=2182)
**zoo3.cfg:** (dataDir=/tmp/zookeeper/zk3, clientPort=2183)

Utwórz pliki myid:
```bash
mkdir -p /tmp/zookeeper/zk1 /tmp/zookeeper/zk2 /tmp/zookeeper/zk3
echo "1" > /tmp/zookeeper/zk1/myid
echo "2" > /tmp/zookeeper/zk2/myid
echo "3" > /tmp/zookeeper/zk3/myid
```

Uruchom 3 serwery (3 terminale):
```bash
zkServer.sh --config /path/to/zoo1.cfg start-foreground
zkServer.sh --config /path/to/zoo2.cfg start-foreground
zkServer.sh --config /path/to/zoo3.cfg start-foreground
```

## Uruchomienie aplikacji

```bash
# Linux (eog = Eye of GNOME, można użyć xterm, gedit, firefox, ...)
./gradlew run --args="eog"

# macOS
./gradlew run --args="open -a Calculator"

# Windows
./gradlew run --args="notepad"

# Z opcją natychmiastowego wyświetlenia drzewa /a:
./gradlew run --args="eog tree"
```

## Komendy w trakcie działania (wpisywane w konsoli)

| Komenda | Akcja                              |
|---------|------------------------------------|
| `tree`  | Wyświetla drzewo /a w oknie Swing  |
| `quit`  | Kończy aplikację                   |
| Enter   | Kończy aplikację                   |

## Testowanie (zkCli)

```bash
zkCli.sh -server localhost:2181

# Wywołaj uruchomienie apki graficznej:
create /a "dane"

# Wywołaj wyświetlenie liczby potomków:
create /a/b1 "b1"
create /a/b2 "b2"

# Wywołaj zatrzymanie apki graficznej:
deleteall /a
```

## Jak działają watche

ZooKeeper watche są **jednorazowe** - po wyzwoleniu trzeba je ustawiać ponownie.
Aplikacja ustawia watch po każdym zdarzeniu:

- `exists(/a, watch)` → czeka na `NodeCreated`
- `exists(/a, watch)` + `getChildren(/a, watch)` → czeka na `NodeDeleted` i `NodeChildrenChanged`
