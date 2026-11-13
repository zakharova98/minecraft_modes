package com.example.golem;

public enum GolemCategory {

    WOOD("Дерево"),
    FOOD("Еда"),
    ORES("Руды и минералы"),
    BUILDING("Строительные материалы"),
    ARMOR("Броня"),
    WEAPON("Оружие"),
    TOOLS("Инструменты"),
    POTIONS("Зелья"),
    MISC("Разное"),
    NETHER_WORLD("Предметы ада");

    private final String displayName;

    GolemCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}