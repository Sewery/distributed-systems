package org.example.ice.server;

import Demo.Counter;
import com.zeroc.Ice.*;
import com.zeroc.Ice.Object;

import java.io.File;
import java.io.PrintWriter;
import java.lang.Exception;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;


public class DedicatedCounterLocator implements ServantLocator {

    private final int MAX_SIZE = 3;
    // Mapa LRU: accessOrder=true sprawia, że każde użycie przesuwa element na koniec
    private final Map<Identity, CounterI> queue = new LinkedHashMap<>(MAX_SIZE, 0.75f, true);

    // Obiekt pomocniczy dla checkedCast (aby nie tworzyć serwanta zbyt wcześnie)
    private final Counter typeProbe = new Counter() {
        @Override public void setValue(int v, Current c) {
            throw new UnsupportedOperationException("Type probe servant cannot process operations.");
        }
        @Override public int getValue(Current c) {
            throw new UnsupportedOperationException("Type probe servant cannot process operations.");
        }
        @Override public int increment(int d, Current c) {
            throw new UnsupportedOperationException("Type probe servant cannot process operations.");
        }
    };

    @Override
    public LocateResult locate(Current curr) {
        // Jeśli to tylko sprawdzenie typu (checkedCast)
        if (curr.operation.startsWith("ice_")) {
            System.out.printf("Locator: Probe (ice_isA) dla: %s\n", curr.id.name);
            return new LocateResult(typeProbe, null);
        }
        synchronized (this) {
            // Sprawdzenie, czy serwant już jest w RAM (ASM/Queue)
            CounterI servant = queue.get(curr.id);
            if (servant != null) {
                System.out.printf("Locator: %s znaleziony w pamięci RAM.\n", curr.id.name);
                return new LocateResult(servant, null);
            }

            System.out.printf("Locator: Aktywacja serwanta dla: %s\n", curr.id.name);


            servant = loadFromDisk(curr.id); // Próba wczytania stanu
            if (servant == null) servant = new CounterI(); // Jeśli brak pliku, nowy obiekt

            // Rejestracja w ASM i kolejce ewikcji
            queue.put(curr.id, servant);
            curr.adapter.add(servant, curr.id);
            if (queue.size() > MAX_SIZE) {
                Identity oldestId = queue.keySet().iterator().next();
                CounterI oldestServant = queue.remove(oldestId);

                saveToDisk(oldestId, oldestServant.getRawValue());
                // Usuwamy z ASM
                curr.adapter.remove(oldestId);

                System.out.printf("Evictor: Serwant %s usunięty z RAM (ewikcja).\n", oldestId.name);
            }
            return new LocateResult(servant, null);
        }
    }

    private void saveToDisk(Identity id, int val) {
        try (PrintWriter out = new PrintWriter("db/" + id.name + ".txt")) {
            out.println(val);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private CounterI loadFromDisk(Identity id) {
        File f = new File("db/" + id.name + ".txt");
        if (!f.exists()) return null;
        try (Scanner in = new Scanner(f)) {
            CounterI s = new CounterI();
            s.restoreValue(Integer.parseInt(in.nextLine()));
            System.out.println("Evicotor: Przywrócono z pliku: " + id.name);
            return s;
        } catch (Exception e) { return null; }
    }

    @Override
    public void finished(Current current, Object object, java.lang.Object o) throws UserException {

    }
    @Override
    public void deactivate(String category) {}

}
