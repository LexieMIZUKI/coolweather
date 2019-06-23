package com.lexieluv.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 6.终于来到了实体类的创建，实体类的内容是根据json格式的数据来定的，区别db包内的实体是数据库来的
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
