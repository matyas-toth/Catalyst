package com.reigindustries.catalyst.command.factory.tab;

import com.reigindustries.catalyst.command.factory.annotations.AutoComplete;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TabCompletionResolver {

    public List<String> completeForIndex(Method indexMethod, String[] args) {
        List<String> completions = new ArrayList<>();
        if (indexMethod == null) return completions;

        Annotation[][] paramAnnotations = indexMethod.getParameterAnnotations();
        int argIndex = args.length - 1;
        if (argIndex < 0 || argIndex >= paramAnnotations.length) return completions;

        for (Annotation annotation : paramAnnotations[argIndex]) {
            if (annotation instanceof AutoComplete) {
                populateFromToken(((AutoComplete) annotation).value(), completions);
                break;
            }
        }
        return filterPrefix(completions, args[args.length - 1]);
    }

    public List<String> completeForSub(Method subMethod, String[] args) {
        List<String> completions = new ArrayList<>();
        if (subMethod == null) return completions;

        Annotation[][] paramAnnotations = subMethod.getParameterAnnotations();
        int argIndex = args.length - 2;
        if (argIndex < 0 || argIndex >= paramAnnotations.length) return completions;

        for (Annotation annotation : paramAnnotations[argIndex]) {
            if (annotation instanceof AutoComplete) {
                populateFromToken(((AutoComplete) annotation).value(), completions);
                break;
            }
        }
        return filterPrefix(completions, args[args.length - 1]);
    }

    private void populateFromToken(String token, List<String> completions) {
        if ("#onlineplayers".equalsIgnoreCase(token)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                completions.add(onlinePlayer.getName());
            }
        } else if ("#materials".equalsIgnoreCase(token)) {
            for (Material material : Material.values()) {
                completions.add(material.name());
            }
        } else if (token.startsWith("#range:")) {
            String[] split = token.split(":");
            String[] range = split[1].split("-");
            int start = Integer.parseInt(range[0]);
            int end = Integer.parseInt(range[1]);
            for (int x = start; x <= end; x++) {
                completions.add(String.valueOf(x));
            }
        } else if (token.startsWith("#enum:")) {
            String enumName = token.substring("#enum:".length());
            try {
                Class<?> enumClass = Class.forName(enumName);
                if (enumClass.isEnum()) {
                    for (Object enumConstant : enumClass.getEnumConstants()) {
                        completions.add(enumConstant.toString());
                    }
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
    }

    private List<String> filterPrefix(List<String> options, String prefixRaw) {
        String prefix = prefixRaw == null ? "" : prefixRaw.toLowerCase();
        List<String> out = new ArrayList<>();
        for (String s : options) {
            if (s.toLowerCase().startsWith(prefix)) out.add(s);
        }
        return out;
    }
}


