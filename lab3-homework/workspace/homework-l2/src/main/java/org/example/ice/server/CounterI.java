package org.example.ice.server;

import Demo.Counter;
import com.zeroc.Ice.Current;

import java.util.concurrent.atomic.AtomicInteger;

public class CounterI implements Counter {
	private final AtomicInteger val = new AtomicInteger(0);

    @Override
    public int getValue(Current c) {
        int v = val.get();
        System.out.printf("Op: %s/%s -> getValue(): %d [Servant: %h]\n", c.id.category, c.id.name, v, this);
        return v;
    }

    @Override
    public int increment(int delta, Current c) {
        int res = val.addAndGet(delta);
        System.out.printf("Op: %s/%s -> increment(%d): %d [Servant: %h]\n", c.id.category, c.id.name, delta, res, this);
        return res;
    }

    @Override
    public void setValue(int v, Current c) {
        val.set(v);
        System.out.printf("Op: %s/%s -> setValue(%d) [Servant: %h]\n", c.id.category, c.id.name, v, this);
    }

    public int getRawValue() {
        return val.get();
    }

    public void restoreValue(int v) {
        val.set(v);
    }
}