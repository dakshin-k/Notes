package com.dakshin.notes;
public class MenuItem {
    private String Name="";
    private String type="";
    private String path="";

    public MenuItem(String name, String type, String path) {
        Name = name;
        this.type = type;
        this.path = path;
    }
    public MenuItem()
    {

    }

    public void setName(String name) {
        Name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return Name;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }
}
