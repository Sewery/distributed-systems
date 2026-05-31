# Tier 2 — DFS na rozproszonym klastrze Ray

Rozproszony, HDFS-podobny system przechowywania artefaktów zbudowany z aktorów Ray,
uruchamiany w wielokontenerowym klastrze przez Docker Compose.

## Architektura środowiska

```
┌────────────────────────────────────────────────────────┐
│                    docker-compose                       │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   ray-head   │  │ ray-worker-1 │  │ ray-worker-2 │  │
│  │              │◄─┤              │  │              │  │
│  │  dashboard   │  │              │  │              │  │
│  │  8265        │  └──────────────┘  └──────────────┘  │
│  │  GCS 6379    │         ▲                  ▲         │
│  │  client 10001│         └──────────────────┘         │
│  └──────┬───────┘                                       │
│         │                                               │
│  ┌──────▼───────┐                                       │
│  │   jupyter    │  ← RAY_ADDRESS=ray://ray-head:10001  │
│  │   :8888      │                                       │
│  └──────────────┘                                       │
└────────────────────────────────────────────────────────┘
        ▲                       ▲
        │ localhost:8888        │ localhost:8265
        │  (token: raylab)      │  (dashboard)
        │                       │
       PRZEGLĄDARKA
```

Wszystkie kontenery dzielą tę samą sieć `ray_net`. Aktorzy DFS (NameNode + DataNodes)
uruchamiani z notebooka są **fizycznie rozkładani** na head + workery przez scheduler Ray —
widać to na dashboardzie (zakładka Actors → kolumna Node).

## Uruchomienie

```bash
docker compose up --build
```

Po starcie (pierwszy build ~2-3 min, kolejne sekundy):

| Endpoint        | URL                       | Uwagi              |
| --------------- | ------------------------- | ------------------ |
| **JupyterLab**  | http://localhost:8888     | token: `raylab`    |
| **Ray dashboard** | http://localhost:8265   | Actors, Nodes, Logs|

W JupyterLab otwórz `tier2_dfs.ipynb` i uruchom kolejno wszystkie komórki.

## Zatrzymanie

```bash
docker compose down
```

## Konfiguracja (`.env`)

| Zmienna           | Domyślnie    | Opis                                    |
| ----------------- | ------------ | --------------------------------------- |
| `RAY_VERSION`     | `2.55.1`     | Wersja Ray (tag oficjalnego obrazu)     |
| `PY_VERSION`      | `py310`      | Wariant Pythona w obrazie               |
| `NUM_WORKERS`     | `2`          | Liczba kontenerów worker                |
| `NUM_CPU_HEAD`    | `1`          | CPU dla head node                       |
| `NUM_CPU_WORKER`  | `2`          | CPU dla każdego workera                 |
| `DASHBOARD_PORT`  | `8265`       | Port dashboardu Ray                     |
| `GCS_PORT`        | `6379`       | Port GCS / cluster join                 |
| `CLIENT_PORT`     | `10001`      | Port Ray Client                         |
| `JUPYTER_PORT`    | `8888`       | Port JupyterLab                         |

Zmiana liczby workerów na 4 (bez przebudowy obrazu):

```bash
NUM_WORKERS=4 docker compose up
```

## Weryfikacja rozproszenia

Treść zadania wymaga *„dostępu do konsoli Ray”* potwierdzającego, że jest więcej niż
jeden węzeł w klastrze. Trzy miejsca, gdzie to widać:

1. **Pierwsza komórka notebooka** — drukuje `Cluster nodes: 3` i tabelkę z hostnamami węzłów.
2. **Dashboard zakładka „Cluster”** (http://localhost:8265) — lista wszystkich węzłów ze statusem.
3. **Dashboard zakładka „Actors”** — każdy z 5 DataNodes ma kolumnę „Node”, pokazującą,
   na którym fizycznym węźle (head/worker-1/worker-2) został zescheduledowany.

## Co się dzieje pod spodem

- `Dockerfile` rozszerza oficjalny `rayproject/ray:2.55.1-py310` o JupyterLab i `ipywidgets`.
- `ray-head` startuje z `ray start --head` z dashboardem i otwartym portem Ray Client (`10001`).
- Każdy `ray-worker` dołącza do klastra przez `ray start --address=ray-head:6379`.
- `jupyter` ma `RAY_ADDRESS=ray://ray-head:10001` — `ray.init()` w notebooku łączy się
  przez Ray Client bez kodu hardcoded.
- Notebook tworzy `NameNode` jako **named actor w namespace `dfs`** (lifetime=detached),
  rejestruje 5 `DataNode` aktorów, każdy z `max_restarts=2`. Scheduler Ray rozkłada ich
  pomiędzy fizyczne węzły.