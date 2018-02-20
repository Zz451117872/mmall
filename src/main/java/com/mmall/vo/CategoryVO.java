package com.mmall.vo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by aa on 2017/7/1.
 */
public class CategoryVO implements Serializable{

    private Integer id;
    private  Integer parentCategory;
    private String parentCategoryName;

    private String name;

    private Boolean status;
    private String statusDesc;

    private Integer sortOrder;

    private String createTime;

    private String updateTime;

    private List<CategoryVO> childs;

    public Integer getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(Integer parentCategory) {
        this.parentCategory = parentCategory;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getParentCategoryName() {
        return parentCategoryName;
    }

    public void setParentCategoryName(String parentCategoryName) {
        this.parentCategoryName = parentCategoryName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public List<CategoryVO> getChilds() {
        return childs;
    }

    public void setChilds(List<CategoryVO> childs) {
        this.childs = childs;
    }
}
