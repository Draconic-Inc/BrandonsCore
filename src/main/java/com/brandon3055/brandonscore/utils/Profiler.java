package com.brandon3055.brandonscore.utils;

import java.util.*;

/**
 * Created by brandon3055 on 13/11/2017.
 */
public class Profiler {

    public String currentSection = "";
    public Stack<Section> sectionStack = new Stack<>();
    public Map<String, Long> sectionTimeMap = new HashMap<>();
    public Map<String, Long> sectionCallCount = new HashMap<>();
    public List<String> sectionList = new LinkedList<>();
    public boolean enabled = true;

    public void startSection(String name) {
        if (!enabled) return;
        name = name.replaceAll("\\.", ",");
        if (currentSection.length() > 0) {
            currentSection = currentSection + ".";
        }

        currentSection = currentSection + name;
        sectionStack.push(new Section(currentSection));
        if (!sectionList.contains(currentSection)) {
            sectionList.add(currentSection);
        }

        sectionTimeMap.putIfAbsent(currentSection, 0L);
        sectionCallCount.put(currentSection, sectionCallCount.getOrDefault(currentSection, 0L) + 1);
    }

    public void endSection() {
        if (!enabled || sectionStack.isEmpty()) {
            return;
        }

        Section current = sectionStack.pop();

        long time = current.getTimeElapsed();
        Long t = sectionTimeMap.get(current.name);
        sectionTimeMap.put(current.name, t == null ? time : time + t);

        if (sectionStack.isEmpty()) {
            currentSection = "";
        }
        else {
            currentSection = sectionStack.peek().name;
        }
    }

    public void finish() {
        if (!enabled) return;
        StringBuilder builder = new StringBuilder("\n");

        for (String section : sectionList) {
            String s = section;
            int depth = 0;
            while (s.contains(".")) {
                s = s.replaceFirst("\\.", "");
                depth++;
            }

            String name = section.contains(".") ? section.substring(section.lastIndexOf(".") + 1) : section;
            builder.append(indent(depth)).append(name).append(": ").append(sectionTimeMap.get(section) / 1000000D).append("ms | ").append(sectionCallCount.get(section)).append(" Calls\n");
        }

        LogHelperBC.dev(builder.toString());

        currentSection = "";
        sectionStack.clear();
        sectionTimeMap.clear();
        sectionCallCount.clear();
        sectionList.clear();
    }

    private String indent(int depth) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            s.append("|  ");
        }
        return s.toString();
    }

    public void endStartSection(String name) {
        endSection();
        startSection(name);
    }

    public static class Section {
        public String name;
        public long startTime;

        public Section(String name) {
            this.name = name;
            this.startTime = System.nanoTime();
        }

        public long getTimeElapsed() {
            return System.nanoTime() - startTime;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
