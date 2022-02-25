package me.oddlyoko.odoo.models;

import com.google.gson.Gson;

public class ModuleDescriptor {
    public String name;

    @Override
    public String toString() {
        return "ModuleDescriptor{" +
                "name='" + name + '\'' +
                '}';
    }

    public static ModuleDescriptor fromJson(String json) {
        return new Gson().fromJson(json, ModuleDescriptor.class);
    }
}
