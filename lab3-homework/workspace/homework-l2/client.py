import Ice
import os
import sys

import Ice


PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
GENERATED_DIR = os.path.join(PROJECT_ROOT, "generated")
if GENERATED_DIR not in sys.path:
	sys.path.insert(0, GENERATED_DIR)

import Demo


def main():
    # Podstawowa konfiguracja - możesz zmienić na host.docker.internal i 10015
    host = "host.docker.internal"
    port = 10012

    with Ice.initialize(sys.argv) as communicator:
        print(f"Klient ICE - Host: {host}, Port: {port}")
        print("Komendy: <strategia> <nazwa> <opcja> [wartość] [checked]")
        print("Przykład: shared u1 set 100")
        print("Przykład: dedicated d1 inc 5 checked")
        print("Wpisz exit aby wyjść.\n")

        while True:
            try:
                line = input("> ").strip().split()
                if not line: continue
                if line[0] == "exit": break

                # Minimalna obsługa argumentów pozycyjnych
                strategy = line[0]# shared / dedicated
                name = line[1] # np. u1
                op = line[2] # set / get / inc

                # Opcjonalne parametry
                val = int(line[3]) if len(line) > 3 and line[3].isdigit() else 0
                use_checked = "checked" in line


                proxy_str = f"{strategy}/{name}:tcp -h {host} -p {port}"
                base = communicator.stringToProxy(proxy_str)

                if use_checked:
                    counter = Demo.CounterPrx.checkedCast(base)
                else:
                    counter = Demo.CounterPrx.uncheckedCast(base)

                if not counter:
                    print("Błąd: Nie można uzyskać dostępu do obiektu.")
                    continue

                # Wykonanie operacji
                if op == "set":
                    counter.setValue(val)
                    print(f"OK: Ustawiono {val}")
                elif op == "get":
                    print(f"Wynik: {counter.getValue()}")
                elif op == "inc":
                    res = counter.increment(val)
                    print(f"Wynik po inkrementacji: {res}")
                else:
                    print("Nieznana operacja.")

            except EOFError: break
            except Exception as e:
                print(f"Błąd: {e}")

    return 0

if __name__ == "__main__":
    sys.exit(main())