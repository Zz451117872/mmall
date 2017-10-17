package com.mmall.vo;

import com.mmall.pojo.Product;

import java.util.List;

/**
 * Created by aa on 2017/7/1.
 */
public class CategoryVO {
    private Integer id;
    private String name;
    private List<CategoryVO> childs = null;
    private List<Product> products = null;
    private boolean isSun;

    public boolean isSun() {
        return isSun;
    }

    public void setSun(boolean sun) {
        isSun = sun;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public CategoryVO(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public CategoryVO()
    {}
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CategoryVO> getChilds() {
        return childs;
    }

    public void setChilds(List<CategoryVO> childs) {
        this.childs = childs;
    }
}
