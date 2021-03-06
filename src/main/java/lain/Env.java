package lain;

import lain.Types.LainException;
import lain.Types.LainList;
import lain.Types.LainObj;
import lain.Types.LainSymbol;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by merlin on 16/7/27.
 */
class Env {
    private Env outer;
    private Map inner;

    Env(Map env) {
        inner = new HashMap();
        inner.putAll(env);
    }

    Env(Env outer) {
        inner = new HashMap();
        this.outer = outer;
    }

    Env(Env outer, LainList binds, LainList exprs) {
        inner = new HashMap();
        this.outer = outer;
        for (int i = 0; i < binds.size(); i++) {
            LainSymbol symbol = (LainSymbol) binds.get(i);
            if (symbol.getValue().equals("&")) {
                set((LainSymbol) binds.get(i + 1), exprs.sub(i));
                return;
            } else {
                set(symbol, exprs.get(i));
            }
        }
    }

    private Map getInner() {
        return inner;
    }

    Env set(LainSymbol key, LainObj value) {
        inner.put(key.getValue(), value);
        return this;
    }

    Env find(LainSymbol key) {
        if (inner.containsKey(key.getValue())) {
            return this;
        } else if (outer != null) {
            return outer.find(key);
        } else {
            return null;
        }
    }

    LainObj get(LainSymbol key) throws LainException {
        Env keyEnv = find(key);
        if (keyEnv == null)
            throw new LainException("can't find :" + key);
        else
            return (LainObj) keyEnv.getInner().get(key.getValue());
    }
}
