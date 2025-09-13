package com.reigindustries.catalyst.command.factory.parsing;

import com.reigindustries.catalyst.command.factory.annotations.Join;
import com.reigindustries.catalyst.command.factory.annotations.Optional;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class ParameterResolver {

    public static class Resolution {
        public final Object[] values;
        public final String expectedType;
        public final Integer expectedIndex;

        public Resolution(Object[] values, String expectedType, Integer expectedIndex) {
            this.values = values;
            this.expectedType = expectedType;
            this.expectedIndex = expectedIndex;
        }
    }

    public Object[] resolveForIndex(Method method, String[] args) throws ParameterParseException {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] paramValues = new Object[paramTypes.length];
        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < paramTypes.length; i++) {
            boolean isOptional = false;
            boolean isJoin = false;

            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation.annotationType() == Optional.class) {
                    isOptional = true;
                } else if (annotation.annotationType() == Join.class) {
                    isJoin = true;
                }
            }

            if (i + 1 == paramTypes.length && paramTypes[i] == String.class && isJoin) {
                String concatenated = "";
                if (i >= args.length) {
                    concatenated = null;
                } else {
                    for (int y = i; y < args.length; y++) {
                        if (y + 1 != args.length) {
                            concatenated = concatenated + args[y] + " ";
                        } else {
                            concatenated = concatenated + args[y];
                        }
                    }
                }
                paramValues[i] = concatenated;
            }

            if (i < args.length) {
                Object value = parseArg(paramTypes[i], args[i], isJoin);
                if (value == null) {
                    throw new ParameterParseException(getTypeName(paramTypes[i]), i, ParameterParseException.ErrorKind.INVALID);
                }
                paramValues[i] = value;
            } else if (isOptional) {
                paramValues[i] = getDefaultValue(paramTypes[i]);
            } else {
                throw new ParameterParseException(null, i, ParameterParseException.ErrorKind.MISSING_REQUIRED);
            }
        }

        return paramValues;
    }

    public Object[] resolveForSub(Method method, String[] args) throws ParameterParseException {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] paramValues = new Object[paramTypes.length];
        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < paramTypes.length; i++) {
            boolean isOptional = false;
            boolean isJoin = false;

            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation.annotationType() == Optional.class) {
                    isOptional = true;
                } else if (annotation.annotationType() == Join.class) {
                    isJoin = true;
                }
            }

            if (i + 1 == paramTypes.length && paramTypes[i] == String.class && isJoin) {
                String concatenated = "";
                if (i + 1 >= args.length) {
                    concatenated = null;
                } else {
                    for (int y = i + 1; y < args.length; y++) {
                        if (y + 1 != args.length) {
                            concatenated = concatenated + args[y] + " ";
                        } else {
                            concatenated = concatenated + args[y];
                        }
                    }
                }
                paramValues[i] = concatenated;
            }

            if (i + 1 < args.length) {
                Object value = parseArg(paramTypes[i], args[i + 1], isJoin);
                if (value == null) {
                    throw new ParameterParseException(getTypeName(paramTypes[i]), i + 1, ParameterParseException.ErrorKind.INVALID);
                }
                paramValues[i] = value;
            } else if (isOptional) {
                paramValues[i] = getDefaultValue(paramTypes[i]);
            } else {
                throw new ParameterParseException(null, i + 1, ParameterParseException.ErrorKind.MISSING_REQUIRED);
            }
        }

        return paramValues;
    }

    private Object parseArg(Class<?> paramType, String raw, boolean isJoin) {
        if (paramType == String.class && !isJoin) {
            return raw;
        } else if (paramType == Integer.class) {
            try { return Integer.parseInt(raw); } catch (NumberFormatException e) { return null; }
        } else if (paramType == Double.class) {
            try { return Double.parseDouble(raw); } catch (NumberFormatException e) { return null; }
        } else if (paramType == Float.class) {
            try { return Float.parseFloat(raw); } catch (NumberFormatException e) { return null; }
        } else if (paramType == Material.class) {
            return Material.getMaterial(raw) == null ? null : Material.getMaterial(raw);
        } else if (paramType == World.class) {
            return Bukkit.getWorld(raw);
        } else if (paramType == Player.class) {
            return Bukkit.getPlayer(raw);
        } else if (paramType == OfflinePlayer.class) {
            return Bukkit.getOfflinePlayer(raw);
        } else if (paramType == Boolean.class) {
            return Boolean.parseBoolean(raw);
        } else if (paramType.isEnum()) {
            try { return Enum.valueOf((Class<Enum>) paramType, raw.toUpperCase()); } catch (Exception e) { return null; }
        } else if (paramType == UUID.class) {
            try { return UUID.fromString(raw); } catch (Exception e) { return null; }
        } else if (paramType == PotionEffectType.class) {
            PotionEffectType effect = PotionEffectType.getByName(raw.toUpperCase());
            return effect;
        } else if (paramType == Enchantment.class) {
            Enchantment enchantment = Enchantment.getByName(raw.toUpperCase());
            return enchantment;
        } else if (paramType == Sound.class) {
            try { return Sound.valueOf(raw.toUpperCase()); } catch (Exception e) { return null; }
        } else if (paramType == ChatColor.class) {
            try { return ChatColor.valueOf(raw.toUpperCase()); } catch (Exception e) { return null; }
        } else if (paramType == Difficulty.class) {
            try { return Difficulty.valueOf(raw.toUpperCase()); } catch (Exception e) { return null; }
        } else if (paramType == GameMode.class) {
            try { return GameMode.valueOf(raw.toUpperCase()); } catch (Exception e) { return null; }
        } else if (paramType == Biome.class) {
            try { return Biome.valueOf(raw.toUpperCase()); } catch (Exception e) { return null; }
        } else if (paramType == WeatherType.class) {
            try { return WeatherType.valueOf(raw.toUpperCase()); } catch (Exception e) { return null; }
        } else if (paramType == EntityType.class) {
            try { return EntityType.valueOf(raw.toUpperCase()); } catch (Exception e) { return null; }
        }
        return null;
    }

    private Object getDefaultValue(Class<?> paramType) {
        if (paramType == String.class) {
            return "";
        } else if (paramType == Integer.class) {
            return 1;
        } else if (paramType == Double.class) {
            return 1.0;
        } else if (paramType == Boolean.class) {
            return false;
        }
        return null;
    }

    private String getTypeName(Class<?> type) {
        if (type == PotionEffectType.class) return "Potion Effect";
        if (type == Enchantment.class) return "Enchantment";
        if (type == ChatColor.class) return "Color";
        if (type == WeatherType.class) return "Weather";
        return type.getSimpleName();
    }
}


