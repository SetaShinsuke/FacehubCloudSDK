package com.azusasoft.facehubcloudsdk.api.models;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SETA on 2016/5/26.
 * 用于存储作者{@link Author}
 */
public class AuthorContainer {
    private HashMap<String,Author> authors = new HashMap<>();

    public Author getUniqueAuthorByName(String name){
        if(name==null){
            return null;
        }
        Author author = authors.get(name);
        if(author == null){
            author = new Author(name);
            authors.put(name,author);
        }
        return author;
    }
}
