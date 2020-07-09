package com.cs446.housematehub.grouplist;

import java.util.List;

public class GroupList {
    public int id;
    public String title;
    public String description;
    public boolean isSubscribed;
    List<GroupListItem> listItems;

    public GroupList(int id, String title, String description, boolean isSubscribed, List<GroupListItem> listItems) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isSubscribed = isSubscribed;
        this.listItems = listItems;
    }

    public static class GroupListItem {
        public String title;
        public boolean isDone;

        public GroupListItem(String title, boolean isDone) {
            this.title = title;
            this.isDone = isDone;
        }

        public void setIsDone(boolean isDone) {
            this.isDone = isDone;
        }
    }
}
