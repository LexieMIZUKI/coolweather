package com.lexieluv.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * 1.第一步创建三个实体类
 * （其一这个是省类）
 */
public class Province extends DataSupport{
    //成员属性，一般以小写字母开头
    private int id;
    private String provinceName;
    private int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
