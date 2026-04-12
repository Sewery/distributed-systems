package org.example.ice.server;

import Demo.Counter;
import com.zeroc.Ice.Current;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedCounterI implements Counter {
    private final ConcurrentHashMap<String, AtomicInteger> states = new ConcurrentHashMap<>();

    private AtomicInteger stateFor(Current c) {
        return states.computeIfAbsent(c.id.category + "/" + c.id.name, k -> new AtomicInteger(0));
    }

    @Override
    public int getValue(Current c) {
        int v = stateFor(c).get();
        System.out.printf("[OP] %s/%s -> getValue(): %d [Shared Servant: %h]\n", c.id.category, c.id.name, v, this);
        return v;
    }

    @Override
    public int increment(int delta, Current c) {
        int res = stateFor(c).addAndGet(delta);
        System.out.printf("[OP] %s/%s -> increment(%d): %d [Shared Servant: %h]\n", c.id.category, c.id.name, delta, res, this);
        return res;
    }

    @Override
    public void setValue(int v, Current c) {
        stateFor(c).set(v);
        System.out.printf("[OP] %s/%s -> setValue(%d) [Shared Servant: %h]\n", c.id.category, c.id.name, v, this);
    }
}
