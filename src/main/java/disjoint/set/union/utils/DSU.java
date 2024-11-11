package disjoint.set.union.utils;

import disjoint.set.union.data.Group;

import java.util.*;

public class DSU {
    private final ArrayList<Integer> parent;
    private final ArrayList<Integer> rank;

    public DSU() {
        parent = new ArrayList<>();
        rank = new ArrayList<>();
    }

    public void addString(int num) {
        parent.add(num);
        rank.add(0);
    }

    public int get(int v) {
        if (v == parent.get(v))
            return v;
        parent.set(v, get(parent.get(v)));
        return parent.get(v);
    }

    public void union(int a, int b) {
        a = get(a);
        b = get(b);
        if (a != b) {
            if (rank.get(a) < rank.get(b)) {
                int tmp = a;
                a = b;
                b = tmp;
            }
            parent.set(b, a);
            if (Objects.equals(rank.get(a), rank.get(b)))
                rank.set(a, rank.get(a) + 1);
        }
    }

    public Group[] getGroupsAndPos(int[] posRes) {
        Group[] res = getGroups();
        getPos(posRes, res);
        return res;
    }

    private void getPos(int[] posRes, Group[] res) {
        int pos = 0;
        while (pos < res.length && res[pos].getStrings().size() > 1) {
            pos++;
        }
        posRes[0] = pos;
    }

    private Group[] getGroups() {
        LinkedHashMap<Integer, Group> groupMap = new LinkedHashMap<>();

        for (int i = 0; i < parent.size(); i++) {
            int p = get(parent.get(i));

            if (!groupMap.containsKey(p)) {
                groupMap.put(p, new Group());
            }

            groupMap.get(p).addString(i);
        }

        return groupMap.values().stream().sorted(Group::compareTo).toArray(Group[]::new);
    }
}
