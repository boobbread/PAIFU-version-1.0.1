package org.paifu.core.utils;

public class VertexKey {
    private int pos, tex, norm;

    public VertexKey(int p, int t, int n) {
        pos = p;
        tex = t;
        norm = n;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VertexKey)) return false;
        VertexKey v = (VertexKey) o;
        return pos == v.pos && tex == v.tex && norm == v.norm;
    }

    public int hashCode() {
        return (pos * 31 + tex) * 31 + norm;
    }
}
