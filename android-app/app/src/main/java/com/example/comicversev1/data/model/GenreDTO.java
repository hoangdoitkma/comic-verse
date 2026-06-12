package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class GenreDTO {
    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;
}
